<template>
  <div class="execution-log-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>测试执行历史</span>
          <el-button type="primary" :icon="Refresh" @click="loadData" :loading="loading">
            刷新
          </el-button>
        </div>
      </template>

      <!-- Search Form -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="执行状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="通过" value="PASS" />
            <el-option label="失败" value="FAILED" />
            <el-option label="异常" value="ERROR" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- Table -->
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="caseName" label="用例名称" min-width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="elapsedMs" label="耗时(ms)" width="100" align="center" />
        <el-table-column prop="executedBy" label="执行人" width="100" />
        <el-table-column prop="executeTime" label="执行时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- Detail Dialog -->
    <el-dialog v-model="showDetail" title="执行详情" width="700px">
      <div v-if="currentLog" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用例名称">{{ currentLog.caseName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentLog.status)" size="small">
              {{ getStatusText(currentLog.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="耗时">{{ currentLog.elapsedMs }}ms</el-descriptions-item>
          <el-descriptions-item label="执行时间">{{ currentLog.executeTime }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">输入数据</el-divider>
        <pre class="json-content">{{ formatJson(currentLog.inputJson) }}</pre>

        <el-divider content-position="left">期望输出</el-divider>
        <pre class="json-content">{{ formatJson(currentLog.expectedJson) }}</pre>

        <el-divider content-position="left">实际输出</el-divider>
        <pre class="json-content">{{ formatJson(currentLog.actualJson) }}</pre>

        <template v-if="currentLog.diffJson && currentLog.diffJson !== 'null'">
          <el-divider content-position="left">差异对比</el-divider>
          <pre class="json-content diff">{{ formatJson(currentLog.diffJson) }}</pre>
        </template>

        <template v-if="currentLog.errorMessage">
          <el-divider content-position="left">错误信息</el-divider>
          <pre class="json-content error">{{ currentLog.errorMessage }}</pre>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getExecutionLogs, getExecutionLogById } from '@/api/execution-log'
import type { TestExecutionLogVO } from '@/api/execution-log'

const loading = ref(false)
const tableData = ref<TestExecutionLogVO[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const dateRange = ref<string[]>([])
const showDetail = ref(false)
const currentLog = ref<TestExecutionLogVO | null>(null)

const queryParams = reactive({
  status: '',
  startDate: '',
  endDate: '',
})

function getStatusType(status: string) {
  switch (status) {
    case 'PASS': return 'success'
    case 'FAILED': return 'danger'
    case 'ERROR': return 'warning'
    default: return 'info'
  }
}

function getStatusText(status: string) {
  switch (status) {
    case 'PASS': return '通过'
    case 'FAILED': return '失败'
    case 'ERROR': return '异常'
    default: return status
  }
}

function formatJson(jsonStr: string | null) {
  if (!jsonStr) return ''
  try {
    return JSON.stringify(JSON.parse(jsonStr), null, 2)
  } catch {
    return jsonStr
  }
}

function handleDateChange(val: string[] | null) {
  if (val && val.length === 2) {
    queryParams.startDate = val[0]
    queryParams.endDate = val[1]
  } else {
    queryParams.startDate = ''
    queryParams.endDate = ''
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getExecutionLogs(currentPage.value, pageSize.value, {
      status: queryParams.status || undefined,
      startDate: queryParams.startDate || undefined,
      endDate: queryParams.endDate || undefined,
    })
    if (res.code === '0') {
      tableData.value = res.data.list
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e: any) {
    ElMessage.error('加载失败: ' + (e.message || ''))
  } finally {
    loading.value = false
  }
}

async function handleViewDetail(row: TestExecutionLogVO) {
  try {
    const res = await getExecutionLogById(row.id)
    if (res.code === '0') {
      currentLog.value = res.data
      showDetail.value = true
    } else {
      ElMessage.error(res.message || '加载详情失败')
    }
  } catch (e: any) {
    ElMessage.error('加载详情失败: ' + (e.message || ''))
  }
}

function handleSearch() {
  currentPage.value = 1
  loadData()
}

function handleReset() {
  queryParams.status = ''
  queryParams.startDate = ''
  queryParams.endDate = ''
  dateRange.value = []
  handleSearch()
}

function handlePageChange(page: number) {
  currentPage.value = page
  loadData()
}

function handleSizeChange(size: number) {
  pageSize.value = size
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.execution-log-list {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 16px;
  }

  .pagination {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }

  .detail-content {
    .json-content {
      background: #f5f7fa;
      padding: 12px;
      border-radius: 4px;
      font-size: 12px;
      max-height: 200px;
      overflow: auto;
      font-family: 'Courier New', monospace;

      &.diff {
        border: 1px solid #f56c6c;
      }

      &.error {
        background: #fef0f0;
        color: #f56c6c;
        border: 1px solid #f56c6c;
      }
    }
  }
}
</style>