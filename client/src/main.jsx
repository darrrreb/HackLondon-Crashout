import React from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Route, Routes } from "react-router-dom";
import './index.css';
import "bootstrap/dist/css/bootstrap.min.css";
import App from './App.jsx';
import LandingPage from './pages/LandingPage';

createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <Routes>
      <Route path="/" element={<App />}>
        <Route path="/" element={<LandingPage />} />
      </Route>
    </Routes>
  </BrowserRouter>
)
