<template>
  <div class="his-market">
    <div class="his-market__card">
      <div class="his-market__search">
        <el-form :inline="true" :model="queryForm" class="his-market__search-form">
          <el-form-item label="分类">
            <el-select v-model="queryForm.category" placeholder="请选择" clearable>
              <el-option label="全部" value="" />
              <el-option label="医保报销" value="REIMBURSE" />
              <el-option label="合理用药" value="DRUG" />
              <el-option label="质量控制" value="QUALITY" />
              <el-option label="DRG分组" value="DRG" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="queryForm.keyword" placeholder="搜索模板名称/标签" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="loadData">搜索</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="his-market__toolbar">
        <el-button type="primary" :icon="Plus" @click="handlePublish">发布模板</el-button>
      </div>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="模板市场" name="market" />
        <el-tab-pane label="我的模板" name="mine" />
        <el-tab-pane label="已订阅" name="subscribed" />
        <el-tab-pane label="我的收藏" name="favorites" />
      </el-tabs>

      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="模板名称" width="200" />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <span :class="['his-market__category', `his-market__category--${row.category?.toLowerCase()}`]">{{ categoryLabel(row.category) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签" width="150">
          <template #default="{ row }">
            <span
              v-for="tag in (row.tags || '').split(',').filter((t: string) => t)"
              :key="tag"
              class="his-market__tag"
            >{{ tag }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="providerName" label="提供者" width="120" />
        <el-table-column prop="installCount" label="安装数" width="80" align="center" />
        <el-table-column prop="ratingSummary" label="评分" width="120">
          <template #default="{ row }">
            <template v-if="row.ratingSummary?.count > 0">
              <el-rate v-model="row.ratingSummary.avgRating" disabled text-size="12" />
              <span class="his-market__rating-text">{{ row.ratingSummary.avgRating.toFixed(1) }} ({{ row.ratingSummary.count }})</span>
            </template>
            <span v-else class="his-market__rating-text his-market__rating-text--empty">暂无评分</span>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80">
          <template #default="{ row }">
            <span class="his-market__version">{{ row.version }}</span>
          </template>
        </el-table-column>
        <el-table-column label="收藏" width="70" align="center">
          <template #default="{ row }">
            <el-button link type="warning" @click.stop="handleFavorite(row)">
              <el-icon><Star /></el-icon>
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handlePreview(row)">预览</el-button>
            <el-button v-if="activeTab === 'market'" link type="success" @click="handleInstall(row)">安装</el-button>
            <el-button v-if="activeTab === 'subscribed'" link type="warning" @click="handleUpgrade(row)">
              {{ row.installInfo?.availableVersion && row.installInfo?.availableVersion !== row.installInfo?.installedVersion ? '升级' : '已最新' }}
            </el-button>
            <el-button v-if="activeTab === 'subscribed'" link type="danger" @click="handleUninstall(row)">卸载</el-button>
            <el-button v-if="activeTab === 'mine'" link type="primary" @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="his-market__pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>

    <el-dialog v-model="previewVisible" title="模板预览" width="800px">
      <div v-if="previewData" class="his-market__preview">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="模板名称">{{ previewData.name }}</el-descriptions-item>
          <el-descriptions-item label="版本">{{ previewData.version }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ categoryLabel(previewData.category) }}</el-descriptions-item>
          <el-descriptions-item label="安装次数">{{ previewData.installCount }}</el-descriptions-item>
          <el-descriptions-item label="提供者">{{ previewData.providerName }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ previewData.createTime }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">模板评分</el-divider>
        <div class="his-market__rating-section">
          <template v-if="previewData.ratingSummary?.count > 0">
            <div class="his-market__rating-header">
              <el-rate v-model="previewData.ratingSummary.avgRating" disabled show-score />
              <span class="his-market__rating-text">{{ previewData.ratingSummary.avgRating.toFixed(1) }} / 5 ({{ previewData.ratingSummary.count }}人评分)</span>
            </div>
            <div class="his-market__rating-distribution">
              <div v-for="star in [5,4,3,2,1]" :key="star" class="his-market__rating-bar">
                <span>{{ star }}星</span>
                <el-progress :percentage="getStarPercentage(previewData.ratingSummary.distribution, star)" :show-text="false" />
                <span>{{ previewData.ratingSummary.distribution[star] || 0 }}</span>
              </div>
            </div>
          </template>
          <span v-else class="his-market__rating-text his-market__rating-text--empty">暂无评分</span>

          <div class="his-market__rating-actions">
            <el-button link type="primary" @click="showRatingDialog">评分 / 评论</el-button>
            <el-button link type="primary" @click="loadRatings">查看评论</el-button>
          </div>
        </div>

        <el-divider />
        <h4>模板内容</h4>
        <el-tabs>
          <el-tab-pane v-if="previewData.rules?.length" label="规则">
            <div v-for="rule in previewData.rules" :key="rule.ruleKey" class="his-market__content-item">
              <strong>{{ rule.ruleName || rule.ruleKey }}</strong>
              <p class="his-market__content-key">{{ rule.ruleKey }}</p>
            </div>
          </el-tab-pane>
          <el-tab-pane v-if="previewData.formulas?.length" label="公式">
            <div v-for="f in previewData.formulas" :key="f.formulaKey" class="his-market__content-item">
              <strong>{{ f.formulaName || f.formulaKey }}</strong>
              <code class="his-market__content-code">{{ f.formulaText }}</code>
            </div>
          </el-tab-pane>
          <el-tab-pane v-if="previewData.flows?.length" label="规则流">
            <div v-for="flow in previewData.flows" :key="flow.flowKey" class="his-market__content-item">
              <strong>{{ flow.flowName || flow.flowKey }}</strong>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <el-dialog v-model="publishVisible" :title="isEdit ? '编辑模板' : '发布模板'" width="700px" @close="resetForm">
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择" style="width: 100%">
            <el-option label="医保报销" value="REIMBURSE" />
            <el-option label="合理用药" value="DRUG" />
            <el-option label="质量控制" value="QUALITY" />
            <el-option label="DRG分组" value="DRG" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        <el-form-item label="版本" prop="version">
          <el-input v-model="form.version" placeholder="如: 1.0.0" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="ratingVisible" title="评分 / 评论" width="500px">
      <el-form :model="ratingForm" label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="ratingForm.rating" show-text />
        </el-form-item>
        <el-form-item label="评论">
          <el-input v-model="ratingForm.comment" type="textarea" :rows="3" placeholder="请输入评论内容（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ratingVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleRateSubmit">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="commentsVisible" title="用户评论" width="600px">
      <div v-if="ratingsList.length > 0" class="his-market__comments-list">
        <div v-for="r in ratingsList" :key="r.id" class="his-market__comment-item">
          <div class="his-market__comment-header">
            <el-rate v-model="r.rating" disabled size="small" />
            <span class="his-market__comment-time">{{ r.createTime }}</span>
          </div>
          <div v-if="r.comment" class="his-market__comment-text">{{ r.comment }}</div>
          <div v-else class="his-market__rating-text his-market__rating-text--empty">该用户未留下评论</div>
        </div>
      </div>
      <el-empty v-else description="暂无评论" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { Search, Refresh, Plus, Star, StarFilled } from '@element-plus/icons-vue'
import { marketApi } from '@/api/market'
import type { RuleTemplateDTO } from '@/api/market'

const loading = ref(false)
const saving = ref(false)
const activeTab = ref('market')
const tableData = ref<RuleTemplateDTO[]>([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const previewVisible = ref(false)
const publishVisible = ref(false)
const ratingVisible = ref(false)
const commentsVisible = ref(false)
const isEdit = ref(false)
const previewData = ref<RuleTemplateDTO | null>(null)
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)
const ratingsList = ref<any[]>([])

const queryForm = reactive({
  category: '',
  keyword: '',
})

const form = reactive<any>({
  name: '',
  category: 'REIMBURSE',
  tags: '',
  version: '1.0.0',
  description: '',
})

const ratingForm = reactive({
  rating: 5,
  comment: '',
})

function categoryLabel(cat: string) {
  const map: Record<string, string> = {
    REIMBURSE: '医保报销', DRUG: '合理用药', QUALITY: '质量控制', DRG: 'DRG分组'
  }
  return map[cat] || cat
}

function getStarPercentage(distribution: Record<string, number> | undefined, star: number): number {
  if (!distribution) return 0
  const count = distribution[star] || 0
  const total = Object.values(distribution).reduce((a, b) => a + b, 0)
  if (total === 0) return 0
  return Math.round((count / total) * 100)
}

function showRatingDialog() {
  if (!previewData.value?.id) return
  ratingForm.rating = 5
  ratingForm.comment = ''
  ratingVisible.value = true
}

async function loadRatings() {
  if (!previewData.value?.id) return
  try {
    const res = await marketApi.getRatings(previewData.value.id)
    ratingsList.value = res.data || []
    commentsVisible.value = true
  } catch (e: any) {
    ElMessage.error('加载评论失败: ' + e.message)
  }
}

async function handleRateSubmit() {
  if (!previewData.value?.id) return
  if (ratingForm.rating < 1) {
    ElMessage.warning('请选择评分')
    return
  }
  saving.value = true
  try {
    await marketApi.rateTemplate(previewData.value.id, { rating: ratingForm.rating, comment: ratingForm.comment })
    ElMessage.success('评分成功')
    ratingVisible.value = false
    const res = await marketApi.getTemplate(previewData.value.id)
    if (res.code === '0') {
      previewData.value = res.data
      const idx = tableData.value.findIndex(t => t.id === previewData.value?.id)
      if (idx !== -1) {
        tableData.value[idx] = { ...tableData.value[idx], ...res.data }
      }
    }
  } catch (e: any) {
    ElMessage.error('评分失败: ' + e.message)
  } finally {
    saving.value = false
  }
}

async function loadData() {
  loading.value = true
  try {
    if (activeTab.value === 'market') {
      const res = await marketApi.getTemplates({ page: page.value, pageSize: pageSize.value, category: queryForm.category, keyword: queryForm.keyword })
      tableData.value = res.list || []
      total.value = res.total || 0
    } else if (activeTab.value === 'mine') {
      const res = await marketApi.getMyTemplates()
      tableData.value = res.data || []
      total.value = tableData.value.length
    } else if (activeTab.value === 'subscribed') {
      const res = await marketApi.getSubscribedTemplates()
      tableData.value = res.data || []
      total.value = tableData.value.length
    } else if (activeTab.value === 'favorites') {
      const res = await marketApi.getFavorites()
      tableData.value = res.data || []
      total.value = tableData.value.length
    }
  } catch (e: any) {
    ElMessage.error('加载数据失败: ' + e.message)
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryForm.category = ''
  queryForm.keyword = ''
  page.value = 1
  loadData()
}

function handleTabChange() {
  page.value = 1
  loadData()
}

function handlePreview(row: RuleTemplateDTO) {
  previewData.value = row
  previewVisible.value = true
}

function handlePublish() {
  isEdit.value = false
  editingId.value = null
  resetForm()
  publishVisible.value = true
}

function handleEdit(row: RuleTemplateDTO) {
  isEdit.value = true
  editingId.value = row.id || null
  form.name = row.name
  form.category = row.category
  form.tags = row.tags
  form.version = row.version
  form.description = row.description
  publishVisible.value = true
}

async function handleInstall(row: RuleTemplateDTO) {
  try {
    await ElMessageBox.confirm('确认安装该模板?', '安装模板', { type: 'success' })
    await marketApi.installTemplate(row.id!)
    ElMessage.success('安装成功')
    loadData()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('安装失败: ' + e.message)
  }
}

async function handleUninstall(row: RuleTemplateDTO) {
  try {
    await ElMessageBox.confirm('确认卸载该模板?', '卸载模板', { type: 'warning' })
    await marketApi.uninstallTemplate(row.id!)
    ElMessage.success('卸载成功')
    loadData()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('卸载失败: ' + e.message)
  }
}

async function handleUpgrade(row: RuleTemplateDTO) {
  if (!row.id) return
  try {
    await ElMessageBox.confirm('确认升级到最新版本?', '升级模板', { type: 'info' })
    await marketApi.upgrade(row.id)
    ElMessage.success('升级成功')
    loadData()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('升级失败: ' + e.message)
  }
}

async function handleFavorite(row: RuleTemplateDTO) {
  if (!row.id) return
  try {
    const res = await marketApi.getFavoriteStatus(row.id)
    if (res.data?.favorited) {
      await marketApi.unfavorite(row.id)
      ElMessage.success('已取消收藏')
    } else {
      await marketApi.favorite(row.id)
      ElMessage.success('已收藏')
    }
  } catch (e: any) {
    ElMessage.error('操作失败: ' + e.message)
  }
}

function resetForm() {
  form.name = ''
  form.category = 'REIMBURSE'
  form.tags = ''
  form.version = '1.0.0'
  form.description = ''
}

async function handleSubmit() {
  if (!form.name) {
    ElMessage.warning('请填写模板名称')
    return
  }
  saving.value = true
  try {
    if (isEdit.value && editingId.value) {
      await marketApi.updateTemplate(editingId.value, form)
    } else {
      await marketApi.publishTemplate(form)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '发布成功')
    publishVisible.value = false
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

<style lang="scss" scoped>

.his-market {
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

    &--reimburse {
      color: $color-semantic-info;
      background-color: $color-semantic-info-bg;
    }

    &--drug {
      color: $color-semantic-pass;
      background-color: $color-semantic-pass-bg;
    }

    &--quality {
      color: $color-semantic-warn;
      background-color: $color-semantic-warn-bg;
    }

    &--drg {
      color: $color-semantic-block;
      background-color: $color-semantic-block-bg;
    }
  }

  &__tag {
    display: inline-flex;
    align-items: center;
    padding: 2px 8px;
    margin-right: 4px;
    border-radius: $radius-pill;
    font-size: $font-size-fine-print;
    font-weight: $font-weight-medium;
    color: $color-ink-secondary;
    background-color: $color-surface-1;
  }

  &__version {
    display: inline-flex;
    align-items: center;
    padding: 2px 8px;
    border-radius: $radius-pill;
    font-size: $font-size-fine-print;
    font-weight: $font-weight-semibold;
    font-family: $font-family-mono;
    color: $color-ink-secondary;
    background-color: $color-surface-1;
  }

  &__rating-text {
    margin-left: $spacing-xs;
    font-size: $font-size-fine-print;
    color: $color-ink-muted;

    &--empty {
      color: $color-ink-muted;
    }
  }

  &__rating-section {
    padding: $spacing-sm 0;
  }

  &__rating-header {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    margin-bottom: $spacing-sm;
  }

  &__rating-actions {
    margin-top: $spacing-sm;
    display: flex;
    gap: $spacing-sm;
  }

  &__rating-distribution {
    display: flex;
    flex-direction: column-reverse;
    gap: 6px;
    max-width: 300px;
  }

  &__rating-bar {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    font-size: $font-size-fine-print;
    color: $color-ink-secondary;

    span:first-child {
      width: 30px;
    }

    .el-progress {
      flex: 1;
    }

    span:last-child {
      width: 20px;
      text-align: right;
    }
  }

  &__preview {
  }

  &__content-item {
    padding: $spacing-xs;
    border-bottom: 1px solid $color-hairline;
  }

  &__content-key {
    font-size: $font-size-fine-print;
    color: $color-ink-muted;
    margin: 4px 0 0 0;
  }

  &__content-code {
    display: block;
    margin-top: 4px;
    font-size: $font-size-fine-print;
    color: $color-ink-muted;
  }

  &__comments-list {
    max-height: 400px;
    overflow-y: auto;
  }

  &__comment-item {
    padding: $spacing-sm 0;
    border-bottom: 1px solid $color-hairline;
  }

  &__comment-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-xs;
  }

  &__comment-time {
    font-size: $font-size-fine-print;
    color: $color-ink-muted;
  }

  &__comment-text {
    font-size: $font-size-body;
    color: $color-ink;
    line-height: 1.5;
  }
}
</style>
