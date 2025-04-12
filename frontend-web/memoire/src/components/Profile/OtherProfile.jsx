import React, { useState, useEffect } from 'react';
import { useParams, useLocation, useNavigate } from 'react-router-dom';
import apiService from './apiService';
import Header from '../Header';
import ProfilePictureSample from '../../assets/ProfilePictureSample.png';
import { useAuth } from '../AuthProvider';  // Import useAuth hook instead of PersonalInfoContext

const ProfilePageOther = () => {
  const { user } = useAuth();  // Use the auth context
  const { userId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const [profile, setProfile] = useState(location.state?.profile || null);
  const [isLoading, setIsLoading] = useState(!location.state?.profile);
  const [error, setError] = useState(null);
  const [friendshipStatus, setFriendshipStatus] = useState(null);
  const [isLoadingFriendship, setIsLoadingFriendship] = useState(true);

  // Check friendship status when profile or user changes
  useEffect(() => {
    const checkFriendshipStatus = async () => {
      if (!user || !profile) return;
      
      try {
        setIsLoadingFriendship(true);
        const response = await apiService.get(`/api/friendships/areFriends/${profile.userId}`);
        setFriendshipStatus(response.data ? 'friends' : 'not_friends');
      } catch (error) {
        console.error('Error checking friendship status:', error);
        setFriendshipStatus('error');
      } finally {
        setIsLoadingFriendship(false);
      }
    };

    checkFriendshipStatus();
  }, [profile, user]);

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

  useEffect(() => {
    if (!location.state?.profile) {
      fetchProfile();
    }
  }, [userId, location.state]);

  const handleSendFriendRequest = async () => {
    try {
      setIsLoadingFriendship(true);
      if(profile.userId !== 0) {
        await apiService.post('/api/friendships/create', {
          friendId: profile.userId
        });
      }
      setFriendshipStatus('request_sent');
    } catch (error) {
      console.error('Error sending friend request:', error);
      setError('Failed to send friend request');
    } finally {
      setIsLoadingFriendship(false);
    }
  };

  const renderFriendshipButton = () => {
    if (isLoadingFriendship || !friendshipStatus) {
      return (
        <button className="px-4 py-2 bg-gray-200 text-gray-600 rounded-md">
          Loading...
        </button>
      );
    }

    switch (friendshipStatus) {
      case 'friends':
        return (
          <div className="flex items-center space-x-2">
            <span className="text-green-600">✓ Friends</span>
            <button 
              onClick={() => navigate(`/friends`)}
              className="px-4 py-2 bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200"
            >
              View Friends
            </button>
          </div>
        );
      case 'request_sent':
        return (
          <button className="px-4 py-2 bg-yellow-100 text-yellow-700 rounded-md">
            Request Sent
          </button>
        );
      case 'not_friends':
        return (
          <button 
            onClick={handleSendFriendRequest}
            className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
          >
            Add Friend
          </button>
        );
      default:
        return (
          <button 
            onClick={handleSendFriendRequest}
            className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
          >
            Add Friend
          </button>
        );
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header userData={user} />  {/* Pass user data to Header */}
      
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
                <div className="mt-4">
                  {renderFriendshipButton()}
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