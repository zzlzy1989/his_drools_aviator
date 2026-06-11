/**
 * @file Vite 插件入口
 * @description
 *   vite-plugin-auto-testid：构建期自动为 Vue SFC 模板中的目标组件
 *   注入 data-testid 属性，用于 UI 自动化录制 / 定位。
 *
 * 用法（vite.config.js）：
 *
 *   import createAutoTestIdPlugin from './scripts/plugins/auto-testid/index.js'
 *
 *   export default defineConfig(({ mode }) => {
 *     const env = loadEnv(mode, process.cwd(), '')
 *     return {
 *       plugins: [
 *         vue(),
 *         createAutoTestIdPlugin({
 *           enabled: env.VITE_ENABLE_AUTO_TESTID !== 'false',
 *           // 可选：扩展标签映射（与默认表合并，用户优先）
 *           // tagTypeMap: { 'my-custom-button': 'btn' },
 *           // 可选：调试时打印每文件注入数
 *           // verbose: true,
 *         }),
 *       ],
 *     }
 *   })
 *
 * 环境变量：
 *   VITE_ENABLE_AUTO_TESTID=false  关闭插件（不注入任何 testid）
 *
 * 详细文档见同目录 README.md
 */

import { parse as parseSfc } from 'vue/compiler-sfc'
import { normalizeTagName } from './helpers.js'
import { DEFAULT_TAG_TYPE_MAP } from './tag-map.js'
import { injectTabPaneLabelTestIds, processTemplate } from './transformer.js'

/**
 * @typedef {Object} AutoTestIdOptions
 * @property {boolean} [enabled=true]
 *   是否启用插件。false 时 transform 直接返回 null，不修改任何文件。
 * @property {Record<string, string>} [tagTypeMap]
 *   额外的 tag → 元素类型映射，与 DEFAULT_TAG_TYPE_MAP 合并（用户优先）。
 * @property {string} [signName="test-id"]
 *   注入的属性名。一般不要改，UI 自动化录制器约定为 test-id。
 * @property {(RegExp|string)[]} [include]
 *   仅处理匹配的文件 ID。默认 [/\.vue($|\?)/]（所有 .vue）。
 * @property {(RegExp|string)[]} [exclude]
 *   跳过匹配的文件 ID。默认 [/node_modules/]。
 * @property {boolean} [verbose=false]
 *   打印每个文件的注入数量到控制台。
 */

const DEFAULT_INCLUDE = [/\.vue($|\?)/]
const DEFAULT_EXCLUDE = [/node_modules/]
const DEFAULT_SIGN_NAME = 'test-id'

/**
 * 文件 ID 是否匹配模式（字符串包含 或 正则 test）
 * @param {string} id
 * @param {(RegExp|string)[]} patterns
 * @returns {boolean}
 */
function matchesAny(id, patterns) {
  for (const p of patterns) {
    if (typeof p === 'string') {
      if (id.includes(p)) return true
    } else if (p instanceof RegExp) {
      if (p.test(id)) return true
    }
  }
  return false
}

/**
 * 粗略估算源码里 data-testid 出现的差量（用于 verbose 计数）
 * @param {string} before
 * @param {string} after
 * @param {string} signName
 * @returns {number}
 */
function countInjected(before, after, signName) {
  const re = new RegExp(`\\s${signName}=`, 'g')
  const beforeCount = (before.match(re) || []).length
  const afterCount = (after.match(re) || []).length
  return afterCount - beforeCount
}

/**
 * 创建 auto-testid Vite 插件
 *
 * @param {AutoTestIdOptions} [options]
 * @returns {import('vite').Plugin}
 */
export function createAutoTestIdPlugin(options = {}) {
  const {
    enabled = true,
    tagTypeMap = {},
    signName = DEFAULT_SIGN_NAME,
    include = DEFAULT_INCLUDE,
    exclude = DEFAULT_EXCLUDE,
    verbose = false,
  } = options

  // 合并标签映射：默认表 + 用户表（用户优先）
  const mergedTagTypeMap = {
    ...DEFAULT_TAG_TYPE_MAP,
    ...tagTypeMap,
  }

  /**
   * 查标签的元素类型：先按原样查，再按 kebab-case 查
   * @param {string} tagName
   * @returns {string | undefined}
   */
  const resolveElementType = (tagName) => {
    if (mergedTagTypeMap[tagName]) return mergedTagTypeMap[tagName]
    const kebab = normalizeTagName(tagName)
    return mergedTagTypeMap[kebab]
  }

  return {
    name: 'vite-plugin-auto-testid',
    enforce: 'pre',

    transform(code, id) {
      if (!enabled) return null

      // 文件过滤
      if (!matchesAny(id, include)) return null
      if (matchesAny(id, exclude)) return null

      // 解析 SFC，无 template 块直接跳过
      const sfcResult = parseSfc(code, { filename: id })
      const templateBlock = sfcResult.descriptor.template
      if (!templateBlock) return null

      // 两阶段处理：先 tab-pane 的 label slot 补全，再通用注入
      const withTabPaneLabels = injectTabPaneLabelTestIds(
        templateBlock.content,
        id,
        signName
      )
      const processed = processTemplate(
        withTabPaneLabels,
        id,
        resolveElementType,
        signName
      )

      // 无改动则不返回新代码（避免 Vite 误判文件变化）
      if (processed === templateBlock.content) return null

      // 拼回 SFC 全代码：原 code 头 + 处理后的 template 内容 + 原 code 尾
      const templateStart = templateBlock.loc.start.offset
      const templateEnd = templateBlock.loc.end.offset
      const newCode = code.slice(0, templateStart) + processed + code.slice(templateEnd)

      if (verbose) {
        const count = countInjected(templateBlock.content, processed, signName)
        if (count > 0) {
          // eslint-disable-next-line no-console
          console.log(`[auto-testid] ${id}: 注入 ${count} 个 ${signName}`)
        }
      }

      return { code: newCode, map: null }
    },
  }
}

export default createAutoTestIdPlugin
