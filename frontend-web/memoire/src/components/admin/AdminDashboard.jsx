import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Box, 
  Typography,
  Paper,
  Grid,
  Card,
  CardContent,
  CardHeader,
  Button,
  Divider,
  CircularProgress
} from '@mui/material';
import { 
  AdminPanelSettings,
  Group,
  Flag,
  OpenInNew,
  Dashboard
} from '@mui/icons-material';
import UserListComponent from './UserList';
import ReportListComponent from './ReportList';
import { useAuth } from '../AuthProvider';

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
      // This would typically be a separate endpoint
      // For now we'll simulate it with the data we have
      
      // Fetch users for stats
      const usersResponse = await fetch('http://localhost:8080/api/users/admin/dashboard', {
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
      const reportsResponse = await fetch('http://localhost:8080/api/reports', {
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
    <Box sx={{ padding: 3 }}>
      <Typography variant="h4" component="h1" sx={{ mb: 3, display: 'flex', alignItems: 'center' }}>
        <AdminPanelSettings sx={{ mr: 1, fontSize: 32 }} />
        Admin Dashboard
      </Typography>
      
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <>
          {/* Dashboard Summary Stats */}
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
              <Card sx={{ height: '100%', cursor: 'pointer' }} onClick={() => navigate('/admin/reports')}>
                <CardContent>
                  <Typography variant="h6" component="div" color="warning.main" gutterBottom>
                    Pending Reports
                  </Typography>
                  <Typography variant="h3" component="div" color="warning.main" sx={{ mb: 1.5 }}>
                    {dashboardStats.pendingReports}
                  </Typography>
                  <Typography variant="body2" sx={{ display: 'flex', alignItems: 'center' }}>
                    <Flag sx={{ mr: 0.5, fontSize: 20, color: 'warning.main' }} />
                    Click to review
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
          
          {/* Users Section */}
          <Paper sx={{ p: 3, mb: 4 }}>
            <UserListComponent 
              preview={true} 
              previewLimit={PREVIEW_LIMIT} 
              authToken={authToken}
            />
          </Paper>
          
          {/* Reports Section */}
          <Paper sx={{ p: 3 }}>
            <ReportListComponent 
              preview={true} 
              previewLimit={PREVIEW_LIMIT} 
              authToken={authToken}
              onReportAction={handleReportAction}
            />
          </Paper>
        </>
      )}
    </Box>
  );
};

export default AdminDashboard;