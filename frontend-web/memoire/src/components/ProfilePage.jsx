import React, { useState, useRef, useEffect, useContext } from 'react';
import mmrlogo from '../assets/mmrlogo.png';
import ProfilePictureSample from '../assets/ProfilePictureSample.png';
import { FaSearch, FaMoon, FaBell, FaPlus, FaHome, FaStar, FaShareAlt } from 'react-icons/fa';
import { Link, useNavigate } from "react-router-dom";
import { PersonalInfoContext } from '../components/PersonalInfoContext';
import { profileService } from '../components/ProfileFunctionalities';
import Header from '../components/Header';
import Sidebar from '../components/Sidebar';

const ProfilePage = () => {
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const { personalInfo, setPersonalInfo } = useContext(PersonalInfoContext);
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    biography: '',
    email: '',
    name: '',
    username: ''
  });
  const [profileImage, setProfileImage] = useState(null);
  const [previewImage, setPreviewImage] = useState('');
  const fileInputRef = useRef(null);
  const [isLoading, setIsLoading] = useState(false);

  // Fetch user data and profile picture on component mount
  useEffect(() => {
    const fetchUserData = async () => {
      try {
        setIsLoading(true);
        // Fetch user data
        const userData = await profileService.getCurrentUser();
        setPersonalInfo(userData);
        setFormData({
          biography: userData.biography || '',
          email: userData.email || '',
          name: userData.name || '',
          username: userData.username || ''
        });

        // Fetch profile picture
        try {
          const pictureUrl = await profileService.getProfilePicture();
          setPreviewImage(pictureUrl);
        } catch (pictureError) {
          console.error('Failed to load profile picture:', pictureError);
          setPreviewImage(ProfilePictureSample);
        }
      } catch (error) {
        console.error('Failed to fetch user data:', error);
        alert('Failed to load profile data. Please try again.');
      } finally {
        setIsLoading(false);
      }
    };

    if (!personalInfo?.name) {
      fetchUserData();
    } else {
      setFormData({
        biography: personalInfo.biography || '',
        email: personalInfo.email || '',
        name: personalInfo.name || '',
        username: personalInfo.username || ''
      });
      // Try to load profile picture if not already loaded
      if (!previewImage || previewImage === ProfilePictureSample) {
        profileService.getProfilePicture()
          .then(pictureUrl => setPreviewImage(pictureUrl))
          .catch(() => setPreviewImage(ProfilePictureSample));
      }
    }
  }, [personalInfo, setPersonalInfo]);

  const toggleProfile = () => {
    setIsProfileOpen(!isProfileOpen);
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    console.log(file)
    if (file) {
      setProfileImage(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewImage(reader.result);
      };
     
      reader.readAsDataURL(file);
     handleSaveChanges(file)
    }
  
 
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSaveChanges = async (imageFile) => {
    const fileToUpload = imageFile || null  ;
  
    try {
      setIsLoading(true);
      const userData = {
        biography: formData.biography,
        email: formData.email,
        name: formData.name,
        username: formData.username
      };

      let updatedUser;
      
      // First handle profile picture upload if there's a new image
      if (fileToUpload) {
        const pictureResponse = await profileService.uploadProfilePicture(fileToUpload);
        
      }
      // Then update user details
      updatedUser = await profileService.updateUserDetails(userData);

      // Update context and local state
      setPersonalInfo(prev => ({
        ...prev,
        ...updatedUser
      }));

      setFormData(prev => ({
        ...prev,
        biography: updatedUser.biography || prev.biography,
        email: updatedUser.email || prev.email,
        name: updatedUser.name || prev.name,
        username: updatedUser.username || prev.username
      }));

      setProfileImage(null);
      alert('Profile updated successfully!');
    } catch (error) {
      console.error('Failed to update profile:', {
        error: error,
        response: error.response?.data
      });
      alert(`Failed to update profile: ${error.response?.data?.message || error.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeactivateAccount = async () => {
    if (window.confirm('Are you sure you want to deactivate your account? This action cannot be undone.')) {
      try {
        setIsLoading(true);
        await profileService.deactivateAccount();
        navigate('/login');
      } catch (error) {
        console.error('Failed to deactivate account:', error);
        alert('Failed to deactivate account. Please try again.');
      } finally {
        setIsLoading(false);
      }
    }
  };

  const handleChangePassword = () => {
    alert('Password change functionality will be implemented here');
  };

  const userData = personalInfo || {
    username: "",
    email: "",
    biography: "",
    profilePicture: ProfilePictureSample
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-[#AF3535] mx-auto"></div>
          <p className="mt-4 text-gray-700">Loading profile...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <div className="flex flex-col h-screen overflow-hidden">
        <Header userData={userData} />
        
        <div className="flex flex-1 h-screen overflow-hidden">
          <Sidebar />

          <section className="flex-1 p-8 overflow-y-auto">
            <div className="max-w-4xl mx-auto bg-white rounded-lg shadow-md overflow-hidden">
              <div className="p-6 border-b border-gray-200">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-4">
                    <div className="relative">
                      <img 
                        src={previewImage} 
                        alt="Profile" 
                        className="h-24 w-24 rounded-full object-cover border-2 border-[#AF3535]"
                      />
                      <button 
                        className="absolute bottom-0 right-0 bg-[#AF3535] text-white p-1.5 rounded-full hover:bg-[#AF3535]/90 transition-colors"
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
                    </div>
                    
                    <div>
                      <h1 className="text-2xl font-bold">{formData.name || userData.username || "Loading..."}</h1>
                      <p className="text-gray-600">@{userData.username || "loading"}</p>
                    </div>
                  </div>
                  
                  <button 
                    className="px-4 py-2 bg-[#AF3535] text-white rounded-md hover:bg-[#AF3535]/90 transition-colors"
                    onClick={handleSaveChanges}
                  >
                    Save Changes
                  </button>
                </div>
              </div>

              <div className="p-6">
                <div className="space-y-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Full Name</label>
                    <input
                      type="text"
                      name="name"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#AF3535]"
                      value={formData.name}
                      onChange={handleInputChange}
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Biography</label>
                    <textarea
                      name="biography"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#AF3535]"
                      rows="3"
                      value={formData.biography}
                      onChange={handleInputChange}
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                    <input
                      type="email"
                      name="email"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#AF3535]"
                      value={formData.email}
                      onChange={handleInputChange}
                    />
                  </div>

                  <div className="pt-4 border-t border-gray-200">
                    <h3 className="text-lg font-medium text-gray-900 mb-3">Account Actions</h3>
                    
                    <div className="space-y-3">
                      <button 
                        className="w-full text-left px-4 py-3 bg-gray-100 hover:bg-gray-200 rounded-md transition-colors"
                        onClick={handleChangePassword}
                      >
                        <div className="font-medium">Change Password</div>
                        <p className="text-sm text-gray-500">Update your account password</p>
                      </button>
                      
                      <button 
                        className="w-full text-left px-4 py-3 bg-red-50 hover:bg-red-100 text-red-600 rounded-md transition-colors"
                        onClick={handleDeactivateAccount}
                      >
                        <div className="font-medium">Deactivate Account</div>
                        <p className="text-sm text-red-500">Hide your profile and content</p>
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
    </div>
  );
};

export default ProfilePage;