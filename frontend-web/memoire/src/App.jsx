import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
import { BrowserRouter } from "react-router-dom";
import AppRoutes from "./Routes";

function App() {
  return (
    <>
    <BrowserRouter>
      <AppRoutes />
    </BrowserRouter>
    </>
  )
}

export default App
