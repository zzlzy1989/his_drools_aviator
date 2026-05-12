<template>
  <div class="page-container">
    <div class="page-card">
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新建数据集</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="dataSetName" label="数据集名称" />
        <el-table-column prop="category" label="分类" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.category || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="testCases" label="用例数" width="80">
          <template #default="{ row }">
            {{ row.testCases?.length || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleRun(row)">执行</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑数据集' : '新建数据集'" width="800px" @close="resetForm">
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="数据集名称" prop="dataSetName">
          <el-input v-model="form.dataSetName" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择" clearable style="width: 100%">
            <el-option label="结算" value="settlement" />
            <el-option label="DRG" value="drg" />
            <el-option label="用药" value="drug" />
            <el-option label="质控" value="quality" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入" />
        </el-form-item>

        <el-divider content-position="left">测试用例</el-divider>

        <div v-for="(tc, index) in form.testCases" :key="index" class="test-case-item">
          <div class="test-case-header">
            <span>用例 {{ index + 1 }}</span>
            <el-button link type="danger" @click="removeTestCase(index)">删除</el-button>
          </div>
          <el-form-item label="用例ID" prop="caseId">
            <el-input v-model="tc.caseId" placeholder="如: TC001" />
          </el-form-item>
          <el-form-item label="用例名称" prop="caseName">
            <el-input v-model="tc.caseName" placeholder="请输入" />
          </el-form-item>
          <el-form-item label="输入数据(JSON)" prop="fact">
            <el-input v-model="tc.factJson" type="textarea" :rows="3" placeholder='{"totalFee": 10000}' />
          </el-form-item>
          <el-form-item label="期望结果(JSON)" prop="expected">
            <el-input v-model="tc.expectedJson" type="textarea" :rows="3" placeholder='{"reimburseAmount": 7500}' />
          </el-form-item>
        </div>

        <el-button type="dashed" :icon="Plus" @click="addTestCase" style="width: 100%; margin-top: 10px">
          添加测试用例
        </el-button>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resultVisible" title="执行结果" width="700px">
      <div v-if="execResults.length > 0" class="results-container">
        <div v-for="(r, idx) in execResults" :key="idx" class="result-item">
          <el-alert :type="r.status === 'SUCCESS' ? 'success' : 'error'" :title="r.status" closable style="margin-bottom: 10px">
            <template #title>
              <span>用例: {{ r.caseName || r.caseId }}</span>
            </template>
          </el-alert>
          <div v-if="r.input" class="result-detail">
            <p><strong>输入:</strong> {{ JSON.stringify(r.input, null, 2) }}</p>
          </div>
          <div v-if="r.message && r.status !== 'SUCCESS'" class="result-detail">
            <p><strong>错误:</strong> {{ r.message }}</p>
          </div>
        </div>
      </div>
      <div v-else>
        <el-empty description="无执行结果" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  listDatasets,
  createDataset,
  updateDataset,
  deleteDataset,
  batchExecute,
} from '@/api/sandbox'
import type { TestDataSetDTO, TestCaseDTO } from '@/api/sandbox'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const resultVisible = ref(false)
const isEdit = ref(false)
const tableData = ref<TestDataSetDTO[]>([])
const execResults = ref<any[]>([])
const formRef = ref<FormInstance>()

const form = reactive<any>({
  dataSetName: '',
  category: '',
  description: '',
  testCases: [] as any[],
})

function addTestCase() {
  form.testCases.push({
    caseId: '',
    caseName: '',
    factJson: '{}',
    expectedJson: '{}',
  })
}

function removeTestCase(index: number) {
  form.testCases.splice(index, 1)
}

function resetForm() {
  form.dataSetName = ''
  form.category = ''
  form.description = ''
  form.testCases = []
  isEdit.value = false
}

async function loadData() {
  loading.value = true
  try {
    const res = await listDatasets()
    tableData.value = res.data || []
  } catch (e: any) {
    ElMessage.error('加载数据失败: ' + e.message)
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: TestDataSetDTO) {
  isEdit.value = true
  form.id = row.id
  form.dataSetName = row.dataSetName
  form.category = row.category
  form.description = row.description
  form.testCases = (row.testCases || []).map((tc: any) => ({
    caseId: tc.caseId,
    caseName: tc.caseName,
    factJson: tc.fact ? JSON.stringify(tc.fact, null, 2) : '{}',
    expectedJson: tc.expected ? JSON.stringify(tc.expected, null, 2) : '{}',
  }))
  dialogVisible.value = true
}

function handleRun(row: TestDataSetDTO) {
  if (!row.id) return
  batchExecute(row.id).then((res: any) => {
    execResults.value = res.data || []
    resultVisible.value = true
  }).catch((e: any) => {
    ElMessage.error('执行失败: ' + e.message)
  })
}

function handleDelete(row: TestDataSetDTO) {
  ElMessageBox.confirm('确认删除该数据集?', '警告', { type: 'warning' }).then(async () => {
    try {
      await deleteDataset(row.id!)
      ElMessage.success('删除成功')
      loadData()
    } catch (e: any) {
      ElMessage.error('删除失败: ' + e.message)
    }
  }).catch(() => {})
}

async function handleSubmit() {
  if (!form.dataSetName) {
    ElMessage.warning('请填写数据集名称')
    return
  }
  saving.value = true
  try {
    const dto: any = {
      dataSetName: form.dataSetName,
      category: form.category,
      description: form.description,
      testCases: form.testCases.map((tc: any) => ({
        caseId: tc.caseId,
        caseName: tc.caseName,
        fact: tc.factJson ? JSON.parse(tc.factJson) : {},
        expected: tc.expectedJson ? JSON.parse(tc.expectedJson) : {},
      })),
    }
    if (isEdit.value) {
      await updateDataset(form.id, dto)
    } else {
      await createDataset(dto)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error('保存失败: ' + e.message)
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.test-case-item {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 16px;
  background: #fafafa;
}
.test-case-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-weight: 500;
}
.results-container {
  max-height: 500px;
  overflow-y: auto;
}
.result-item {
  margin-bottom: 16px;
}
.result-detail {
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 13px;
  white-space: pre-wrap;
}
</style>