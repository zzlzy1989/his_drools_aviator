<template>
  <div class="page-container">
    <div class="page-card">
      <!-- Search Bar -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="规则组名称">
          <el-input v-model="queryForm.groupName" placeholder="请输入" clearable />
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
            <el-option label="启用" value="active" />
            <el-option label="停用" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- Toolbar -->
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增规则组</el-button>
      </div>

      <!-- Table -->
      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="groupKey" label="组标识" width="180" />
        <el-table-column prop="groupName" label="规则组名称" />
        <el-table-column prop="category" label="分类" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.category === 'insurance'" type="primary" size="small">医保规则</el-tag>
            <el-tag v-else-if="row.category === 'clinical'" type="success" size="small">临床规则</el-tag>
            <el-tag v-else type="info" size="small">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
              {{ row.status === 'active' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 'active' ? 'warning' : 'success'" @click="handleToggle(row)">
              {{ row.status === 'active' ? '停用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination">
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

    <!-- Add/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑规则组' : '新增规则组'"
      width="600px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="组标识" prop="groupKey">
          <el-input v-model="form.groupKey" :disabled="isEdit" placeholder="如: rule_group_insurance_01" />
        </el-form-item>
        <el-form-item label="名称" prop="groupName">
          <el-input v-model="form.groupName" placeholder="请输入规则组名称" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择">
            <el-option label="医保规则" value="insurance" />
            <el-option label="临床规则" value="clinical" />
            <el-option label="用药规则" value="medication" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getRuleGroupPage,
  createRuleGroup,
  updateRuleGroup,
  deleteRuleGroup,
  toggleRuleGroupStatus,
} from '@/api/rule-group'
import type {
  RuleGroupVO,
  CreateRuleGroupDTO,
  UpdateRuleGroupDTO,
  RuleGroupQueryDTO,
} from '@/api/rule-group'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const tableData = ref<RuleGroupVO[]>([])
const formRef = ref<FormInstance>()

const queryForm = reactive<RuleGroupQueryDTO>({
  groupName: '',
  category: '',
  status: '',
})

const form = reactive<CreateRuleGroupDTO>({
  groupKey: '',
  groupName: '',
  description: '',
  category: 'insurance',
  sortOrder: 0,
  status: 'active',
})

const rules = {
  groupKey: [{ required: true, message: '请输入组标识', trigger: 'blur' }],
  groupName: [{ required: true, message: '请输入规则组名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
}

async function loadData() {
  loading.value = true
  try {
    const res = await getRuleGroupPage(page.value, pageSize.value)
    tableData.value = res.list
    total.value = res.total
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryForm.groupName = ''
  queryForm.category = ''
  queryForm.status = ''
  page.value = 1
  loadData()
}

function handleAdd() {
  isEdit.value = false
  dialogVisible.value = true
}

function handleEdit(row: RuleGroupVO) {
  isEdit.value = true
  form.groupKey = row.groupKey
  form.groupName = row.groupName
  form.description = row.description || ''
  form.category = row.category
  form.sortOrder = row.sortOrder
  form.status = row.status
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (isEdit.value) {
      const id = tableData.value.find(t => t.groupKey === form.groupKey)?.id
      if (id) {
        await updateRuleGroup(id, form as UpdateRuleGroupDTO)
      }
    } else {
      await createRuleGroup(form)
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

async function handleToggle(row: RuleGroupVO) {
  await toggleRuleGroupStatus(row.id)
  ElMessage.success('状态切换成功')
  loadData()
}

async function handleDelete(row: RuleGroupVO) {
  await ElMessageBox.confirm(`确定删除规则组「${row.groupName}」？`, '确认删除', {
    type: 'warning',
  })
  await deleteRuleGroup(row.id)
  ElMessage.success('删除成功')
  loadData()
}

function resetForm() {
  form.groupKey = ''
  form.groupName = ''
  form.description = ''
  form.category = 'insurance'
  form.sortOrder = 0
  form.status = 'active'
  formRef.value?.resetFields()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 16px;
}
.page-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}
.search-form {
  margin-bottom: 16px;
}
.toolbar {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
