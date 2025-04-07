import React, { useState, useEffect, useContext } from 'react';
import { useParams, useLocation } from 'react-router-dom';
import  apiService  from './apiService';
import Header from '../Header';
import ProfilePictureSample from '../../assets/ProfilePictureSample.png';
import { PersonalInfoContext } from '../PersonalInfoContext';

const ProfilePageOther = () => {
  const { personalInfo } = useContext(PersonalInfoContext);
  const { userId } = useParams();
  const location = useLocation();
  const [profile, setProfile] = useState(location.state?.profile || null);
  const [isLoading, setIsLoading] = useState(!location.state?.profile);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        setIsLoading(true);
        const response = await apiService.get(`/api/profiles/view/${userId}`);
        setProfile(response.data);
        setError(null);
      } catch (error) {
        console.error('Error fetching profile:', error);
        setError('Failed to load profile');
      } finally {
        setIsLoading(false);
      }
    };

    if (!location.state?.profile) {
      fetchProfile();
    }
  }, [userId, location.state]);

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      
      <main className="container mx-auto px-4 py-8">
        {isLoading ? (
          <div className="flex justify-center items-center py-12">
            <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-red-700"></div>
          </div>
        ) : error ? (
          <div className="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6">
            <p>{error}</p>
          </div>
        ) : profile ? (
          <div className="bg-white rounded-lg shadow overflow-hidden">
            {/* Profile Header */}
            <div className="bg-gradient-to-r from-red-600 to-red-800 h-32"></div>
            
            {/* Profile Info */}
            <div className="px-6 pb-6 relative">
              <div className="flex justify-between items-start">
                <div className="flex items-end -mt-16 space-x-4">
                  <img 
                    src={profile.profilePicture || ProfilePictureSample} 
                    alt={profile.username}
                    className="w-32 h-32 rounded-full border-4 border-white object-cover"
                    onError={(e) => {
                      e.target.onerror = null;
                      e.target.src = ProfilePictureSample;
                    }}
                  />
                  <div>
                    <h1 className="text-2xl font-bold text-gray-800">{profile.username}</h1>
                    {profile.name && <p className="text-gray-600">{profile.name}</p>}
                  </div>
                </div>
              </div>
              
              {/* Additional Profile Details */}
              <div className="mt-6 space-y-4">
                {profile.biography && (
                  <div>
                    <h2 className="text-lg font-semibold text-gray-800">About</h2>
                    <p className="text-gray-600">{profile.biography}</p>
                  </div>
                )}
                
                <div>
                  <h2 className="text-lg font-semibold text-gray-800">Member Since</h2>
                  <p className="text-gray-600">
                    {new Date(profile.createdAt).toLocaleDateString()}
                  </p>
                </div>
              </div>
            </div>
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow p-6 text-center">
            <p className="text-gray-600">Profile not found</p>
          </div>
        )}
      </main>
    </div>
  );
};

export default ProfilePageOther; 