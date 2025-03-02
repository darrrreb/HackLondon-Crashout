import React, { useCallback, useState, useEffect } from 'react';
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

import {initialNodes, initialEdges, createNodesFromSteps, STEPS} from './initialElements.js';
import { ArrowLeftCircleFill } from "react-bootstrap-icons";
import { useNavigate } from "react-router-dom";
import { Modal, Button } from "react-bootstrap";
import bookmark from "../assets/images/bookmark.png";
import { StepModal } from "../components/StepModal.jsx";

import axios from "axios";

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

// const { nodes: rawNodes, edges: rawEdges } = createNodesFromSteps(STEPS);
//
// const { nodes: layoutedNodes, edges: layoutedEdges } = getLayoutedElements(
//   rawNodes,
//   rawEdges,
// );

const Flow = () => {
  const [steps, setSteps] = useState([]);
  const [merge, setMerge] = useState(false);
  const [sha1, setSha1] = useState("");
  const [show, setShow] = useState(false);
  const [selectedNode, setSelectedNode] = useState(null);
  const navigate = useNavigate();



  const handleShow = () => setShow(true);
  const handleClose = () => setShow(false);

  useEffect(() => {
    axios.get("http://localhost:8080/api/steps/demo")
        .then((response) => {
          const byteArrays = response.data.map((step) => new Uint8Array(step));
          const jsonStrings = byteArrays.map((byteArray) => new TextDecoder().decode(byteArray));
          const jsonObjects = jsonStrings.map((jsonString) => JSON.parse(jsonString));
          console.log(jsonObjects);
          setSteps(jsonObjects.reverse());
        })
    //setSteps(STEPS);
    addGraph();
  }, [steps]);

  const addGraph = () => {
    const { nodes: rawNodes, edges: rawEdges } = createNodesFromSteps(steps);

    const { nodes: layoutedNodes, edges: layoutedEdges } = getLayoutedElements(
      rawNodes,
      rawEdges,
    );

    setNodes(layoutedNodes);
    setEdges(layoutedEdges);
  }
/*
  const getSteps = async () => {
    try {
      await axios.get("http://localhost:8080/api/steps/demo")
          .then(response => {
            console.log(response.data)
            setSteps(response.data);
          })
    } catch (e) { console.log(e)}
  }*/

  const handleNavigation = () => {
    navigate('/');
  };

  const onNodeClick = (event, node) => {
    console.log('click node', node);
    setSelectedNode(node);
    handleShow();
  }

 const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

  const handleMerge = async (event, node) => {
    setNodes((nds) => nds.map((n) => n.id === node.id ? { ...n, style: { ...n.style, backgroundColor: '#5b2323' } } : n));
    if (sha1 === "") {
      console.log(node.data.sha);
      setSha1(node.data.sha);
    } else if (sha1 === node.data.sha) {
      setSha1("");
    } else {
      console.log(node.data.sha);
      await sleep(1000);
      executeMerge(node, node.data.sha);
    }
  };

  const executeMerge = (node, sha2) => {
    setNodes((nds) => nds.map((n) => ({ ...n, style: { ...n.style, backgroundColor: '#a14949' } })));
    // axios.post("http://localhost:8080/merge/" + sha1 + "/" + sha2, {})
    //   .then(response => {
    //     getSteps();  // Fetch updated steps after merge
    //   })
    //   .catch(error => console.error("Merge error:", error));
    setSha1("");  // Reset selected SHA
  };


  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);

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
            onNodeClick={merge ? (handleMerge) : (onNodeClick)}
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
      <div style={{ display: 'flex', justifyContent: 'center', marginTop: '20px' }}>
        <Button onClick={() => setMerge(!merge)} className="merge-btn">
          Currently in:
          {merge ? " Merge mode" : " View mode"}
        </Button>
      </div>
      <div style={{ display: 'flex', justifyContent: 'center', marginTop: '20px' }}>
        <h5>
          {merge ? "Select two nodes to merge" : ""}
        </h5>
      </div>
      <StepModal node={selectedNode} show={show} handleClose={handleClose}/>
    </>
  );
};

export default Flow;