/**
 * @file 标签 → 元素类型映射表
 * @description
 *   将 Element Plus 与项目自定义组件标签名映射到 testid 中的元素类型片段。
 *   生成规则示例：el-button → btn → `data-testid="page-btn-{semantic}"`
 *
 *   扩展方式（二选一）：
 *   1. 通用映射：直接编辑本文件，往下加键值对（推荐，团队共享）
 *   2. 项目级临时映射：在 vite.config.js 调用
 *      createAutoTestIdPlugin({ tagTypeMap: { 'my-component': 'select' } })
 *      会与本表合并，用户配置优先
 *
 *   命名约定：
 *   - **只列 kebab-case**（如 `el-button`、`el-form-item`）；PascalCase 写法
 *     （如 `ElButton`、`ElFormItem`）由 `helpers.js` 的 `normalizeTagName` 自动
 *     转为 kebab-case 查表，不需要重复登记
 *   - 同语义组件用同一类型（如所有下拉选择类都用 'select'，便于 UI 录制器统一处理）
 *
 *   覆盖范围：表单控件 / 选择器 / 表格 / 弹窗 / 菜单 / 反馈 / 布局 / 数据展示
 *   等所有 Element Plus 在本项目 `src/views/` 下实际使用的组件。
 */

/**
 * 默认标签 → 元素类型映射
 * @type {Record<string, string>}
 */
export const DEFAULT_TAG_TYPE_MAP = {
  // ===== 按钮类（点击触发动作） =====
  'el-button': 'btn',

  // ===== 表单输入类 =====
  'el-input': 'input',
  'el-input-number': 'input',

  // ===== 表单容器与字段标签 =====
  'el-form': 'form',
  'el-form-item': 'formitem',

  // ===== 选择器类（统一为 select，下游录制器统一处理） =====
  'el-select': 'select',
  'el-cascader': 'select',
  'el-option': 'option',
  'el-select-v2': 'select',
  'el-cascader-panel': 'select',
  'dept-select': 'select',
  'storage-select': 'select',
  'user-select': 'select',
  'user-select-model': 'select',
  'dept-select-model': 'select',

  // ===== 日期 / 时间 =====
  'el-date-picker': 'datepicker',
  'el-time-picker': 'timepicker',
  'el-time-select': 'timeselect',

  // ===== 开关 / 复选 / 单选 =====
  'el-switch': 'switch',
  'el-checkbox': 'check',
  'el-checkbox-group': 'checkgroup',
  'el-checkbox-button': 'checkbutton',
  'el-radio': 'radio',
  'el-radio-group': 'radiogroup',
  'el-radio-button': 'radiobutton',

  // ===== 树形选择 / 树 =====
  'v-tree-select': 'tree-select',
  'list-tree': 'tree',
  'dept-tree': 'tree',

  // ===== 表格 =====
  'el-table': 'table',
  'el-table-column': 'tablecol',
  'el-pagination': 'pagination',

  // ===== Tabs =====
  'el-tabs': 'tab',
  'el-tab-pane': 'tab-item',

  // ===== 弹窗 / 抽屉 =====
  'el-dialog': 'dialog',
  'el-drawer': 'drawer',
  'el-popover': 'popover',
  'el-tooltip': 'tooltip',
  'el-popconfirm': 'popconfirm',

  // ===== 菜单 / 下拉 =====
  'el-menu': 'menu',
  'el-menu-item': 'menuitem',
  'el-sub-menu': 'submenu',
  'el-dropdown': 'dropdown',
  'el-dropdown-menu': 'dropdownmenu',
  'el-dropdown-item': 'dropdownitem',

  // ===== 数据展示 =====
  'el-card': 'card',
  'el-tag': 'tag',
  'el-descriptions': 'descriptions',
  'el-descriptions-item': 'descriptionsitem',
  'el-empty': 'empty',
  'el-text': 'text',
  'el-link': 'link',
  'el-icon': 'icon',
  'el-image': 'image',
  'el-avatar': 'avatar',
  'el-badge': 'badge',
  'el-alert': 'alert',
  'el-divider': 'divider',
  'el-scrollbar': 'scrollbar',
  'el-progress': 'progress',
  'el-skeleton': 'skeleton',
  'el-skeleton-item': 'skeletonitem',

  // ===== 折叠 / 面包屑 / 步骤 =====
  'el-collapse': 'collapse',
  'el-collapse-item': 'collapseitem',
  'el-breadcrumb': 'breadcrumb',
  'el-breadcrumb-item': 'breadcrumbitem',
  'el-steps': 'steps',
  'el-step': 'step',

  // ===== 布局（一般用容器上下文定位，列用 span） =====
  'el-row': 'row',
  'el-col': 'col',
  'el-container': 'layout',
  'el-header': 'layout',
  'el-aside': 'layout',
  'el-main': 'layout',
  'el-footer': 'layout',

  // ===== Vue 动态组件（<component :is="...">） =====
  // 这些元素内包含运行时才确定的组件；插件只能给外层 wrapper 加 testid，
  // 真实动态组件实例仍需运行时 DOM 增强（当前范围之外）。
  'component': 'dynamic',
}
