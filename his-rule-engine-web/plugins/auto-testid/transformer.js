/**
 * @file 核心模板转换器
 * @description
 *   接受 Vue SFC 的 <template> 源码，输出注入了 data-testid 属性的等价源码。
 *   两个主要导出：
 *   - injectTabPaneLabelTestIds：为 <el-tab-pane label="..."> 补 #label slot
 *     （因为 label 是 prop，最终渲染在 Tabs 头部，外部选不到，所以要包一层 span）
 *   - processTemplate：为其他目标组件直接在开标签内注入 data-testid="..."
 *
 *   两者按顺序调用，先 tab-pane，后通用（通用流程会跳过 tab-pane 避免重复）。
 */

import { parse as parseSfc } from 'vue/compiler-sfc'
import {
    escapeHtml,
    extractPageName,
    findOpeningTagEnd,
    getStaticOrDirectiveProp,
    hasExistingTestId,
    hasNamedSlot,
    isTabPaneTag,
    resolveSemanticKey,
    resolveTabItemSemanticKey,
} from './helpers.js'

/**
 * 生成本次 SFC 内唯一的 testid
 * - 同 elementType 同语义：第二次起加 -2 / -3 ...
 * - 无语义：用类型计数器 -1 / -2 / -3 ...
 *
 * @param {string} pageName
 * @param {string} elementType
 * @param {string | null} semantic
 * @param {Record<string, number>} typeCounter - 跨调用累计
 * @param {Record<string, number>} usedTestIds - 跨调用累计
 * @returns {string}
 */
export function buildTestId(pageName, elementType, semantic, typeCounter, usedTestIds) {
  typeCounter[elementType] = (typeCounter[elementType] || 0) + 1

  const baseId = semantic
    ? `${pageName}-${elementType}-${semantic}`
    : `${pageName}-${elementType}-${typeCounter[elementType]}`

  if (usedTestIds[baseId] !== undefined) {
    usedTestIds[baseId] += 1
    return `${baseId}-${usedTestIds[baseId]}`
  }

  usedTestIds[baseId] = 1
  return baseId
}

/**
 * 深度遍历 AST，收集所有需要注入 testid 的目标节点
 * @param {import('./helpers.js').AstNode} ast
 * @param {(tagName: string) => string | undefined} resolveElementType
 * @returns {import('./helpers.js').AstNode[]}
 */
export function collectTargetNodes(ast, resolveElementType) {
  /** @type {import('./helpers.js').AstNode[]} */
  const result = []

  /**
   * @param {import('./helpers.js').AstNode} node
   */
  function walk(node) {
    if (!node) return

    if (node.type === 0 && node.children) {
      for (const child of node.children) walk(child)
      return
    }

    if (node.type === 1 && node.tag) {
      const elementType = resolveElementType(node.tag)
      if (elementType) result.push(node)

      for (const child of node.children || []) walk(child)
    }
  }

  walk(ast)
  return result
}

/**
 * 为 <el-tab-pane label="..."> 自动补 #label slot 并加 testid
 *
 * 原因：el-tab-pane 的 label 通过 prop 渲染到 Tabs 头部 DOM，不在 tab-pane 节点
 *      自身的 DOM 子树里，所以在 tab-pane 上加 data-testid 选不到 tab 头部。
 *      解决：把 label 文本搬到 #label 具名插槽，包一层 <span data-testid="...">
 *
 * 跳过条件：
 * - 该节点已有 #label 具名插槽（用户自定义渲染，不能动）
 * - 该节点没有 label prop（动态渲染场景，无 label 可包）
 *
 * @param {string} templateSource - 不带 <template> 包裹的内层源码
 * @param {string} filePath
 * @param {string} signName - 注入的属性名（如 data-testid）
 * @returns {string}
 */
export function injectTabPaneLabelTestIds(templateSource, filePath, signName) {
  const prefix = '<template>'
  const fullCode = `${prefix}${templateSource}</template>`
  const sfcResult = parseSfc(fullCode, { filename: filePath })
  const templateAst = /** @type {import('./helpers.js').AstNode | undefined} */ (
    /** @type {unknown} */ (sfcResult.descriptor.template?.ast)
  )

  if (!templateAst) return templateSource

  const pageName = extractPageName(filePath)
  const prefixLen = prefix.length
  /** @type {Record<string, number>} */
  const typeCounter = {}
  /** @type {Record<string, number>} */
  const usedTestIds = {}
  /** @type {{ start: number, end: number, text: string }[]} */
  const patches = []

  /**
   * @param {import('./helpers.js').AstNode} node
   */
  const walk = (node) => {
    if (!node) return

    if (node.type === 0 && node.children) {
      for (const child of node.children) walk(child)
      return
    }

    if (node.type !== 1 || !node.tag) {
      return
    }

    if (!isTabPaneTag(node.tag)) {
      for (const child of node.children || []) walk(child)
      return
    }

    if (hasNamedSlot(node.children, 'label')) {
      return
    }

    const label = getStaticOrDirectiveProp(node.props || [], 'label')
    if (!label) {
      return
    }

    const testId = buildTestId(
      pageName,
      'tab-item',
      resolveTabItemSemanticKey(node.props || []),
      typeCounter,
      usedTestIds
    )

    const labelContent =
      label.kind === 'static' ? escapeHtml(label.value) : `{{ ${label.value} }}`
    const labelSlot = `<template #label><span ${signName}="${testId}">${labelContent}</span></template>`

    const nodeStart = node.loc.start.offset - prefixLen
    const nodeEnd = node.loc.end.offset - prefixLen
    const nodeSource = templateSource.slice(nodeStart, nodeEnd)
    const openingTagEnd = findOpeningTagEnd(nodeSource)

    if (openingTagEnd < 0) {
      return
    }

    const openingTagSource = nodeSource.slice(0, openingTagEnd + 1)
    const isSelfClosing = /\/\s*>$/.test(openingTagSource)

    if (isSelfClosing) {
      const normalizedOpenTag = openingTagSource.replace(/\/\s*>$/, '>')
      patches.push({
        start: nodeStart,
        end: nodeEnd,
        text: `${normalizedOpenTag}${labelSlot}</${node.tag}>`,
      })
      return
    }

    patches.push({
      start: nodeStart + openingTagEnd + 1,
      end: nodeStart + openingTagEnd + 1,
      text: labelSlot,
    })
  }

  walk(templateAst)

  if (patches.length === 0) {
    return templateSource
  }

  // 从后往前应用补丁，避免后续偏移失效
  patches.sort((a, b) => b.start - a.start)

  let result = templateSource
  for (const patch of patches) {
    result = result.slice(0, patch.start) + patch.text + result.slice(patch.end)
  }

  return result
}

/**
 * 通用流程：为所有 resolveElementType 命中的标签注入 data-testid
 *
 * 跳过条件：
 * - 已存在 testid（hasExistingTestId）
 * - 是 el-tab-pane（由 injectTabPaneLabelTestIds 单独处理）
 *
 * @param {string} templateSource
 * @param {string} filePath
 * @param {(tagName: string) => string | undefined} resolveElementType
 * @param {string} signName
 * @returns {string}
 */
export function processTemplate(templateSource, filePath, resolveElementType, signName) {
  const pageName = extractPageName(filePath)
  const prefix = '<template>'
  const fullCode = `${prefix}${templateSource}</template>`
  const sfcResult = parseSfc(fullCode, { filename: filePath })
  const templateAst = /** @type {import('./helpers.js').AstNode | undefined} */ (
    /** @type {unknown} */ (sfcResult.descriptor.template?.ast)
  )

  if (!templateAst) return templateSource

  const targetNodes = collectTargetNodes(templateAst, resolveElementType)
  /** @type {Record<string, number>} */
  const typeCounter = {}
  /** @type {Record<string, number>} */
  const usedTestIds = {}
  /** @type {{ offset: number, testId: string }[]} */
  const injections = []
  const prefixLen = prefix.length

  for (const node of targetNodes) {
    const tagName = /** @type {string} */ (node.tag)
    const props = node.props || []

    if (hasExistingTestId(props, signName)) continue
    if (isTabPaneTag(tagName)) continue

    const elementType = /** @type {string} */ (resolveElementType(tagName))
    const semantic = resolveSemanticKey(elementType, props, node.children)
    const testId = buildTestId(pageName, elementType, semantic, typeCounter, usedTestIds)

    // 在开标签的 `<tag` 后插入 ` data-testid="..."`
    const insertOffset = node.loc.start.offset + 1 + tagName.length - prefixLen
    injections.push({ offset: insertOffset, testId })
  }

  if (injections.length === 0) return templateSource

  // 从前往后逐个插入，维护 delta 偏移
  injections.sort((a, b) => a.offset - b.offset)

  let result = templateSource
  let delta = 0

  for (const injection of injections) {
    const position = injection.offset + delta
    const text = ` ${signName}="${injection.testId}"`
    result = result.slice(0, position) + text + result.slice(position)
    delta += text.length
  }

  return result
}
