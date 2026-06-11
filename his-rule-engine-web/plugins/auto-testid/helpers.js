/**
 * @file 工具函数集合
 * @description
 *   auto-testid 插件的纯函数工具层：
 *   - 字符串规范化（normalizeTagName / normalizeToken / chineseTextStableKey）
 *   - 文件路径 → 页面名提取（extractPageName）
 *   - Vue SFC AST 属性/指令读取（getStaticProp / getDirectiveProp 等）
 *   - 语义键解析（resolveSemanticKey / resolveTabItemSemanticKey）
 *
 *   本文件不直接依赖 vue/compiler-sfc，仅消费已解析的 AST 节点对象，
 *   便于将来切换解析器或单测桩入。
 */

import { createHash } from 'node:crypto'

/**
 * Vue SFC AST 属性节点
 * @typedef {Object} AstProp
 * @property {number} type - 6=静态属性, 7=指令
 * @property {string} [name]
 * @property {{ content?: string }} [value]
 * @property {string} [rawName]
 * @property {{ content?: string }} [exp]
 * @property {{ content?: string }} [arg]
 */

/**
 * Vue SFC AST 节点
 * @typedef {Object} AstNode
 * @property {number} type - 0=根, 1=元素, 2=文本
 * @property {string} [tag]
 * @property {AstProp[]} [props]
 * @property {AstNode[]} [children]
 * @property {string} [content]
 * @property {{ start: { offset: number }, end: { offset: number } }} loc
 */

/**
 * 把 PascalCase 标签名转为 kebab-case
 * @param {string} tag
 * @returns {string}
 */
export function normalizeTagName(tag) {
  return tag.replace(/([a-z])([A-Z])/g, '$1-$2').toLowerCase()
}

/**
 * 把任意字符串规范化为 kebab-case 词元（仅 [a-z0-9-]）
 * @param {string} value
 * @returns {string}
 */
export function normalizeToken(value) {
  return value
    .replace(/([a-z])([A-Z])/g, '$1-$2')
    .replace(/([A-Z]+)([A-Z][a-z])/g, '$1-$2')
    .toLowerCase()
    .replace(/[^a-z0-9-]/g, '')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '')
}

/**
 * 中文文本 → 稳定哈希键（zh-{md5前6位}）
 * 同一段中文每次构建产出同一个 testid，便于自动化脚本复用
 * @param {string} text
 * @returns {string | null}
 */
export function chineseTextStableKey(text) {
  if (!/[\u4e00-\u9fa5]/.test(text)) return null
  const digest = createHash('md5').update(text).digest('hex').slice(0, 6)
  return `zh-${digest}`
}

/**
 * 从绝对文件路径提取"页面名"，作为 testid 的第一段
 * 例：src/views/ui-automation/test-cases/TestCaseManager.vue
 *   → ui-automation-test-cases-test-case-manager
 * @param {string} filePath
 * @returns {string}
 */
export function extractPageName(filePath) {
  const srcMatch = filePath.match(/src[\\/](.*)/)
  if (!srcMatch) {
    const fileMatch = filePath.match(/([^/\\]+)\.\w+$/)
    return fileMatch ? normalizeToken(fileMatch[1]) || 'app' : 'app'
  }

  let relative = srcMatch[1].replace(/\\/g, '/')
  relative = relative
    .replace(/^views\//, '')
    .replace(/^components\//, '')
    .replace(/\/index\.vue$/, '')
    .replace(/\.vue$/, '')

  const result = relative
    .split('/')
    .filter((part) => part !== 'index' && part !== 'components' && part !== 'views')
    .map((part) => normalizeToken(part))
    .filter(Boolean)
    .join('-')

  if (result) return result

  const fileMatch = filePath.match(/([^/\\]+)\.\w+$/)
  return fileMatch ? normalizeToken(fileMatch[1]) || 'app' : 'app'
}

/**
 * 转义正则元字符
 * @param {string} value
 * @returns {string}
 */
export function escapeRegExp(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

/**
 * 转义 HTML 特殊字符
 * @param {string} text
 * @returns {string}
 */
export function escapeHtml(text) {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

/**
 * 判断节点是否已经手动写了 testid（含静态、:bind、v-bind="{...}"）
 * 已存在则跳过注入，避免覆盖业务代码显式声明
 * @param {AstProp[]} props
 * @param {string} signName
 * @returns {boolean}
 */
export function hasExistingTestId(props, signName) {
  return props.some((prop) => {
    if (prop.type === 6) return prop.name === signName
    if (prop.type === 7) {
      if (prop.rawName === `:${signName}` || prop.rawName === `v-bind:${signName}`) {
        return true
      }

      if (prop.name === 'bind' && !prop.arg) {
        const exp = prop.exp?.content || ''
        return new RegExp(`['"]${escapeRegExp(signName)}['"]\\s*:`).test(exp)
      }
    }

    return false
  })
}

/**
 * 读取静态属性值（type=6）
 * @param {AstProp[]} props
 * @param {string} name
 * @returns {string | null}
 */
export function getStaticProp(props, name) {
  const prop = props.find((item) => item.type === 6 && item.name === name)
  return prop?.value?.content || null
}

/**
 * 读取指令属性（type=7），可按指令名/参数过滤
 * @param {AstProp[]} props
 * @param {string} directiveName
 * @param {string} [arg]
 * @returns {{ exp: string, arg: string } | null}
 */
export function getDirectiveProp(props, directiveName, arg) {
  const prop = props.find((item) => {
    if (item.type !== 7) return false
    if (item.name !== directiveName) return false
    if (arg !== undefined && item.arg?.content !== arg) return false
    return true
  })

  if (!prop?.exp?.content) return null
  return { exp: prop.exp.content, arg: prop.arg?.content || '' }
}

/**
 * 读取静态属性 或 v-bind 指令的值，统一返回类型
 * @param {AstProp[]} props
 * @param {string} name
 * @returns {{ kind: 'static' | 'dynamic', value: string } | null}
 */
export function getStaticOrDirectiveProp(props, name) {
  const staticValue = getStaticProp(props, name)
  if (staticValue) {
    return { kind: 'static', value: staticValue }
  }

  const bindValue = getDirectiveProp(props, 'bind', name)
  if (bindValue) {
    return { kind: 'dynamic', value: bindValue.exp.trim() }
  }

  return null
}

/**
 * 取 v-model="form.userName" 表达式的最后一段（用作 testid 语义键）
 * @param {AstProp[]} props
 * @returns {string | null}
 */
export function getVModelLastSegment(props) {
  const prop = props.find((item) => item.type === 7 && item.name === 'model')
  if (!prop?.exp?.content) return null
  const parts = prop.exp.content.split('.')
  return parts[parts.length - 1]
}

/**
 * 取以指定后缀结尾的事件参数（如 @user-change → 'user-change'）
 * @param {AstProp[]} props
 * @param {string} eventSuffix
 * @returns {string | null}
 */
export function getEventArg(props, eventSuffix) {
  const prop = props.find((item) => {
    if (item.type !== 7 || item.name !== 'on') return false
    return item.arg?.content?.endsWith(eventSuffix) || false
  })

  if (!prop?.arg?.content) return null
  return prop.arg.content
}

/**
 * 取 @click 处理函数名（去掉 handle/on 前缀）
 * @param {AstProp[]} props
 * @returns {string | null}
 */
export function getClickHandler(props) {
  const prop = props.find((item) => {
    if (item.type !== 7 || item.name !== 'on') return false
    return item.arg?.content === 'click'
  })

  if (!prop?.exp?.content) return null
  return prop.exp.content.replace(/^handle/, '').replace(/^on/, '') || null
}

/**
 * 取简单文本子节点（≤30 字符）；中文则返回稳定哈希键
 * @param {AstNode[]} [children]
 * @returns {string | null}
 */
export function getSimpleText(children = []) {
  const textNode = children.find((child) => child.type === 2)
  if (!textNode?.content) return null

  const text = textNode.content.trim()
  if (!text || text.length > 30) return null

  if (/[\u4e00-\u9fa5]/.test(text)) {
    return chineseTextStableKey(text)
  }

  return text.replace(/\s+/g, '-')
}

/**
 * 把属性值统一转成 testid 片段：
 *   静态 → normalizeToken
 *   动态 → normalizeExpressionTail（避免 row.status 被 strip 成 rowstatus）
 *
 * 用于 type/size/color/index 等可能用 `:attr="obj.path"` 写的属性。
 *
 * @param {{ kind: 'static' | 'dynamic', value: string } | null | undefined} attr
 * @returns {string | null}
 */
export function normalizeAttrValue(attr) {
  if (!attr) return null
  if (attr.kind === 'static') return normalizeToken(attr.value)
  return normalizeExpressionTail(attr.value)
}

/**
 * 规范化表达式尾段：去掉 ternary / 默认值 / 可选链，取最后一段属性名
 * @param {string} expression
 * @returns {string | null}
 */
export function normalizeExpressionTail(expression) {
  const cleaned = expression
    .replace(/\?.*$/, '')
    .replace(/\|\|.*$/, '')
    .replace(/\?\./g, '.')
    .trim()
  const parts = cleaned.split('.')
  const tail = parts[parts.length - 1]
  return tail ? normalizeToken(tail) : null
}

/**
 * 判断是否存在指定名称的具名 slot
 * @param {AstNode[]} children
 * @param {string} slotName
 * @returns {boolean}
 */
export function hasNamedSlot(children = [], slotName) {
  return children.some((child) => {
    if (child.type !== 1 || child.tag !== 'template') return false
    return (child.props || []).some((prop) => {
      if (prop.type !== 7 || prop.name !== 'slot') return false
      return prop.arg?.content === slotName
    })
  })
}

/**
 * 从具名 slot 内提取简单文本（取第一个文本子节点；中文走哈希）
 * 用于 el-card #header、el-collapse-item #title、el-table-column #header 等
 * @param {AstNode[]} [children]
 * @param {string} slotName
 * @returns {string | null}
 */
export function getSlotText(children = [], slotName) {
  for (const child of children) {
    if (child.type !== 1 || child.tag !== 'template') continue

    const isNamedSlot = (child.props || []).some((prop) => {
      if (prop.type !== 7 || prop.name !== 'slot') return false
      return prop.arg?.content === slotName
    })
    if (!isNamedSlot) continue

    const text = getSimpleText(child.children)
    if (text) return text
  }
  return null
}

/**
 * 是否为 el-tab-pane 标签（支持 PascalCase 写法）
 * @param {string} tagName
 * @returns {boolean}
 */
export function isTabPaneTag(tagName) {
  return normalizeTagName(tagName) === 'el-tab-pane'
}

/**
 * 找开标签 ">" 的位置（跳过字符串内的 ">"）
 * @param {string} source
 * @returns {number}
 */
export function findOpeningTagEnd(source) {
  /** @type {'"' | "'" | null} */
  let quote = null

  for (let index = 0; index < source.length; index += 1) {
    const char = source[index]

    if (quote) {
      if (char === quote && source[index - 1] !== '\\') {
        quote = null
      }
      continue
    }

    if (char === '"' || char === "'") {
      quote = char
      continue
    }

    if (char === '>') {
      return index
    }
  }

  return -1
}

/**
 * 解析 el-tab-pane 的语义键：优先 label，其次 name
 * @param {AstProp[]} props
 * @returns {string | null}
 */
export function resolveTabItemSemanticKey(props) {
  const label = getStaticOrDirectiveProp(props, 'label')
  if (label?.kind === 'static') {
    return chineseTextStableKey(label.value) || normalizeToken(label.value)
  }

  if (label?.kind === 'dynamic') {
    return normalizeExpressionTail(label.value)
  }

  const name = getStaticOrDirectiveProp(props, 'name')
  if (name?.kind === 'static') {
    return chineseTextStableKey(name.value) || normalizeToken(name.value)
  }

  if (name?.kind === 'dynamic') {
    return normalizeExpressionTail(name.value)
  }

  return null
}

/**
 * 根据元素类型 + props + children 解析语义键
 * 表单类用 prop/field/name/v-model/@change；按钮用 code/icon/text/click；
 * 对话框/抽屉用 title；表格用 name/field/:table-data；tabs 用 v-model；tab-item 走专门解析
 *
 * 各元素类型的语义键提取优先级：
 *
 * | 类型 | 优先级（高 → 低） |
 * |------|-------------------|
 * | formitem | label attr / prop |
 * | form | :model 末段 / 子文本 |
 * | option | label / value |
 * | tablecol | prop / label attr / #header slot |
 * | menuitem | index / 子文本 |
 * | submenu | index / 子文本 |
 * | menu | 子文本 / v-model 末段 |
 * | dropdownitem | command / 子文本 |
 * | dropdown / dropdownmenu | （纯容器，无语义） |
 * | card | header attr / #header slot / title / 子文本 |
 * | descriptions | title / column |
 * | descriptionsitem | label / prop |
 * | tag | 子文本 / type |
 * | icon | size / color |
 * | text / link | 子文本 / href 末段 |
 * | alert | title / type / 子文本 |
 * | divider | direction / content-position |
 * | tooltip / popover / popconfirm | content / title / 子文本 |
 * | collapse | v-model 末段 |
 * | collapseitem | title / name |
 * | breadcrumb | （纯容器） |
 * | breadcrumbitem | to / 子文本 |
 * | pagination | （纯容器） |
 * | empty | description / 子文本 |
 * | checkbutton / radiobutton | label / value |
 * | col | span / 子文本 |
 * | row | gutter |
 * | progress | percentage / status |
 * | layout / scrollbar | （纯容器） |
 * | dynamic（<component :is>） | :is 末段 / is 静态 |
 *
 * @param {string} elementType
 * @param {AstProp[]} props
 * @param {AstNode[]} [children]
 * @returns {string | null}
 */
export function resolveSemanticKey(elementType, props, children) {
  switch (elementType) {
    case 'input':
    case 'select':
    case 'datepicker':
    case 'timepicker':
    case 'timeselect':
    case 'switch':
    case 'check':
    case 'checkgroup':
    case 'radio':
    case 'radiogroup':
    case 'tree': {
      for (const attr of ['prop', 'field', 'name']) {
        const value = getStaticProp(props, attr)
        if (value) return normalizeToken(value)
      }

      const fromModel = getVModelLastSegment(props)
      if (fromModel) return normalizeToken(fromModel)

      const changeArg = getEventArg(props, '-change')
      if (changeArg) return normalizeToken(changeArg.replace(/-change$/, ''))
      break
    }

    case 'btn': {
      const fromCode = getStaticProp(props, 'code')
      if (fromCode) return normalizeToken(fromCode)

      const fromIcon = getStaticProp(props, 'icon')
      if (fromIcon) return normalizeToken(fromIcon)

      const fromText = getSimpleText(children)
      if (fromText) return normalizeToken(fromText)

      const fromClick = getClickHandler(props)
      if (fromClick) return normalizeToken(fromClick)
      break
    }

    case 'dialog':
    case 'drawer':
    case 'popover':
    case 'popconfirm': {
      const fromTitle = getStaticOrDirectiveProp(props, 'title')
      if (fromTitle?.kind === 'static') {
        return chineseTextStableKey(fromTitle.value) || normalizeToken(fromTitle.value)
      }
      if (fromTitle?.kind === 'dynamic') {
        return normalizeExpressionTail(fromTitle.value)
      }
      break
    }

    case 'tooltip':
    case 'tooltip-content': {
      const fromContent = getStaticOrDirectiveProp(props, 'content')
      if (fromContent?.kind === 'static') {
        return chineseTextStableKey(fromContent.value) || normalizeToken(fromContent.value)
      }
      if (fromContent?.kind === 'dynamic') {
        return normalizeExpressionTail(fromContent.value)
      }
      break
    }

    case 'table': {
      for (const attr of ['name', 'field']) {
        const value = getStaticProp(props, attr)
        if (value) return normalizeToken(value)
      }

      const tableDataBind = getDirectiveProp(props, 'bind', 'table-data')
      if (tableDataBind) {
        const parts = tableDataBind.exp.split('.')
        return normalizeToken(parts[parts.length - 1])
      }
      break
    }

    case 'tablecol': {
      // prop（数据字段名）优先，label / #header slot 次之
      const propVal = getStaticProp(props, 'prop')
      if (propVal) return normalizeToken(propVal)

      const labelVal = getStaticOrDirectiveProp(props, 'label')
      if (labelVal?.kind === 'static') {
        return chineseTextStableKey(labelVal.value) || normalizeToken(labelVal.value)
      }
      if (labelVal?.kind === 'dynamic') {
        return normalizeExpressionTail(labelVal.value)
      }

      const headerSlot = getSlotText(children, 'header')
      if (headerSlot) return normalizeToken(headerSlot)
      break
    }

    case 'formitem': {
      // el-form-item: label 显示文本优先，prop 字段名次之
      const labelVal = getStaticOrDirectiveProp(props, 'label')
      if (labelVal?.kind === 'static') {
        return chineseTextStableKey(labelVal.value) || normalizeToken(labelVal.value)
      }
      if (labelVal?.kind === 'dynamic') {
        return normalizeExpressionTail(labelVal.value)
      }

      const propVal = getStaticProp(props, 'prop')
      if (propVal) return normalizeToken(propVal)
      break
    }

    case 'form': {
      const modelBind = getDirectiveProp(props, 'bind', 'model')
      if (modelBind) return normalizeExpressionTail(modelBind.exp)

      const text = getSimpleText(children)
      if (text) return normalizeToken(text)
      break
    }

    case 'option': {
      // el-option: label（显示文本）优先，value 次之
      const labelVal = getStaticOrDirectiveProp(props, 'label')
      if (labelVal?.kind === 'static') {
        return chineseTextStableKey(labelVal.value) || normalizeToken(labelVal.value)
      }
      if (labelVal?.kind === 'dynamic') {
        return normalizeExpressionTail(labelVal.value)
      }

      const valueVal = getStaticOrDirectiveProp(props, 'value')
      if (valueVal?.kind === 'static') return normalizeToken(valueVal.value)
      if (valueVal?.kind === 'dynamic') return normalizeExpressionTail(valueVal.value)
      break
    }

    case 'menuitem':
    case 'submenu': {
      const indexVal = getStaticOrDirectiveProp(props, 'index')
      if (indexVal?.kind === 'static') return normalizeToken(indexVal.value)
      if (indexVal?.kind === 'dynamic') return normalizeExpressionTail(indexVal.value)

      const text = getSimpleText(children)
      if (text) return normalizeToken(text)
      break
    }

    case 'menu': {
      const text = getSimpleText(children)
      if (text) return normalizeToken(text)

      const active = getVModelLastSegment(props)
      if (active) return normalizeToken(active)
      break
    }

    case 'dropdownitem': {
      const commandVal = getStaticOrDirectiveProp(props, 'command')
      if (commandVal?.kind === 'static') return normalizeToken(commandVal.value)
      if (commandVal?.kind === 'dynamic') return normalizeExpressionTail(commandVal.value)

      const text = getSimpleText(children)
      if (text) return normalizeToken(text)
      break
    }

    case 'dropdown':
    case 'dropdownmenu':
    case 'scrollbar':
    case 'layout':
    case 'breadcrumb':
    case 'pagination':
      // 纯容器 / 装饰性，无稳定语义，由计数器生成
      break

    case 'card': {
      const headerAttr = getStaticOrDirectiveProp(props, 'header')
      if (headerAttr?.kind === 'static') {
        return chineseTextStableKey(headerAttr.value) || normalizeToken(headerAttr.value)
      }
      if (headerAttr?.kind === 'dynamic') {
        return normalizeExpressionTail(headerAttr.value)
      }

      const headerSlot = getSlotText(children, 'header')
      if (headerSlot) return normalizeToken(headerSlot)

      const titleAttr = getStaticProp(props, 'title')
      if (titleAttr) return chineseTextStableKey(titleAttr) || normalizeToken(titleAttr)

      const text = getSimpleText(children)
      if (text) return normalizeToken(text)
      break
    }

    case 'descriptions': {
      const titleAttr = getStaticOrDirectiveProp(props, 'title')
      if (titleAttr?.kind === 'static') {
        return chineseTextStableKey(titleAttr.value) || normalizeToken(titleAttr.value)
      }
      if (titleAttr?.kind === 'dynamic') {
        return normalizeExpressionTail(titleAttr.value)
      }

      const columnAttr = getStaticOrDirectiveProp(props, 'column')
      if (columnAttr) return `col-${normalizeAttrValue(columnAttr)}`
      break
    }

    case 'descriptionsitem': {
      const labelAttr = getStaticOrDirectiveProp(props, 'label')
      if (labelAttr?.kind === 'static') {
        return chineseTextStableKey(labelAttr.value) || normalizeToken(labelAttr.value)
      }
      if (labelAttr?.kind === 'dynamic') {
        return normalizeExpressionTail(labelAttr.value)
      }

      const propAttr = getStaticProp(props, 'prop')
      if (propAttr) return normalizeToken(propAttr)
      break
    }

    case 'tag': {
      const text = getSimpleText(children)
      if (text) return normalizeToken(text)

      const typeAttr = getStaticOrDirectiveProp(props, 'type')
      if (typeAttr) return normalizeAttrValue(typeAttr)
      break
    }

    case 'icon': {
      const sizeAttr = getStaticOrDirectiveProp(props, 'size')
      if (sizeAttr) return `size-${normalizeAttrValue(sizeAttr)}`

      const colorAttr = getStaticOrDirectiveProp(props, 'color')
      if (colorAttr) return normalizeAttrValue(colorAttr)
      break
    }

    case 'text': {
      const text = getSimpleText(children)
      if (text) return normalizeToken(text)

      const typeAttr = getStaticOrDirectiveProp(props, 'type')
      if (typeAttr) return normalizeAttrValue(typeAttr)
      break
    }

    case 'link': {
      const text = getSimpleText(children)
      if (text) return normalizeToken(text)

      const href = getStaticOrDirectiveProp(props, 'href')
      if (href?.kind === 'static') {
        const last = href.value.split('/').filter(Boolean).pop() || href.value
        return normalizeToken(last)
      }
      break
    }

    case 'alert': {
      const titleAttr = getStaticOrDirectiveProp(props, 'title')
      if (titleAttr?.kind === 'static') {
        return chineseTextStableKey(titleAttr.value) || normalizeToken(titleAttr.value)
      }
      if (titleAttr?.kind === 'dynamic') {
        return normalizeExpressionTail(titleAttr.value)
      }

      const typeAttr = getStaticOrDirectiveProp(props, 'type')
      if (typeAttr) return normalizeAttrValue(typeAttr)

      const text = getSimpleText(children)
      if (text) return normalizeToken(text)
      break
    }

    case 'divider': {
      const directionAttr = getStaticOrDirectiveProp(props, 'direction')
      if (directionAttr) return normalizeAttrValue(directionAttr)

      const cpAttr = getStaticOrDirectiveProp(props, 'content-position')
      if (cpAttr) return normalizeAttrValue(cpAttr)
      break
    }

    case 'collapse': {
      const active = getVModelLastSegment(props)
      if (active) return normalizeToken(active)
      break
    }

    case 'collapseitem': {
      const titleAttr = getStaticOrDirectiveProp(props, 'title')
      if (titleAttr?.kind === 'static') {
        return chineseTextStableKey(titleAttr.value) || normalizeToken(titleAttr.value)
      }
      if (titleAttr?.kind === 'dynamic') {
        return normalizeExpressionTail(titleAttr.value)
      }

      const nameAttr = getStaticProp(props, 'name')
      if (nameAttr) return normalizeToken(nameAttr)

      const titleSlot = getSlotText(children, 'title')
      if (titleSlot) return normalizeToken(titleSlot)
      break
    }

    case 'breadcrumbitem': {
      const toAttr = getStaticOrDirectiveProp(props, 'to')
      if (toAttr?.kind === 'static') return normalizeToken(toAttr.value)

      const text = getSimpleText(children)
      if (text) return normalizeToken(text)
      break
    }

    case 'empty': {
      const descAttr = getStaticOrDirectiveProp(props, 'description')
      if (descAttr?.kind === 'static') return normalizeToken(descAttr.value)

      const text = getSimpleText(children)
      if (text) return normalizeToken(text)
      break
    }

    case 'checkbutton':
    case 'radiobutton': {
      const labelAttr = getStaticOrDirectiveProp(props, 'label')
      if (labelAttr?.kind === 'static') return normalizeToken(labelAttr.value)
      if (labelAttr?.kind === 'dynamic') return normalizeExpressionTail(labelAttr.value)

      const valueAttr = getStaticOrDirectiveProp(props, 'value')
      if (valueAttr?.kind === 'static') return normalizeToken(valueAttr.value)
      break
    }

    case 'col': {
      const spanAttr = getStaticOrDirectiveProp(props, 'span')
      if (spanAttr) return `span-${normalizeAttrValue(spanAttr)}`

      const text = getSimpleText(children)
      if (text) return normalizeToken(text)
      break
    }

    case 'row': {
      const gutterAttr = getStaticOrDirectiveProp(props, 'gutter')
      if (gutterAttr) return `gutter-${normalizeAttrValue(gutterAttr)}`
      break
    }

    case 'progress': {
      const percentageAttr = getStaticOrDirectiveProp(props, 'percentage')
      if (percentageAttr) return `${normalizeAttrValue(percentageAttr)}pct`

      const statusAttr = getStaticOrDirectiveProp(props, 'status')
      if (statusAttr) return normalizeAttrValue(statusAttr)
      break
    }

    case 'dynamic': {
      // <component :is="SomeComp"> 或 <component is="SomeComp">
      const isBind = getDirectiveProp(props, 'bind', 'is')
      if (isBind) return normalizeExpressionTail(isBind.exp)

      const isStatic = getStaticProp(props, 'is')
      if (isStatic) return normalizeToken(isStatic)
      break
    }

    case 'tab': {
      const fromModel = getVModelLastSegment(props)
      if (fromModel) return normalizeToken(fromModel)
      break
    }

    case 'tab-item':
      return resolveTabItemSemanticKey(props)
  }

  return null
}
