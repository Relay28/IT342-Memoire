import React, { useState, useEffect } from 'react';
import { useParams, useLocation } from 'react-router-dom';
import apiService from './apiService';
import timeCapsuleService from '../../services/TimeCapsuleService'; // Import the service for counts
import Header from '../Header';
import ProfilePictureSample from '../../assets/ProfilePictureSample.png';
import { useAuth } from '../AuthProvider';
import { useThemeMode } from '../../context/ThemeContext';
import ImageDisplay from '../MediaShower/ImageDisplay';

const ProfilePageOther = () => {
  const { isDark } = useThemeMode();
  const { user } = useAuth();
  const { userId } = useParams();
  const location = useLocation();
  const [profile, setProfile] = useState(location.state?.profile || null);
  const [isLoading, setIsLoading] = useState(!location.state?.profile);
  const [error, setError] = useState(null);
  const [publicCapsules, setPublicCapsules] = useState([]);
  const [friendsCount, setFriendsCount] = useState(0); // State for friends count
  const [publishedCapsulesCount, setPublishedCapsulesCount] = useState(0); // State for published capsules count
  const [friendshipStatus, setFriendshipStatus] = useState(null);
  const [isLoadingFriendship, setIsLoadingFriendship] = useState(true);
  const [hasPendingRequest, setHasPendingRequest] = useState(false);
  const [isReceiver, setIsReceiver] = useState(false);

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

  const fetchCounts = async () => {
    try {
      const [friends, publishedCapsules] = await Promise.all([
        timeCapsuleService.getUserFriendsCount(userId), // Fetch friends count
        timeCapsuleService.getPublicPublishedTimeCapsuleCountForUser(userId), // Fetch published capsules count
      ]);
      setFriendsCount(friends);
      setPublishedCapsulesCount(publishedCapsules);
    } catch (error) {
      console.error('Error fetching counts:', error);
      setError('Failed to load counts');
    }
  };

  const fetchPublicCapsules = async () => {
    try {
      const response = await apiService.get(`/api/timecapsules/public/published/${userId}`);
      const capsules = response.data;

      // Fetch the first content of each capsule for preview
      const capsulesWithPreview = await Promise.all(
        capsules.map(async (capsule) => {
          try {
            const contentsResponse = await apiService.get(`/api/capsule-content/public/${capsule.id}/contents`);
            const contents = contentsResponse.data;

            if (contents.length > 0) {
              const firstContent = contents[0];
              const previewUrl = `${import.meta.env.VITE_API_BASE_URL}/api/capsule-content/public/content/${firstContent.id}/download`;
              return { ...capsule, previewUrl };
            }
          } catch (err) {
            console.error(`Error fetching contents for capsule ${capsule.id}:`, err);
          }
          return { ...capsule, previewUrl: null };
        })
      );

      setPublicCapsules(capsulesWithPreview);
    } catch (error) {
      console.error('Error fetching public capsules:', error);
      setError('Failed to load public capsules');
    }
  };

  const checkFriendshipStatus = async () => {
    if (!user || !profile) return;

    try {
      setIsLoadingFriendship(true);
      // Check if friends
      const friendsResponse = await apiService.get(`/api/friendships/areFriends/${profile.userId}`);
      if (friendsResponse.data) {
        setFriendshipStatus('friends');
        return;
      }

      // Check for pending request
      const pendingResponse = await apiService.get(`/api/friendships/hasPendingRequest/${profile.userId}`);
      if (pendingResponse.data) {
        // Check if current user is the receiver of the request
        const isReceiverResponse = await apiService.get(`/api/friendships/isReceiver/${profile.userId}`);
        setIsReceiver(isReceiverResponse.data);
        setFriendshipStatus('request_pending');
        setHasPendingRequest(true);
      } else {
        setFriendshipStatus('not_friends');
        setHasPendingRequest(false);
        setIsReceiver(false);
      }
    } catch (error) {
      console.error('Error checking friendship status:', error);
      setFriendshipStatus('error');
    } finally {
      setIsLoadingFriendship(false);
    }
  };

  useEffect(() => {
    if (!location.state?.profile) {
      fetchProfile();
    }
    fetchCounts(); // Fetch counts when the component loads
    fetchPublicCapsules();
    checkFriendshipStatus();
  }, [userId, location.state]);

  const handleSendFriendRequest = async () => {
    try {
      setIsLoadingFriendship(true);
      if (profile.userId !== 0) {
        await apiService.post('/api/friendships/create', {
          friendId: profile.userId
        });
      }
      setFriendshipStatus('request_sent');
      setHasPendingRequest(true);
      setIsReceiver(false);
    } catch (error) {
      console.error('Error sending friend request:', error);
      setError('Failed to send friend request');
    } finally {
      setIsLoadingFriendship(false);
    }
  };

  const handleCancelFriendRequest = async () => {
    try {
      setIsLoadingFriendship(true);
      await apiService.delete(`/api/friendships/cancel/${profile.userId}`);
      setFriendshipStatus('not_friends');
      setHasPendingRequest(false);
    } catch (error) {
      console.error('Error canceling friend request:', error);
      setError(error.response?.data?.message || 'Failed to cancel friend request');
    } finally {
      setIsLoadingFriendship(false);
    }
  };

  const handleAcceptFriendRequest = async () => {
    try {
      setIsLoadingFriendship(true);
      const response = await apiService.get(`/api/friendships/findByUsers/${profile.userId}`);
      if (response.data) {
        await apiService.put(`/api/friendships/${response.data.id}/accept`);
        setFriendshipStatus('friends');
        setHasPendingRequest(false);
      }
    } catch (error) {
      console.error('Error accepting friend request:', error);
      setError(error.response?.data?.message || 'Failed to accept friend request');
    } finally {
      setIsLoadingFriendship(false);
    }
  };

  const handleDeclineFriendRequest = async () => {
    try {
      setIsLoadingFriendship(true);
      const response = await apiService.get(`/api/friendships/findByUsers/${profile.userId}`);
      if (response.data) {
        await apiService.delete(`/api/friendships/${response.data.id}`);
        setFriendshipStatus('not_friends');
        setHasPendingRequest(false);
      }
    } catch (error) {
      console.error('Error declining friend request:', error);
      setError('Failed to decline friend request');
    } finally {
      setIsLoadingFriendship(false);
    }
  };

  const handleUnfriend = async () => {
    try {
      setIsLoadingFriendship(true);
      const response = await apiService.get(`/api/friendships/findByUsers/${profile.userId}`);
      if (response.data) {
        await apiService.delete(`/api/friendships/${response.data.id}`);
        setFriendshipStatus('not_friends');
        setHasPendingRequest(false);
      }
    } catch (error) {
      console.error('Error unfriending:', error);
      setError(error.response?.data?.message || 'Failed to unfriend');
    } finally {
      setIsLoadingFriendship(false);
    }
  };

  const renderFriendshipButton = () => {
    if (isLoadingFriendship || !friendshipStatus) {
      return (
        <button className={`px-4 py-2 ${isDark ? 'bg-gray-700 text-gray-300' : 'bg-gray-200 text-gray-600'} rounded-md`}>
          Loading...
        </button>
      );
    }

    switch (friendshipStatus) {
      case 'friends':
        return (
          <div className="flex items-center space-x-2">
            <span className={isDark ? 'text-green-400' : 'text-green-600'}>✓ Friends</span>
            <button 
              onClick={handleUnfriend}
              className={`px-4 py-2 ${isDark ? 'bg-gray-700 text-gray-300 hover:bg-gray-600' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'} rounded-md`}
            >
              Unfriend
            </button>
          </div>
        );
      case 'request_sent':
        return (
          <div className="flex space-x-2">
            <button 
              className={`px-4 py-2 ${isDark ? 'bg-yellow-800 text-yellow-200' : 'bg-yellow-100 text-yellow-700'} rounded-md`}
            >
              Request Sent
            </button>
            <button
              onClick={handleCancelFriendRequest}
              className={`px-4 py-2 ${isDark ? 'bg-gray-600 text-gray-200 hover:bg-gray-500' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'} rounded-md`}
            >
              Cancel
            </button>
          </div>
        );
      case 'request_pending':
        return isReceiver ? (
          <div className="flex space-x-2">
            <button
              onClick={handleAcceptFriendRequest}
              className={`px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700`}
            >
              Accept
            </button>
            <button
              onClick={handleDeclineFriendRequest}
              className={`px-4 py-2 ${isDark ? 'bg-gray-600 text-gray-200 hover:bg-gray-500' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'} rounded-md`}
            >
              Decline
            </button>
          </div>
        ) : (
          <div className="flex space-x-2">
            <button 
              className={`px-4 py-2 ${isDark ? 'bg-[#AF3535] text-white' : 'bg-#AF3535 text-white'} rounded-md`}
            >
              Request Pending
            </button>
            <button 
              onClick={handleCancelFriendRequest}
              className={`px-4 py-2 ${isDark ? 'bg-yellow-800 text-yellow-200' : 'bg-yellow-100 text-yellow-700'} rounded-md`}
            >
              Cancel
            </button>
          </div>
        );
      case 'not_friends':
        return (
          <button 
            onClick={handleSendFriendRequest}
            className={`px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700`}
          >
            Add Friend
          </button>
        );
      default:
        return (
          <button 
            onClick={handleSendFriendRequest}
            className={`px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700`}
          >
            Add Friend
          </button>
        );
    }
  };

  return (
    <div className={`min-h-screen ${isDark ? 'bg-gray-900 text-gray-200' : 'bg-gray-50 text-gray-900'}`}>
      <Header userData={user} />

      <main className="container mx-auto px-4 py-8">
        {isLoading ? (
          <div className="flex justify-center items-center py-12">
            <div className={`animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 ${isDark ? 'border-red-400' : 'border-red-700'}`}></div>
          </div>
        ) : error ? (
          <div className={`${isDark ? 'bg-red-900 border-red-700 text-red-200' : 'bg-red-100 border-red-500 text-red-700'} border-l-4 p-4 mb-6`}>
            <p>{error}</p>
          </div>
        ) : profile ? (
          <div className={`rounded-lg shadow overflow-hidden ${isDark ? 'bg-gray-800' : 'bg-white'}`}>
            <div className="bg-gradient-to-r from-red-600 to-red-800 h-32"></div>

            <div className="px-6 pb-6 relative">
              <div className="flex justify-between items-start">
                <div className="flex items-end -mt-16 space-x-4">
                  <img
                    src={
                      profile.profilePicture
                        ? typeof profile.profilePicture === 'string'
                          ? profile.profilePicture.startsWith('data:image')
                            ? profile.profilePicture
                            : `data:image/jpeg;base64,${profile.profilePicture}`
                          : Array.isArray(profile.profilePicture)
                          ? `data:image/jpeg;base64,${btoa(String.fromCharCode.apply(null, profile.profilePicture))}`
                          : ProfilePictureSample
                        : ProfilePictureSample
                    }
                    alt={profile.username}
                    className="w-32 h-32 rounded-full border-4 border-white object-cover"
                    onError={(e) => {
                      e.target.onerror = null;
                      e.target.src = ProfilePictureSample;
                    }}
                  />
                  <div>
                    <h1 className={`text-2xl font-bold ${isDark ? 'text-gray-100' : 'text-gray-800'}`}>{profile.username}</h1>
                    {profile.name && <p className={isDark ? 'text-gray-400' : 'text-gray-600'}>{profile.name}</p>}
                  </div>
                </div>
                <div className="mt-4">
                  {renderFriendshipButton()}
                </div>
              </div>

              {/* Counts Section */}
              <div className="mt-6 flex justify-around text-center">
                <div>
                  <h2 className={`text-lg font-semibold ${isDark ? 'text-gray-200' : 'text-gray-800'}`}>{publishedCapsulesCount}</h2>
                  <p className={isDark ? 'text-gray-400' : 'text-gray-600'}>Published Capsules</p>
                </div>
                <div>
                  <h2 className={`text-lg font-semibold ${isDark ? 'text-gray-200' : 'text-gray-800'}`}>{friendsCount}</h2>
                  <p className={isDark ? 'text-gray-400' : 'text-gray-600'}>Friends</p>
                </div>
              </div>

              <div className="mt-6 space-y-4">
                {profile.biography && (
                  <div>
                    <h2 className={`text-lg font-semibold ${isDark ? 'text-gray-200' : 'text-gray-800'}`}>About</h2>
                    <p className={isDark ? 'text-gray-300' : 'text-gray-600'}>{profile.biography}</p>
                  </div>
                )}
              </div>
            </div>
          </div>
        ) : (
          <div className={`rounded-lg shadow p-6 text-center ${isDark ? 'bg-gray-800 text-gray-200' : 'bg-white text-gray-600'}`}>
            <p>Profile not found</p>
          </div>
        )}

        {/* Public Time Capsules Grid */}
        <div className="mt-8">
          <h2 className={`text-xl font-bold ${isDark ? 'text-gray-100' : 'text-gray-800'}`}>Public Time Capsules</h2>
          {publicCapsules.length === 0 ? (
            <p className={isDark ? 'text-gray-400' : 'text-gray-600'}>No public time capsules available.</p>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 mt-4">
              {publicCapsules.map((capsule) => (
                <div key={capsule.id} className="border rounded-lg overflow-hidden shadow-sm">
                  {capsule.previewUrl ? (
                    <ImageDisplay src={capsule.previewUrl} className="w-full h-48 object-cover" />
                  ) : (
                    <div className="w-full h-48 bg-gray-200 flex items-center justify-center">
                      <span className="text-gray-500">No Preview</span>
                    </div>
                  )}
                  <div className="p-4">
                    <h3 className={`text-lg font-semibold ${isDark ? 'text-gray-100' : 'text-gray-800'}`}>{capsule.title || 'Untitled Capsule'}</h3>
                    <p className={isDark ? 'text-gray-400' : 'text-gray-600'}>{capsule.description || 'No description available.'}</p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  );
};

export default ProfilePageOther;