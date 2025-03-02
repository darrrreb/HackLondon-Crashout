import React, { useCallback, useState } from 'react';
import {
  Background,
  ReactFlow,
  addEdge,
  ConnectionLineType,
  Panel,
  useNodesState,
  useEdgesState, MiniMap, Controls,
} from '@xyflow/react';
import dagre from '@dagrejs/dagre';

import '@xyflow/react/dist/style.css';

import {initialNodes, initialEdges, createNodesFromSteps, steps} from './initialElements.js';
import { ArrowLeftCircleFill } from "react-bootstrap-icons";
import { useNavigate } from "react-router-dom";
import { Modal } from "react-bootstrap";
import bookmark from "../assets/images/bookmark.png";
import { StepModal } from "../components/StepModal.jsx";

const dagreGraph = new dagre.graphlib.Graph().setDefaultEdgeLabel(() => ({}));

const nodeWidth = 172;
const nodeHeight = 36;

const getLayoutedElements = (nodes, edges, direction = 'LR') => {
  const isHorizontal = direction === 'LR';
  dagreGraph.setGraph({ rankdir: direction });

  nodes.forEach((node) => {
    dagreGraph.setNode(node.id, { width: nodeWidth, height: nodeHeight });
  });

  edges.forEach((edge) => {
    dagreGraph.setEdge(edge.source, edge.target);
  });

  dagre.layout(dagreGraph);

  const newNodes = nodes.map((node) => {
    const nodeWithPosition = dagreGraph.node(node.id);
    const newNode = {
      ...node,
      targetPosition: isHorizontal ? 'left' : 'top',
      sourcePosition: isHorizontal ? 'right' : 'bottom',
      position: {
        x: nodeWithPosition.x - nodeWidth / 2,
        y: nodeWithPosition.y - nodeHeight / 2,
      },
    };

    return newNode;
  });

  return { nodes: newNodes, edges };
};

const { nodes: rawNodes, edges: rawEdges } = createNodesFromSteps(steps);

const { nodes: layoutedNodes, edges: layoutedEdges } = getLayoutedElements(
  rawNodes,
  rawEdges,
);

const Flow = () => {
  const [show, setShow] = useState(false);
  const [selectedNode, setSelectedNode] = useState(null);
  const navigate = useNavigate();

  const handleShow = () => setShow(true);
  const handleClose = () => setShow(false);

  const handleNavigation = () => {
    navigate('/');
  };

  const onNodeClick = (event, node) => {
    console.log('click node', node);
    setSelectedNode(node);
    handleShow();
  }
  const [nodes, setNodes, onNodesChange] = useNodesState(layoutedNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(layoutedEdges);

  const onConnect = useCallback(
      (params) =>
          setEdges((eds) =>
              addEdge(
                  { ...params, type: ConnectionLineType.SmoothStep, animated: true },
                  eds,
              ),
          ),
      [],
  );
  const onLayout = useCallback(
      (direction) => {
        const { nodes: layoutedNodes, edges: layoutedEdges } =
            getLayoutedElements(nodes, edges, direction);

        setNodes([...layoutedNodes]);
        setEdges([...layoutedEdges]);
      },
      [nodes, edges],
  );

  return (
    <>
      <div className="icon-repo-container">
        <ArrowLeftCircleFill size={25} color={"white"} onClick={handleNavigation} className={"icon"}/>
        <h3 className="main-heading">My hackathon project</h3>
      </div>
      <div style={{ width: 'auto', backgroundColor: '#ffdfae', borderRadius: '50px', height: '70vh' }}>
        <ReactFlow
            nodes={nodes}
            edges={edges}
            nodesDraggable={false}
            nodesConnectable={false}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            onConnect={onConnect}
            onNodeClick={onNodeClick}
            connectionLineType={ConnectionLineType.SmoothStep}
            fitView
        >
          <Panel position="top-left">
            <img
              alt="Bookmark"
              src={bookmark}
              width="100"
              height="100"
              className="d-inline-block"
            />
            <h3 style={{ marginTop: "2.25rem", marginLeft: "1.25rem", color: '#5b2323' }}>Repo Cookbook</h3>
          </Panel>
          <Panel position="top-right" style={{ margin: "2rem" }}>
            <button onClick={() => onLayout('TB')}>vertical layout</button>
            <button onClick={() => onLayout('LR')}>horizontal layout</button>
          </Panel>
          <MiniMap />
          <Controls />
        </ReactFlow>
      </div>
      <StepModal node={selectedNode} show={show} handleClose={handleClose}/>
    </>
  );
};

export default Flow;