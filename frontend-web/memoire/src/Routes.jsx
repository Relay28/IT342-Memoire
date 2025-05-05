// AppRoutes.jsx
import { Routes, Route, Navigate } from "react-router-dom";
import { lazy, Suspense, useContext } from "react";
import { CircularProgress } from "@mui/material";
import { CapsuleContentProvider } from "./context/CapsuleWebContextProvider";
import React, { useState, useEffect, useRef, useCallback } from 'react';

// Lazy-loaded components
const Homepage = lazy(() => import("./components/Homepage"));
const Register = lazy(() => import("./components/Register"));
const Login = lazy(() => import("./components/Login"));
const ProfilePage = lazy(() => import("./components/ProfilePage"));
const SearchResult = lazy(() => import("./components/Profile/SearchResult"));
const ProfilePageOther = lazy(() => import("./components/Profile/OtherProfile"));
const CreateCapsule = lazy(() => import("./components/CreateCapsule"));
const Capsules = lazy(() => import("./components/Capsules"));
const ArchivedCapsules = lazy(() => import("./components/ArchivedCapsules"));
const FriendsPage = lazy(() => import("./components/FriendsPage"));
const ErrorPage = lazy(() => import("./components/ErrorPage"));
const AdminLogin = lazy(() => import("./components/admin/AdminLogin"));
const AdminDashboard = lazy(() => import("./components/admin/AdminDashboard"));
const ReportDetailsPage = lazy(()  =>  import("./components/admin/ReportDetailsPage"));
const ReportListPage = lazy(()  =>  import("./components/admin/ReportListPage"));
const UserListPage = lazy(()  =>  import("./components/admin/UserList"));
const ConfiscatedListPage = lazy(()  =>  import("./components/admin/ConfiscatedContentPage"));
const CountdownTimerPage = lazy(()  =>  import("./components/CountdownTimer"));
const RouteLoader = () => (
  <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
    <CircularProgress size={60} />
  </div>
);

import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "./components/AuthProvider";

// Add these wrapper components before the AppRoutes component
const ProtectedUserRoute = ({ children }) => {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
    }
  }, [isAuthenticated, navigate]);

  return children;
};

const ProtectedAdminRoute = ({ children }) => {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/admin/login');
    } else if (user?.role !== 'ROLE_ADMIN') {
      navigate('/login');
    }
  }, [isAuthenticated, user, navigate]);

  return children;
};
const AppRoutes = () => {
  return (
    <Suspense fallback={<RouteLoader />}>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        
        {/* Admin Routes */}
        <Route path="/admin/login" element={<AdminLogin />} />
        <Route
          path="/admin/*"
          element={
            <ProtectedAdminRoute>
              <Routes>
                <Route path="dashboard" element={<AdminDashboard />} />
                <Route path="reports" element={<ReportListPage />} />
                <Route path="reports/:reportId" element={<ReportDetailsPage />} />
                <Route path="users" element={<UserListPage />} />
                <Route path="confiscated/content" element={<ConfiscatedListPage />} />
              </Routes>
            </ProtectedAdminRoute>
          }
        />

        {/* User Routes */}
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />

        {/* Protected User Routes */}
        <Route
          path="/*"
          element={
            <ProtectedUserRoute>
              <Routes>
                <Route path="homepage" element={<Homepage />} />
                <Route path="profile" element={<ProfilePage />} />
                <Route path="search" element={<SearchResult />} />
                <Route path="profile/:userId" element={<ProfilePageOther />} />
                <Route path="locked-capsules" element={<CountdownTimerPage />} />
                <Route path="create" element={<CreateCapsule />} />
                <Route
                  path="edit/:id"
                  element={
                    <CapsuleContentProvider>
                      <CreateCapsule />
                    </CapsuleContentProvider>
                  }
                />
                <Route path="capsules" element={<Capsules />} />
                <Route path="archived_capsules" element={<ArchivedCapsules />} />
                <Route path="friends" element={<FriendsPage />} />
              </Routes>
            </ProtectedUserRoute>
          }
        />

        <Route path="*" element={<ErrorPage />} />
      </Routes>
    </Suspense>
  );
};

export default AppRoutes;