import React, { useState } from 'react';
import { ArrowLeftCircleFill } from "react-bootstrap-icons";
import { useNavigate } from "react-router-dom";
import {CodeBlock, zenburn} from 'react-code-blocks';

const CommandTutorial = () => {
  const navigate = useNavigate();

  const handleNavigation = () => {
    navigate('/');
  };

  return (
    <>
      <div className="icon-repo-container">
        <ArrowLeftCircleFill size={25} color={"white"} onClick={handleNavigation} className={"icon"}/>
        <h3 className="main-heading">Command Line Interface</h3>
      </div>
      <h7 className="main-heading">Run this command to initialise version control.</h7>
      <CodeBlock
        text='chef prep'
        language='bash'
        showLineNumbers={false}
        theme={zenburn}
      />
      <h7 className="main-heading">Creates the repository in the directory of your choice. Inserts a .chef file.</h7>
      <br />
      <h7 className="main-heading">Run this command to add your changes straight to the repo.</h7>
      <CodeBlock
        text='chef cook'
        language='bash'
        showLineNumbers={false}
        theme={zenburn}
      />
      <h7 className="main-heading">Adds your differences to a new node in the repository tree.</h7>
      <br />
      <div className="space">
      </div>
    </>
  );
};

export default CommandTutorial