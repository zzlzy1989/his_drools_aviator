<template>
  <div class="flow-history">
    <div class="flow-history__header">
      <h2>规则流版本历史</h2>
      <el-button @click="handleBack">返回</el-button>
    </div>

    <el-card v-if="flowInfo">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="规则流名称">{{ flowInfo.flowName }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ flowInfo.category }}</el-descriptions-item>
        <el-descriptions-item label="当前版本">v{{ flowInfo.version }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-table :data="versions" v-loading="loading" style="margin-top: 16px" stripe>
      <el-table-column prop="version" label="版本" width="80">
        <template #default="{ row }">
          <el-tag :type="row.version === flowInfo?.version ? 'success' : 'info'" size="small">
            v{{ row.version }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="changeDesc" label="变更说明" min-width="200" />
      <el-table-column prop="changeBy" label="变更人" width="100" />
      <el-table-column prop="changeTime" label="变更时间" width="180" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handlePreview(row)">预览</el-button>
          <el-button link type="primary" @click="handleCompare(row)" :disabled="row.version === flowInfo?.version">对比</el-button>
          <el-button link type="warning" @click="handleRollback(row)" :disabled="row.version === flowInfo?.version">回滚</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 预览对话框 -->
    <el-dialog v-model="previewVisible" title="版本预览" width="900px">
      <div v-if="previewData" class="preview-content">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="版本">v{{ previewData.version }}</el-descriptions-item>
          <el-descriptions-item label="变更说明">{{ previewData.changeDesc }}</el-descriptions-item>
          <el-descriptions-item label="变更人">{{ previewData.changeBy }}</el-descriptions-item>
          <el-descriptions-item label="变更时间">{{ previewData.changeTime }}</el-descriptions-item>
        </el-descriptions>
        <el-divider />
        <div class="node-list">
          <div class="section-title">节点 ({{ previewData.flowDefinition?.nodes?.length || 0 }})</div>
          <el-table :data="previewData.flowDefinition?.nodes || []" size="small" stripe>
            <el-table-column prop="nodeId" label="节点ID" width="120" />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="label" label="名称" />
            <el-table-column prop="expression" label="表达式/规则Key" show-overflow-tooltip />
          </el-table>
        </div>
        <div class="edge-list" style="margin-top: 16px">
          <div class="section-title">连线 ({{ previewData.flowDefinition?.edges?.length || 0 }})</div>
          <el-table :data="previewData.flowDefinition?.edges || []" size="small" stripe>
            <el-table-column prop="source" label="源节点" width="120" />
            <el-table-column prop="target" label="目标节点" width="120" />
            <el-table-column prop="label" label="标签" />
          </el-table>
        </div>
      </div>
    </el-dialog>

    <!-- 对比对话框 -->
    <el-dialog v-model="compareVisible" title="版本对比" width="1000px">
      <div class="compare-tip">
        <el-tag type="warning">当前版本: v{{ flowInfo?.version }}</el-tag>
        <span style="margin: 0 8px">vs</span>
        <el-tag v-if="compareTarget">目标版本: v{{ compareTarget.version }}</el-tag>
      </div>
      <el-tabs v-model="compareTab">
        <el-tab-pane label="节点对比" name="nodes">
          <el-table :data="nodeDiff" size="small" stripe>
            <el-table-column prop="changeType" label="变更类型" width="100">
              <template #default="{ row }">
                <el-tag :type="row.changeType === '新增' ? 'success' : row.changeType === '删除' ? 'danger' : 'warning'" size="small">
                  {{ row.changeType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="nodeId" label="节点ID" width="120" />
            <el-table-column prop="type" label="类型" width="100" />
            <el-table-column prop="label" label="名称" />
            <el-table-column prop="expression" label="表达式/Key" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="连线对比" name="edges">
          <el-table :data="edgeDiff" size="small" stripe>
            <el-table-column prop="changeType" label="变更类型" width="100">
              <template #default="{ row }">
                <el-tag :type="row.changeType === '新增' ? 'success' : row.changeType === '删除' ? 'danger' : 'warning'" size="small">
                  {{ row.changeType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="source" label="源节点" width="120" />
            <el-table-column prop="target" label="目标节点" width="120" />
            <el-table-column prop="label" label="标签" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFlowVersions, rollbackFlow, getFlowById } from '@/api/rule-flow'
import type { FlowVersionVO, RuleFlowVO } from '@/api/rule-flow'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const versions = ref<FlowVersionVO[]>([])
const flowInfo = ref<RuleFlowVO | null>(null)

const previewVisible = ref(false)
const previewData = ref<FlowVersionVO | null>(null)

const compareVisible = ref(false)
const compareTab = ref('nodes')
const compareTarget = ref<FlowVersionVO | null>(null)
const nodeDiff = ref<any[]>([])
const edgeDiff = ref<any[]>([])

const flowId = computed(() => Number(route.params.id))

function handleBack() {
  router.push('/flow')
}

async function loadVersions() {
  loading.value = true
  try {
    const [flowRes, versionsRes] = await Promise.all([
      getFlowById(flowId.value),
      getFlowVersions(flowId.value),
    ])
    flowInfo.value = flowRes.data
    versions.value = versionsRes.data || []
  } catch (e) {
    ElMessage.error('加载版本历史失败')
  } finally {
    loading.value = false
  }
}

function handlePreview(row: FlowVersionVO) {
  previewData.value = row
  previewVisible.value = true
}

function handleCompare(row: FlowVersionVO) {
  compareTarget.value = row

  const currentDef = flowInfo.value?.flowDefinition
  const targetDef = row.flowDefinition

  if (!currentDef || !targetDef) return

  // 计算节点差异
  const currentNodes = currentDef.nodes || []
  const targetNodes = targetDef.nodes || []
  const currentNodeMap = new Map(currentNodes.map(n => [n.nodeId, n]))
  const targetNodeMap = new Map(targetNodes.map(n => [n.nodeId, n]))

  const addedNodes = currentNodes.filter(n => !targetNodeMap.has(n.nodeId))
  const removedNodes = targetNodes.filter(n => !currentNodeMap.has(n.nodeId))
  const commonNodeIds = currentNodes.filter(n => targetNodeMap.has(n.nodeId)).map(n => n.nodeId)
  const modifiedNodes = commonNodeIds.filter(id => {
    const curr = currentNodeMap.get(id)!
    const tgt = targetNodeMap.get(id)!
    return JSON.stringify(curr) !== JSON.stringify(tgt)
  }).map(id => ({ changeType: '修改', ...currentNodeMap.get(id), original: targetNodeMap.get(id) }))

  nodeDiff.value = [
    ...addedNodes.map(n => ({ changeType: '新增', ...n })),
    ...removedNodes.map(n => ({ changeType: '删除', ...n })),
    ...modifiedNodes,
  ]

  // 计算连线差异
  const currentEdges = currentDef.edges || []
  const targetEdges = targetDef.edges || []
  const currentEdgeSet = new Set(currentEdges.map(e => `${e.source}-${e.target}`))
  const targetEdgeSet = new Set(targetEdges.map(e => `${e.source}-${e.target}`))

  const addedEdges = currentEdges.filter(e => !targetEdgeSet.has(`${e.source}-${e.target}`))
  const removedEdges = targetEdges.filter(e => !currentEdgeSet.has(`${e.source}-${e.target}`))

  edgeDiff.value = [
    ...addedEdges.map(e => ({ changeType: '新增', ...e })),
    ...removedEdges.map(e => ({ changeType: '删除', ...e })),
  ]

  compareVisible.value = true
}

async function handleRollback(row: FlowVersionVO) {
  try {
    await ElMessageBox.confirm(
      `确定要回滚到版本 v${row.version} 吗？当前版本将作为历史记录保存。`,
      '确认回滚',
      { type: 'warning' }
    )
    await rollbackFlow(flowId.value, row.version)
    ElMessage.success('回滚成功')
    loadVersions()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('回滚失败: ' + (e.message || ''))
    }
  }
}

onMounted(() => {
  if (flowId.value) {
    loadVersions()
  }
})
</script>

<style lang="scss" scoped>
.flow-history {
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

  .section-title {
    font-weight: $font-weight-medium;
    margin-bottom: $spacing-xs;
    color: $color-ink-secondary;
  }

  .compare-tip {
    display: flex;
    align-items: center;
    margin-bottom: $spacing-md;
  }

  .preview-content {
    max-height: 60vh;
    overflow-y: auto;
  }
}
</style>