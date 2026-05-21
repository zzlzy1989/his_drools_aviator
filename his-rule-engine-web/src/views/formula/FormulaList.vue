<template>
  <div class="his-formula-list">
    <div class="his-formula-list__card">
      <div class="his-formula-list__search">
        <el-form :inline="true" :model="queryForm" class="his-formula-list__search-form">
          <el-form-item label="公式名称">
            <el-input v-model="queryForm.formulaName" placeholder="请输入" clearable />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="queryForm.category" placeholder="请选择" clearable>
              <el-option label="计算" value="calc" />
              <el-option label="校验" value="validate" />
              <el-option label="转换" value="transform" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryForm.status" placeholder="请选择" clearable>
              <el-option label="草稿" value="draft" />
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

      <div class="his-formula-list__toolbar">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增公式</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="formulaKey" label="公式Key" width="200" />
        <el-table-column prop="formulaName" label="公式名称" />
        <el-table-column prop="expression" label="表达式" show-overflow-tooltip>
          <template #default="{ row }">
            <code class="his-formula-list__expression">{{ row.expression }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="returnType" label="返回类型" width="100">
          <template #default="{ row }">
            <span class="his-formula-list__return-type">{{ row.returnType }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :type="row.status === 'active' ? 'published' : row.status === 'inactive' ? 'disabled' : 'draft'" show-dot size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleTest(row)">测试</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="his-formula-list__pagination">
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
      :title="isEdit ? '编辑公式' : '新增公式'"
      width="700px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="公式Key" prop="formulaKey">
          <el-input v-model="form.formulaKey" :disabled="isEdit" placeholder="如: formula.calc.total_fee" />
        </el-form-item>
        <el-form-item label="公式名称" prop="formulaName">
          <el-input v-model="form.formulaName" placeholder="请输入公式名称" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择">
            <el-option label="计算" value="calc" />
            <el-option label="校验" value="validate" />
            <el-option label="转换" value="transform" />
          </el-select>
        </el-form-item>
        <el-form-item label="返回类型" prop="returnType">
          <el-select v-model="form.returnType" placeholder="请选择">
            <el-option label="数值" value="number" />
            <el-option label="字符串" value="string" />
            <el-option label="布尔" value="boolean" />
          </el-select>
        </el-form-item>
        <el-form-item label="表达式" prop="expression">
          <el-input v-model="form.expression" type="textarea" :rows="4" placeholder="如: a + b * 0.8" />
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

    <el-dialog v-model="testVisible" title="公式测试" width="600px">
      <el-form label-width="120px">
        <el-form-item label="测试参数(JSON)">
          <el-input v-model="testParams" type="textarea" :rows="4" placeholder='{"a": 100, "b": 200}' />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="testing" @click="runTest">执行测试</el-button>
        </el-form-item>
        <el-form-item v-if="testResult" label="测试结果">
          <el-alert :type="testResult.success ? 'success' : 'error'" :title="String(testResult.result)" show-icon />
          <p v-if="testResult.executionTime">执行耗时: {{ testResult.executionTime }}ms</p>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { StatusTag } from '@/components/HIS'
import {
  getFormulaPage,
  createFormula,
  updateFormula,
  deleteFormula,
  testFormula,
} from '@/api/formula'
import type {
  FormulaVO,
  CreateFormulaDTO,
  UpdateFormulaDTO,
  FormulaQueryDTO,
} from '@/api/formula'

const loading = ref(false)
const saving = ref(false)
const testing = ref(false)
const dialogVisible = ref(false)
const testVisible = ref(false)
const isEdit = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const tableData = ref<FormulaVO[]>([])
const formRef = ref<FormInstance>()
const testParams = ref('')
const testResult = ref<any>(null)
const testFormulaId = ref(0)

const queryForm = reactive<FormulaQueryDTO>({
  formulaName: '',
  category: '',
  status: '',
})

const form = reactive<CreateFormulaDTO>({
  formulaKey: '',
  formulaName: '',
  expression: '',
  category: 'calc',
  returnType: 'number',
  description: '',
  status: 'draft',
})

const rules = {
  formulaKey: [{ required: true, message: '请输入公式Key', trigger: 'blur' }],
  formulaName: [{ required: true, message: '请输入公式名称', trigger: 'blur' }],
  expression: [{ required: true, message: '请输入表达式', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  returnType: [{ required: true, message: '请选择返回类型', trigger: 'change' }],
}

async function loadData() {
  loading.value = true
  try {
    const res = await getFormulaPage(page.value, pageSize.value, queryForm)
    tableData.value = res.list
    total.value = res.total
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryForm.formulaName = ''
  queryForm.category = ''
  queryForm.status = ''
  page.value = 1
  loadData()
}

function handleAdd() {
  isEdit.value = false
  dialogVisible.value = true
}

function handleEdit(row: FormulaVO) {
  isEdit.value = true
  form.formulaKey = row.formulaKey
  form.formulaName = row.formulaName
  form.expression = row.expression
  form.category = row.category
  form.returnType = row.returnType
  form.description = row.description || ''
  form.status = row.status
  dialogVisible.value = true
}

function handleTest(row: FormulaVO) {
  testFormulaId.value = row.id
  testParams.value = '{}'
  testResult.value = null
  testVisible.value = true
}

async function runTest() {
  let params = {}
  try {
    params = JSON.parse(testParams.value)
  } catch {
    ElMessage.error('参数JSON格式错误')
    return
  }
  testing.value = true
  try {
    const res = await testFormula(testFormulaId.value, params)
    testResult.value = res.data
    ElMessage.success('测试完成')
  } catch {
    ElMessage.error('测试失败')
  } finally {
    testing.value = false
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value) {
      const id = tableData.value.find(t => t.formulaKey === form.formulaKey)?.id
      if (id) await updateFormula(id, form as UpdateFormulaDTO)
    } else {
      await createFormula(form)
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

async function handleDelete(row: FormulaVO) {
  await ElMessageBox.confirm(`确定删除公式「${row.formulaName}」？`, '确认删除', { type: 'warning' })
  await deleteFormula(row.id)
  ElMessage.success('删除成功')
  loadData()
}

function resetForm() {
  form.formulaKey = ''
  form.formulaName = ''
  form.expression = ''
  form.category = 'calc'
  form.returnType = 'number'
  form.description = ''
  form.status = 'draft'
  formRef.value?.resetFields()
}

onMounted(() => { loadData() })
</script>

<style lang="scss" scoped>

.his-formula-list {
  &__card {
    background-color: $color-canvas;
    border: 1px solid $color-hairline;
    padding: $spacing-lg;
  }

  &__search {
    margin-bottom: $spacing-md;
  }

  &__search-form {
    display: flex;
    flex-wrap: wrap;
    gap: 0;
  }

  &__toolbar {
    margin-bottom: $spacing-md;
  }

  &__pagination {
    margin-top: $spacing-md;
    display: flex;
    justify-content: flex-end;
  }

  &__expression {
    font-family: $font-family-mono;
    font-size: 13px;
    color: $color-primary-deep;
    background-color: $color-surface-1;
    padding: 1px 4px;
    border-radius: $radius-xs;
  }

  &__return-type {
    display: inline-flex;
    align-items: center;
    padding: 2px 8px;
    border-radius: $radius-xs;
    font-size: $font-size-body;
    font-weight: $font-weight-semibold;
    color: $color-ink-secondary;
    background-color: $color-surface-1;
  }
}
</style>
