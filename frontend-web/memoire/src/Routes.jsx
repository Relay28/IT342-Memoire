import { lazy, Suspense } from 'react';
import { Routes, Route } from 'react-router-dom';
import { Box, CircularProgress } from '@mui/material';

// 1. Lazy load all page components
const Homepage = lazy(() => import('./components/Homepage'));
const Register = lazy(() => import('./components/Register'));
const Login = lazy(() => import('./components/Login'));
const ProfilePage = lazy(() => import('./components/ProfilePage'));

// 2. Centralized circular loader component
const RouteLoader = () => (
  <Box 
    sx={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      height: '100vh', // Full viewport height
      width: '100vw'  // Full viewport width
    }}
  >
    <CircularProgress 
      size={60} 
      thickness={4}
      sx={{
        color: (theme) => theme.palette.primary.main
      }} 
    />
  </Box>
);

// 3. Component with proper lazy loading
export default function AppRoutes() {
  return (
    <Suspense fallback={<RouteLoader />}>
      <Routes>
        <Route path="/homepage" element={<Homepage />} />
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="/profile" element={<ProfilePage />} />
      </Routes>
    </Suspense>
  );
}