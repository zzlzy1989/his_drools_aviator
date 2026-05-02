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
        <el-button :icon="RefreshLeft" @click="handleUndo" :disabled="!store.canUndo">撤销</el-button>
        <el-button :icon="RefreshRight" @click="handleRedo" :disabled="!store.canRedo">重做</el-button>
      </el-button-group>
      <el-divider direction="vertical" />
      <el-button-group>
        <el-button :icon="View" @click="handlePreview" :type="store.isPreviewMode ? 'primary' : 'default'">预览</el-button>
        <el-button :icon="Download" @click="handleExport">导出</el-button>
        <el-button :icon="Upload" @click="handleImport">导入</el-button>
      </el-button-group>
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
      </div>

      <!-- Property Panel -->
      <div class="flow-editor__properties">
        <div class="properties-title">属性配置</div>
        <div v-if="store.selectedNode" class="properties-form">
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { Graph, Node, Edge, Cell } from '@antv/x6'
import { DataUri } from '@antv/x6'
import {
  Plus, RefreshLeft, RefreshRight, View, Download, Upload,
  VideoPlay, VideoPause, SetUp, Connection, Operation, FolderOpened, Guide
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
const saving = ref(false)

const flowId = computed(() => route.params.id as string | undefined)
const currentVersion = computed(() => store.currentFlow?.version || 1)

const otherNodes = computed(() =>
  store.flowDefinition.nodes.filter(n => n.nodeId !== store.selectedNodeId)
)

let graph: Graph | null = null

// Initialize graph
onMounted(async () => {
  if (!graphRef.value) return

  graph = new Graph({
    container: graphRef.value,
    grid: true,
    mousewheel: {
      enabled: true,
      modifiers: ['Ctrl', 'Meta'],
    },
    panning: {
      enabled: true,
      modifiers: [],
    },
    connecting: {
      snap: true,
      allowBlank: false,
      allowLoop: false,
      highlight: true,
      connector: 'rounded',
      router: 'manhattan',
    },
    selecting: {
      enabled: true,
      multiple: false,
      showNodeSelectionBox: true,
    },
    history: false,
  })

  // Configure minimap
  const minimap = new (Graph as any).Minimap({
    width: 150,
    height: 100,
    container: document.createElement('div'),
  })
  graph.options.plugins = [minimap]

  // Node click handler
  graph.on('node:click', ({ node }: { node: Node }) => {
    store.selectNode(node.id as string)
  })

  // Edge click handler
  graph.on('edge:click', ({ edge }: { edge: Edge }) => {
    store.selectEdge(edge.source as string, edge.target as string)
  })

  // Blank click handler
  graph.on('blank:click', () => {
    store.clearSelection()
  })

  // Edge added handler
  graph.on('edge:added', ({ edge }: { edge: Edge }) => {
    const source = edge.source as string
    const target = edge.target as string
    if (source && target) {
      store.addEdge({ source, target })
    }
  })

  // Edge removed handler
  graph.on('edge:removed', ({ edge }: { edge: Edge }) => {
    const source = edge.source as string
    const target = edge.target as string
    if (source && target) {
      store.removeEdge(source, target)
    }
  })

  // Node moved handler
  graph.on('node:moved', ({ node }: { node: Node }) => {
    const nodeData = store.flowDefinition.nodes.find(n => n.nodeId === node.id)
    if (nodeData) {
      const position = node.getPosition()
      store.updateNode(node.id as string, { x: position.x, y: position.y })
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
})

onBeforeUnmount(() => {
  if (graph) {
    graph.dispose()
    graph = null
  }
})

// Watch flow definition changes and update graph
watch(
  () => store.flowDefinition,
  (definition) => {
    if (!graph) return
    renderGraph(definition)
  },
  { deep: true }
)

function renderGraph(definition: typeof store.flowDefinition) {
  if (!graph) return

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
          e => e.source === nodeId && e.target === nextId
        )
        if (edge) {
          edge.attr('line/stroke', '#67C23A')
          edge.attr('line/strokeWidth', 3)
        }
      }
      const node = graph!.getCell(nodeId)
      if (node) {
        node.attr('body/stroke', '#67C23A')
        node.attr('body/strokeWidth', 3)
      }
    })
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
  if (!nodeType || !graph) return

  const position = graph.clientToGraph({ x: event.offsetX, y: event.offsetY })
  const nodeId = `node_${Date.now()}`
  const config = nodeTypes.find(nt => nt.type === nodeType)

  const nodeData: NodeDTO = {
    nodeId,
    type: nodeType,
    label: config?.label || '新节点',
    x: position.x,
    y: position.y,
  }

  if (nodeType === 'condition') {
    nodeData.branches = { true: '', false: '' }
  }

  store.addNode(nodeData)

  // Add to graph
  graph.addNode({
    id: nodeId,
    x: position.x,
    y: position.y,
    width: 120,
    height: 50,
    attrs: {
      body: {
        fill: config?.color || '#409EFF',
        stroke: '#fff',
        strokeWidth: 2,
        rx: 8,
        ry: 8,
      },
      label: {
        text: nodeData.label,
        fill: '#fff',
        fontSize: 13,
      },
    },
    data: nodeData,
  })
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
  store.undo()
  renderGraph(store.flowDefinition)
}

function handleRedo() {
  store.redo()
  renderGraph(store.flowDefinition)
}

function handlePreview() {
  store.setPreviewMode(!store.isPreviewMode)
  if (store.isPreviewMode) {
    ElMessage.info('预览模式：点击节点查看执行路径')
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
  store.removeNode(store.selectedNodeId)
  graph?.removeNode(store.selectedNodeId)
}

function handleDeleteEdge() {
  if (!store.selectedEdge) return
  store.removeEdge(store.selectedEdge.source, store.selectedEdge.target)
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
</style>