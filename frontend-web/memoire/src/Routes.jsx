import { Routes, Route } from "react-router-dom";
import Register from "./Register";
import Login from "./Login";
import Homepage from "./Homepage";

const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/register" element={<Register />} />
      <Route path="/login" element={<Login />} />
      <Route path="/homepage" element={<Homepage />} />
    </Routes>
  );
};

export default AppRoutes;
