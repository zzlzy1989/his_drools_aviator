<template>
  <div class="flow-list">
    <div class="flow-list__header">
      <h2>规则流管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleCreate">新建规则流</el-button>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="规则流名称">
          <el-input v-model="queryForm.flowName" placeholder="请输入规则流名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="草稿" value="draft" />
            <el-option label="已发布" value="active" />
            <el-option label="已禁用" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="flowKey" label="规则流Key" min-width="150" />
      <el-table-column prop="flowName" label="规则流名称" min-width="150" />
      <el-table-column prop="category" label="分类" width="120">
        <template #default="{ row }">
          {{ row.category || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="version" label="版本" width="80">
        <template #default="{ row }">
          <el-tag size="small">v{{ row.version }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column prop="updateTime" label="更新时间" width="180" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleVersion(row)">版本</el-button>
          <el-button link type="primary" @click="handlePublish(row)" v-if="row.status === 'draft'">发布</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getFlowPage, deleteFlow, publishFlow } from '@/api/rule-flow'
import type { RuleFlowVO, FlowQueryDTO } from '@/api/rule-flow'

const router = useRouter()

const loading = ref(false)
const tableData = ref<RuleFlowVO[]>([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const queryForm = reactive<FlowQueryDTO>({
  flowName: '',
  category: '',
  status: '',
})

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const result = await getFlowPage(page.value, pageSize.value, queryForm)
    tableData.value = result.list
    total.value = result.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  page.value = 1
  loadData()
}

function handleReset() {
  queryForm.flowName = ''
  queryForm.status = ''
  queryForm.category = ''
  handleQuery()
}

function handleSizeChange() {
  loadData()
}

function handlePageChange() {
  loadData()
}

function handleCreate() {
  router.push('/flow/editor')
}

function handleEdit(row: RuleFlowVO) {
  router.push(`/flow/editor/${row.id}`)
}

async function handleVersion(row: RuleFlowVO) {
  router.push(`/flow/history/${row.id}`)
}

async function handlePublish(row: RuleFlowVO) {
  try {
    await ElMessageBox.confirm('发布后将无法直接修改，是否继续？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await publishFlow(row.id)
    ElMessage.success('发布成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发布失败')
    }
  }
}

async function handleDelete(row: RuleFlowVO) {
  try {
    await ElMessageBox.confirm('确定要删除该规则流吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteFlow(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

function getStatusType(status: string): string {
  const map: Record<string, string> = {
    draft: 'info',
    active: 'success',
    inactive: 'danger',
  }
  return map[status] || 'info'
}

function getStatusLabel(status: string): string {
  const map: Record<string, string> = {
    draft: '草稿',
    active: '已发布',
    inactive: '已禁用',
  }
  return map[status] || status
}
</script>

<style lang="scss" scoped>
.flow-list {
  @include page-container;

  &__header {
    @include flex-between;
    margin-bottom: $spacing-md;

    h2 {
      margin: 0;
      font-size: $font-size-tagline;
      font-weight: $font-weight-semibold;
      letter-spacing: $letter-spacing-headline;
    }
  }

  .filter-card {
    margin-bottom: $spacing-md;
    border-radius: $radius-lg;
  }

  .pagination {
    @include pagination-wrapper;
  }
}
</style>