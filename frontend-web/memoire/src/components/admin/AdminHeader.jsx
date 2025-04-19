import React from 'react';
import { 
  AppBar, 
  Toolbar, 
  Typography, 
  IconButton, 
  Box
} from '@mui/material';
import { 
  AdminPanelSettings, 
  Menu as MenuIcon 
} from '@mui/icons-material';

const drawerWidth = 240;

const AdminHeader = ({ title, onDrawerToggle }) => {
  return (
    <AppBar
      position="fixed"
      sx={{
        width: { md: `calc(100% - ${drawerWidth}px)` },
        ml: { md: `${drawerWidth}px` },
      }}
    >
      <Toolbar>
        <IconButton
          color="inherit"
          aria-label="open drawer"
          edge="start"
          onClick={onDrawerToggle}
          sx={{ mr: 2, display: { md: 'none' } }}
        >
          <MenuIcon />
        </IconButton>
        <Typography variant="h6" noWrap component="div" sx={{ display: 'flex', alignItems: 'center' }}>
          <AdminPanelSettings sx={{ mr: 1 }} />
          {title}
        </Typography>
      </Toolbar>
    </AppBar>
  );
};

export default AdminHeader;