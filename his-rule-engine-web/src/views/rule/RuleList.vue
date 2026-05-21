<template>
  <div class="his-rule-list">
    <div class="his-rule-list__card">
      <div class="his-rule-list__search">
        <el-form :inline="true" :model="queryForm" class="his-rule-list__search-form">
          <el-form-item label="规则名称">
            <el-input v-model="queryForm.ruleName" placeholder="请输入" clearable />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="queryForm.category" placeholder="请选择" clearable>
              <el-option label="医保规则" value="insurance" />
              <el-option label="临床规则" value="clinical" />
              <el-option label="用药规则" value="medication" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryForm.status" placeholder="请选择" clearable>
              <el-option label="草稿" value="draft" />
              <el-option label="已发布" value="active" />
              <el-option label="已停用" value="inactive" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="his-rule-list__toolbar">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增规则</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="ruleKey" label="规则Key" width="220" />
        <el-table-column prop="ruleName" label="规则名称" />
        <el-table-column prop="category" label="分类" width="120">
          <template #default="{ row }">
            <span v-if="row.category === 'insurance'" class="his-rule-list__category his-rule-list__category--insurance">医保规则</span>
            <span v-else-if="row.category === 'clinical'" class="his-rule-list__category his-rule-list__category--clinical">临床规则</span>
            <span v-else class="his-rule-list__category">{{ row.category }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :type="row.status === 'active' ? 'published' : row.status === 'inactive' ? 'disabled' : 'draft'" show-dot size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status !== 'active'" link type="success" @click="handlePublish(row)">发布</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="his-rule-list__pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑规则' : '新增规则'"
      width="700px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="规则Key" prop="ruleKey">
          <el-input v-model="form.ruleKey" :disabled="isEdit" placeholder="如: rule.insurance.limit_01" />
        </el-form-item>
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="form.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择">
            <el-option label="医保规则" value="insurance" />
            <el-option label="临床规则" value="clinical" />
            <el-option label="用药规则" value="medication" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="form.priority" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="DRL内容" prop="ruleText">
          <el-input v-model="form.ruleText" type="textarea" :rows="8" placeholder="请输入DRL规则内容" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewVisible" title="规则详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="规则Key">{{ viewData.ruleKey }}</el-descriptions-item>
        <el-descriptions-item label="规则名称">{{ viewData.ruleName }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ viewData.category }}</el-descriptions-item>
        <el-descriptions-item label="优先级">{{ viewData.priority }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ viewData.status }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ viewData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="DRL内容" :span="2">
          <pre style="white-space: pre-wrap; margin: 0">{{ viewData.ruleText }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { StatusTag } from '@/components/HIS'
import {
  getRulePage,
  createRule,
  updateRule,
  deleteRule,
  publishRule,
} from '@/api/rule-definition'
import type {
  RuleDefinitionVO,
  CreateRuleDTO,
  UpdateRuleDTO,
  RuleQueryDTO,
} from '@/api/rule-definition'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const viewVisible = ref(false)
const isEdit = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const tableData = ref<RuleDefinitionVO[]>([])
const formRef = ref<FormInstance>()

const queryForm = reactive<RuleQueryDTO>({
  ruleName: '',
  category: '',
  status: '',
})

const form = reactive<CreateRuleDTO>({
  ruleKey: '',
  ruleName: '',
  groupId: 0,
  ruleText: '',
  category: 'insurance',
  priority: 0,
  status: 'draft',
  description: '',
})

const viewData = reactive<RuleDefinitionVO>({
  id: 0, ruleKey: '', ruleName: '', groupId: 0, ruleText: '',
  category: '', priority: 0, status: 'draft', tenantId: '', createTime: '', updateTime: '',
})

const rules = {
  ruleKey: [{ required: true, message: '请输入规则Key', trigger: 'blur' }],
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleText: [{ required: true, message: '请输入DRL内容', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
}

async function loadData() {
  loading.value = true
  try {
    const res = await getRulePage(page.value, pageSize.value, queryForm)
    tableData.value = res.list
    total.value = res.total
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryForm.ruleName = ''
  queryForm.category = ''
  queryForm.status = ''
  page.value = 1
  loadData()
}

function handleAdd() {
  isEdit.value = false
  dialogVisible.value = true
}

function handleEdit(row: RuleDefinitionVO) {
  isEdit.value = true
  form.ruleKey = row.ruleKey
  form.ruleName = row.ruleName
  form.groupId = row.groupId
  form.ruleText = row.ruleText
  form.category = row.category
  form.priority = row.priority
  form.description = row.description || ''
  form.status = row.status
  dialogVisible.value = true
}

function handleView(row: RuleDefinitionVO) {
  Object.assign(viewData, row)
  viewVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value) {
      const id = tableData.value.find(t => t.ruleKey === form.ruleKey)?.id
      if (id) await updateRule(id, form as UpdateRuleDTO)
    } else {
      await createRule(form)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

async function handlePublish(row: RuleDefinitionVO) {
  await publishRule(row.id)
  ElMessage.success('发布成功')
  loadData()
}

async function handleDelete(row: RuleDefinitionVO) {
  await ElMessageBox.confirm(`确定删除规则「${row.ruleName}」？`, '确认删除', { type: 'warning' })
  await deleteRule(row.id)
  ElMessage.success('删除成功')
  loadData()
}

function resetForm() {
  form.ruleKey = ''
  form.ruleName = ''
  form.groupId = 0
  form.ruleText = ''
  form.category = 'insurance'
  form.priority = 0
  form.status = 'draft'
  form.description = ''
  formRef.value?.resetFields()
}

onMounted(() => { loadData() })
</script>

<style lang="scss" scoped>

.his-rule-list {
  &__card {
    background-color: $color-canvas;
    border: 1px solid $color-hairline;
    padding: $spacing-lg;
  }

  &__search {
    margin-bottom: $spacing-md;
  }

  &__search-form {
    display: flex;
    flex-wrap: wrap;
    gap: 0;
  }

  &__toolbar {
    margin-bottom: $spacing-md;
  }

  &__pagination {
    margin-top: $spacing-md;
    display: flex;
    justify-content: flex-end;
  }

  &__category {
    display: inline-flex;
    align-items: center;
    padding: 2px 8px;
    border-radius: $radius-xs;
    font-size: $font-size-body;
    font-weight: $font-weight-semibold;
    letter-spacing: $letter-spacing-body;

    &--insurance {
      color: $color-semantic-info;
      background-color: $color-semantic-info-bg;
    }

    &--clinical {
      color: $color-semantic-pass;
      background-color: $color-semantic-pass-bg;
    }

    &--medication {
      color: $color-semantic-warn;
      background-color: $color-semantic-warn-bg;
    }
  }
}
</style>
