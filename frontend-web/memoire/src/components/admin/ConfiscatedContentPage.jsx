import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Paper,
  Typography,
  Grid,
  Divider,
  Button,
  Alert,
  CircularProgress,
  Card,
  CardContent,
  CardHeader,
  List,
  Tabs,
  Tab,
  Snackbar
} from '@mui/material';
import {
  ArrowBack,
} from '@mui/icons-material';
import AdminLayout from './AdminLayout';

const ConfiscatedContentPage = () => {
  const navigate = useNavigate();
  
  const [confiscatedContent, setConfiscatedContent] = useState({
    confiscatedCapsules: [],
    confiscatedComments: []
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [tab, setTab] = useState(0);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success'
  });
  
  // For the layout - assuming you'll get this from an API in a real implementation
  const [pendingReportsCount, setPendingReportsCount] = useState(0);

  // Retrieve auth token from localStorage
  const authToken = sessionStorage.getItem('authToken');

  useEffect(() => {
    fetchConfiscatedContent();
    // In a real implementation, you might want to fetch pending reports count here
    // For now, we'll just set a dummy value
    setPendingReportsCount(5);
  }, []);

  const fetchConfiscatedContent = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await fetch('https://memoire-it342.as.r.appspot.com/api/admin/reports/confiscated-content', {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (!response.ok) {
        throw new Error('Failed to fetch confiscated content. Please ensure you have admin privileges.');
      }
      
      const data = await response.json();
      setConfiscatedContent(data);
    } catch (err) {
      setError(err.message || 'An error occurred while fetching confiscated content');
      console.error('Error fetching confiscated content:', err);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const handleBackToList = () => {
    navigate('/admin/reports');
  };

  const handleChangeTab = (event, newValue) => {
    setTab(newValue);
  };

  const handleViewTimeCapsule = (id) => {
    navigate(`/time-capsules/${id}`);
  };

  const handleCloseSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  const renderContent = () => {
    if (loading) {
      return (
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
          <CircularProgress />
        </Box>
      );
    }

    if (error) {
      return (
        <Box sx={{ maxWidth: '1200px', margin: '0 auto' }}>
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
          <Button 
            startIcon={<ArrowBack />} 
            variant="outlined" 
            onClick={handleBackToList}
          >
            Back to Reports List
          </Button>
        </Box>
      );
    }

    return (
      <Box sx={{ maxWidth: '1200px', margin: '0 auto' }}>
        <Button 
          startIcon={<ArrowBack />} 
          variant="outlined" 
          onClick={handleBackToList}
          sx={{ mb: 3 }}
        >
          Back to Reports List
        </Button>
        
        <Paper elevation={2} sx={{ p: 3, mb: 3 }}>
          <Typography variant="h5" component="h1" sx={{ mb: 2 }}>
            Confiscated Content
          </Typography>
          
          <Divider sx={{ mb: 3 }} />
          
          <Tabs value={tab} onChange={handleChangeTab} sx={{ mb: 3 }}>
            <Tab label={`Time Capsules (${confiscatedContent.confiscatedCapsules.length})`} />
            <Tab label={`Comments (${confiscatedContent.confiscatedComments.length})`} />
          </Tabs>
          
          {tab === 0 && (
            <>
              {confiscatedContent.confiscatedCapsules.length === 0 ? (
                <Alert severity="info">No confiscated time capsules found.</Alert>
              ) : (
                <List>
                  {confiscatedContent.confiscatedCapsules.map((capsule) => (
                    <Card key={capsule.id} sx={{ mb: 2 }}>
                      <CardHeader 
                        title={capsule.title} 
                        subheader={`Created: ${formatDate(capsule.createDate)}`}
                        sx={{ backgroundColor: '#f5f5f5' }}
                        action={
                          <Button 
                            size="small"
                            onClick={() => handleViewTimeCapsule(capsule.id)}
                          >
                            View Details
                          </Button>
                        }
                      />
                      <CardContent>
                        <Grid container spacing={2}>
                          <Grid item xs={12}>
                            <Typography variant="body2" color="text.secondary">
                              Description
                            </Typography>
                            <Typography variant="body1">
                              {capsule.description || "No description provided"}
                            </Typography>
                          </Grid>
                          <Grid item xs={12} sm={6}>
                            <Typography variant="body2" color="text.secondary">
                              ID
                            </Typography>
                            <Typography variant="body1">
                              {capsule.id}
                            </Typography>
                          </Grid>
                          <Grid item xs={12} sm={6}>
                            <Typography variant="body2" color="text.secondary">
                              Original Owner
                            </Typography>
                            <Typography variant="body1">
                              {capsule.originalOwner || "Unknown"}
                            </Typography>
                          </Grid>
                          <Grid item xs={12} sm={6}>
                            <Typography variant="body2" color="text.secondary">
                              Status
                            </Typography>
                            <Typography variant="body1">
                              {capsule.status}
                            </Typography>
                          </Grid>
                          <Grid item xs={12} sm={6}>
                            <Typography variant="body2" color="text.secondary">
                              Unlock Date
                            </Typography>
                            <Typography variant="body1">
                              {formatDate(capsule.unlockDate)}
                            </Typography>
                          </Grid>
                        </Grid>
                      </CardContent>
                    </Card>
                  ))}
                </List>
              )}
            </>
          )}
          
          {tab === 1 && (
            <>
              {confiscatedContent.confiscatedComments.length === 0 ? (
                <Alert severity="info">No confiscated comments found.</Alert>
              ) : (
                <List>
                  {confiscatedContent.confiscatedComments.map((comment) => (
                    <Card key={comment.id} sx={{ mb: 2 }}>
                      <CardHeader 
                        title={`Comment #${comment.id}`} 
                        subheader={`Created: ${formatDate(comment.createdAt || comment.createDate)}`}
                        sx={{ backgroundColor: '#f5f5f5' }}
                      />
                      <CardContent>
                        <Grid container spacing={2}>
                          <Grid item xs={12}>
                            <Typography variant="body2" color="text.secondary">
                              Content
                            </Typography>
                            <Typography variant="body1">
                              {comment.text || comment.content}
                            </Typography>
                          </Grid>
                          <Grid item xs={12} sm={6}>
                            <Typography variant="body2" color="text.secondary">
                              ID
                            </Typography>
                            <Typography variant="body1">
                              {comment.id}
                            </Typography>
                          </Grid>
                          <Grid item xs={12} sm={6}>
                            <Typography variant="body2" color="text.secondary">
                              Parent ID
                            </Typography>
                            <Typography variant="body1">
                              {comment.parentId || comment.timeCapsuleId || "Unknown"}
                            </Typography>
                          </Grid>
                          <Grid item xs={12} sm={6}>
                            <Typography variant="body2" color="text.secondary">
                              Parent Type
                            </Typography>
                            <Typography variant="body1">
                              {comment.parentType || "TIME_CAPSULE"}
                            </Typography>
                          </Grid>
                        </Grid>
                      </CardContent>
                    </Card>
                  ))}
                </List>
              )}
            </>
          )}
        </Paper>
      </Box>
    );
  };

  return (
    <AdminLayout 
      title="Confiscated Content"
      pendingReportsCount={pendingReportsCount}
    >
      {renderContent()}
      
      {/* Snackbar for notifications */}
      <Snackbar 
        open={snackbar.open} 
        autoHideDuration={6000} 
        onClose={handleCloseSnackbar}
      >
        <Alert 
          onClose={handleCloseSnackbar} 
          severity={snackbar.severity} 
          sx={{ width: '100%' }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </AdminLayout>
  );
};

export default ConfiscatedContentPage;