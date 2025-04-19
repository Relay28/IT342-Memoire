import React, { useState } from 'react';
import { Box } from '@mui/material';
import AdminHeader from './AdminHeader';
import AdminSidebar from './AdminSidebar';

const drawerWidth = 240;

const AdminLayout = ({ children, title, pendingReportsCount = 0 }) => {
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  return (
    <Box sx={{ display: 'flex' }}>
      <AdminHeader 
        title={title} 
        onDrawerToggle={handleDrawerToggle} 
      />
      
      <AdminSidebar 
        mobileOpen={mobileOpen} 
        handleDrawerToggle={handleDrawerToggle} 
        pendingReportsCount={pendingReportsCount} 
      />
      
      {/* Main content */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          width: { md: `calc(100% - ${drawerWidth}px)` },
          mt: 8
        }}
      >
        {children}
      </Box>
    </Box>
  );
};

export default AdminLayout;