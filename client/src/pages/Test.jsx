import { useCallback } from 'react';
import { ArrowLeftCircleFill} from "react-bootstrap-icons";
import { useNavigate } from "react-router-dom";
import {
  ReactFlow,
  MiniMap,
  Controls,
  Background,
  useNodesState,
  useEdgesState,
  addEdge,
} from '@xyflow/react';



const LandingPage = () => {
  const navigate = useNavigate();

  const handleNavigation = () => {
    navigate('/');
  };

  const initialNodes = [
    { id: '1', position: { x: 0, y: 0 }, data: { label: 'step one' } },
    { id: '2', position: { x: 0, y: 100 }, data: { label: 'step two' } },
    { id: '3', position: { x: -100, y: 200 }, data: { label: 'step three' } },
    { id: '4', position: { x: 100, y: 200 }, data: { label: 'step four' } },
    { id: '5', position: { x: -200, y: 300 }, data: { label: 'step five' } },
    { id: '6', position: { x: 0, y: 300 }, data: { label: 'step six' } },
    { id: '7', position: { x: 200, y: 300 }, data: { label: 'step seven' } },
  ];
  const initialEdges = [
    { id: 'e1-2', source: '1', type: 'smoothstep', target: '2' },
    { id: 'e2-3', source: '2', type: 'smoothstep', target: '3' },
    { id: 'e2-4', source: '2', type: 'smoothstep', target: '4' },
    { id: 'e3-5', source: '3', type: 'smoothstep', target: '5' },
    { id: 'e4-6', source: '4', type: 'smoothstep', target: '6' },
    { id: 'e4-7', source: '4', type: 'smoothstep', target: '7' }
  ];
  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

  const onConnect = useCallback(
    (params) => setEdges((eds) => addEdge(params, eds)),
    [setEdges],
  );

  return (
    <>
      <div className="icon-repo-container">
        <ArrowLeftCircleFill size={25} color={"white"} onClick={handleNavigation} className={"icon"}/>
        <h3 className="main-heading">My hackathon project</h3>
      </div>
      <div style={{ width: 'auto', backgroundColor: '#ffdfae', borderRadius: '50px', height: '100vh' }}>
        <ReactFlow
          nodes={nodes}
          edges={edges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          onConnect={onConnect}
          fitView
        >
          <Controls />
          <MiniMap />
        </ReactFlow>
      </div>
    </>
  );
};

export default LandingPage