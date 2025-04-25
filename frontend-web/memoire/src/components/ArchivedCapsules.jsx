import React, { useState, useEffect } from 'react';
import bgmemoire from '../assets/bgmemoire.jpg';
import ProfilePictureSample from '../assets/ProfilePictureSample.png';
import Header from '../components/Header';
import Sidebar from '../components/Sidebar';
import { useThemeMode } from '../context/ThemeContext';
import TimeCapsuleService from '../services/TimeCapsuleService';
import { useAuth } from '../components/AuthProvider';

const ArchivedCapsules = () => {
  const { isDark } = useThemeMode();
  const { authToken } = useAuth();
  const [archivedCapsules, setArchivedCapsules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchArchivedCapsules = async () => {
      try {
        const capsules = await TimeCapsuleService.getArchivedTimeCapsules(authToken);
        setArchivedCapsules(capsules);
        setLoading(false);
      } catch (err) {
        console.error('Failed to fetch archived capsules:', err);
        setError('Failed to load archived capsules');
        setLoading(false);
      }
    };

    fetchArchivedCapsules();
  }, [authToken]);

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
        {/* Header */}
        <Header/>
        
        <div className="flex flex-1 h-screen overflow-hidden">
          {/* Sidebar */}
          <Sidebar />

          {/* Main Content */}
          <section className={`flex-1 p-8 overflow-y-auto ${isDark ? 'bg-gray-800' : 'bg-gray-100'}`}>
            <div className="max-w-4xl mx-auto">
              {/* Archived Capsules Title */}
              <h1 className={`text-3xl font-bold mb-8 ${isDark ? 'text-white' : 'text-gray-900'}`}>
                Archived Capsules
              </h1>

              {archivedCapsules.length === 0 ? (
                <div className={`text-center py-8 ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                  No archived capsules found
                </div>
              ) : (
                archivedCapsules.map((capsule) => (
                  <div 
                    key={capsule.id} 
                    className={`rounded-lg shadow-md overflow-hidden mb-8 ${
                      isDark ? 'bg-gray-700 border border-gray-600' : 'bg-white'
                    }`}
                  >
                    <div className="p-6">
                      {/* User Info */}
                      <div className="flex items-center mb-4">
                        <img 
                          src={capsule.user?.profilePicture || ProfilePictureSample} 
                          alt="user" 
                          className="h-12 w-12 rounded-full mr-4" 
                        />
                        <div>
                          <strong className={`block ${isDark ? 'text-white' : 'text-gray-900'}`}>
                            {capsule.user?.name || 'Anonymous'}
                          </strong>
                          <p className={`text-sm ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                            Opened on {new Date(capsule.openedAt || capsule.createdAt).toLocaleDateString()}
                          </p>
                        </div>
                      </div>

                      {/* Capsule Content */}
                      <div className="mb-4">
                        <p className={`mb-4 ${isDark ? 'text-gray-300' : 'text-gray-700'}`}>
                          {capsule.description || 'No description provided'}
                        </p>
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
                          {capsule.message || 'No message content'}
                        </p>
                      </div>

                      {/* Memory Images - if any */}
                      {capsule.images && capsule.images.length > 0 && (
                        <div className="grid grid-cols-3 gap-2 mt-4">
                          {capsule.images.slice(0, 3).map((image, index) => (
                            <img 
                              key={index}
                              src={image.url || bgmemoire} 
                              alt={`memory ${index}`} 
                              className="h-32 w-full object-cover rounded" 
                            />
                          ))}
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