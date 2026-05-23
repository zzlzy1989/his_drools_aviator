<template>
  <div class="page-container">
    <div class="page-card">
      <!-- Search Bar -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="规则组名称">
          <el-input v-model="queryForm.groupName" placeholder="请输入" clearable />
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
      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="groupKey" label="组标识" width="200" />
        <el-table-column prop="groupName" label="规则组名称" />
        <el-table-column prop="description" label="描述" width="200" show-overflow-tooltip />
        <el-table-column prop="priority" label="优先级" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
              {{ row.status === 'active' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
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
        <el-form-item label="组标识" prop="groupCode">
          <el-input v-model="form.groupCode" :disabled="isEdit" placeholder="如: REIMBURSEMENT/DRUG_CHECK" />
        </el-form-item>
        <el-form-item label="名称" prop="groupName">
          <el-input v-model="form.groupName" placeholder="请输入规则组名称" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="form.priority" :min="0" :max="9999" />
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
import { ref, reactive, onMounted, computed } from 'vue'
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
const editRowId = ref<number>(0)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const tableData = ref<RuleGroupVO[]>([])
const formRef = ref<FormInstance>()

const queryForm = reactive<RuleGroupQueryDTO>({
  groupName: '',
  status: '',
})

const form = reactive<CreateRuleGroupDTO>({
  groupCode: '',
  groupName: '',
  description: '',
  priority: 0,
})

const rules = {
  groupCode: [{ required: true, message: '请输入组标识', trigger: 'blur' }],
  groupName: [{ required: true, message: '请输入规则组名称', trigger: 'blur' }],
}

async function loadData() {
  loading.value = true
  try {
    const res = await getRuleGroupPage(page.value, pageSize.value, queryForm)
    tableData.value = res.list.map((item: any) => ({
      ...item,
      groupKey: item.groupCode || item.groupKey,
      status: item.isEnabled === 1 || item.isEnabled === true ? 'active' : 'inactive',
      priority: item.priority || 0,
    }))
    total.value = res.total
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryForm.groupName = ''
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
  editRowId.value = row.id
  form.groupCode = row.groupKey
  form.groupName = row.groupName
  form.description = row.description || ''
  form.priority = row.priority || 0
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (isEdit.value) {
      await updateRuleGroup(editRowId.value, {
        groupName: form.groupName,
        description: form.description,
        priority: form.priority,
      } as UpdateRuleGroupDTO)
    } else {
      await createRuleGroup(form)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    saving.value = false
  }
}

async function handleToggle(row: RuleGroupVO) {
  await toggleRuleGroupStatus(row.id, row.status === 'active')
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
  form.groupCode = ''
  form.groupName = ''
  form.description = ''
  form.priority = 0
  editRowId.value = 0
  formRef.value?.resetFields()
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.page-container {
  @include page-container;
}

.page-card {
  @include apple-card;
  border-radius: $radius-lg;
}

.search-form {
  @include search-form;
}

.toolbar {
  @include toolbar;
}

.pagination {
  @include pagination-wrapper;
}
</style>
