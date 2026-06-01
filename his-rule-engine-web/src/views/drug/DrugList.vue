<template>
  <div class="his-drug-list">
    <div class="his-drug-list__card">
      <div class="his-drug-list__search">
        <el-form :inline="true" :model="queryForm" class="his-drug-list__search-form">
          <el-form-item label="药品名称">
            <el-input v-model="queryForm.drugName" placeholder="请输入" clearable />
          </el-form-item>
          <el-form-item label="药品类型">
            <el-select v-model="queryForm.drugType" placeholder="请选择" clearable>
              <el-option label="西药" value="western" />
              <el-option label="中药" value="chinese" />
              <el-option label="生物制品" value="biological" />
              <el-option label="医疗器械" value="device" />
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

      <div class="his-drug-list__toolbar">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增药品</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="drugCode" label="药品编码" width="150" />
        <el-table-column prop="drugName" label="药品名称" width="150" />
        <el-table-column prop="genericName" label="通用名" width="150" />
        <el-table-column prop="specification" label="规格" width="120" />
        <el-table-column prop="drugType" label="类型" width="100">
          <template #default="{ row }">
            <span :class="['his-drug-list__type', `his-drug-list__type--${row.drugType}`]">{{ drugTypeLabel(row.drugType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="insuranceType" label="医保类型" width="100">
          <template #default="{ row }">
            <span :class="['his-drug-list__insurance', `his-drug-list__insurance--${row.insuranceType}`]">{{ insuranceTypeLabel(row.insuranceType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="unitPrice" label="单价" width="100" align="right">
          <template #default="{ row }">
            <span class="his-drug-list__price">¥{{ row.unitPrice?.toFixed(2) }}</span>
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

      <div class="his-drug-list__pagination">
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
      :title="isEdit ? '编辑药品' : '新增药品'"
      width="700px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="药品编码" prop="drugCode">
          <el-input v-model="form.drugCode" :disabled="isEdit" placeholder="请输入药品编码" />
        </el-form-item>
        <el-form-item label="药品名称" prop="drugName">
          <el-input v-model="form.drugName" placeholder="请输入药品名称" />
        </el-form-item>
        <el-form-item label="通用名" prop="genericName">
          <el-input v-model="form.genericName" placeholder="请输入通用名" />
        </el-form-item>
        <el-form-item label="规格" prop="specification">
          <el-input v-model="form.specification" placeholder="如: 0.25g*24片" />
        </el-form-item>
        <el-form-item label="生产厂家" prop="manufacturer">
          <el-input v-model="form.manufacturer" placeholder="请输入生产厂家" />
        </el-form-item>
        <el-form-item label="药品类型" prop="drugType">
          <el-select v-model="form.drugType" placeholder="请选择">
            <el-option label="西药" value="western" />
            <el-option label="中药" value="chinese" />
            <el-option label="生物制品" value="biological" />
            <el-option label="医疗器械" value="device" />
          </el-select>
        </el-form-item>
        <el-form-item label="医保类型" prop="insuranceType">
          <el-select v-model="form.insuranceType" placeholder="请选择">
            <el-option label="甲类" value="jia" />
            <el-option label="乙类" value="yi" />
            <el-option label="丙类" value="bing" />
            <el-option label="自费" value="self" />
          </el-select>
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number v-model="form.unitPrice" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="处方药">
          <el-switch v-model="form.prescriptionFlag" active-text="是" inactive-text="否" />
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
  getDrugPage,
  createDrug,
  updateDrug,
  deleteDrug,
} from '@/api/drug'
import type {
  DrugVO,
  CreateDrugDTO,
  UpdateDrugDTO,
  DrugQueryDTO,
} from '@/api/drug'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const tableData = ref<DrugVO[]>([])
const formRef = ref<FormInstance>()

const queryForm = reactive<DrugQueryDTO>({
  drugName: '',
  drugType: '',
  status: '',
})

const form = reactive<CreateDrugDTO>({
  drugCode: '',
  drugName: '',
  genericName: '',
  specification: '',
  manufacturer: '',
  drugType: 'western',
  prescriptionFlag: false,
  insuranceType: 'jia',
  unitPrice: 0,
  description: '',
})

const rules = {
  drugCode: [{ required: true, message: '请输入药品编码', trigger: 'blur' }],
  drugName: [{ required: true, message: '请输入药品名称', trigger: 'blur' }],
  genericName: [{ required: true, message: '请输入通用名', trigger: 'blur' }],
  specification: [{ required: true, message: '请输入规格', trigger: 'blur' }],
  drugType: [{ required: true, message: '请选择药品类型', trigger: 'change' }],
  insuranceType: [{ required: true, message: '请选择医保类型', trigger: 'change' }],
  unitPrice: [{ required: true, message: '请输入单价', trigger: 'blur' }],
}

function drugTypeLabel(type: string) {
  const map: Record<string, string> = { western: '西药', chinese: '中药', biological: '生物制品', device: '器械' }
  return map[type] || type
}

function insuranceTypeLabel(type: string) {
  const map: Record<string, string> = { jia: '甲类', yi: '乙类', bing: '丙类', self: '自费' }
  return map[type] || type
}

async function loadData() {
  loading.value = true
  try {
    const res = await getDrugPage(page.value, pageSize.value, queryForm)
    tableData.value = res.list
    total.value = res.total
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryForm.drugName = ''
  queryForm.drugType = ''
  queryForm.status = ''
  page.value = 1
  loadData()
}

function handleAdd() {
  isEdit.value = false
  dialogVisible.value = true
}

function handleEdit(row: DrugVO) {
  isEdit.value = true
  form.drugCode = row.drugCode
  form.drugName = row.drugName
  form.genericName = row.genericName
  form.specification = row.specification
  form.manufacturer = row.manufacturer
  form.drugType = row.drugType
  form.prescriptionFlag = row.prescriptionFlag
  form.insuranceType = row.insuranceType
  form.unitPrice = row.unitPrice
  form.description = row.description || ''
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value) {
      const id = tableData.value.find(t => t.drugCode === form.drugCode)?.id
      if (id) await updateDrug(id, form as UpdateDrugDTO)
    } else {
      await createDrug(form)
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

async function handleDelete(row: DrugVO) {
  await ElMessageBox.confirm(`确定删除药品「${row.drugName}」？`, '确认删除', { type: 'warning' })
  await deleteDrug(row.id)
  ElMessage.success('删除成功')
  loadData()
}

function resetForm() {
  form.drugCode = ''
  form.drugName = ''
  form.genericName = ''
  form.specification = ''
  form.manufacturer = ''
  form.drugType = 'western'
  form.prescriptionFlag = false
  form.insuranceType = 'jia'
  form.unitPrice = 0
  form.description = ''
  formRef.value?.resetFields()
}

onMounted(() => { loadData() })
</script>

<style lang="scss" scoped>

.his-drug-list {
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

  &__type {
    display: inline-flex;
    align-items: center;
    padding: 3px 10px;
    border-radius: $radius-pill;
    font-size: $font-size-caption;
    font-weight: $font-weight-semibold;
    letter-spacing: $letter-spacing-caption;

    &--western {
      color: $color-semantic-info;
      background-color: $color-semantic-info-bg;
    }

    &--chinese {
      color: $color-semantic-pass;
      background-color: $color-semantic-pass-bg;
    }

    &--biological {
      color: $color-semantic-warn;
      background-color: $color-semantic-warn-bg;
    }

    &--device {
      color: $color-ink-secondary;
      background-color: $color-surface-1;
    }
  }

  &__insurance {
    display: inline-flex;
    align-items: center;
    padding: 3px 10px;
    border-radius: $radius-pill;
    font-size: $font-size-caption;
    font-weight: $font-weight-semibold;
    letter-spacing: $letter-spacing-caption;

    &--jia {
      color: $color-semantic-block;
      background-color: $color-semantic-block-bg;
    }

    &--yi {
      color: $color-semantic-warn;
      background-color: $color-semantic-warn-bg;
    }

    &--bing {
      color: $color-semantic-info;
      background-color: $color-semantic-info-bg;
    }

    &--self {
      color: $color-ink-secondary;
      background-color: $color-surface-1;
    }
  }

  &__price {
    font-family: $font-family-mono;
    font-weight: $font-weight-semibold;
    font-size: $font-size-fine-print;
    color: $color-ink;
    font-feature-settings: "tnum";
  }
}
</style>
