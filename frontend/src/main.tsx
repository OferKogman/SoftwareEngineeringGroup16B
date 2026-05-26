import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Route, Routes } from "react-router-dom";
import './index.css';

import ViewEvent from "./Components/ViewEvent";

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/events/:id" element={<ViewEvent />} />
      </Routes>
    </BrowserRouter>
  </StrictMode>
);