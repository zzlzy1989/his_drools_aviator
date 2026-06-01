<template>
  <div class="his-drg-list">
    <div class="his-drg-list__card">
      <div class="his-drg-list__search">
        <el-form :inline="true" :model="queryForm" class="his-drg-list__search-form">
          <el-form-item label="DRG名称">
            <el-input v-model="queryForm.drgName" placeholder="请输入" clearable />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="queryForm.category" placeholder="请选择" clearable>
              <el-option label="手术" value="surgery" />
              <el-option label="内科" value="medical" />
              <el-option label="其他" value="other" />
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
      </div>

      <div class="his-drg-list__toolbar">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增DRG</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="drgCode" label="DRG编码" width="180" />
        <el-table-column prop="drgName" label="DRG名称" />
        <el-table-column prop="mdcCode" label="分类" width="120">
          <template #default="{ row }">
            <span :class="['his-drg-list__category', `his-drg-list__category--${row.category}`]">{{ categoryLabel(row.category) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="mdcName" label="分类名称" width="150" />
        <el-table-column prop="baseWeight" label="权重" width="100" align="right">
          <template #default="{ row }">
            <span class="his-drg-list__weight">{{ row.baseWeight?.toFixed(4) }}</span>
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

      <div class="his-drg-list__pagination">
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
      :title="isEdit ? '编辑DRG' : '新增DRG'"
      width="600px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="DRG编码" prop="drgCode">
          <el-input v-model="form.drgCode" :disabled="isEdit" placeholder="如: DRG_A01" />
        </el-form-item>
        <el-form-item label="DRG名称" prop="drgName">
          <el-input v-model="form.drgName" placeholder="请输入DRG名称" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择">
            <el-option label="手术" value="surgery" />
            <el-option label="内科" value="medical" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="权重" prop="weight">
          <el-input-number v-model="form.weight" :min="0" :precision="4" :step="0.01" />
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
  getDrgPage,
  createDrg,
  updateDrg,
  deleteDrg,
} from '@/api/drg'
import type {
  DrgVO,
  CreateDrgDTO,
  UpdateDrgDTO,
  DrgQueryDTO,
} from '@/api/drg'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const tableData = ref<DrgVO[]>([])
const formRef = ref<FormInstance>()

const queryForm = reactive<DrgQueryDTO>({
  drgName: '',
  category: '',
  status: '',
})

const form = reactive<CreateDrgDTO>({
  drgCode: '',
  drgName: '',
  category: 'surgery',
  weight: 1.0,
  description: '',
  status: 'active',
})

const rules = {
  drgCode: [{ required: true, message: '请输入DRG编码', trigger: 'blur' }],
  drgName: [{ required: true, message: '请输入DRG名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  weight: [{ required: true, message: '请输入权重', trigger: 'blur' }],
}

function categoryLabel(cat: string) {
  const map: Record<string, string> = { surgery: '手术', medical: '内科', other: '其他' }
  return map[cat] || cat
}

async function loadData() {
  loading.value = true
  try {
    const res = await getDrgPage(page.value, pageSize.value, queryForm)
    tableData.value = res.list
    total.value = res.total
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryForm.drgName = ''
  queryForm.category = ''
  queryForm.status = ''
  page.value = 1
  loadData()
}

function handleAdd() {
  isEdit.value = false
  dialogVisible.value = true
}

function handleEdit(row: DrgVO) {
  isEdit.value = true
  form.drgCode = row.drgCode
  form.drgName = row.drgName
  form.category = row.category
  form.weight = row.weight
  form.description = row.description || ''
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value) {
      const id = tableData.value.find(t => t.drgCode === form.drgCode)?.id
      if (id) await updateDrg(id, form as UpdateDrgDTO)
    } else {
      await createDrg(form)
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

async function handleDelete(row: DrgVO) {
  await ElMessageBox.confirm(`确定删除DRG「${row.drgName}」？`, '确认删除', { type: 'warning' })
  await deleteDrg(row.id)
  ElMessage.success('删除成功')
  loadData()
}

function resetForm() {
  form.drgCode = ''
  form.drgName = ''
  form.category = 'surgery'
  form.weight = 1.0
  form.description = ''
  formRef.value?.resetFields()
}

onMounted(() => { loadData() })
</script>

<style lang="scss" scoped>

.his-drg-list {
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

    &--surgery {
      color: $color-semantic-info;
      background-color: $color-semantic-info-bg;
    }

    &--medical {
      color: $color-semantic-pass;
      background-color: $color-semantic-pass-bg;
    }

    &--other {
      color: $color-ink-secondary;
      background-color: $color-surface-1;
    }
  }

  &__weight {
    font-family: $font-family-mono;
    font-weight: $font-weight-semibold;
    font-size: $font-size-fine-print;
    color: $color-ink;
    font-feature-settings: "tnum";
  }
}
</style>
