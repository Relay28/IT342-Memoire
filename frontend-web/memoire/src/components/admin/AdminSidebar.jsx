import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { 
  Box, 
  Drawer, 
  Toolbar, 
  Typography, 
  Divider, 
  List, 
  ListItem, 
  ListItemIcon, 
  ListItemText, 
  Button 
} from '@mui/material';
import { 
  Dashboard, 
  Person, 
  Flag, 
  Settings, 
  Logout 
} from '@mui/icons-material';
import { useAuth } from '../AuthProvider';

const drawerWidth = 240;

const AdminSidebar = ({ mobileOpen, handleDrawerToggle, pendingReportsCount = 0 }) => {
  const location = useLocation();
  const { user, logout } = useAuth(); // Access user info and logout function
  
  const isActive = (path) => {
    return location.pathname === path || location.pathname.startsWith(`${path}/`);
  };

  const sidebarContent = (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <div>
        <Toolbar>
          <Typography variant="h6" noWrap component="div" sx={{ fontWeight: 'bold' }}>
            Admin Panel
          </Typography>
        </Toolbar>
        <Divider />
        <List>
          <ListItem 
            button 
            component={Link} 
            to="/admin/dashboard"
            selected={isActive('/admin/dashboard')}
          >
            <ListItemIcon>
              <Dashboard />
            </ListItemIcon>
            <ListItemText primary="Dashboard" />
          </ListItem>
          <ListItem 
            button 
            component={Link} 
            to="/admin/users"
            selected={isActive('/admin/users')}
          >
            <ListItemIcon>
              <Person />
            </ListItemIcon>
            <ListItemText primary="User Management" />
          </ListItem>
          <ListItem 
            button 
            component={Link} 
            to="/admin/reports"
            selected={isActive('/admin/reports')}
          >
            <ListItemIcon>
              <Flag />
            </ListItemIcon>
            <ListItemText primary="Reports" />
            {pendingReportsCount > 0 && (
              <Box 
                sx={{ 
                  bgcolor: 'warning.main',
                  color: 'warning.contrastText',
                  borderRadius: '50%',
                  width: 24,
                  height: 24,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '0.75rem'
                }}
              >
                {pendingReportsCount}
              </Box>
            )}
          </ListItem>
          <ListItem 
            button 
            component={Link} 
            to="/admin/confiscated/content"
            selected={isActive('/admin/confiscated/content')}
          >
            <ListItemIcon>
              <Flag />
            </ListItemIcon>
            <ListItemText primary="Confiscated Content" />
          </ListItem>
        </List>
        <Divider />
        <List>
          <ListItem 
            button 
            component={Link} 
            to="/admin/settings"
            selected={isActive('/admin/settings')}
          >
            <ListItemIcon>
              <Settings />
            </ListItemIcon>
            <ListItemText primary="Settings" />
          </ListItem>
        </List>
      </div>
      
      {/* User Info and Logout Button */}
      <Box sx={{ mt: 'auto', p: 2 }}>
        <Divider />
        <Box sx={{ textAlign: 'center', mb: 2 }}>
          <Typography variant="body1" sx={{ fontWeight: 'bold' }}>
            {user?.name || 'Admin'}
          </Typography>
          <Typography variant="body2" color="textSecondary">
            {user?.email || 'admin@example.com'}
          </Typography>
        </Box>
        <Button 
          variant="contained" 
          color="error" 
          startIcon={<Logout />} 
          fullWidth 
          onClick={logout}
        >
          Logout
        </Button>
      </Box>
    </div>
  );

  return (
    <Box
      component="nav"
      sx={{ width: { md: drawerWidth }, flexShrink: { md: 0 } }}
    >
      {/* Mobile drawer */}
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={handleDrawerToggle}
        ModalProps={{ keepMounted: true }}
        sx={{
          display: { xs: 'block', md: 'none' },
          '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
        }}
      >
        {sidebarContent}
      </Drawer>
      
      {/* Desktop drawer */}
      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', md: 'block' },
          '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
        }}
        open
      >
        {sidebarContent}
      </Drawer>
    </Box>
  );
};

export default AdminSidebar;