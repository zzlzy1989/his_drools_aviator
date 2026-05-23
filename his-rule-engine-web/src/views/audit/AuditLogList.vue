<template>
  <div class="page-container">
    <div class="page-card">
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="操作类型">
          <el-select v-model="queryForm.action" placeholder="请选择" clearable>
            <el-option label="创建" value="CREATE" />
            <el-option label="更新" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="结算" value="SETTLEMENT" />
            <el-option label="执行" value="EXECUTE" />
          </el-select>
        </el-form-item>
        <el-form-item label="对象类型">
          <el-select v-model="queryForm.targetType" placeholder="请选择" clearable>
            <el-option label="规则" value="RULE" />
            <el-option label="公式" value="FORMULA" />
            <el-option label="规则流" value="FLOW" />
            <el-option label="结算" value="SETTLEMENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="queryForm.operator" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="queryForm.startDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="queryForm.endDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="action" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.action === 'CREATE'" type="success" size="small">创建</el-tag>
            <el-tag v-else-if="row.action === 'UPDATE'" type="warning" size="small">更新</el-tag>
            <el-tag v-else-if="row.action === 'DELETE'" type="danger" size="small">删除</el-tag>
            <el-tag v-else-if="row.action === 'SETTLEMENT'" type="info" size="small">结算</el-tag>
            <el-tag v-else type="info" size="small">{{ row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="对象类型" width="100" />
        <el-table-column prop="targetKey" label="对象标识" width="200" show-overflow-tooltip />
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="ipAddress" label="IP地址" width="130" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <el-dialog v-model="viewVisible" title="操作详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ viewData.id }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ viewData.action }}</el-descriptions-item>
        <el-descriptions-item label="对象类型">{{ viewData.targetType }}</el-descriptions-item>
        <el-descriptions-item label="对象标识">{{ viewData.targetKey }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ viewData.operator }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ viewData.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ viewData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="详情" :span="2">
          <pre style="white-space: pre-wrap; word-break: break-all; max-height: 300px; overflow-y: auto;">{{ formatDetail(viewData.detail) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getAuditLogPage } from '@/api/auditLog'
import type { AuditLogVO, AuditLogQueryDTO } from '@/api/auditLog'

const loading = ref(false)
const viewVisible = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const tableData = ref<AuditLogVO[]>([])

const queryForm = reactive<AuditLogQueryDTO & { startDate?: string; endDate?: string }>({
  action: '',
  targetType: '',
  targetKey: '',
  operator: '',
  startDate: '',
  endDate: '',
})

const viewData = ref<AuditLogVO>({
  id: 0, tenantId: '', action: '', targetType: '', targetId: '', targetKey: '',
  operator: '', detail: '', ipAddress: '', userAgent: '', createTime: '',
})

async function loadData() {
  loading.value = true
  try {
    const res = await getAuditLogPage(page.value, pageSize.value, queryForm)
    tableData.value = res.records
    total.value = res.total
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryForm.action = ''
  queryForm.targetType = ''
  queryForm.targetKey = ''
  queryForm.operator = ''
  queryForm.startDate = ''
  queryForm.endDate = ''
  page.value = 1
  loadData()
}

function handleView(row: AuditLogVO) {
  viewData.value = row
  viewVisible.value = true
}

function formatDetail(detail: string): string {
  if (!detail) return ''
  try {
    const obj = JSON.parse(detail)
    return JSON.stringify(obj, null, 2)
  } catch {
    return detail
  }
}

onMounted(() => { loadData() })
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

.pagination {
  @include pagination-wrapper;
}
</style>