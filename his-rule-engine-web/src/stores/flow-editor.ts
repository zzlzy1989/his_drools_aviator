import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { FlowDefinitionDTO, NodeDTO, EdgeDTO, RuleFlowVO } from '@/api/rule-flow'

export interface FlowEditorState {
  currentFlow: RuleFlowVO | null
  flowDefinition: FlowDefinitionDTO
  selectedNodeId: string | null
  selectedEdgeId: string | null
  isDirty: boolean
  isPreviewMode: boolean
  previewPath: string[]
  historyStack: FlowDefinitionDTO[]
  historyIndex: number
}

export const useFlowEditorStore = defineStore('flowEditor', () => {
  // State
  const currentFlow = ref<RuleFlowVO | null>(null)
  const flowDefinition = ref<FlowDefinitionDTO>({ nodes: [], edges: [] })
  const selectedNodeId = ref<string | null>(null)
  const selectedEdgeId = ref<string | null>(null)
  const isDirty = ref(false)
  const isPreviewMode = ref(false)
  const previewPath = ref<string[]>([])
  const historyStack = ref<FlowDefinitionDTO[]>([])
  const historyIndex = ref(-1)

  // Computed
  const selectedNode = computed(() => {
    if (!selectedNodeId.value) return null
    return flowDefinition.value.nodes.find(n => n.nodeId === selectedNodeId.value) || null
  })

  const selectedEdge = computed(() => {
    if (!selectedEdgeId.value) return null
    return flowDefinition.value.edges.find(e => `${e.source}-${e.target}` === selectedEdgeId.value) || null
  })

  const canUndo = computed(() => historyIndex.value > 0)
  const canRedo = computed(() => historyIndex.value < historyStack.value.length - 1)

  // Actions
  function setCurrentFlow(flow: RuleFlowVO | null) {
    currentFlow.value = flow
    if (flow?.flowDefinition) {
      flowDefinition.value = flow.flowDefinition
      saveToHistory()
    } else {
      flowDefinition.value = { nodes: [], edges: [] }
    }
    selectedNodeId.value = null
    selectedEdgeId.value = null
    isDirty.value = false
  }

  function setFlowDefinition(definition: FlowDefinitionDTO) {
    flowDefinition.value = definition
    isDirty.value = true
    saveToHistory()
  }

  function addNode(node: NodeDTO) {
    flowDefinition.value.nodes.push(node)
    isDirty.value = true
    saveToHistory()
  }

  function updateNode(nodeId: string, updates: Partial<NodeDTO>) {
    const node = flowDefinition.value.nodes.find(n => n.nodeId === nodeId)
    if (node) {
      Object.assign(node, updates)
      isDirty.value = true
      saveToHistory()
    }
  }

  function removeNode(nodeId: string) {
    flowDefinition.value.nodes = flowDefinition.value.nodes.filter(n => n.nodeId !== nodeId)
    flowDefinition.value.edges = flowDefinition.value.edges.filter(
      e => e.source !== nodeId && e.target !== nodeId
    )
    if (selectedNodeId.value === nodeId) {
      selectedNodeId.value = null
    }
    isDirty.value = true
    saveToHistory()
  }

  function addEdge(edge: EdgeDTO) {
    // Check if edge already exists
    const exists = flowDefinition.value.edges.some(
      e => e.source === edge.source && e.target === edge.target
    )
    if (!exists) {
      flowDefinition.value.edges.push(edge)
      isDirty.value = true
      saveToHistory()
    }
  }

  function updateEdge(source: string, target: string, updates: Partial<EdgeDTO>) {
    const edge = flowDefinition.value.edges.find(e => e.source === source && e.target === target)
    if (edge) {
      Object.assign(edge, updates)
      isDirty.value = true
      saveToHistory()
    }
  }

  function removeEdge(source: string, target: string) {
    flowDefinition.value.edges = flowDefinition.value.edges.filter(
      e => !(e.source === source && e.target === target)
    )
    if (selectedEdgeId.value === `${source}-${target}`) {
      selectedEdgeId.value = null
    }
    isDirty.value = true
    saveToHistory()
  }

  function selectNode(nodeId: string | null) {
    selectedNodeId.value = nodeId
    selectedEdgeId.value = null
  }

  function selectEdge(source: string, target: string) {
    selectedEdgeId.value = `${source}-${target}`
    selectedNodeId.value = null
  }

  function clearSelection() {
    selectedNodeId.value = null
    selectedEdgeId.value = null
  }

  function setPreviewMode(enabled: boolean) {
    isPreviewMode.value = enabled
    if (!enabled) {
      previewPath.value = []
    }
  }

  function setPreviewPath(path: string[]) {
    previewPath.value = path
  }

  function saveToHistory() {
    // Remove any future history if we're not at the end
    if (historyIndex.value < historyStack.value.length - 1) {
      historyStack.value = historyStack.value.slice(0, historyIndex.value + 1)
    }
    // Add current state to history
    historyStack.value.push(JSON.parse(JSON.stringify(flowDefinition.value)))
    historyIndex.value = historyStack.value.length - 1
    // Limit history size
    if (historyStack.value.length > 50) {
      historyStack.value.shift()
      historyIndex.value--
    }
  }

  function undo() {
    if (canUndo.value) {
      historyIndex.value--
      flowDefinition.value = JSON.parse(JSON.stringify(historyStack.value[historyIndex.value]))
      isDirty.value = true
    }
  }

  function redo() {
    if (canRedo.value) {
      historyIndex.value++
      flowDefinition.value = JSON.parse(JSON.stringify(historyStack.value[historyIndex.value]))
      isDirty.value = true
    }
  }

  function markClean() {
    isDirty.value = false
  }

  function reset() {
    currentFlow.value = null
    flowDefinition.value = { nodes: [], edges: [] }
    selectedNodeId.value = null
    selectedEdgeId.value = null
    isDirty.value = false
    isPreviewMode.value = false
    previewPath.value = []
    historyStack.value = []
    historyIndex.value = -1
  }

  return {
    // State
    currentFlow,
    flowDefinition,
    selectedNodeId,
    selectedEdgeId,
    isDirty,
    isPreviewMode,
    previewPath,
    // Computed
    selectedNode,
    selectedEdge,
    canUndo,
    canRedo,
    // Actions
    setCurrentFlow,
    setFlowDefinition,
    addNode,
    updateNode,
    removeNode,
    addEdge,
    updateEdge,
    removeEdge,
    selectNode,
    selectEdge,
    clearSelection,
    setPreviewMode,
    setPreviewPath,
    undo,
    redo,
    markClean,
    reset,
  }
})