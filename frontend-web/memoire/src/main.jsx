import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App.jsx';
import { Buffer } from 'buffer';
import { BrowserRouter } from 'react-router-dom';

// Polyfill for Buffer (if needed for libraries like Firebase)
window.Buffer = Buffer;
window.global = window;

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>
);