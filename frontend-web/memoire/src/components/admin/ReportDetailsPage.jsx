import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Paper,
  Typography,
  Grid,
  Divider,
  Chip,
  Button,
  IconButton,
  Alert,
  CircularProgress,
  Card,
  CardContent,
  CardHeader,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Snackbar
} from '@mui/material';
import {
  CheckCircle,
  Block,
  Delete,
  ArrowBack,
  Person,
  CalendarToday,
  Flag
} from '@mui/icons-material';

const ReportDetailsPage = () => {
  const { reportId } = useParams();
  const navigate = useNavigate();
  
  const [report, setReport] = useState(null);
  const [reportedEntity, setReportedEntity] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [confirmDialogOpen, setConfirmDialogOpen] = useState(false);
  const [confirmAction, setConfirmAction] = useState(null);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success'
  });

  // Retrieve auth token from localStorage
  const authToken = sessionStorage.getItem('authToken');

  useEffect(() => {
    fetchReportDetails();
  }, [reportId]);

  const fetchReportDetails = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await fetch(`http://localhost:8080/api/reports/${reportId}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (!response.ok) {
        throw new Error('Failed to fetch report details. Please ensure you have admin privileges.');
      }
      
      const data = await response.json();
      setReport(data.report);
      setReportedEntity(data.reportedEntity);
    } catch (err) {
      setError(err.message || 'An error occurred while fetching report details');
      console.error('Error fetching report details:', err);
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

  const openConfirmDialog = (action) => {
    setConfirmAction(action);
    setConfirmDialogOpen(true);
  };

  const closeConfirmDialog = () => {
    setConfirmDialogOpen(false);
    setConfirmAction(null);
  };

  const executeAction = async () => {
    if (!confirmAction) {
      closeConfirmDialog();
      return;
    }

    try {
      let response;
      let message = '';

      switch (confirmAction) {
        case 'report-approve':
          response = await fetch(`http://localhost:8080/api/reports/${reportId}/status?status=APPROVED`, {
            method: 'PUT',
            headers: {
              'Authorization': `Bearer ${authToken}`,
              'Content-Type': 'application/json'
            }
          });
          message = 'Report approved successfully';
          break;
        
        case 'report-reject':
          response = await fetch(`http://localhost:8080/api/reports/${reportId}/status?status=REJECTED`, {
            method: 'PUT',
            headers: {
              'Authorization': `Bearer ${authToken}`,
              'Content-Type': 'application/json'
            }
          });
          message = 'Report rejected successfully';
          break;
        
        case 'report-delete':
          response = await fetch(`http://localhost:8080/api/reports/${reportId}`, {
            method: 'DELETE',
            headers: {
              'Authorization': `Bearer ${authToken}`
            }
          });
          message = 'Report deleted successfully';
          
          // Redirect to reports list after successful deletion
          setSnackbar({
            open: true,
            message,
            severity: 'success'
          });
          
          setTimeout(() => {
            navigate('/admin/reports');
          }, 1500);
          
          closeConfirmDialog();
          return;
        
        default:
          throw new Error('Invalid action');
      }

      if (!response.ok) {
        throw new Error('Failed to perform action');
      }

      // Show success message
      setSnackbar({
        open: true,
        message,
        severity: 'success'
      });

      // Refresh report details
      fetchReportDetails();
    } catch (err) {
      setSnackbar({
        open: true,
        message: err.message || 'An error occurred',
        severity: 'error'
      });
    } finally {
      closeConfirmDialog();
    }
  };

  const handleCloseSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  const getDialogMessage = () => {
    switch (confirmAction) {
      case 'report-approve':
        return `Are you sure you want to approve report #${reportId}? This will mark the report as APPROVED.`;
      case 'report-reject':
        return `Are you sure you want to reject report #${reportId}? This will mark the report as REJECTED.`;
      case 'report-delete':
        return `Are you sure you want to delete report #${reportId}? This action cannot be undone.`;
      default:
        return '';
    }
  };

  const getDialogTitle = () => {
    switch (confirmAction) {
      case 'report-approve': return 'Approve Report';
      case 'report-reject': return 'Reject Report';
      case 'report-delete': return 'Delete Report';
      default: return 'Confirm Action';
    }
  };

  const renderReportedEntityDetails = () => {
    if (!reportedEntity) return null;

    // Handle TimeCapsule type
    if (report.itemType === 'TIMECAPSULE') {
      return (
        <Card sx={{ mt: 3 }} elevation={2}>
          <CardHeader 
            title="Time Capsule Details" 
            sx={{ backgroundColor: '#f5f5f5' }}
          />
          <CardContent>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <Typography variant="subtitle2">ID:</Typography>
                <Typography variant="body1">{reportedEntity.id}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="subtitle2">Creator:</Typography>
                <Typography variant="body1">{reportedEntity.creator?.username || 'Unknown'}</Typography>
              </Grid>
              <Grid item xs={12}>
                <Typography variant="subtitle2">Title:</Typography>
                <Typography variant="body1">{reportedEntity.title}</Typography>
              </Grid>
              <Grid item xs={12}>
                <Typography variant="subtitle2">Description:</Typography>
                <Typography variant="body1">{reportedEntity.description || 'No description'}</Typography>
              </Grid>
              <Grid item xs={12}>
                <Typography variant="subtitle2">Created Date:</Typography>
                <Typography variant="body1">{formatDate(reportedEntity.createDate)}</Typography>
              </Grid>
              <Grid item xs={12}>
                <Typography variant="subtitle2">Unlocked Date:</Typography>
                <Typography variant="body1">{formatDate(reportedEntity.unlockDate)}</Typography>
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      );
    }
    
    // Handle Comment type
    if (report.itemType === 'COMMENT') {
      return (
        <Card sx={{ mt: 3 }} elevation={2}>
          <CardHeader 
            title="Comment Details" 
            sx={{ backgroundColor: '#f5f5f5' }}
          />
          <CardContent>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <Typography variant="subtitle2">ID:</Typography>
                <Typography variant="body1">{reportedEntity.id}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="subtitle2">Author:</Typography>
                <Typography variant="body1">{reportedEntity.author?.username || 'Unknown'}</Typography>
              </Grid>
              <Grid item xs={12}>
                <Typography variant="subtitle2">Content:</Typography>
                <Typography variant="body1">{reportedEntity.content || 'No content'}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="subtitle2">Created Date:</Typography>
                <Typography variant="body1">{formatDate(reportedEntity.createDate)}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="subtitle2">Parent Type:</Typography>
                <Typography variant="body1">{reportedEntity.parentType || 'N/A'}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="subtitle2">Parent ID:</Typography>
                <Typography variant="body1">{reportedEntity.parentId || 'N/A'}</Typography>
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      );
    }
    
    // Generic fallback for other entity types
    return (
      <Card sx={{ mt: 3 }} elevation={2}>
        <CardHeader 
          title={`${report.itemType} Details`} 
          sx={{ backgroundColor: '#f5f5f5' }}
        />
        <CardContent>
          <Typography variant="body2" component="pre" sx={{ whiteSpace: 'pre-wrap' }}>
            {JSON.stringify(reportedEntity, null, 2)}
          </Typography>
        </CardContent>
      </Card>
    );
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Box sx={{ p: 3, maxWidth: '1000px', margin: '0 auto' }}>
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

  if (!report) {
    return (
      <Box sx={{ p: 3, maxWidth: '1000px', margin: '0 auto' }}>
        <Alert severity="warning" sx={{ mb: 2 }}>
          Report not found
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
    <Box sx={{ p: 3, maxWidth: '1000px', margin: '0 auto' }}>
      <Button 
        startIcon={<ArrowBack />} 
        variant="outlined" 
        onClick={handleBackToList}
        sx={{ mb: 3 }}
      >
        Back to Reports List
      </Button>
      
      <Paper elevation={2} sx={{ p: 3, mb: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h5" component="h1">
            Report #{report.id}
          </Typography>
          <Chip 
            color={
              report.status === 'PENDING' ? 'warning' :
              report.status === 'APPROVED' ? 'success' :
              report.status === 'REJECTED' ? 'error' : 'default'
            } 
            label={report.status} 
            icon={<Flag />}
          />
        </Box>
        
        <Divider sx={{ mb: 3 }} />
        
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Box sx={{ display: 'flex', alignItems: 'flex-start', mb: 2 }}>
              <Person sx={{ mr: 1, color: 'text.secondary' }} />
              <Box>
                <Typography variant="body2" color="text.secondary">
                  Reporter
                </Typography>
                <Typography variant="body1">
                  {report.reporter?.username || 'Unknown'} (ID: {report.reporter?.id || 'N/A'})
                </Typography>
              </Box>
            </Box>
          </Grid>
          
          <Grid item xs={12} md={6}>
            <Box sx={{ display: 'flex', alignItems: 'flex-start', mb: 2 }}>
              <CalendarToday sx={{ mr: 1, color: 'text.secondary' }} />
              <Box>
                <Typography variant="body2" color="text.secondary">
                  Reported Date
                </Typography>
                <Typography variant="body1">
                  {formatDate(report.createdAt || report.createdDate)}
                </Typography>
              </Box>
            </Box>
          </Grid>
          
          <Grid item xs={12} md={6}>
            <Typography variant="body2" color="text.secondary">
              Item Type
            </Typography>
            <Typography variant="body1">
              {report.itemType}
            </Typography>
          </Grid>
          
          <Grid item xs={12} md={6}>
            <Typography variant="body2" color="text.secondary">
              Reported Item ID
            </Typography>
            <Typography variant="body1">
              {report.reportedID}
            </Typography>
          </Grid>
        </Grid>
        
        {report.status === 'PENDING' && (
          <Box sx={{ display: 'flex', gap: 2, mt: 3 }}>
            <Button
              variant="contained"
              color="success"
              startIcon={<CheckCircle />}
              onClick={() => openConfirmDialog('report-approve')}
            >
              Approve Report
            </Button>
            
            <Button
              variant="contained"
              color="error"
              startIcon={<Block />}
              onClick={() => openConfirmDialog('report-reject')}
            >
              Reject Report
            </Button>
          </Box>
        )}
        
        <Box sx={{ mt: report.status === 'PENDING' ? 2 : 3 }}>
          <Button
            variant="outlined"
            color="error"
            startIcon={<Delete />}
            onClick={() => openConfirmDialog('report-delete')}
          >
            Delete Report
          </Button>
        </Box>
      </Paper>
      
      {renderReportedEntityDetails()}
      
      {/* Confirmation Dialog */}
      <Dialog
        open={confirmDialogOpen}
        onClose={closeConfirmDialog}
      >
        <DialogTitle>{getDialogTitle()}</DialogTitle>
        <DialogContent>
          <DialogContentText>
            {getDialogMessage()}
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={closeConfirmDialog} color="primary">
            Cancel
          </Button>
          <Button 
            onClick={executeAction} 
            color={confirmAction === 'report-delete' ? 'error' : 'primary'}
            variant="contained"
            autoFocus
          >
            Confirm
          </Button>
        </DialogActions>
      </Dialog>

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
    </Box>
  );
};

export default ReportDetailsPage;