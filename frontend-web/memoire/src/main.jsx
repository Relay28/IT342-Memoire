import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App.jsx';
import { Buffer } from 'buffer';

// Polyfill for Buffer (if needed for libraries like Firebase)
window.Buffer = Buffer;
window.global = window;

createRoot(document.getElementById('root')).render(
  <StrictMode>
   
      <App />
   
  </StrictMode>
);