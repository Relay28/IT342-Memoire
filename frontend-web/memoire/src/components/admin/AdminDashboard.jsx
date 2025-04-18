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
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Alert,
  CircularProgress
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
  AdminPanelSettings
} from '@mui/icons-material';
import { useAuth } from '../AuthProvider';

const AdminDashboard = () => {
  const [users, setUsers] = useState([]);
  const [filteredUsers, setFilteredUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedUser, setSelectedUser] = useState(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [dialogAction, setDialogAction] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  
  const navigate = useNavigate();
  const { user, authToken } = useAuth();
  
  // Fetch users on component mount
  useEffect(() => {
    // Check if logged in user is admin
    if (!user || !authToken || user.role !== 'ROLE_ADMIN') {
      navigate('/admin/login');
      return;
    }
    
    fetchUsers();
  }, [user, authToken, navigate]);
  
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
  }, [searchTerm, users]);
  
  const fetchUsers = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await fetch('http://localhost:8080/api/users/admin/dashboard', {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${authToken}`,
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
    } catch (err) {
      setError(err.message || 'An error occurred while fetching users');
      console.error('Error fetching users:', err);
    } finally {
      setLoading(false);
    }
  };
  
  const handleSearchChange = (event) => {
    setSearchTerm(event.target.value);
  };
  
  const handleRefresh = () => {
    fetchUsers();
  };
  
  const formatCreatedAt = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };
  
  const handleUserAction = (user, action) => {
    setSelectedUser(user);
    setDialogAction(action);
    setDialogOpen(true);
  };
  
  const handleDialogClose = () => {
    setDialogOpen(false);
  };
  
  const handleConfirmAction = async () => {
    if (!selectedUser) return;
    
    try {
      let endpoint = '';
      let method = 'PUT';
      let body = {};
      let successMsg = '';
      
      switch (dialogAction) {
        case 'disable':
          endpoint = `/api/users/${selectedUser.id}/disable`;
          method = 'PATCH';
          successMsg = `User ${selectedUser.username} has been disabled`;
          break;
        case 'enable':
          endpoint = `/api/users/${selectedUser.id}`;
          method = 'PUT';
          body = { ...selectedUser, isActive: true };
          successMsg = `User ${selectedUser.username} has been enabled`;
          break;
        case 'delete':
          endpoint = `/api/users/${selectedUser.id}`;
          method = 'DELETE';
          successMsg = `User ${selectedUser.username} has been deleted`;
          break;
        case 'makeAdmin':
          endpoint = `/api/users/${selectedUser.id}`;
          method = 'PUT';
          body = { ...selectedUser, role: 'ROLE_ADMIN' };
          successMsg = `${selectedUser.username} has been promoted to admin`;
          break;
        case 'removeAdmin':
          endpoint = `/api/users/${selectedUser.id}`;
          method = 'PUT';
          body = { ...selectedUser, role: 'ROLE_USER' };
          successMsg = `Admin privileges have been removed from ${selectedUser.username}`;
          break;
        default:
          throw new Error('Unknown action');
      }
      
      const response = await fetch(endpoint, {
        method,
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        },
        body: method !== 'PATCH' && method !== 'DELETE' ? JSON.stringify(body) : undefined
      });
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Action failed');
      }
      
      // Update local state
      fetchUsers();
      setSuccessMessage(successMsg);
      setTimeout(() => setSuccessMessage(''), 5000);
    } catch (err) {
      setError(err.message || 'An error occurred');
      console.error('Error performing action:', err);
    } finally {
      setDialogOpen(false);
    }
  };
  
  const viewUserDetails = (userId) => {
    navigate(`/admin/users/${userId}`);
  };
  
  const renderDialogContent = () => {
    if (!selectedUser) return null;
    
    switch (dialogAction) {
      case 'disable':
        return (
          <>
            <DialogTitle>Disable User Account</DialogTitle>
            <DialogContent>
              <DialogContentText>
                Are you sure you want to disable the account for user "{selectedUser.username}"? 
                This will prevent them from logging in.
              </DialogContentText>
            </DialogContent>
          </>
        );
      case 'enable':
        return (
          <>
            <DialogTitle>Enable User Account</DialogTitle>
            <DialogContent>
              <DialogContentText>
                Are you sure you want to enable the account for user "{selectedUser.username}"?
                This will allow them to log in again.
              </DialogContentText>
            </DialogContent>
          </>
        );
      case 'delete':
        return (
          <>
            <DialogTitle>Delete User Account</DialogTitle>
            <DialogContent>
              <DialogContentText>
                Are you sure you want to permanently delete the user "{selectedUser.username}"?
                This action cannot be undone and all associated data will be lost.
              </DialogContentText>
            </DialogContent>
          </>
        );
      case 'makeAdmin':
        return (
          <>
            <DialogTitle>Promote User to Admin</DialogTitle>
            <DialogContent>
              <DialogContentText>
                Are you sure you want to grant admin privileges to "{selectedUser.username}"?
                This will give them full access to the admin dashboard and all administrative functions.
              </DialogContentText>
            </DialogContent>
          </>
        );
      case 'removeAdmin':
        return (
          <>
            <DialogTitle>Remove Admin Privileges</DialogTitle>
            <DialogContent>
              <DialogContentText>
                Are you sure you want to remove admin privileges from "{selectedUser.username}"?
                They will no longer have access to the admin dashboard.
              </DialogContentText>
            </DialogContent>
          </>
        );
      default:
        return null;
    }
  };
  
  return (
    <Box sx={{ padding: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1" sx={{ display: 'flex', alignItems: 'center' }}>
          <AdminPanelSettings sx={{ mr: 1, fontSize: 32 }} />
          User Management
        </Typography>
        
        <Button 
          variant="contained" 
          color="primary" 
          startIcon={<Person />}
          onClick={() => navigate('/admin/users/new')}
        >
          Create User
        </Button>
      </Box>
      
      {successMessage && (
        <Alert severity="success" sx={{ mb: 2 }}>
          {successMessage}
        </Alert>
      )}
      
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <TextField
          label="Search Users"
          variant="outlined"
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
        <TableContainer component={Paper} sx={{ mt: 2 }}>
          <Table>
            <TableHead sx={{ backgroundColor: '#f5f5f5' }}>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Username</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Name</TableCell>
                <TableCell>Role</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Joined</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredUsers.length > 0 ? (
                filteredUsers.map((user) => (
                  <TableRow key={user.id} hover>
                    <TableCell>{user.id}</TableCell>
                    <TableCell>{user.username}</TableCell>
                    <TableCell>{user.email}</TableCell>
                    <TableCell>{user.name || '-'}</TableCell>
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
                    <TableCell>{formatCreatedAt(user.createdAt)}</TableCell>
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
                          onClick={() => handleUserAction(user, 'disable')}
                        >
                          <Block fontSize="small" />
                        </IconButton>
                      ) : (
                        <IconButton 
                          size="small" 
                          color="success"
                          title="Enable User"
                          onClick={() => handleUserAction(user, 'enable')}
                        >
                          <CheckCircle fontSize="small" />
                        </IconButton>
                      )}
                      
                      {user.role === 'ROLE_ADMIN' ? (
                        <IconButton 
                          size="small" 
                          color="default"
                          title="Remove Admin"
                          onClick={() => handleUserAction(user, 'removeAdmin')}
                        >
                          <Person fontSize="small" />
                        </IconButton>
                      ) : (
                        <IconButton 
                          size="small" 
                          color="primary"
                          title="Make Admin"
                          onClick={() => handleUserAction(user, 'makeAdmin')}
                        >
                          <AdminPanelSettings fontSize="small" />
                        </IconButton>
                      )}
                      
                      <IconButton 
                        size="small" 
                        color="error"
                        title="Delete User"
                        onClick={() => handleUserAction(user, 'delete')}
                      >
                        <Delete fontSize="small" />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={8} align="center">
                    {searchTerm ? 'No users match your search' : 'No users found'}
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}
      
      <Dialog open={dialogOpen} onClose={handleDialogClose}>
        {renderDialogContent()}
        <DialogActions>
          <Button onClick={handleDialogClose}>Cancel</Button>
          <Button 
            onClick={handleConfirmAction} 
            color={dialogAction === 'delete' || dialogAction === 'disable' ? 'error' : 'primary'}
            autoFocus
          >
            Confirm
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AdminDashboard;