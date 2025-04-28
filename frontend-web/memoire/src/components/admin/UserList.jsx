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
  Person, 
  Block, 
  CheckCircle, 
  Edit, 
  Delete,
  Visibility,
  AdminPanelSettings,
  OpenInNew
} from '@mui/icons-material';
import AdminLayout from './AdminLayout';
import { useAuth } from '../AuthProvider';

const UserListComponent = ({ 
  preview = false, 
  previewLimit = 5, 
  authToken,
  onUserAction
}) => {
  const [users, setUsers] = useState([]);
  const [filteredUsers, setFilteredUsers] = useState([]);
  const [displayedUsers, setDisplayedUsers] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedUser, setSelectedUser] = useState(null);
  const [pendingReportsCount, setPendingReportsCount] = useState(0);
  
  const navigate = useNavigate();
  const auth = useAuth();
  
  // If not in preview mode, use authToken from auth context
  const token = authToken || (auth ? auth.authToken : null);
  
  useEffect(() => {
    fetchUsers();
    if (!preview) {
      fetchPendingReportsCount();
    }
  }, [token, preview]);
  
  // Filter users when search term changes
  useEffect(() => {
    if (searchTerm.trim() === '') {
      setFilteredUsers(users);
    } else {
      const lowercasedSearch = searchTerm.toLowerCase();
      const filtered = users.filter(user => 
        user.username.toLowerCase().includes(lowercasedSearch) ||
        user.email.toLowerCase().includes(lowercasedSearch) ||
        (user.name && user.name.toLowerCase().includes(lowercasedSearch))
      );
      setFilteredUsers(filtered);
    }
    
    // If preview mode, limit the displayed users
    const usersToDisplay = preview ? 
      filteredUsers.slice(0, previewLimit) : 
      filteredUsers;
      
    setDisplayedUsers(usersToDisplay);
  }, [searchTerm, users, filteredUsers, preview, previewLimit]);
  
  const fetchUsers = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await fetch('https://20250428t092311-dot-memoire-it342.as.r.appspot.com/api/users/admin/dashboard', {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to fetch users');
      }
      
      const data = await response.json();
      setUsers(data);
      setFilteredUsers(data);
      
      // If preview mode, limit the displayed users
      const usersToDisplay = preview ? 
        data.slice(0, previewLimit) : 
        data;
        
      setDisplayedUsers(usersToDisplay);
    } catch (err) {
      setError(err.message || 'An error occurred while fetching users');
      console.error('Error fetching users:', err);
    } finally {
      setLoading(false);
    }
  };
  
  const fetchPendingReportsCount = async () => {
    try {
      const response = await fetch('https://20250428t092311-dot-memoire-it342.as.r.appspot.com/api/reports', {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (!response.ok) {
        return;
      }
      
      const reports = await response.json();
      const pendingCount = reports.filter(report => report.status === 'PENDING').length;
      setPendingReportsCount(pendingCount);
    } catch (err) {
      console.error('Error fetching pending reports count:', err);
    }
  };
  
  const handleSearchChange = (event) => {
    setSearchTerm(event.target.value);
  };
  
  const handleRefresh = () => {
    fetchUsers();
  };
  
  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };
  
  const handleUserAction = (user, action) => {
    setSelectedUser(user);
    // This would be handled by the parent component
    if (onUserAction) onUserAction(user, action);
  };
  
  const viewUserDetails = (userId) => {
    navigate(`/admin/users/${userId}`);
  };
  
  const navigateToFullList = () => {
    navigate('/admin/users');
  };
  
  // Content for the component
  const content = (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h5" component="h2">
          {preview ? 'Recent Users' : 'All Users'}
        </Typography>
        
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button 
            variant="outlined" 
            color="primary" 
            startIcon={<Person />}
            onClick={() => navigate('/admin/users/new')}
          >
            Create User
          </Button>
          
          {preview && (
            <Button 
              variant="contained" 
              color="primary"
              endIcon={<OpenInNew />}
              onClick={navigateToFullList}
            >
              See All Users
            </Button>
          )}
        </Box>
      </Box>
      
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <TextField
          label="Search Users"
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
                <TableCell>Username</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Role</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Joined</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {displayedUsers.length > 0 ? (
                displayedUsers.map((user) => (
                  <TableRow key={user.id} hover>
                    <TableCell>{user.id}</TableCell>
                    <TableCell>{user.username}</TableCell>
                    <TableCell>{user.email}</TableCell>
                    <TableCell>
                      {user.role === 'ROLE_ADMIN' ? (
                        <Chip 
                          color="primary" 
                          size="small" 
                          icon={<AdminPanelSettings />} 
                          label="Admin" 
                        />
                      ) : (
                        <Chip 
                          color="default" 
                          size="small" 
                          icon={<Person />} 
                          label="User" 
                        />
                      )}
                    </TableCell>
                    <TableCell>
                      {user.active ? (
                        <Chip 
                          color="success" 
                          size="small" 
                          icon={<CheckCircle />} 
                          label="Active" 
                        />
                      ) : (
                        <Chip 
                          color="error" 
                          size="small" 
                          icon={<Block />} 
                          label="Disabled" 
                        />
                      )}
                    </TableCell>
                    <TableCell>{formatDate(user.createdAt)}</TableCell>
                    <TableCell>
                      <IconButton 
                        size="small" 
                        color="primary"
                        title="View Details"
                        onClick={() => viewUserDetails(user.id)}
                      >
                        <Visibility fontSize="small" />
                      </IconButton>
                      
                      <IconButton 
                        size="small" 
                        color="secondary"
                        title="Edit User"
                        onClick={() => navigate(`/admin/users/${user.id}/edit`)}
                      >
                        <Edit fontSize="small" />
                      </IconButton>
                      
                      {user.active ? (
                        <IconButton 
                          size="small" 
                          color="error"
                          title="Disable User"
                          onClick={() => handleUserAction(user, 'user-disable')}
                        >
                          <Block fontSize="small" />
                        </IconButton>
                      ) : (
                        <IconButton 
                          size="small" 
                          color="success"
                          title="Enable User"
                          onClick={() => handleUserAction(user, 'user-enable')}
                        >
                          <CheckCircle fontSize="small" />
                        </IconButton>
                      )}
                      
                      <IconButton 
                        size="small" 
                        color="error"
                        title="Delete User"
                        onClick={() => handleUserAction(user, 'user-delete')}
                      >
                        <Delete fontSize="small" />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={7} align="center">
                    {searchTerm ? 'No users match your search' : 'No users found'}
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Box>
  );

  // If in preview mode, just return the content
  if (preview) {
    return content;
  }
  
  // If not in preview mode, wrap with the admin layout
  return (
    <AdminLayout 
      title="User Management" 
      pendingReportsCount={pendingReportsCount}
    >
      {content}
    </AdminLayout>
  );
};

export default UserListComponent;