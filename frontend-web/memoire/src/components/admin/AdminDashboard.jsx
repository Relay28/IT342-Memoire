import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Box, 
  Typography,
  Grid,
  Card,
  CardContent,
  CircularProgress,
  Paper,
  IconButton
} from '@mui/material';
import { 
  Group,
  Flag,
  Dashboard,
  ChevronRight
} from '@mui/icons-material';
import UserListComponent from './UserList';
import ReportListComponent from './ReportList';
import { useAuth } from '../AuthProvider';
import AdminLayout from './AdminLayout';


const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const AdminDashboard = () => {

  const [dashboardStats, setDashboardStats] = useState({
    totalUsers: 0,
    newUsers: 0,
    totalReports: 0,
    pendingReports: 0
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const navigate = useNavigate();
  const { user, authToken } = useAuth();
  
  // Number of items to show in preview sections
  const PREVIEW_LIMIT = 5;
  
  useEffect(() => {
    // Check if logged in user is admin
    if (!user || !authToken || user.role !== 'ROLE_ADMIN') {
      navigate('/admin/login');
      return;
    }
    
    fetchDashboardStats();
  }, [user, authToken, navigate]);
  
  const fetchDashboardStats = async () => {
    setLoading(true);
    setError(null);
    
    try {
      // Fetch users for stats
      const usersResponse = await fetch(`${API_BASE_URL}/api/users/admin/dashboard`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (!usersResponse.ok) {
        throw new Error('Failed to fetch user data');
      }
      
      const usersData = await usersResponse.json();
      
      // Fetch reports for stats
      const reportsResponse = await fetch(`${API_BASE_URL}/api/reports`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (!reportsResponse.ok) {
        throw new Error('Failed to fetch report data');
      }
      
      const reportsData = await reportsResponse.json();
      
      // Calculate stats
      const today = new Date();
      const lastWeek = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);
      
      const newUsersCount = usersData.filter(user => 
        new Date(user.createdAt) > lastWeek
      ).length;
      
      const pendingReportsCount = reportsData.filter(report => 
        report.status === 'PENDING'
      ).length;
      
      setDashboardStats({
        totalUsers: usersData.length,
        newUsers: newUsersCount,
        totalReports: reportsData.length,
        pendingReports: pendingReportsCount
      });
      
    } catch (err) {
      setError(err.message || 'An error occurred while fetching dashboard data');
      console.error('Error fetching dashboard data:', err);
    } finally {
      setLoading(false);
    }
  };
  
  const handleReportAction = (report, action) => {
    // Handle report actions (approve, reject, delete)
    console.log(`Report action: ${action} for report ID: ${report.id}`);
    // Implementation would call your API endpoints and refresh data
  };

  return (
    <AdminLayout 
      title="Admin Dashboard" 
      pendingReportsCount={dashboardStats.pendingReports}
    >
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <>
          {/* Dashboard Summary Stats */}
          <Typography variant="h5" gutterBottom>
            Overview
          </Typography>
          <Grid container spacing={3} sx={{ mb: 4 }}>
            <Grid item xs={12} sm={6} md={3}>
              <Card sx={{ height: '100%' }}>
                <CardContent>
                  <Typography variant="h6" component="div" color="text.secondary" gutterBottom>
                    Total Users
                  </Typography>
                  <Typography variant="h3" component="div" sx={{ mb: 1.5 }}>
                    {dashboardStats.totalUsers}
                  </Typography>
                  <Typography variant="body2" sx={{ display: 'flex', alignItems: 'center' }}>
                    <Group sx={{ mr: 0.5, fontSize: 20 }} />
                    {dashboardStats.newUsers} new this week
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            
            <Grid item xs={12} sm={6} md={3}>
              <Card sx={{ height: '100%' }}>
                <CardContent>
                  <Typography variant="h6" component="div" color="text.secondary" gutterBottom>
                    Total Reports
                  </Typography>
                  <Typography variant="h3" component="div" sx={{ mb: 1.5 }}>
                    {dashboardStats.totalReports}
                  </Typography>
                  <Typography variant="body2" sx={{ display: 'flex', alignItems: 'center' }}>
                    <Flag sx={{ mr: 0.5, fontSize: 20 }} />
                    {dashboardStats.pendingReports} pending review
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            
            <Grid item xs={12} sm={6} md={3}>
              <Card sx={{ height: '100%' }}>
                <CardContent>
                  <Typography variant="h6" component="div" color="text.secondary" gutterBottom>
                    Active Users
                  </Typography>
                  <Typography variant="h3" component="div" sx={{ mb: 1.5 }}>
                    {dashboardStats.totalUsers - 0}
                  </Typography>
                  <Typography variant="body2" sx={{ display: 'flex', alignItems: 'center' }}>
                    <Dashboard sx={{ mr: 0.5, fontSize: 20 }} />
                    0 users deactivated
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            
            <Grid item xs={12} sm={6} md={3}>
              <Card 
                sx={{ 
                  height: '100%', 
                  cursor: 'pointer',
                  bgcolor: dashboardStats.pendingReports > 0 ? 'warning.light' : 'inherit'
                }} 
                onClick={() => navigate('/admin/reports')}
              >
                <CardContent>
                  <Typography variant="h6" component="div" color={dashboardStats.pendingReports > 0 ? 'warning.dark' : 'text.secondary'} gutterBottom>
                    Pending Reports
                  </Typography>
                  <Typography variant="h3" component="div" color={dashboardStats.pendingReports > 0 ? 'warning.dark' : 'text.primary'} sx={{ mb: 1.5 }}>
                    {dashboardStats.pendingReports}
                  </Typography>
                  <Typography variant="body2" sx={{ display: 'flex', alignItems: 'center' }}>
                    <Flag sx={{ mr: 0.5, fontSize: 20, color: dashboardStats.pendingReports > 0 ? 'warning.dark' : 'inherit' }} />
                    {dashboardStats.pendingReports > 0 ? 'Require attention' : 'No pending reports'}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
          
          {/* Recent Activity Sections */}
          <Grid container spacing={3}>
            {/* Users Section */}
            <Grid item xs={12} md={6}>
              <Paper sx={{ p: 2, height: '100%' }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                  <Typography variant="h6">Recent Users</Typography>
                  <IconButton 
                    size="small" 
                    onClick={() => navigate('/admin/users')}
                    title="View all users"
                  >
                    <ChevronRight />
                  </IconButton>
                </Box>
                <UserListComponent 
                  preview={true} 
                  previewLimit={PREVIEW_LIMIT} 
                  authToken={authToken}
                />
              </Paper>
            </Grid>
            
            {/* Reports Section */}
            <Grid item xs={12} md={6}>
              <Paper sx={{ p: 2, height: '100%' }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                  <Typography variant="h6">Recent Reports</Typography>
                  <IconButton 
                    size="small" 
                    onClick={() => navigate('/admin/reports')}
                    title="View all reports"
                  >
                    <ChevronRight />
                  </IconButton>
                </Box>
                <ReportListComponent 
                  preview={true} 
                  previewLimit={PREVIEW_LIMIT} 
                  authToken={authToken}
                  onReportAction={handleReportAction}
                />
              </Paper>
            </Grid>
          </Grid>
        </>
      )}
    </AdminLayout>
  );
};

export default AdminDashboard;