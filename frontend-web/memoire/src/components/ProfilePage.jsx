import React, { useState, useRef, useEffect } from 'react';
import mmrlogo from '../assets/mmrlogo.png';
import ProfilePictureSample from '../assets/ProfilePictureSample.png';
import { FaSearch, FaMoon, FaBell, FaPlus, FaHome, FaStar, FaShareAlt, FaEdit, FaTimes, FaLock, FaUserSlash, FaCheck } from 'react-icons/fa';
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from './AuthProvider';
import { profileService } from '../components/ProfileFunctionalities';
import Header from '../components/Header';
import Sidebar from '../components/Sidebar';
import { useThemeMode } from '../context/ThemeContext';
import ChangePasswordModal from './ChangePasswordModal';
import Snackbar from '@mui/material/Snackbar';
import Alert from '@mui/material/Alert';

const ProfilePage = () => {
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const { user, updateUserProfile, uploadProfileImage, logout } = useAuth();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    biography: '',
    email: '',
    name: '',
    username: ''
  });
  const [originalData, setOriginalData] = useState({
    biography: '',
    email: '',
    name: '',
    username: ''
  });
  const [profileImage, setProfileImage] = useState(null);
  const [previewImage, setPreviewImage] = useState('');
  const fileInputRef = useRef(null);
  const [isLoading, setIsLoading] = useState(false);
  const { isDark } = useThemeMode();
  const [isPasswordModalOpen, setIsPasswordModalOpen] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success'
  });
  let profilePic=null;
  const showSnackbar = (message, severity = 'success') => {
    setSnackbar({ open: true, message, severity });
  };

  const handleCloseSnackbar = () => {
    setSnackbar(prev => ({ ...prev, open: false }));
  };

  // Fetch user data and profile picture on component mount
  useEffect(() => {
    const fetchUserData = async () => {
      try {
        setIsLoading(true);
        const userData = await profileService.getCurrentUser();
        
        setFormData({
          biography: userData.biography || '',
          email: userData.email || '',
          name: userData.name || '',
          username: userData.username || ''
        });

        setOriginalData({
          biography: userData.biography || '',
          email: userData.email || '',
          name: userData.name || '',
          username: userData.username || ''
        });
        alert(userData.profilePicture)
        if (userData.profilePicture) {
          let imageUrl;
          if (typeof user.profilePicture === 'string') {
            imageUrl = user.profilePicture.startsWith('data:image') 
              ? user.profilePicture 
              : `data:image/jpeg;base64,${user.profilePicture}`;
          } else if (Array.isArray(user.profilePicture)) {
            const binaryString = String.fromCharCode.apply(null, user.profilePicture);
            imageUrl = `data:image/jpeg;base64,${btoa(binaryString)}`;
          }
          
          if (imageUrl) {
            setPreviewImage(imageUrl);
          }
        }
      } catch (error) {
        console.error('Failed to fetch user data:', error);
        showSnackbar('Failed to load profile data. Please try again.', 'error');
      } finally {
        setIsLoading(false);
      }
    };

    if (!user?.name) {
      fetchUserData();
    } else {
      setFormData({
        biography: user.biography || '',
        email: user.email || '',
        name: user.name || '',
        username: user.username || ''
      });
      setOriginalData({
        biography: user.biography || '',
        email: user.email || '',
        name: user.name || '',
        username: user.username || ''
      });

      if (user.profilePicture) {
        let imageUrl;
        if (typeof user.profilePicture === 'string') {
          imageUrl = user.profilePicture.startsWith('data:image') 
            ? user.profilePicture 
            : `data:image/jpeg;base64,${user.profilePicture}`;
        } else if (Array.isArray(user.profilePicture)) {
          const binaryString = String.fromCharCode.apply(null, user.profilePicture);
          imageUrl = `data:image/jpeg;base64,${btoa(binaryString)}`;
        }
        
        if (imageUrl) {
          setPreviewImage(imageUrl);
        }
      }
      if (!previewImage || previewImage === ProfilePictureSample & user.profilePicture!=null) {
     
        if (typeof user.profilePicture === 'string') {
          profilePic = `data:image/jpeg;base64,${user.profilePicture}`;
          setProfileImage(profilePic)
        }  
      }
    }
  }, [user,originalData,profileImage]);

  const toggleEditMode = () => {
    setIsEditMode(!isEditMode);
    if (isEditMode) {
      // If exiting edit mode, revert any unsaved changes
      setFormData(originalData);
    }
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setProfileImage(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewImage(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSaveChanges = async () => {
    try {
      setIsLoading(true);
      const userData = {
        biography: formData.biography,
        email: formData.email,
        name: formData.name,
        username: formData.username
      };

      if (profileImage) {
        await uploadProfileImage(profileImage);
      }
      
      const { success } = await updateUserProfile(userData);

      if (success) {
        setProfileImage(null);
        setOriginalData(formData);
        setIsEditMode(false);
        showSnackbar('Profile updated successfully!');
      }
    } catch (error) {
      console.error('Failed to update profile:', {
        error: error,
        response: error.response?.data
      });
      showSnackbar(`Failed to update profile: ${error.response?.data?.message || error.message}`, 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeactivateAccount = async () => {
    if (window.confirm('Are you sure you want to deactivate your account? This action cannot be undone.')) {
      try {
        setIsLoading(true);
        await profileService.deactivateAccount();
        logout();
        navigate('/login');
      } catch (error) {
        console.error('Failed to deactivate account:', error);
        showSnackbar('Failed to deactivate account. Please try again.', 'error');
      } finally {
        setIsLoading(false);
      }
    }
  };

  const handleChangePassword = () => {
    setIsPasswordModalOpen(true);
  };

  const handlePasswordChangeSubmit = async (currentPassword, newPassword) => {
    console.log('Attempting password change with:', {
      currentPassword,
      newPassword
    });
    
    try {
      const result = await profileService.changePassword(currentPassword, newPassword);
      console.log('Change password result:', result);
      showSnackbar('Password changed successfully!');
      setIsPasswordModalOpen(false);
      
      // Immediately test the new password
      try {
        console.log('Attempting to login with new password...');
        // Add your login API call here using the new password
      } catch (loginError) {
        console.error('Login with new password failed:', loginError);
      }
    } catch (error) {
      console.error('Password change error:', {
        message: error.message,
        response: error.response?.data
      });
      throw error;
    }
  };

  const userData = user || {
    username: "",
    email: "",
    biography: "",
    profilePicture: ProfilePictureSample
  };

  if (isLoading) {
    return (
      <div className={`min-h-screen ${isDark ? 'dark bg-gray-900' : 'bg-gray-100'} flex items-center justify-center`}>
        <div className="text-center">
          <div className={`animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 ${isDark ? 'text-[#AF3535]' : 'border-[#AF3535]'} mx-auto`}></div>
          <p className={`mt-4 ${isDark ? 'text-gray-300' : 'text-gray-700'}`}>Loading profile...</p>
        </div>
      </div>
    );
  }

  return (
    <div className={`min-h-screen ${isDark ? 'dark bg-gray-800' : 'bg-gray-100'}`}>
      <div className="flex flex-col h-screen overflow-hidden">
        <Header userData={user} />
        
        <div className="flex flex-1 h-screen overflow-hidden">
          <Sidebar />

          <section className="flex-1 p-8 overflow-y-auto">
            <div className={`max-w-4xl mx-auto rounded-lg shadow-md overflow-hidden ${
              isDark ? 'bg-gray-900 border-gray-700' : 'bg-white border-gray-200'
            }`}>
              <div className={`p-6 border-b ${
                isDark ? 'border-gray-700' : 'border-gray-200'
              }`}>
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-4">
                    <div className="relative">
                    <img 
      src={previewImage} 
      alt="Profile" 
      className={`h-24 w-24 rounded-full object-cover border-2 ${
        isDark ? 'border-[#AF3535]' : 'border-[#AF3535]'
      }`}
      onError={(e) => {
        e.target.onerror = null;
        e.target.src = ProfilePictureSample;
      }}
    />
                      {isEditMode && (
                        <button 
                          className={`absolute bottom-0 right-0 ${
                            isDark ? 'bg-[#AF3535] hover:bg-red-600' : 'bg-[#AF3535] hover:bg-[#AF3535]/90'
                          } text-white p-1.5 rounded-full transition-colors`}
                          onClick={() => fileInputRef.current.click()}
                        >
                          <svg 
                            xmlns="http://www.w3.org/2000/svg" 
                            className="h-5 w-5" 
                            viewBox="0 0 20 20" 
                            fill="currentColor"
                          >
                            <path 
                              fillRule="evenodd" 
                              d="M4 5a2 2 0 00-2 2v8a2 2 0 002 2h12a2 2 0 002-2V7a2 2 0 00-2-2h-1.586a1 1 0 01-.707-.293l-1.121-1.121A2 2 0 0011.172 3H8.828a2 2 0 00-1.414.586L6.293 4.707A1 1 0 015.586 5H4zm6 9a3 3 0 100-6 3 3 0 000 6z" 
                              clipRule="evenodd" 
                            />
                          </svg>
                        </button>
                      )}
                    </div>
                    
                    <div>
                      <h1 className={`text-2xl font-bold ${
                        isDark ? 'text-white' : 'text-gray-900'
                      }`}>{formData.name || user?.username || "Loading..."}</h1>
                      <p className={isDark ? 'text-gray-400' : 'text-gray-600'}>@{user?.username || "loading"}</p>
                    </div>
                  </div>
                  
                  {isEditMode ? (
                    <div className="flex gap-2">
                    <button 
                      className={`p-2 rounded-full transition-colors ${
                        isDark ? 'text-gray-400 hover:text-red-500 hover:bg-gray-700' : 'text-gray-500 hover:text-red-600 hover:bg-gray-200'
                      }`}
                      onClick={toggleEditMode}
                      aria-label="Cancel editing"
                    >
                      <FaTimes className="text-2xl" />
                    </button>
                    <button 
                      className={`p-2 rounded-full transition-colors ${
                        isDark ? 'text-[#AF3535] hover:text-red-400 hover:bg-gray-700' : 'text-[#AF3535] hover:text-red-600 hover:bg-gray-200'
                      }`}
                      onClick={handleSaveChanges}
                      aria-label="Save changes"
                    >
                      <FaCheck className="text-2xl"/>
                    </button>
                  </div>
                  ) : (
                    <button 
                      className={`p-5 rounded-full transition-colors ${
                        isDark ? 'text-[#AF3535] hover:bg-gray-700' : 'text-[#AF3535] hover:bg-gray-200'
                      }`}
                      onClick={toggleEditMode}
                    >
                      <FaEdit className="text-2xl" />
                    </button>
                  )}
                </div>
              </div>

              <div className="p-6">
                <div className="space-y-6">
                  <div>
                    <label className={`block text-sm font-medium mb-1 ${
                      isDark ? 'text-gray-300' : 'text-gray-700'
                    }`}>Full Name</label>
                    {isEditMode ? (
                      <input
                        type="text"
                        name="name"
                        className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 ${
                          isDark 
                            ? 'bg-gray-700 border-gray-600 text-white focus:ring-red-500' 
                            : 'border-gray-300 focus:ring-[#AF3535]'
                        }`}
                        value={formData.name}
                        onChange={handleInputChange}
                      />
                    ) : (
                      <div className={`px-3 py-2 rounded-md ${
                        isDark ? 'bg-gray-800 text-gray-200' : 'bg-gray-50 text-gray-800'
                      }`}>
                        {formData.name}
                      </div>
                    )}
                  </div>

                  <div>
                    <label className={`block text-sm font-medium mb-1 ${
                      isDark ? 'text-gray-300' : 'text-gray-700'
                    }`}>Biography</label>
                    {isEditMode ? (
                      <textarea
                        name="biography"
                        className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 ${
                          isDark 
                            ? 'bg-gray-700 border-gray-600 text-white focus:ring-red-500' 
                            : 'border-gray-300 focus:ring-[#AF3535]'
                        }`}
                        rows="3"
                        value={formData.biography}
                        onChange={handleInputChange}
                      />
                    ) : (
                      <div className={`px-3 py-2 rounded-md ${
                        isDark ? 'bg-gray-800 text-gray-200' : 'bg-gray-50 text-gray-800'
                      } ${!formData.biography ? 'italic text-gray-500' : ''}`}>
                        {formData.biography || "No biography provided"}
                      </div>
                    )}
                  </div>

                  <div>
                    <label className={`block text-sm font-medium mb-1 ${
                      isDark ? 'text-gray-300' : 'text-gray-700'
                    }`}>Email</label>
                    {isEditMode ? (
                      <input
                        type="email"
                        name="email"
                        className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 ${
                          isDark 
                            ? 'bg-gray-700 border-gray-600 text-white focus:ring-red-500' 
                            : 'border-gray-300 focus:ring-[#AF3535]'
                        }`}
                        value={formData.email}
                        onChange={handleInputChange}
                      />
                    ) : (
                      <div className={`px-3 py-2 rounded-md ${
                        isDark ? 'bg-gray-800 text-gray-200' : 'bg-gray-50 text-gray-800'
                      }`}>
                        {formData.email}
                      </div>
                    )}
                  </div>

                  
                    
                    <div className={`pt-6 mt-6 border-t ${
                  isDark ? 'border-gray-700' : 'border-gray-200'
                }`}>
                  <h3 className={`text-lg font-medium mb-4 ${
                    isDark ? 'text-gray-300' : 'text-gray-900'
                  }`}>
                    Account Actions
                  </h3>
                  
                  <div className="grid grid-cols-1 gap-3">
                    <button 
                      className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                        isDark 
                          ? 'bg-gray-700 hover:bg-gray-600' 
                          : 'bg-gray-100 hover:bg-gray-200'
                      }`}
                      onClick={handleChangePassword}
                    >
                      <FaLock className={`text-lg ${
                        isDark ? 'text-[#AF3535]' : 'text-[#AF3535]'
                      }`} />
                      <div className="text-left">
                        <div className={`font-medium ${
                          isDark ? 'text-white' : 'text-gray-900'
                        }`}>
                          Change Password
                        </div>
                        <p className={`text-sm ${
                          isDark ? 'text-gray-400' : 'text-gray-500'
                        }`}>
                          Update your account password
                        </p>
                      </div>
                    </button>
                    
                    <button 
                      className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                        isDark 
                          ? 'bg-red-900/30 hover:bg-red-900/40' 
                          : 'bg-red-50 hover:bg-red-100'
                      }`}
                      onClick={handleDeactivateAccount}
                    >
                      <FaUserSlash className={`text-lg ${
                        isDark ? 'text-red-400' : 'text-red-600'
                      }`} />
                      <div className="text-left">
                        <div className={`font-medium ${
                          isDark ? 'text-red-200' : 'text-red-600'
                        }`}>
                          Deactivate Account
                        </div>
                        <p className={`text-sm ${
                          isDark ? 'text-red-300' : 'text-red-500'
                        }`}>
                          Hide your profile and content
                        </p>
                      </div>
                    </button>
                  </div>
                </div>
                 
                </div>
              </div>
            </div>
          </section>
        </div>
      </div>

      <input 
        type="file" 
        ref={fileInputRef}
        className="hidden" 
        accept="image/*"
        onChange={handleImageChange}
      />

      <ChangePasswordModal
        isOpen={isPasswordModalOpen}
        onClose={() => setIsPasswordModalOpen(false)}
        onChangePassword={handlePasswordChangeSubmit}
      />

      <Snackbar
        open={snackbar.open}
        autoHideDuration={2000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert
          onClose={handleCloseSnackbar}
          severity={snackbar.severity}
          sx={{ width: '100%' }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </div>
  );
};

export default ProfilePage;