import React, { useState, useEffect } from 'react';
import { FiMoreHorizontal } from 'react-icons/fi';
import bgmemoire from '../assets/bgmemoire.jpg';
import ProfilePictureSample from '../assets/ProfilePictureSample.png';
import Header from '../components/Header';
import Sidebar from '../components/Sidebar';
import { useThemeMode } from '../context/ThemeContext';
import TimeCapsuleService from '../services/TimeCapsuleService';
import { useAuth } from '../components/AuthProvider';
import MediaCarousel from './MediaShower/MediaCarousel';
import { useCapsuleContent } from '../context/CapsuleWebContextProvider';

const ArchivedCapsules = () => {
  const { isDark } = useThemeMode();
  const { authToken, user } = useAuth();
  const { fetchMediaContent } = useCapsuleContent();
  const [archivedCapsules, setArchivedCapsules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeDropdown, setActiveDropdown] = useState(null);
  const [unarchiveSuccess, setUnarchiveSuccess] = useState(false);
  const [mediaContent, setMediaContent] = useState({});

  useEffect(() => {
    const fetchArchivedCapsules = async () => {
      try {
        const capsules = await TimeCapsuleService.getArchivedTimeCapsules(authToken);
        setArchivedCapsules(capsules);
        
        // Fetch media content for each capsule
        const mediaPromises = capsules.map(async capsule => {
          try {
            const media = await fetchMediaContent(capsule.id);
            return { 
              capsuleId: capsule.id, 
              media: media.map(item => ({
                ...item,
                url: item.url.startsWith('http') ? item.url : `${API_BASE_URL}/${item.id}/download`,
                contentType: item.contentType || (item.url?.match(/\.(jpe?g|png|gif|webp)$/i) ? 'image/jpeg' : 'video/mp4')
              }))
            };
          } catch (err) {
            console.error(`Error fetching media for capsule ${capsule.id}:`, err);
            return { capsuleId: capsule.id, media: [] };
          }
        });
        
        const mediaResults = await Promise.all(mediaPromises);
        const mediaMap = mediaResults.reduce((acc, { capsuleId, media }) => {
          acc[capsuleId] = media;
          return acc;
        }, {});
        
        setMediaContent(mediaMap);
        setLoading(false);
      } catch (err) {
        console.error('Failed to fetch archived capsules:', err);
        setError('Failed to load archived capsules');
        setLoading(false);
      }
    };

    fetchArchivedCapsules();
  }, [authToken, fetchMediaContent, unarchiveSuccess]); // Added unarchiveSuccess to dependencies

  const handleUnarchive = async (id) => {
    try {
      // Use the new publish endpoint
      await TimeCapsuleService.publishTimeCapsule(id, authToken);
      
      // Update local state by changing the status
      setArchivedCapsules(prev => prev.map(capsule => 
        capsule.id === id ? { ...capsule, status: 'PUBLISHED' } : capsule
      ));
      
      setUnarchiveSuccess(true);
      setTimeout(() => setUnarchiveSuccess(false), 3000);
    } catch (err) {
      console.error('Failed to unarchive capsule:', err);
      setError('Failed to unarchive capsule. Please try again.');
    }
  };

  if (loading) {
    return (
      <div className={`min-h-screen ${isDark ? 'bg-gray-900' : 'bg-gray-100'}`}>
        <div className="flex flex-col h-screen">
          <Header />
          <div className="flex flex-1 h-screen overflow-hidden">
            <Sidebar />
            <section className={`flex-1 p-8 overflow-y-auto ${isDark ? 'bg-gray-800' : 'bg-gray-100'}`}>
              <div className="max-w-4xl mx-auto">
                <h1 className={`text-3xl font-bold mb-8 ${isDark ? 'text-white' : 'text-gray-900'}`}>
                  Loading archived capsules...
                </h1>
              </div>
            </section>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={`min-h-screen ${isDark ? 'bg-gray-900' : 'bg-gray-100'}`}>
        <div className="flex flex-col h-screen">
          <Header />
          <div className="flex flex-1 h-screen overflow-hidden">
            <Sidebar />
            <section className={`flex-1 p-8 overflow-y-auto ${isDark ? 'bg-gray-800' : 'bg-gray-100'}`}>
              <div className="max-w-4xl mx-auto">
                <h1 className={`text-3xl font-bold mb-8 ${isDark ? 'text-white' : 'text-gray-900'}`}>
                  Error
                </h1>
                <p className={isDark ? 'text-red-400' : 'text-red-600'}>{error}</p>
              </div>
            </section>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className={`min-h-screen ${isDark ? 'bg-gray-900' : 'bg-gray-100'}`}>
      <div className="flex flex-col h-screen">
        <Header/>
        
        <div className="flex flex-1 h-screen overflow-hidden">
          <Sidebar />

          <section className={`flex-1 p-8 overflow-y-auto ${isDark ? 'bg-gray-800' : 'bg-gray-100'}`}>
            <div className="max-w-4xl mx-auto">
              <h1 className={`text-3xl font-bold mb-8 ${isDark ? 'text-white' : 'text-gray-900'}`}>
                Archived Capsules
              </h1>

              {unarchiveSuccess && (
                <div className={`mb-4 p-4 rounded-lg ${isDark ? 'bg-emerald-900 text-emerald-100' : 'bg-emerald-100 text-emerald-800'}`}>
                  Capsule has been successfully unarchived and is now published!
                </div>
              )}

              {archivedCapsules.length === 0 ? (
                <div className={`text-center py-8 ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                  No archived capsules found
                </div>
              ) : (
                archivedCapsules.map((capsule) => (
                  <div 
                    key={capsule.id} 
                    className={`rounded-lg shadow-md overflow-hidden mb-8 relative ${
                      isDark ? 'bg-gray-700 border border-gray-600' : 'bg-white'
                    }`}
                  >
                    <div className="absolute top-4 right-4">
                      <button 
                        onClick={() => setActiveDropdown(activeDropdown === capsule.id ? null : capsule.id)}
                        className={`p-1 rounded-full ${isDark ? 'hover:bg-gray-600' : 'hover:bg-gray-200'}`}
                      >
                        <FiMoreHorizontal className={`h-5 w-5 ${isDark ? 'text-gray-300' : 'text-gray-500'}`} />
                      </button>
                      
                      {activeDropdown === capsule.id && (
                        <div className={`absolute right-0 mt-2 w-48 rounded-md shadow-lg py-1 z-10 ${
                          isDark ? 'bg-gray-700 border border-gray-600' : 'bg-white border border-gray-200'
                        }`}>
                          <button
                            onClick={() => handleUnarchive(capsule.id)}
                            className={`block w-full text-left px-4 py-2 text-sm ${
                              isDark 
                                ? 'text-white hover:bg-gray-600' 
                                : 'text-gray-700 hover:bg-gray-100'
                            }`}
                          >
                            Unarchive Capsule
                          </button>
                        </div>
                      )}
                    </div>

                    <div className="p-6">
                      <div className="flex items-center mb-4">
                        <img 
                          src={user?.profilePicture || ProfilePictureSample} 
                          alt="user" 
                          className="h-12 w-12 rounded-full mr-4" 
                        />
                        <div>
                          <strong className={`block ${isDark ? 'text-white' : 'text-gray-900'}`}>
                            {user?.name || 'Anonymous'}
                          </strong>
                          <p className={`text-sm ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                            Opened on {new Date(capsule.openedAt || capsule.createdAt).toLocaleDateString()}
                          </p>
                        </div>
                      </div>

                      <div className="mb-4">
                        
                        <hr className={`my-2 ${isDark ? 'border-gray-600' : 'border-gray-200'}`} />
                        <div className="my-4">
                          <div className={`text-xl font-semibold ${isDark ? 'text-white' : 'text-gray-900'}`}>
                            {capsule.title || 'Untitled Capsule'}
                          </div>
                          <div className={`text-sm ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                            Created on {new Date(capsule.createdAt).toLocaleDateString()}
                          </div>
                        </div>
                        <p className={isDark ? 'text-gray-300' : 'text-gray-700'}>
                          {capsule.description || 'No message content'}
                        </p>
                      </div>

                      {mediaContent[capsule.id]?.length > 0 && (
                        <div className="mt-4 rounded-lg overflow-hidden">
                          <MediaCarousel 
                            mediaItems={mediaContent[capsule.id]}
                            fallback={bgmemoire}
                          />
                        </div>
                      )}
                    </div>
                  </div>
                ))
              )}
            </div>
          </section>
        </div>
      </div>
    </div>
  );
};

export default ArchivedCapsules;