// AppRoutes.jsx
import { Routes, Route, Navigate } from "react-router-dom";
import { lazy, Suspense, useContext } from "react";
import { CircularProgress } from "@mui/material";
import { CapsuleContentProvider } from "./context/CapsuleWebContextProvider";


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
const RouteLoader = () => (
  <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
    <CircularProgress size={60} />
  </div>
);


const AppRoutes = () => {
  return (
    <Suspense fallback={<RouteLoader />}>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/admin/login" element={<AdminLogin />} />
        <Route path="/admin/dashboard" element={<AdminDashboard />} />
        <Route path="/admin/reports" element={<ReportListPage/>} />
        <Route path="/admin/users" element={<UserListPage/>} />
        <Route path="/admin/reports/:reportId" element={<ReportDetailsPage/>} />
        <Route path="/homepage" element={<Homepage />} />
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/search" element={<SearchResult />} />
        <Route path="/profile/:userId" element={<ProfilePageOther />} />
        
        <Route path="/create" element={<CreateCapsule />} />
        <Route 
          path="/edit/:id" 
          element={
            <CapsuleContentProvider>
              <CreateCapsule />
            </CapsuleContentProvider>
          } 
        />
        <Route path="/capsules" element={<Capsules />} />
        <Route path="/archived_capsules" element={<ArchivedCapsules />} />
        <Route path="/friends" element={<FriendsPage />} />

       


        <Route path="*" element={<ErrorPage />} />

      
      </Routes>
    </Suspense>
  );
};

export default AppRoutes;