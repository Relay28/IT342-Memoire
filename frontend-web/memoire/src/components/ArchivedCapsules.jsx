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
      <div className={`min-h-screen ${isDark ? 'bg-gray-900' : 'bg-gray-50'}`}>
        <Header />
        <div className="flex flex-1 h-[calc(100vh-4rem)] overflow-hidden"> {/* Adjust height to account for header */}
          <Sidebar />
          <main className={`flex-1 overflow-y-auto flex items-center justify-center ${isDark ? 'bg-gray-800' : 'bg-gray-50'}`}>
            <div className={`animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 ${isDark ? 'border-gray-300' : 'border-gray-600'}`}></div>
          </main>
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
    <div className={`min-h-screen ${isDark ? 'bg-gray-900' : 'bg-gray-50'}`}>
      <div className="flex flex-col h-screen">
        <Header />
        
        <div className="flex flex-1 h-screen overflow-hidden">
          <Sidebar />
  
          <section className={`flex-1 p-4 md:p-8 overflow-y-auto ${isDark ? 'bg-gray-800' : 'bg-gray-50'}`}>
          <div className="max-w-6xl mx-auto">
              <div className="mb-8">
                <h1 className={`text-2xl font-bold mb-2 ${isDark ? 'text-white' : 'text-gray-900'}`}>Archived Capsules</h1>
                <p className={`text-sm ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                  {archivedCapsules.length} archived capsule{archivedCapsules.length !== 1 ? 's' : ''} 
                </p>
              </div>
  
              {unarchiveSuccess && (
                <div className={`mb-4 p-4 rounded-lg border ${isDark ? 'bg-emerald-900/30 border-emerald-700 text-emerald-100' : 'bg-emerald-100 border-emerald-200 text-emerald-800'}`}>
                  Capsule has been successfully unarchived and is now published!
                </div>
              )}
  
              {archivedCapsules.length === 0 ? (
                <div className={`text-center py-12 ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                  <div className="inline-block p-6 rounded-full bg-gray-100 dark:bg-gray-700/50 mb-4">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12 text-gray-400 dark:text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4" />
                    </svg>
                  </div>
                  <h3 className="text-xl font-medium mb-2">No archived capsules</h3>
                  <p className="max-w-md mx-auto">Capsules you archive will appear here for future reference.</p>
                </div>
              ) : (
                <div className="space-y-6">
                  {archivedCapsules.map((capsule) => (
                    <div 
                      key={capsule.id} 
                      className={`rounded-xl overflow-hidden transition-all ${isDark ? 'bg-gray-700/50 text-gray-100 border border-gray-600' : 'bg-white text-gray-800 border border-gray-200'}`}
                    >
                      <div className="absolute top-4 right-4">
                        <button 
                          onClick={() => setActiveDropdown(activeDropdown === capsule.id ? null : capsule.id)}
                          className={`p-1.5 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors ${isDark ? 'text-gray-300 hover:text-rose-400' : 'text-gray-500 hover:text-rose-500'}`}
                        >
                          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                            <path d="M6 10a2 2 0 11-4 0 2 2 0 014 0zM12 10a2 2 0 11-4 0 2 2 0 014 0zM16 12a2 2 0 100-4 2 2 0 000 4z" />
                          </svg>
                        </button>
                        
                        {activeDropdown === capsule.id && (
                          <div className={`absolute right-0 mt-1 w-48 rounded-lg shadow-lg z-10 overflow-hidden ${isDark ? 'bg-gray-700 border border-gray-600' : 'bg-white border border-gray-200'}`}>
                            <div className="py-1">
                              <button
                                onClick={() => handleUnarchive(capsule.id)}
                                className={`block w-full text-left px-4 py-2 text-sm ${
                                  isDark ? 'text-amber-400 hover:bg-gray-600' : 'text-amber-600 hover:bg-gray-100'
                                }`}
                              >
                                Unarchive Capsule
                              </button>
                            </div>
                          </div>
                        )}
                      </div>
  
                      <div className="p-5">
                        <div className="flex items-center mb-4">
                          <img 
                            src={user?.profilePicture || ProfilePictureSample} 
                            alt="user" 
                            className="h-12 w-12 rounded-full mr-4 border-2 border-white dark:border-gray-600 shadow-sm"
                          />
                          <div>
                            <strong className={`block ${isDark ? 'text-white' : 'text-gray-900'}`}>
                              {user?.name || 'Anonymous'}
                            </strong>
                            <p className={`text-xs ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                              Opened on {new Date(capsule.openedAt || capsule.createdAt).toLocaleDateString()}
                            </p>
                          </div>
                        </div>
  
                        <div className="mb-4">
                          <hr className={`my-2 ${isDark ? 'border-gray-600' : 'border-gray-200'}`} />
                          <div className="my-4">
                            <div className={`text-xl font-bold mb-1 ${isDark ? 'text-white' : 'text-gray-900'}`}>
                              {capsule.title || 'Untitled Capsule'}
                            </div>
                            <div className={`text-xs ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                              Created on {new Date(capsule.createdAt).toLocaleDateString()}
                            </div>
                          </div>
                          <p className={`${isDark ? 'text-gray-300' : 'text-gray-700'} mb-4`}>
                            {capsule.description || 'No message content'}
                          </p>
                        </div>
  
                        {mediaContent[capsule.id]?.length > 0 && (
                          <div className="mt-4 rounded-lg overflow-hidden border border-gray-200 dark:border-gray-600">
                            <MediaCarousel 
                              mediaItems={mediaContent[capsule.id]}
                              fallback={bgmemoire}
                            />
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </section>
        </div>
      </div>
    </div>
  );
};

export default ArchivedCapsules;