import React from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Route, Routes } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";
import '@xyflow/react/dist/style.css';
import './index.css';
import App from './App.jsx';
import LandingPage from './pages/LandingPage';
import Flow from './pages/RepoPage';
import CommandTutorial from './pages/CommandsPage';

createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <Routes>
      <Route path="/" element={<App />}>
        <Route path="/" element={<LandingPage />} />
        <Route path="/repo" element={<Flow />} />
        <Route path="/cli" element={<CommandTutorial />} />
      </Route>
    </Routes>
  </BrowserRouter>
)
