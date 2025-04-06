import { Routes, Route } from "react-router-dom";
import { lazy, Suspense } from "react";
import { CircularProgress } from "@mui/material";

// Lazy-loaded components
const Homepage = lazy(() => import("./components/Homepage"));
const Register = lazy(() => import("./components/Register"));
const Login = lazy(() => import("./components/Login"));
const ProfilePage = lazy(() => import("./components/ProfilePage"));
const CreateCapsule = lazy(() => import("./components/CreateCapsule"));
const Capsules = lazy(() => import("./components/Capsules"));
const ArchivedCapsules = lazy(() => import("./components/ArchivedCapsules"));
const FriendsPage = lazy(() => import("./components/FriendsPage"));
const ErrorPage = lazy(() => import("./components/ErrorPage")); // New error page component

// Loading component
const RouteLoader = () => (
  <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
    <CircularProgress size={60} />
  </div>
);

const AppRoutes = () => {
  return (
    <Suspense fallback={<RouteLoader />}>
      <Routes>
        <Route path="/homepage" element={<Homepage />} />
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/create" element={<CreateCapsule />} />
        <Route path="/capsules" element={<Capsules />} />
        <Route path="/archived_capsules" element={<ArchivedCapsules />} />
        <Route path="/friends" element={<FriendsPage />} />
        {/* Error page route - catches all undefined paths */}
        <Route path="*" element={<ErrorPage />} />
      </Routes>
    </Suspense>
  );
};

export default AppRoutes;