const position = { x: 0, y: 0 };
const edgeType = 'smoothstep';

export const initialNodes = [
  {
    id: '1',
    data: { label: 'node 1' },
    position,
  },
  {
    id: '2',
    data: { label: 'node 2' },
    position,
  },
  {
    id: '2a',
    data: { label: 'node 2a' },
    position,
  },
  {
    id: '2b',
    data: { label: 'node 2b' },
    position,
  },
  {
    id: '2c',
    data: { label: 'node 2c' },
    position,
  },
  {
    id: '2d',
    data: { label: 'node 2d' },
    position,
  },
  {
    id: '3',
    data: { label: 'node 3' },
    position,
  },
];

export const initialEdges = [
  { id: 'e12', source: '1', target: '2', type: edgeType },
  { id: 'e13', source: '1', target: '3', type: edgeType },
  { id: 'e22a', source: '2', target: '2a', type: edgeType },
  { id: 'e22b', source: '2', target: '2b', type: edgeType },
  { id: 'e22c', source: '2', target: '2c', type: edgeType, animated: true },
  { id: 'e2c2d', source: '2c', target: '2d', type: edgeType, animated: true }
];

export const STEPS = [
  { sha: 'KLSDF543598592DSLFKNVFVSFUEJDSNCVKBJLksldfjlsdkfj', shortMessage: "Add feature a", summary:"Test summary!!", childrenSha: ['ABClsdkfs'] },
  { sha: 'ABClsdkfs', shortMessage: "Add feature b", summary:"Test summary!!", childrenSha: ['HTGBNCKSLKDFJLSK', 'GGTY498324fsdlkjf'] },
  { sha: 'HTGBNCKSLKDFJLSK', shortMessage: "Add feature c", summary:"Test summary!!", childrenSha: [] },
  { sha: 'GGTY498324fsdlkjf', shortMessage: "Add feature d", summary:"Test summary!!", childrenSha: [] },
  { sha: 'KLSDF543598592DSLFKNVFVSFUEJDSNCVebgds', shortMessage: "Add feature z", summary:"Test summary!!", childrenSha: ['ABClsdkfs'] },
]

export const createNodesFromSteps = (steps) => {
  const nodes = [];
  const edges = [];
  let idCounter = 1;  // To assign unique IDs to nodes

  // Map to track SHA to node ID mapping
  const shaToIdMap = new Map();

  // Recursive function to traverse steps and generate nodes and edges
  const traverse = (sha) => {
    const step = steps.find((s) => s.sha === sha);
    if (!step) return;

    // Create a node for the current step
    const currentId = idCounter.toString();  // Store current ID for edges
    nodes.push({
      id: currentId,
      data: {
        label: step.shortMessage,
        sha: step.sha,
        summary: step.summary
      },
      position,  // Placeholder position
    });
    console.log(`Pushed node with id: ${currentId} and label: ${step.shortMessage}`)

    // Map the SHA to the current ID
    shaToIdMap.set(sha, currentId);
    idCounter += 1;  // Increment ID counter for the next node

    // Create edges to each child node recursively
    step.childrenSha.forEach((childSha) => {
      // Ensure the child node exists or is created
      if (!shaToIdMap.has(childSha) && !visited.has(childSha)) {
        visited.add(childSha);
        traverse(childSha);
      }

      // Create an edge between the current node and the child node
      edges.push({
        id: `e${currentId}-${shaToIdMap.get(childSha)}`,
        source: currentId,
        target: shaToIdMap.get(childSha),
        type: edgeType,  // Customize edge type if needed
        animated: true,       // Make edges animated for better visibility
      });
      console.log(`Pushed edge with source: ${currentId} and target: ${shaToIdMap.get(childSha)}`)
    });
  };

  // Start traversal from root nodes (steps without parents)
  const visited = new Set();  // To prevent re-visiting nodes
  steps.forEach((step) => {
    if (!visited.has(step.sha)) {
      visited.add(step.sha);
      traverse(step.sha);
    }
  });

  // Return both nodes and edges
  return { nodes, edges };
};