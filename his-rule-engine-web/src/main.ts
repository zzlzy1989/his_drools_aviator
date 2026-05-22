import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import router from './router'
import App from './App.vue'
import '@/assets/styles/index.scss'

// 按需导入 Element Plus 组件（减少包体积）
import {
  ElButton,
  ElInput,
  ElForm,
  ElFormItem,
  ElTable,
  ElTableColumn,
  ElPagination,
  ElDialog,
  ElDropdown,
  ElDropdownMenu,
  ElDropdownItem,
  ElMenu,
  ElMenuItem,
  ElSubMenu,
  ElIcon,
  ElBreadcrumb,
  ElBreadcrumbItem,
  ElSelect,
  ElOption,
  ElDatePicker,
  ElTag,
  ElMessage,
  ElMessageBox,
  ElLoading,
  ElAvatar,
  ElEmpty,
  ElCard,
  ElRow,
  ElCol,
  ElDivider,
  ElTooltip,
  ElPopover,
  ElPopper,
  ElBadge,
  ElDescriptions,
  ElDescriptionsItem,
  ElButtonGroup,
  ElInputNumber,
  ElSwitch,
  ElCheckbox,
  ElRadio,
  ElRadioGroup,
  ElSlider,
  ElTimePicker,
  ElColorPicker,
} from 'element-plus'

const app = createApp(App)
const pinia = createPinia()

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册 Element Plus 组件（按需）
app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn })

// 按需注册组件
const components = [
  ElButton,
  ElInput,
  ElForm,
  ElFormItem,
  ElTable,
  ElTableColumn,
  ElPagination,
  ElDialog,
  ElDropdown,
  ElDropdownMenu,
  ElDropdownItem,
  ElMenu,
  ElMenuItem,
  ElSubMenu,
  ElIcon,
  ElBreadcrumb,
  ElBreadcrumbItem,
  ElSelect,
  ElOption,
  ElDatePicker,
  ElTag,
  ElMessage,
  ElMessageBox,
  ElLoading,
  ElAvatar,
  ElEmpty,
  ElCard,
  ElRow,
  ElCol,
  ElDivider,
  ElTooltip,
  ElPopover,
  ElPopper,
  ElBadge,
  ElDescriptions,
  ElDescriptionsItem,
  ElButtonGroup,
  ElInputNumber,
  ElSwitch,
  ElCheckbox,
  ElRadio,
  ElRadioGroup,
  ElSlider,
  ElTimePicker,
  ElColorPicker,
]

components.forEach(comp => {
  app.use(comp)
})

app.mount('#app')