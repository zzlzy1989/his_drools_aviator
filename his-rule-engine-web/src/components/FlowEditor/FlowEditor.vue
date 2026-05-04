<template>
  <div class="flow-editor">
    <!-- Toolbar -->
    <div class="flow-editor__toolbar">
      <el-button-group>
        <el-button :icon="Plus" @click="handleNewFlow">新建</el-button>
        <el-button :icon="Download" @click="handleSave" :loading="saving">保存</el-button>
        <el-button :icon="Upload" @click="handlePublish" :disabled="!flowId">发布</el-button>
      </el-button-group>
      <el-divider direction="vertical" />
      <el-button-group>
        <el-button :icon="RefreshLeft" @click="handleUndo" :disabled="!canUndo">撤销</el-button>
        <el-button :icon="RefreshRight" @click="handleRedo" :disabled="!canRedo">重做</el-button>
      </el-button-group>
      <el-divider direction="vertical" />
      <el-button-group>
        <el-button :icon="View" @click="handlePreview" :type="store.isPreviewMode ? 'primary' : 'default'">预览</el-button>
        <el-button :icon="Download" @click="handleExport">导出</el-button>
        <el-button :icon="Upload" @click="handleImport">导入</el-button>
      </el-button-group>
      <el-divider direction="vertical" />
      <el-button :icon="Warning" @click="showErrorPanel = !showErrorPanel">
        校验
        <el-badge v-if="errors.length" :value="errors.length" class="badge" />
      </el-button>
      <el-button :icon="Rank" @click="autoLayout">自动布局</el-button>
      <div class="flow-editor__status">
        <el-tag v-if="store.isDirty" type="warning" size="small">未保存</el-tag>
        <el-tag v-else type="success" size="small">已保存</el-tag>
        <span v-if="flowId" class="version">v{{ currentVersion }}</span>
      </div>
    </div>

    <!-- Main Editor Area -->
    <div class="flow-editor__main">
      <!-- Node Palette -->
      <div class="flow-editor__palette">
        <div class="palette-title">节点面板</div>
        <div class="palette-nodes">
          <div
            v-for="nodeType in nodeTypes"
            :key="nodeType.type"
            class="palette-node"
            :data-type="nodeType.type"
            draggable="true"
            @dragstart="onDragStart($event, nodeType)"
          >
            <el-icon><component :is="nodeType.icon" /></el-icon>
            <span>{{ nodeType.label }}</span>
          </div>
        </div>
      </div>

      <!-- Canvas -->
      <div class="flow-editor__canvas" ref="canvasRef">
        <div id="flow-canvas" ref="graphRef" />
        <div id="minimap-container" ref="minimapRef" class="minimap-container" />
      </div>

      <!-- Property Panel -->
      <div class="flow-editor__properties">
        <div class="properties-title">属性配置</div>
        <div v-if="store.multiSelectedIds.length > 1" class="properties-form">
          <el-form label-width="80px" size="small">
            <el-form-item label="已选中">
              <el-tag>{{ store.multiSelectedIds.length }} 个节点/连线</el-tag>
            </el-form-item>
            <el-form-item label="批量操作">
              <el-button-group>
                <el-button @click="batchAlign('left')">左对齐</el-button>
                <el-button @click="batchAlign('center')">水平居中</el-button>
                <el-button @click="batchAlign('top')">顶部对齐</el-button>
              </el-button-group>
            </el-form-item>
            <el-form-item label="批量删除">
              <el-button type="danger" @click="batchDelete">删除选中</el-button>
            </el-form-item>
          </el-form>
        </div>
        <div v-else-if="store.selectedNode" class="properties-form">
          <el-form label-width="80px" size="small">
            <el-form-item label="节点ID">
              <el-input v-model="store.selectedNode.nodeId" disabled />
            </el-form-item>
            <el-form-item label="节点类型">
              <el-tag :type="getNodeTypeColor(store.selectedNode.type)">
                {{ getNodeTypeLabel(store.selectedNode.type) }}
              </el-tag>
            </el-form-item>
            <el-form-item label="节点名称">
              <el-input v-model="store.selectedNode.label" @change="handleNodeUpdate" />
            </el-form-item>
            <template v-if="store.selectedNode.type === 'condition'">
              <el-form-item label="表达式">
                <el-input
                  v-model="store.selectedNode.expression"
                  type="textarea"
                  :rows="4"
                  placeholder="例如: fact.patientType == 'RESIDENT'"
                  @change="handleNodeUpdate"
                />
              </el-form-item>
              <el-form-item label="条件分支">
                <div class="branch-item">
                  <span class="branch-label">为true时:</span>
                  <el-select
                    v-model="store.selectedNode.branches!.true"
                    placeholder="选择节点"
                    @change="handleNodeUpdate"
                  >
                    <el-option
                      v-for="node in otherNodes"
                      :key="node.nodeId"
                      :label="node.label"
                      :value="node.nodeId"
                    />
                  </el-select>
                </div>
                <div class="branch-item">
                  <span class="branch-label">为false时:</span>
                  <el-select
                    v-model="store.selectedNode.branches!.false"
                    placeholder="选择节点"
                    @change="handleNodeUpdate"
                  >
                    <el-option
                      v-for="node in otherNodes"
                      :key="node.nodeId"
                      :label="node.label"
                      :value="node.nodeId"
                    />
                  </el-select>
                </div>
              </el-form-item>
            </template>
            <template v-else-if="store.selectedNode.type === 'action'">
              <el-form-item label="规则Key">
                <el-input
                  v-model="store.selectedNode.ruleKey"
                  placeholder="例如: rule.reimburse.resident"
                  @change="handleNodeUpdate"
                />
              </el-form-item>
              <el-form-item label="超时时间">
                <el-input-number
                  v-model="store.selectedNode.timeout"
                  :min="0"
                  :max="300"
                  controls-position="right"
                  @change="handleNodeUpdate"
                />
              </el-form-item>
            </template>
            <template v-else-if="store.selectedNode.type === 'formula'">
              <el-form-item label="公式Key">
                <el-input
                  v-model="store.selectedNode.formulaKey"
                  placeholder="例如: formula.reimburse.calculate"
                  @change="handleNodeUpdate"
                />
              </el-form-item>
            </template>
            <template v-else-if="store.selectedNode.type === 'subflow'">
              <el-form-item label="子流程ID">
                <el-input
                  v-model="store.selectedNode.flowId"
                  placeholder="选择子流程"
                  @change="handleNodeUpdate"
                />
              </el-form-item>
              <el-form-item label="异步执行">
                <el-switch v-model="store.selectedNode.async" @change="handleNodeUpdate" />
              </el-form-item>
            </template>
            <el-form-item>
              <el-button type="danger" @click="handleDeleteNode">删除节点</el-button>
            </el-form-item>
          </el-form>
        </div>
        <div v-else-if="store.selectedEdge" class="properties-form">
          <el-form label-width="80px" size="small">
            <el-form-item label="源节点">
              <el-input :value="getNodeLabel(store.selectedEdge.source)" disabled />
            </el-form-item>
            <el-form-item label="目标节点">
              <el-input :value="getNodeLabel(store.selectedEdge.target)" disabled />
            </el-form-item>
            <el-form-item label="连线标签">
              <el-input v-model="store.selectedEdge.label" @change="handleEdgeUpdate" />
            </el-form-item>
            <el-form-item>
              <el-button type="danger" @click="handleDeleteEdge">删除连线</el-button>
            </el-form-item>
          </el-form>
        </div>
        <div v-else class="properties-empty">
          <el-empty description="请选择节点或连线" :image-size="80" />
        </div>
      </div>
    </div>

    <!-- Error Panel -->
    <el-drawer v-model="showErrorPanel" title="规则流校验结果" direction="btt" size="300px">
      <div v-if="errors.length === 0" style="text-align: center; padding: 40px; color: #67c23a;">
        <el-icon :size="48"><CircleCheckFilled /></el-icon>
        <p style="margin-top: 12px;">规则流校验通过，没有发现问题</p>
      </div>
      <div v-for="(error, index) in errors" :key="index" class="error-item">
        <el-alert :title="error.message" :type="error.type" :closable="false" show-icon />
        <el-button v-if="error.nodeId" size="small" type="primary" @click="locateError(error.nodeId!)">定位</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { Graph, Node, Edge, Cell } from '@antv/x6'
import { DataUri } from '@antv/x6'
import { MiniMap } from '@antv/x6-plugin-minimap'
import dagre from 'dagre'
import {
  Plus, RefreshLeft, RefreshRight, View, Download, Upload,
  VideoPlay, VideoPause, SetUp, Connection, Operation, FolderOpened, Guide,
  Warning, CircleCheckFilled, Rank
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { useFlowEditorStore } from '@/stores/flow-editor'
import { getFlowById, createFlow, updateFlow, publishFlow, exportFlow } from '@/api/rule-flow'
import type { NodeDTO, NodeType } from './types'

interface NodeTypeConfig {
  type: NodeType
  label: string
  icon: string
  color: string
}

const nodeTypes: NodeTypeConfig[] = [
  { type: 'start', label: '开始', icon: 'VideoPlay', color: '#67C23A' },
  { type: 'end', label: '结束', icon: 'VideoPause', color: '#F56C6C' },
  { type: 'condition', label: '条件', icon: 'SetUp', color: '#E6A23C' },
  { type: 'action', label: '动作', icon: 'Operation', color: '#409EFF' },
  { type: 'formula', label: '公式', icon: 'Connection', color: '#9C27B0' },
  { type: 'subflow', label: '子流程', icon: 'FolderOpened', color: '#00BCD4' },
]

const route = useRoute()
const router = useRouter()
const store = useFlowEditorStore()

const graphRef = ref<HTMLElement>()
const canvasRef = ref<HTMLElement>()
const minimapRef = ref<HTMLElement>()
const saving = ref(false)
const showErrorPanel = ref(false)
const errors = ref<Array<{ type: 'error' | 'warning'; message: string; nodeId?: string }>>([])

const flowId = computed(() => route.params.id as string | undefined)
const currentVersion = computed(() => store.currentFlow?.version || 1)

const otherNodes = computed(() =>
  store.flowDefinition.nodes.filter(n => n.nodeId !== store.selectedNodeId)
)

const canUndo = computed(() => graph?.canUndo() ?? false)
const canRedo = computed(() => graph?.canRedo() ?? false)

let graph: Graph | null = null
let minimap: MiniMap | null = null
let isRendering = false
let panGuard = false

// Initialize graph
onMounted(async () => {
  if (!graphRef.value) return

  graph = new Graph({
    container: graphRef.value,
    grid: true,
    mousewheel: {
      enabled: true,
      modifiers: ['ctrl', 'meta'],
    },
    panning: {
      enabled: true,
    },
    connecting: {
      snap: true,
      allowBlank: false,
      allowLoop: false,
      allowMulti: 'withPort',
      allowNode: true,
      allowEdge: false,
      highlight: true,
      connectionPoint: 'anchor',
      anchor: 'center',
      connector: { name: 'rounded', args: { radius: 8 } },
      router: { name: 'manhattan', args: { padding: 10 } },
      validateConnection({ sourceView, targetView, sourceMagnet, targetMagnet }) {
        if (!sourceMagnet || !targetMagnet) return false
        if (sourceView === targetView) return false
        const sourceGroup = (sourceMagnet as HTMLElement).getAttribute('port-group')
        const targetGroup = (targetMagnet as HTMLElement).getAttribute('port-group')
        if (sourceGroup === 'in' && targetGroup === 'in') return false
        if (sourceGroup === 'out' && targetGroup === 'out') return false
        return true
      },
    },
    selecting: {
      enabled: true,
      multiple: true,
      rubberband: true,
      showNodeSelectionBox: true,
    },
    history: {
      enabled: true,
      ignoreAdd: false,
      ignoreRemove: false,
      ignoreChange: false,
    },
  })

  // Configure minimap
  if (minimapRef.value) {
    minimap = new MiniMap({
      width: 160,
      height: 120,
      container: minimapRef.value,
      padding: 5,
      scalable: true,
    })
    graph.use(minimap)
  }

  // Panning constraint - keep viewport near content
  graph.on('graph:translate', () => {
    applyPanConstraint()
  })

  // Node click handler
  graph.on('node:click', ({ node }: { node: Node }) => {
    store.selectNode(node.id)
  })

  // Edge click handler
  graph.on('edge:click', ({ edge }: { edge: Edge }) => {
    store.selectEdge(edge.getSourceCellId(), edge.getTargetCellId())
  })

  // Blank click handler
  graph.on('blank:click', () => {
    store.clearSelection()
  })

  // Edge connected handler with validation
  graph.on('edge:connected', ({ edge, isNew }: { edge: Edge; isNew: boolean }) => {
    if (!isNew) return

    const source = edge.getSourceCellId()
    const target = edge.getTargetCellId()

    if (source === target) {
      edge.remove()
      ElMessage.warning('不能连接同一个节点')
      return
    }

    const existingEdge = graph!.getEdges().find(
      e => e.getSourceCellId() === source && e.getTargetCellId() === target && e.id !== edge.id
    )
    if (existingEdge) {
      edge.remove()
      ElMessage.warning('连线已存在')
      return
    }

    const sourceNode = store.flowDefinition.nodes.find(n => n.nodeId === source)
    if (sourceNode?.type === 'condition') {
      const outgoingEdges = graph!.getEdges().filter(e => e.getSourceCellId() === source && e.id !== edge.id)
      if (outgoingEdges.length >= 2) {
        edge.remove()
        ElMessage.warning('条件节点最多只能有两条连线（true/false）')
        return
      }

      const existingTrue = outgoingEdges.find(e => {
        const label = e.getLabelAt(0)
        return label?.attrs?.label?.text === 'true'
      })
      const existingFalse = outgoingEdges.find(e => {
        const label = e.getLabelAt(0)
        return label?.attrs?.label?.text === 'false'
      })

      if (!existingTrue && !existingFalse) {
        edge.setLabels([{ attrs: { label: { text: 'true' } } }])
        ElMessage.info('连线已标记为 true 分支，可双击标签修改')
      } else if (!existingTrue) {
        edge.setLabels([{ attrs: { label: { text: 'true' } } }])
      } else if (!existingFalse) {
        edge.setLabels([{ attrs: { label: { text: 'false' } } }])
      }
    }

    const label = edge.getLabelAt(0)?.attrs?.label?.text || ''
    store.addEdge({ source, target, label })
  })

  // Edge removed handler
  graph.on('edge:removed', ({ edge }: { edge: Edge }) => {
    const source = edge.getSourceCellId()
    const target = edge.getTargetCellId()
    if (source && target) {
      store.removeEdge(source, target)
    }
  })

  // Node moved handler
  graph.on('node:change:position', ({ node }: { node: Node }) => {
    const area = graph!.getGraphArea()
    const margin = 20
    const pos = node.getPosition()
    const size = node.size()

    let x = pos.x
    let y = pos.y

    if (x < margin) x = margin
    else if (x + size.width > area.width - margin) x = area.width - size.width - margin

    if (y < margin) y = margin
    else if (y + size.height > area.height - margin) y = area.height - size.height - margin

    if (x !== pos.x || y !== pos.y) {
      node.setPosition(x, y)
    }
  })

  graph.on('node:moved', ({ node }: { node: Node }) => {
    const nodeData = store.flowDefinition.nodes.find(n => n.nodeId === node.id)
    if (nodeData) {
      const position = node.getPosition()
      store.updateNode(node.id, { x: position.x, y: position.y })
    }
  })

  // History change handler - sync X6 history with store
  graph.on('history:change', () => {
    syncGraphToStore()
  })

  // Selection changed handler
  graph.on('selection:changed', ({ selected }: { selected: Cell[] }) => {
    if (selected.length > 1) {
      store.setMultiSelection(selected.map(cell => cell.id))
    } else if (selected.length === 1) {
      const cell = selected[0]
      if (cell.isNode()) {
        store.selectNode(cell.id)
      } else if (cell.isEdge()) {
        const edge = cell as Edge
        store.selectEdge(edge.getSourceCellId(), edge.getTargetCellId())
      }
    } else {
      store.clearSelection()
    }
  })

  // Drop handler
  graphRef.value.addEventListener('drop', handleDrop)
  graphRef.value.addEventListener('dragover', (e: DragEvent) => {
    e.preventDefault()
  })

  // Load flow if id exists
  if (flowId.value) {
    await loadFlow(Number(flowId.value))
  }

  // Resize observer
  const resizeObserver = new ResizeObserver(() => {
    graph?.resize()
  })
  if (canvasRef.value) {
    resizeObserver.observe(canvasRef.value)
  }

  // Keyboard shortcuts
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
  if (graph) {
    graph.dispose()
    graph = null
  }
})

// Watch flow definition changes and update graph
watch(
  () => store.flowDefinition,
  (definition, oldDefinition) => {
    if (!graph || isRendering) return
    const nodesChanged = definition.nodes.length !== oldDefinition?.nodes?.length ||
      JSON.stringify(definition.nodes.map(n => n.nodeId)) !== JSON.stringify(oldDefinition?.nodes?.map(n => n.nodeId))
    const edgesChanged = definition.edges.length !== oldDefinition?.edges?.length ||
      JSON.stringify(definition.edges) !== JSON.stringify(oldDefinition?.edges)
    
    if (nodesChanged || edgesChanged) {
      renderGraph(definition)
    }
  },
  { deep: true }
)

function renderGraph(definition: typeof store.flowDefinition) {
  if (!graph || isRendering) return
  isRendering = true

  graph.clearCells()

  // Add nodes
  definition.nodes.forEach(node => {
    const config = nodeTypes.find(nt => nt.type === node.type)
    graph!.addNode({
      id: node.nodeId,
      x: node.x || 100,
      y: node.y || 100,
      width: 120,
      height: 50,
      ports: {
        groups: {
          in: { position: 'left', attrs: { circle: { r: 5, fill: '#fff', stroke: '#409EFF', strokeWidth: 2 } } },
          out: { position: 'right', attrs: { circle: { r: 5, fill: '#fff', stroke: '#409EFF', strokeWidth: 2 } } },
        },
        items: node.type === 'condition'
          ? [
              { id: 'in', group: 'in' },
              { id: 'out-true', group: 'out', label: '是', args: { y: 10 } },
              { id: 'out-false', group: 'out', label: '否', args: { y: 40 } },
            ]
          : [
              { id: 'in', group: 'in' },
              { id: 'out', group: 'out' },
            ],
      },
      attrs: {
        body: {
          fill: config?.color || '#409EFF',
          stroke: '#fff',
          strokeWidth: 2,
          rx: 8,
          ry: 8,
        },
        label: {
          text: node.label,
          fill: '#fff',
          fontSize: 13,
          fontFamily: 'Arial, sans-serif',
        },
      },
      data: node,
    })
  })

  // Add edges
  definition.edges.forEach(edge => {
    graph!.addEdge({
      source: edge.source,
      target: edge.target,
      label: edge.label,
      attrs: {
        line: {
          stroke: '#b1b1b1',
          strokeWidth: 2,
          targetMarker: {
            name: 'classic',
            size: 6,
          },
        },
      },
      labels: edge.label ? [{ attrs: { label: { text: edge.label } } }] : [],
    })
  })

  // Highlight preview path
  if (store.isPreviewMode && store.previewPath.length > 0) {
    store.previewPath.forEach((nodeId, index) => {
      if (index < store.previewPath.length - 1) {
        const nextId = store.previewPath[index + 1]
        const edge = graph!.getEdges().find(
          e => e.getSourceCellId() === nodeId && e.getTargetCellId() === nextId
        )
        if (edge) {
          edge.attr('line/stroke', '#67C23A')
          edge.attr('line/strokeWidth', 3)
        }
      }
      const node = graph!.getCellById(nodeId)
      if (node) {
        node.attr('body/stroke', '#67C23A')
        node.attr('body/strokeWidth', 3)
      }
    })
  }

  isRendering = false

  // Zoom to fit all content - this makes MiniMap viewport correctly sized
  nextTick(() => {
    if (!graph) return
    graph.zoomToFit({ padding: 30 })
  })
}

function syncGraphToStore() {
  if (!graph || isRendering) return

  const cells = graph.getCells()
  const nodes: NodeDTO[] = cells
    .filter(cell => cell.isNode())
    .map(node => ({
      nodeId: node.id,
      type: node.getData<NodeDTO>().type,
      label: node.attr('label/text') as string,
      x: node.getPosition().x,
      y: node.getPosition().y,
      ...node.getData<NodeDTO>(),
    }))

  const edges: EdgeDTO[] = cells
    .filter(cell => cell.isEdge())
    .map(edge => ({
      source: edge.getSourceCellId(),
      target: edge.getTargetCellId(),
      label: (edge.getLabelAt(0)?.attrs?.label?.text as string) || '',
    }))

  store.setFlowDefinition({ nodes, edges })
}

function applyPanConstraint() {
  if (!graph || panGuard) return

  const bbox = graph.getContentBBox()
  if (!bbox || bbox.width === 0 || bbox.height === 0) return

  const area = graph.getGraphArea()
  const padding = 200
  const current = graph.translate()

  let tx = current.tx
  let ty = current.ty

  const minTx = padding - bbox.x
  const maxTx = area.width - padding - (bbox.x + bbox.width)
  const minTy = padding - bbox.y
  const maxTy = area.height - padding - (bbox.y + bbox.height)

  if (minTx <= maxTx) {
    tx = Math.max(minTx, Math.min(maxTx, tx))
  }
  if (minTy <= maxTy) {
    ty = Math.max(minTy, Math.min(maxTy, ty))
  }

  if (tx !== current.tx || ty !== current.ty) {
    panGuard = true
    graph.translate(tx, ty)
    panGuard = false
  }
}

async function loadFlow(id: number) {
  try {
    const flow = await getFlowById(id)
    store.setCurrentFlow(flow)
    renderGraph(store.flowDefinition)
  } catch (error) {
    ElMessage.error('加载规则流失败')
  }
}

function onDragStart(event: DragEvent, nodeType: NodeTypeConfig) {
  event.dataTransfer?.setData('nodeType', nodeType.type)
}

function handleDrop(event: DragEvent) {
  event.preventDefault()
  const nodeType = event.dataTransfer?.getData('nodeType') as NodeType
  if (!nodeType || !graph) {
    return
  }

  const rect = graphRef.value!.getBoundingClientRect()
  const rawX = event.clientX - rect.left
  const rawY = event.clientY - rect.top

  const nodeW = 120, nodeH = 50

  // Convert screen coordinates to graph coordinates (accounting for pan & zoom)
  const point = graph.clientToLocal(rawX, rawY)
  let nodeX = point.x - nodeW / 2
  let nodeY = point.y - nodeH / 2

  // Clamp within canvas bounds with margin
  const area = graph.getGraphArea()
  const margin = 20
  nodeX = Math.max(margin, Math.min(nodeX, area.width - nodeW - margin))
  nodeY = Math.max(margin, Math.min(nodeY, area.height - nodeH - margin))

  const nodeId = `node_${Date.now()}`
  const config = nodeTypes.find(nt => nt.type === nodeType)

  const nodeData: NodeDTO = {
    nodeId,
    type: nodeType,
    label: config?.label || '新节点',
    x: nodeX,
    y: nodeY,
  }

  if (nodeType === 'condition') {
    nodeData.branches = { true: '', false: '' }
  }

  store.addNode(nodeData)
}

async function handleSave() {
  if (!store.flowDefinition.nodes.length) {
    ElMessage.warning('请先添加节点')
    return
  }

  saving.value = true
  try {
    if (flowId.value) {
      await updateFlow(Number(flowId.value), {
        flowDefinition: store.flowDefinition,
        flowName: store.currentFlow?.flowName,
      })
      ElMessage.success('保存成功')
    } else {
      const id = await createFlow({
        flowKey: `flow_${Date.now()}`,
        flowName: '新建规则流',
        flowDefinition: store.flowDefinition,
      })
      store.markClean()
      router.replace(`/flow/editor/${id}`)
      ElMessage.success('创建成功')
    }
    store.markClean()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function handlePublish() {
  if (!flowId.value) {
    ElMessage.warning('请先保存规则流')
    return
  }

  try {
    await ElMessageBox.confirm('发布后将无法直接修改，是否继续？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await publishFlow(Number(flowId.value))
    ElMessage.success('发布成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发布失败')
    }
  }
}

function handleUndo() {
  if (graph?.canUndo()) {
    graph?.undo()
  }
}

function handleRedo() {
  if (graph?.canRedo()) {
    graph?.redo()
  }
}

async function handlePreview() {
  if (store.isPreviewMode) {
    store.setPreviewMode(false)
    renderGraph(store.flowDefinition)
    return
  }

  try {
    const { value: inputData } = await ElMessageBox.prompt('请输入测试数据 (JSON)', '预览模式', {
      confirmButtonText: '执行',
      cancelButtonText: '取消',
      inputValue: '{"patientType": "RESIDENT", "age": 30}',
      inputType: 'textarea',
    })

    const data = JSON.parse(inputData || '{}')
    const path = simulateExecution(data)
    store.setPreviewPath(path)
    store.setPreviewMode(true)
    renderGraph(store.flowDefinition)
    ElMessage.success(`执行路径: ${path.join(' → ')}`)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('JSON 格式错误或执行失败')
    }
  }
}

function simulateExecution(inputData: Record<string, any>): string[] {
  if (!graph) return []

  const startNode = store.flowDefinition.nodes.find(n => n.type === 'start')
  if (!startNode) return []

  const path: string[] = [startNode.nodeId]
  let currentNode = startNode
  const visited = new Set<string>([startNode.nodeId])
  const maxSteps = 100

  for (let step = 0; step < maxSteps; step++) {
    if (currentNode.type === 'end') break

    const outgoingEdges = graph.getEdges().filter(e => e.getSourceCellId() === currentNode.nodeId)
    if (outgoingEdges.length === 0) break

    let nextNodeId: string | null = null

    if (currentNode.type === 'condition') {
      const expression = currentNode.expression || 'true'
      const result = evaluateExpression(expression, inputData)

      const targetEdge = outgoingEdges.find(e => {
        const label = e.getLabelAt(0)?.attrs?.label?.text
        return label === String(result)
      })
      nextNodeId = targetEdge?.getTargetCellId() || outgoingEdges[0]?.getTargetCellId() || null
    } else {
      nextNodeId = outgoingEdges[0]?.getTargetCellId() || null
    }

    if (!nextNodeId || visited.has(nextNodeId)) break

    visited.add(nextNodeId)
    path.push(nextNodeId)

    const nextNode = store.flowDefinition.nodes.find(n => n.nodeId === nextNodeId)
    if (!nextNode) break
    currentNode = nextNode
  }

  return path
}

function evaluateExpression(expr: string, data: Record<string, any>): boolean {
  try {
    const jsExpr = expr.replace(/fact\.(\w+)/g, (_, key) => {
      const value = data[key]
      return typeof value === 'string' ? `'${value}'` : String(value ?? 'undefined')
    })
    return new Function(`return ${jsExpr}`)()
  } catch {
    return false
  }
}

function handleExport() {
  exportFlow(Number(flowId.value))
    .then(json => {
      const blob = new Blob([json], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `${store.currentFlow?.flowName || '规则流'}.json`
      a.click()
      URL.revokeObjectURL(url)
      ElMessage.success('导出成功')
    })
    .catch(() => ElMessage.error('导出失败'))
}

function handleImport() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.json'
  input.onchange = async (e: any) => {
    const file = e.target.files[0]
    if (!file) return
    const reader = new FileReader()
    reader.onload = () => {
      try {
        const json = reader.result as string
        const definition = JSON.parse(json)
        store.setFlowDefinition(definition)
        ElMessage.success('导入成功')
      } catch {
        ElMessage.error('导入失败：文件格式错误')
      }
    }
    reader.readAsText(file)
  }
  input.click()
}

function handleNodeUpdate() {
  renderGraph(store.flowDefinition)
}

function handleEdgeUpdate() {
  renderGraph(store.flowDefinition)
}

function handleDeleteNode() {
  if (!store.selectedNodeId) return
  const nodeId = store.selectedNodeId
  store.removeNode(nodeId)
  graph?.removeCell(nodeId)
  store.clearSelection()
}

function handleDeleteEdge() {
  if (!store.selectedEdge) return
  const { source, target } = store.selectedEdge
  const edge = graph?.getEdges().find(
    e => e.getSourceCellId() === source && e.getTargetCellId() === target
  )
  if (edge) {
    graph?.removeCell(edge.id)
  }
  store.removeEdge(source, target)
  store.clearSelection()
}

function handleNewFlow() {
  store.reset()
  renderGraph({ nodes: [], edges: [] })
  router.push('/flow/editor')
}

function getNodeLabel(nodeId: string): string {
  return store.flowDefinition.nodes.find(n => n.nodeId === nodeId)?.label || nodeId
}

function getNodeTypeColor(type: string): string {
  const colors: Record<string, string> = {
    start: 'success',
    end: 'danger',
    condition: 'warning',
    action: 'primary',
    formula: 'info',
    subflow: '',
  }
  return colors[type] || ''
}

function getNodeTypeLabel(type: string): string {
  return nodeTypes.find(n => n.type === type)?.label || type
}

// Validation
function validateFlow() {
  errors.value = []

  const nodes = store.flowDefinition.nodes
  const edges = store.flowDefinition.edges

  if (!nodes.find(n => n.type === 'start')) {
    errors.value.push({ type: 'error', message: '缺少开始节点' })
  }
  if (!nodes.find(n => n.type === 'end')) {
    errors.value.push({ type: 'error', message: '缺少结束节点' })
  }

  const connectedNodeIds = new Set([
    ...edges.map(e => e.source),
    ...edges.map(e => e.target),
  ])
  nodes.forEach(node => {
    if (node.type !== 'start' && !connectedNodeIds.has(node.nodeId)) {
      errors.value.push({
        type: 'warning',
        message: `节点 "${node.label}" 是孤立的`,
        nodeId: node.nodeId,
      })
    }
  })

  nodes.forEach(node => {
    if (node.type === 'condition') {
      const outgoingEdges = edges.filter(e => e.source === node.nodeId)
      if (outgoingEdges.length !== 2) {
        errors.value.push({
          type: 'error',
          message: `条件节点 "${node.label}" 必须有两条连线（true/false）`,
          nodeId: node.nodeId,
        })
      }
    }
  })

  nodes.forEach(node => {
    const incomingEdges = edges.filter(e => e.target === node.nodeId)
    const outgoingEdges = edges.filter(e => e.source === node.nodeId)

    if (node.type !== 'start' && incomingEdges.length === 0) {
      errors.value.push({
        type: 'warning',
        message: `节点 "${node.label}" 没有输入连线`,
        nodeId: node.nodeId,
      })
    }
    if (node.type !== 'end' && outgoingEdges.length === 0) {
      errors.value.push({
        type: 'warning',
        message: `节点 "${node.label}" 没有输出连线`,
        nodeId: node.nodeId,
      })
    }
  })

  if (hasCycle(nodes, edges)) {
    errors.value.push({ type: 'error', message: '存在循环依赖，无法执行' })
  }
}

function hasCycle(nodes: NodeDTO[], edges: EdgeDTO[]): boolean {
  const adjacencyList = new Map<string, string[]>()
  nodes.forEach(node => adjacencyList.set(node.nodeId, []))
  edges.forEach(edge => adjacencyList.get(edge.source)?.push(edge.target))

  const visited = new Set<string>()
  const recursionStack = new Set<string>()

  function dfs(nodeId: string): boolean {
    visited.add(nodeId)
    recursionStack.add(nodeId)

    for (const neighbor of adjacencyList.get(nodeId) || []) {
      if (!visited.has(neighbor)) {
        if (dfs(neighbor)) return true
      } else if (recursionStack.has(neighbor)) {
        return true
      }
    }

    recursionStack.delete(nodeId)
    return false
  }

  for (const node of nodes) {
    if (!visited.has(node.nodeId)) {
      if (dfs(node.nodeId)) return true
    }
  }
  return false
}

function locateError(nodeId: string) {
  const node = graph?.getCellById(nodeId)
  if (node && graph) {
    graph.centerCell(node)
    graph.select(node)
  }
}

// Keyboard shortcuts
function handleKeydown(e: KeyboardEvent) {
  if (e.ctrlKey || e.metaKey) {
    switch (e.key.toLowerCase()) {
      case 'z':
        e.preventDefault()
        handleUndo()
        break
      case 'y':
        e.preventDefault()
        handleRedo()
        break
      case 's':
        e.preventDefault()
        handleSave()
        break
    }
  }
  if (e.key === 'Delete' || e.key === 'Backspace') {
    if (store.multiSelectedIds.length > 1) {
      e.preventDefault()
      batchDelete()
    } else if (store.selectedNodeId) {
      e.preventDefault()
      handleDeleteNode()
    } else if (store.selectedEdge) {
      e.preventDefault()
      handleDeleteEdge()
    }
  }
}

// Auto layout
function autoLayout() {
  if (!graph || store.flowDefinition.nodes.length === 0) {
    ElMessage.warning('请先添加节点')
    return
  }

  const g = new dagre.graphlib.Graph()
  g.setDefaultEdgeLabel(() => ({}))
  g.setGraph({ rankdir: 'TB', nodesep: 60, ranksep: 100 })

  store.flowDefinition.nodes.forEach(node => {
    g.setNode(node.nodeId, { width: 120, height: 50 })
  })

  store.flowDefinition.edges.forEach(edge => {
    g.setEdge(edge.source, edge.target)
  })

  dagre.layout(g)

  g.nodes().forEach(nodeId => {
    const node = g.node(nodeId)
    store.updateNode(nodeId, { x: node.x - 60, y: node.y - 25 })
  })

  renderGraph(store.flowDefinition)
  ElMessage.success('自动布局完成')
}

// Batch operations
function batchAlign(direction: string) {
  if (!graph) return

  const nodes = store.multiSelectedIds
    .map(id => graph!.getCellById(id))
    .filter((cell): cell is Node => cell != null && cell.isNode())

  if (nodes.length < 2) return

  if (direction === 'left') {
    const minX = Math.min(...nodes.map(n => n.getPosition().x))
    nodes.forEach(n => n.setPosition(minX, n.getPosition().y))
  } else if (direction === 'center') {
    const centerY = nodes.reduce((sum, n) => sum + n.getPosition().y, 0) / nodes.length
    nodes.forEach(n => n.setPosition(n.getPosition().x, centerY))
  } else if (direction === 'top') {
    const minY = Math.min(...nodes.map(n => n.getPosition().y))
    nodes.forEach(n => n.setPosition(n.getPosition().x, minY))
  }

  syncGraphToStore()
}

function batchDelete() {
  if (!graph) return

  store.multiSelectedIds.forEach(id => {
    const cell = graph!.getCellById(id)
    if (cell) {
      if (cell.isNode()) {
        store.removeNode(id)
      }
      graph!.removeCell(id)
    }
  })

  store.clearSelection()
  ElMessage.success('批量删除完成')
}

// Watch for validation
watch(() => store.flowDefinition, () => {
  validateFlow()
}, { deep: true })
</script>

<style lang="scss" scoped>
.flow-editor {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;

  &__toolbar {
    padding: 12px 16px;
    background: #fff;
    border-bottom: 1px solid #e8e8e8;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__status {
    margin-left: auto;
    display: flex;
    align-items: center;
    gap: 8px;

    .version {
      color: #999;
      font-size: 12px;
    }
  }

  &__main {
    flex: 1;
    display: flex;
    overflow: hidden;
  }

  &__palette {
    width: 160px;
    background: #fff;
    border-right: 1px solid #e8e8e8;
    display: flex;
    flex-direction: column;

    .palette-title {
      padding: 12px;
      font-weight: 500;
      border-bottom: 1px solid #e8e8e8;
    }

    .palette-nodes {
      flex: 1;
      padding: 8px;
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .palette-node {
      padding: 8px 12px;
      background: #f5f7fa;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      cursor: move;
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 13px;
      transition: all 0.2s;

      &:hover {
        border-color: #409eff;
        background: #ecf5ff;
      }
    }
  }

  &__canvas {
    flex: 1;
    position: relative;

    #flow-canvas {
      width: 100%;
      height: 100%;
    }

    .minimap-container {
      position: absolute;
      right: 10px;
      bottom: 10px;
      width: 160px;
      height: 120px;
      background: #fff;
      border: 1px solid #e8e8e8;
      border-radius: 4px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      z-index: 10;
      overflow: hidden;
    }
  }

  &__properties {
    width: 280px;
    background: #fff;
    border-left: 1px solid #e8e8e8;
    display: flex;
    flex-direction: column;

    .properties-title {
      padding: 12px;
      font-weight: 500;
      border-bottom: 1px solid #e8e8e8;
    }

    .properties-form {
      flex: 1;
      padding: 12px;
      overflow-y: auto;
    }

    .properties-empty {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .branch-item {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;

      .branch-label {
        width: 70px;
        font-size: 12px;
        color: #666;
      }
    }
  }
}

.error-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}
</style>