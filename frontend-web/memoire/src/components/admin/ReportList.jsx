import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
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
} from '@mui/material';
import { 
  Search, 
  Refresh, 
  Block, 
  CheckCircle, 
  Delete,
  Visibility,
  OpenInNew,
  Flag
} from '@mui/icons-material';

const ReportListComponent = ({ 
  preview = false, 
  previewLimit = 5, 
  authToken,
  onReportAction
}) => {
  const [reports, setReports] = useState([]);
  const [filteredReports, setFilteredReports] = useState([]);
  const [displayedReports, setDisplayedReports] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const navigate = useNavigate();
  
  useEffect(() => {
    fetchReports();
  }, [authToken]);
  
  // Filter reports when search term changes
  useEffect(() => {
    if (searchTerm.trim() === '') {
      setFilteredReports(reports);
    } else {
      const lowercasedSearch = searchTerm.toLowerCase();
      const filtered = reports.filter(report => 
        report.itemType.toLowerCase().includes(lowercasedSearch) ||
        report.status.toLowerCase().includes(lowercasedSearch) ||
        report.reportedID.toString().includes(lowercasedSearch)
      );
      setFilteredReports(filtered);
    }
    
    // If preview mode, limit the displayed reports
    const reportsToDisplay = preview ? 
      filteredReports.slice(0, previewLimit) : 
      filteredReports;
      
    setDisplayedReports(reportsToDisplay);
  }, [searchTerm, reports, filteredReports, preview, previewLimit]);
  
  const fetchReports = async () => {
    setLoading(true);
    setError(null);
    
    try {
      // Using the endpoint from ReportController.java
      const response = await fetch('https://memoire-it342.as.r.appspot.com/api/reports/getAll', {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to fetch reports');
      }
      
      const data = await response.json();
      setReports(data);
      setFilteredReports(data);
      
      // If preview mode, limit the displayed reports
      const reportsToDisplay = preview ? 
        data.slice(0, previewLimit) : 
        data;
        
      setDisplayedReports(reportsToDisplay);
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
  console.log(reports)
  
  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };
  
  const handleReportAction = (report, action) => {
    if (onReportAction) {
      onReportAction(report, action);
    }
  };
  
  const viewReportDetails = (reportId) => {
    navigate(`/admin/reports/${reportId}`);
  };
  
  const navigateToFullList = () => {
    navigate('/admin/reports');
  };


  return (
    
    <Box>
        
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h5" component="h2">
          {preview ? 'Recent Reports' : 'All Reports'}
        </Typography>
        
        {preview && (
          <Button 
            variant="contained" 
            color="primary"
            endIcon={<OpenInNew />}
            onClick={navigateToFullList}
          >
            See All Reports
          </Button>
        )}
      </Box>
      
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
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
        <TableContainer component={Paper}>
          <Table>
            <TableHead sx={{ backgroundColor: '#f5f5f5' }}>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Reported ID</TableCell>
                <TableCell>Item Type</TableCell>
                <TableCell>Reporter</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Date</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {displayedReports.length > 0 ? (
                displayedReports.map((report) => (
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
                    <TableCell>{formatDate(report.date)}</TableCell>
                    <TableCell>
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
                            onClick={() => handleReportAction(report, 'report-approve')}
                          >
                            <CheckCircle fontSize="small" />
                          </IconButton>
                          
                          <IconButton 
                            size="small" 
                            color="error"
                            title="Reject Report"
                            onClick={() => handleReportAction(report, 'report-reject')}
                          >
                            <Block fontSize="small" />
                          </IconButton>
                        </>
                      )}
                      
                      <IconButton 
                        size="small" 
                        color="error"
                        title="Delete Report"
                        onClick={() => handleReportAction(report, 'report-delete')}
                      >
                        <Delete fontSize="small" />
                      </IconButton>
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
    </Box>
  );
};

export default ReportListComponent;