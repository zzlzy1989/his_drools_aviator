<template>
  <div class="his-settlement">
    <div class="his-settlement__card">
      <div class="his-settlement__search">
        <el-form :inline="true" :model="queryForm" class="his-settlement__search-form">
          <el-form-item label="结算单号">
            <el-input v-model="queryForm.settlementNo" placeholder="请输入" clearable />
          </el-form-item>
          <el-form-item label="患者ID">
            <el-input v-model="queryForm.patientId" placeholder="请输入" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryForm.status" placeholder="请选择" clearable>
              <el-option label="待结算" value="pending" />
              <el-option label="结算中" value="processing" />
              <el-option label="已完成" value="completed" />
              <el-option label="已拒绝" value="rejected" />
            </el-select>
          </el-form-item>
          <el-form-item label="开始日期">
            <el-date-picker v-model="queryForm.startDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
          </el-form-item>
          <el-form-item label="结束日期">
            <el-date-picker v-model="queryForm.endDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
          </el-form-item>
          <el-form-item label="金额范围">
            <el-input v-model="queryForm.minAmount" placeholder="最小" style="width: 90px" />
            <span style="padding: 0 4px; color: #7a7a7a">-</span>
            <el-input v-model="queryForm.maxAmount" placeholder="最大" style="width: 90px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="his-settlement__toolbar">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新建结算</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="settlementNo" label="结算单号" width="200" />
        <el-table-column prop="patientId" label="患者ID" width="150" />
        <el-table-column prop="patientName" label="患者姓名" width="120" />
        <el-table-column prop="totalFee" label="总费用" width="120" align="right">
          <template #default="{ row }">
            <span class="his-settlement__amount">¥{{ row.totalFee?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reimburseAmount" label="报销金额" width="120" align="right">
          <template #default="{ row }">
            <span class="his-settlement__amount his-settlement__amount--reimburse">¥{{ row.reimburseAmount?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <StatusTag
              :type="row.status === 'completed' ? 'published' : row.status === 'processing' ? 'running' : row.status === 'rejected' ? 'error' : 'draft'"
              show-dot
              size="small"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">详情</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'pending'" link type="success" @click="handleExecute(row)">执行</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="his-settlement__pagination">
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
      :title="isEdit ? '编辑结算' : '新建结算'"
      width="600px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="就诊ID" prop="visitId">
          <el-input v-model="form.visitId" :disabled="isEdit" placeholder="如: VISIT20260101001" />
        </el-form-item>
        <el-form-item label="患者ID" prop="patientId">
          <el-input v-model="form.patientId" placeholder="如: P10001" />
        </el-form-item>
        <el-form-item label="患者类型" prop="patientType">
          <el-select v-model="form.patientType" placeholder="请选择" style="width:100%">
            <el-option label="在职职工" value="employee" />
            <el-option label="居民医保" value="resident" />
            <el-option label="医疗救助" value="aid" />
          </el-select>
        </el-form-item>
        <el-form-item label="医保类型">
          <el-select v-model="form.insuranceType" placeholder="请选择" clearable style="width:100%">
            <el-option label="基本医保" value="basic" />
            <el-option label="大病保险" value="critical" />
            <el-option label="补充医保" value="supplement" />
          </el-select>
        </el-form-item>
        <el-form-item label="医院等级">
          <el-select v-model="form.hospitalLevel" placeholder="请选择" clearable style="width:100%">
            <el-option label="一级" value="LEVEL_1" />
            <el-option label="二级" value="LEVEL_2" />
            <el-option label="三级" value="LEVEL_3" />
          </el-select>
        </el-form-item>
        <el-form-item label="总费用" prop="totalFee">
          <el-input-number v-model="form.totalFee" :min="0" :precision="2" :step="100" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewVisible" title="结算详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="结算单号">{{ viewData.settlementNo }}</el-descriptions-item>
        <el-descriptions-item label="患者ID">{{ viewData.patientId }}</el-descriptions-item>
        <el-descriptions-item label="患者姓名">{{ viewData.patientName }}</el-descriptions-item>
        <el-descriptions-item label="总费用">¥{{ viewData.totalFee?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="报销金额">¥{{ viewData.reimburseAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="自付金额">¥{{ viewData.selfPayAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ viewData.status }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ viewData.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="viewData.remarks" label="备注" :span="2">{{ viewData.remarks }}</el-descriptions-item>
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
  getSettlementPage,
  createSettlement,
  updateSettlement,
  deleteSettlement,
  executeSettlement,
} from '@/api/settlement'
import type {
  SettlementVO,
  CreateSettlementDTO,
  UpdateSettlementDTO,
  SettlementQueryDTO,
} from '@/api/settlement'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const viewVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number>(0)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const tableData = ref<SettlementVO[]>([])
const formRef = ref<FormInstance>()

const queryForm = reactive<SettlementQueryDTO>({
  settlementNo: '',
  patientId: '',
  status: '',
  startDate: '',
  endDate: '',
  minAmount: undefined,
  maxAmount: undefined,
})

const form = reactive<CreateSettlementDTO>({
  visitId: '',
  patientId: '',
  patientType: '',
  insuranceType: '',
  hospitalLevel: '',
  totalFee: 0,
})

const viewData = reactive<SettlementVO>({
  id: 0, settlementNo: '', patientId: '', patientName: '', totalFee: 0,
  reimburseAmount: 0, selfPayAmount: 0, status: 'pending', tenantId: '', createTime: '', updateTime: '',
})

const rules = {
  visitId: [{ required: true, message: '请输入就诊ID', trigger: 'blur' }],
  patientId: [{ required: true, message: '请输入患者ID', trigger: 'blur' }],
  patientType: [{ required: true, message: '请选择患者类型', trigger: 'change' }],
  totalFee: [{ required: true, message: '请输入总费用', trigger: 'blur' }],
}

async function loadData() {
  loading.value = true
  try {
    const res = await getSettlementPage(page.value, pageSize.value, queryForm)
    tableData.value = res.list
    total.value = res.total
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryForm.settlementNo = ''
  queryForm.patientId = ''
  queryForm.status = ''
  queryForm.startDate = ''
  queryForm.endDate = ''
  queryForm.minAmount = undefined
  queryForm.maxAmount = undefined
  page.value = 1
  loadData()
}

function handleAdd() {
  isEdit.value = false
  dialogVisible.value = true
}

function handleEdit(row: SettlementVO) {
  isEdit.value = true
  editId.value = row.id
  form.visitId = row.visitId || ''
  form.patientId = row.patientId
  form.patientType = row.patientType || ''
  form.insuranceType = row.insuranceType || ''
  form.hospitalLevel = row.hospitalLevel || ''
  form.totalFee = row.totalFee
  dialogVisible.value = true
}

function handleView(row: SettlementVO) {
  Object.assign(viewData, row)
  viewVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value) {
      await updateSettlement(editId.value, form as UpdateSettlementDTO)
      ElMessage.success('更新成功')
    } else {
      await createSettlement(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    saving.value = false
  }
}

async function handleExecute(row: SettlementVO) {
  await ElMessageBox.confirm(`确定执行结算「${row.settlementNo}」？`, '确认执行', { type: 'warning' })
  await executeSettlement(row.id)
  ElMessage.success('结算执行成功')
  loadData()
}

async function handleDelete(row: SettlementVO) {
  await ElMessageBox.confirm(`确定删除结算「${row.settlementNo}」？`, '确认删除', { type: 'warning' })
  await deleteSettlement(row.id)
  ElMessage.success('删除成功')
  loadData()
}

function resetForm() {
  form.visitId = ''
  form.patientId = ''
  form.patientType = ''
  form.insuranceType = ''
  form.hospitalLevel = ''
  form.totalFee = 0
  editId.value = 0
  formRef.value?.resetFields()
}

onMounted(() => { loadData() })
</script>

<style lang="scss" scoped>

.his-settlement {
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

  &__amount {
    font-family: $font-family-mono;
    font-weight: $font-weight-semibold;
    font-size: $font-size-fine-print;
    color: $color-ink;
    font-feature-settings: "tnum";

    &--reimburse {
      color: $color-semantic-pass;
    }
  }
}
</style>
