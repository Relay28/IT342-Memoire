import { Routes, Route } from "react-router-dom";
import Register from "./Register";
import Login from "./Login";

const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/register" element={<Register />} />
      <Route path="/login" element={<Login />} />
    </Routes>
  );
};

export default AppRoutes;
