// Node types
export type NodeType = 'start' | 'end' | 'condition' | 'action' | 'formula' | 'subflow'

export interface NodeDTO {
  nodeId: string
  type: NodeType
  label: string
  expression?: string
  ruleKey?: string
  formulaKey?: string
  flowId?: string
  branches?: Record<string, string>
  params?: Record<string, any>
  timeout?: number
  async?: boolean
  x?: number
  y?: number
}

export interface EdgeDTO {
  source: string
  target: string
  label?: string
}

export interface FlowDefinitionDTO {
  nodes: NodeDTO[]
  edges: EdgeDTO[]
}

// Node type configuration
export interface NodeTypeConfig {
  type: NodeType
  label: string
  icon: string
  color: string
}

export const NODE_TYPES: NodeTypeConfig[] = [
  { type: 'start', label: '开始', icon: 'VideoPlay', color: '#67C23A' },
  { type: 'end', label: '结束', icon: 'VideoPause', color: '#F56C6C' },
  { type: 'condition', label: '条件', icon: 'SetUp', color: '#E6A23C' },
  { type: 'action', label: '动作', icon: 'Operation', color: '#409EFF' },
  { type: 'formula', label: '公式', icon: 'Connection', color: '#9C27B0' },
  { type: 'subflow', label: '子流程', icon: 'FolderOpened', color: '#00BCD4' },
]