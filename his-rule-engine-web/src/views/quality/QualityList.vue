<template>
  <div class="his-quality-list">
    <div class="his-quality-list__card">
      <div class="his-quality-list__search">
        <el-form :inline="true" :model="queryForm" class="his-quality-list__search-form">
          <el-form-item label="质控项目">
            <el-input v-model="queryForm.itemName" placeholder="请输入" clearable />
          </el-form-item>
          <el-form-item label="质控类型">
            <el-select v-model="queryForm.category" placeholder="请选择" clearable>
              <el-option label="合理用药" value="rational_drug" />
              <el-option label="诊疗规范" value="diagnosis" />
              <el-option label="费用控制" value="cost_control" />
            </el-select>
          </el-form-item>
          <el-form-item label="等级">
            <el-select v-model="queryForm.level" placeholder="请选择" clearable>
              <el-option label="提示" value="info" />
              <el-option label="警告" value="warning" />
              <el-option label="拦截" value="error" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="his-quality-list__toolbar">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增质控</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="itemKey" label="项目Key" width="200" />
        <el-table-column prop="itemName" label="质控项目" />
        <el-table-column prop="category" label="类型" width="120">
          <template #default="{ row }">
            <span :class="['his-quality-list__category', `his-quality-list__category--${row.category}`]">{{ categoryLabel(row.category) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="等级" width="100">
          <template #default="{ row }">
            <StatusTag :type="qualityLevelType(row.level)" show-dot size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :type="row.status === 'active' ? 'published' : 'disabled'" show-dot size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="his-quality-list__pagination">
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
      :title="isEdit ? '编辑质控' : '新增质控'"
      width="600px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="项目Key" prop="itemKey">
          <el-input v-model="form.itemKey" :disabled="isEdit" placeholder="如: quality.drug.compatibility" />
        </el-form-item>
        <el-form-item label="质控项目" prop="itemName">
          <el-input v-model="form.itemName" placeholder="请输入质控项目名称" />
        </el-form-item>
        <el-form-item label="类型" prop="category">
          <el-select v-model="form.category" placeholder="请选择">
            <el-option label="合理用药" value="rational_drug" />
            <el-option label="诊疗规范" value="diagnosis" />
            <el-option label="费用控制" value="cost_control" />
          </el-select>
        </el-form-item>
        <el-form-item label="等级" prop="level">
          <el-select v-model="form.level" placeholder="请选择">
            <el-option label="提示" value="info" />
            <el-option label="警告" value="warning" />
            <el-option label="拦截" value="error" />
          </el-select>
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
import { StatusTag } from '@/components/HIS'
import {
  getQualityPage,
  createQuality,
  updateQuality,
  deleteQuality,
} from '@/api/quality'
import type {
  QualityVO,
  CreateQualityDTO,
  UpdateQualityDTO,
  QualityQueryDTO,
} from '@/api/quality'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const tableData = ref<QualityVO[]>([])
const formRef = ref<FormInstance>()

const queryForm = reactive<QualityQueryDTO>({
  itemName: '',
  category: '',
  level: '',
  status: '',
})

const form = reactive<CreateQualityDTO>({
  itemKey: '',
  itemName: '',
  category: 'rational_drug',
  level: 'warning',
  description: '',
  status: 'active',
})

const rules = {
  itemKey: [{ required: true, message: '请输入项目Key', trigger: 'blur' }],
  itemName: [{ required: true, message: '请输入质控项目', trigger: 'blur' }],
  category: [{ required: true, message: '请选择类型', trigger: 'change' }],
  level: [{ required: true, message: '请选择等级', trigger: 'change' }],
}

function categoryLabel(cat: string) {
  const map: Record<string, string> = { rational_drug: '合理用药', diagnosis: '诊疗规范', cost_control: '费用控制' }
  return map[cat] || cat
}

function qualityLevelType(level: string): 'info' | 'warn' | 'block' {
  const map: Record<string, 'info' | 'warn' | 'block'> = { info: 'info', warning: 'warn', error: 'block' }
  return map[level] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await getQualityPage(page.value, pageSize.value, queryForm)
    tableData.value = res.list
    total.value = res.total
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryForm.itemName = ''
  queryForm.category = ''
  queryForm.level = ''
  page.value = 1
  loadData()
}

function handleAdd() {
  isEdit.value = false
  dialogVisible.value = true
}

function handleEdit(row: QualityVO) {
  isEdit.value = true
  form.itemKey = row.itemKey
  form.itemName = row.itemName
  form.category = row.category
  form.level = row.level
  form.description = row.description || ''
  form.status = row.status
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value) {
      const id = tableData.value.find(t => t.itemKey === form.itemKey)?.id
      if (id) await updateQuality(id, form as UpdateQualityDTO)
    } else {
      await createQuality(form)
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

async function handleDelete(row: QualityVO) {
  await ElMessageBox.confirm(`确定删除质控「${row.itemName}」？`, '确认删除', { type: 'warning' })
  await deleteQuality(row.id)
  ElMessage.success('删除成功')
  loadData()
}

function resetForm() {
  form.itemKey = ''
  form.itemName = ''
  form.category = 'rational_drug'
  form.level = 'warning'
  form.description = ''
  form.status = 'active'
  formRef.value?.resetFields()
}

onMounted(() => { loadData() })
</script>

<style lang="scss" scoped>

.his-quality-list {
  &__card {
    @include apple-card;
    border-radius: $radius-lg;
  }

  &__search {
    @include search-form;
  }

  &__search-form {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-xs $spacing-md;
    align-items: flex-end;

    .el-form-item {
      margin-right: 0;
      margin-bottom: $spacing-xs;
    }
  }

  &__toolbar {
    @include toolbar;
  }

  &__pagination {
    @include pagination-wrapper;
  }

  &__category {
    display: inline-flex;
    align-items: center;
    padding: 3px 10px;
    border-radius: $radius-pill;
    font-size: $font-size-caption;
    font-weight: $font-weight-semibold;
    letter-spacing: $letter-spacing-caption;

    &--rational_drug {
      color: $color-semantic-info;
      background-color: $color-semantic-info-bg;
    }

    &--diagnosis {
      color: $color-semantic-pass;
      background-color: $color-semantic-pass-bg;
    }

    &--cost_control {
      color: $color-semantic-warn;
      background-color: $color-semantic-warn-bg;
    }
  }
}
</style>
