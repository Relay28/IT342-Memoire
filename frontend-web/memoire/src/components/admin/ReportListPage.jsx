import React, { useState, useEffect } from 'react';
import { 
  Table, 
  TableBody, 
  TableCell, 
  TableContainer, 
  TableHead, 
  TableRow, 
  Paper, 
  Typography, 
  Box, 
  IconButton, 
  Chip, 
  TextField, 
  InputAdornment,
  Button,
  Alert,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Snackbar
} from '@mui/material';
import { 
  Search, 
  Refresh, 
  Block, 
  CheckCircle, 
  Delete,
  Visibility
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import AdminLayout from './AdminLayout';

const ReportsListPage = () => {
  const [reports, setReports] = useState([]);
  const [filteredReports, setFilteredReports] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [confirmDialogOpen, setConfirmDialogOpen] = useState(false);
  const [confirmAction, setConfirmAction] = useState(null);
  const [selectedReport, setSelectedReport] = useState(null);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success'
  });
  const [pendingReportsCount, setPendingReportsCount] = useState(0);
  
  const navigate = useNavigate();

  // Retrieve auth token from localStorage
  const authToken = sessionStorage.getItem('authToken');

  useEffect(() => {
    fetchReports();
  }, []);
  
  useEffect(() => {
    if (searchTerm.trim() === '') {
      setFilteredReports(reports);
    } else {
      const lowercasedSearch = searchTerm.toLowerCase();
      const filtered = reports.filter(report => 
        report.itemType.toLowerCase().includes(lowercasedSearch) ||
        report.status.toLowerCase().includes(lowercasedSearch) ||
        report.reportedID.toString().includes(lowercasedSearch) ||
        (report.reporter?.username && report.reporter.username.toLowerCase().includes(lowercasedSearch))
      );
      setFilteredReports(filtered);
    }
  }, [searchTerm, reports]);
  
  // Count pending reports for sidebar badge
  useEffect(() => {
    if (reports && reports.length > 0) {
      const pendingCount = reports.filter(report => report.status === 'PENDING').length;
      setPendingReportsCount(pendingCount);
    }
  }, [reports]);
  
  const fetchReports = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await fetch('https://20250428t092311-dot-memoire-it342.as.r.appspot.com/api/reports/getAll', {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (!response.ok) {
        throw new Error('Failed to fetch reports. Please ensure you have admin privileges.');
      }
      
      const data = await response.json();
      setReports(data);
      setFilteredReports(data);
    } catch (err) {
      setError(err.message || 'An error occurred while fetching reports');
      console.error('Error fetching reports:', err);
    } finally {
      setLoading(false);
    }
  };
  
  const handleSearchChange = (event) => {
    setSearchTerm(event.target.value);
  };
  
  const handleRefresh = () => {
    fetchReports();
  };
  
  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };
  
  const viewReportDetails = (reportId) => {
    navigate(`/admin/reports/${reportId}`);
  };

  const openConfirmDialog = (report, action) => {
    setSelectedReport(report);
    setConfirmAction(action);
    setConfirmDialogOpen(true);
  };

  const closeConfirmDialog = () => {
    setConfirmDialogOpen(false);
    setSelectedReport(null);
    setConfirmAction(null);
  };

  const executeAction = async () => {
    if (!selectedReport || !confirmAction) {
      closeConfirmDialog();
      return;
    }

    try {
      let response;
      let message = '';

      switch (confirmAction) {
        case 'report-approve':
          response = await fetch(`https://20250428t092311-dot-memoire-it342.as.r.appspot.com/api/reports/${selectedReport.id}/status?status=APPROVED`, {
            method: 'PUT',
            headers: {
              'Authorization': `Bearer ${authToken}`,
              'Content-Type': 'application/json'
            }
          });
          message = 'Report approved successfully';
          break;
        
        case 'report-reject':
          response = await fetch(`https://20250428t092311-dot-memoire-it342.as.r.appspot.com/api/reports/${selectedReport.id}/status?status=REJECTED`, {
            method: 'PUT',
            headers: {
              'Authorization': `Bearer ${authToken}`,
              'Content-Type': 'application/json'
            }
          });
          message = 'Report rejected successfully';
          break;
        
        case 'report-delete':
          response = await fetch(`https://20250428t092311-dot-memoire-it342.as.r.appspot.com/api/reports/${selectedReport.id}`, {
            method: 'DELETE',
            headers: {
              'Authorization': `Bearer ${authToken}`
            }
          });
          message = 'Report deleted successfully';
          break;
        
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

      // Refresh reports list
      fetchReports();
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
    if (!selectedReport) return '';

    switch (confirmAction) {
      case 'report-approve':
        return `Are you sure you want to approve report #${selectedReport.id}? This will mark the report as APPROVED.`;
      case 'report-reject':
        return `Are you sure you want to reject report #${selectedReport.id}? This will mark the report as REJECTED.`;
      case 'report-delete':
        return `Are you sure you want to delete report #${selectedReport.id}? This action cannot be undone.`;
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

  // Content for the reports list page
  const reportsContent = (
    <>
      <Typography variant="h4" component="h1" sx={{ mb: 3 }}>
        Reports Management
      </Typography>
      
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <TextField
          label="Search Reports"
          variant="outlined"
          size="small"
          value={searchTerm}
          onChange={handleSearchChange}
          sx={{ width: '40%' }}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <Search />
              </InputAdornment>
            ),
          }}
        />
        
        <Button 
          variant="outlined"
          startIcon={<Refresh />}
          onClick={handleRefresh}
        >
          Refresh
        </Button>
      </Box>
      
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <TableContainer component={Paper} elevation={2}>
          <Table>
            <TableHead sx={{ backgroundColor: '#f5f5f5' }}>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Reported ID</TableCell>
                <TableCell>Item Type</TableCell>
                <TableCell>Reporter</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Date</TableCell>
                <TableCell align="center">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredReports.length > 0 ? (
                filteredReports.map((report) => (
                  <TableRow key={report.id} hover>
                    <TableCell>{report.id}</TableCell>
                    <TableCell>{report.reportedID}</TableCell>
                    <TableCell>{report.itemType}</TableCell>
                    <TableCell>{report.reporter?.username || report.reporterId}</TableCell>
                    <TableCell>
                      <Chip 
                        color={
                          report.status === 'PENDING' ? 'warning' :
                          report.status === 'APPROVED' ? 'success' :
                          report.status === 'REJECTED' ? 'error' : 'default'
                        } 
                        size="small" 
                        label={report.status} 
                      />
                    </TableCell>
                    <TableCell>{formatDate(report.createdAt || report.createdDate || report.date)}</TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', justifyContent: 'center' }}>
                        <IconButton 
                          size="small" 
                          color="primary"
                          title="View Details"
                          onClick={() => viewReportDetails(report.id)}
                        >
                          <Visibility fontSize="small" />
                        </IconButton>
                        
                        {report.status === 'PENDING' && (
                          <>
                            <IconButton 
                              size="small" 
                              color="success"
                              title="Approve Report"
                              onClick={() => openConfirmDialog(report, 'report-approve')}
                            >
                              <CheckCircle fontSize="small" />
                            </IconButton>
                            
                            <IconButton 
                              size="small" 
                              color="error"
                              title="Reject Report"
                              onClick={() => openConfirmDialog(report, 'report-reject')}
                            >
                              <Block fontSize="small" />
                            </IconButton>
                          </>
                        )}
                        
                        <IconButton 
                          size="small" 
                          color="error"
                          title="Delete Report"
                          onClick={() => openConfirmDialog(report, 'report-delete')}
                        >
                          <Delete fontSize="small" />
                        </IconButton>
                      </Box>
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={7} align="center">
                    {searchTerm ? 'No reports match your search' : 'No reports found'}
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

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
    </>
  );

  return (
    <AdminLayout title="Reports Management" pendingReportsCount={pendingReportsCount}>
      {reportsContent}
    </AdminLayout>
  );
};

export default ReportsListPage;