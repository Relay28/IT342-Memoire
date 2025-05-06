import React, { useState, useEffect, useRef } from 'react';
import Header from '../components/Header';
import Sidebar from '../components/Sidebar';
import { useThemeMode } from '../context/ThemeContext';
import { useAuth } from '../components/AuthProvider';
import CountdownService from '../services/CountdownService';
import { useTimeCapsule } from '../hooks/useTimeCapsule';
import { format, parseISO, isValid } from 'date-fns';

const CountdownTimerPage = () => {
  const { isDark } = useThemeMode();
  const { authToken } = useAuth();
  const { getClosedTimeCapsules, getTimeCapsule, unlockTimeCapsule} = useTimeCapsule();
  const [closedCapsules, setClosedCapsules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [openMenuId, setOpenMenuId] = useState(null);
  const menuRef = useRef(null);

  useEffect(() => {
    const fetchClosedCapsules = async () => {
      try {
        setLoading(true);
        const capsules = await getClosedTimeCapsules();
        
        const enrichedCapsules = await Promise.all(
          capsules.map(async (capsule) => {
            try {
              const fullCapsule = await getTimeCapsule(capsule.id);
              return {
                ...capsule,
                lockedDate: fullCapsule.lockedDate || fullCapsule.createdAt
              };
            } catch (err) {
              console.error(`Error fetching details for capsule ${capsule.id}:`, err);
              return capsule;
            }
          })
        );
        
        setClosedCapsules(enrichedCapsules);
      } catch (err) {
        setError(err.message || 'Failed to fetch capsules');
      } finally {
        setLoading(false);
      }
    };

    if (authToken) {
      fetchClosedCapsules();
    }
  }, [authToken, getClosedTimeCapsules, getTimeCapsule]);

  useEffect(() => {
    function handleClickOutside(event) {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setOpenMenuId(null);
      }
    }
    
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [menuRef]);

  const handleUnlockCapsule = async (id) => {
    try {
      await unlockTimeCapsule(id);
      // Refresh the list after unlocking
      const capsules = await getClosedTimeCapsules();
      setClosedCapsules(capsules);
      setOpenMenuId(null);
    } catch (err) {
      console.error('Failed to unlock time capsule:', err);
      setError('Failed to unlock capsule');
    }
  };

  const handleToggleMenu = (e, id) => {
    e.stopPropagation();
    setOpenMenuId(openMenuId === id ? null : id);
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'Not specified';
    
    try {
      let date = parseISO(dateString);
      if (!isValid(date)) {
        date = new Date(dateString);
      }
      return isValid(date) ? format(date, 'MMMM d, yyyy') : 'Invalid date';
    } catch (err) {
      console.error('Error formatting date:', err);
      return 'Invalid date';
    }
  };

  // ... (keep the existing loading and error states the same)
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
      <div className={`min-h-screen ${isDark ? 'bg-gray-900' : 'bg-gray-50'}`}>
        <Header />
        <div className="flex flex-1 h-screen overflow-hidden">
          <Sidebar />
          <section className={`flex-1 p-8 overflow-y-auto flex items-center justify-center ${isDark ? 'bg-gray-800' : 'bg-gray-50'}`}>
            <div className={`p-4 rounded-lg ${isDark ? 'bg-gray-700 text-red-400' : 'bg-white text-red-600'} shadow-md`}>
              Error: {error}
            </div>
          </section>
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
          
          <main className={`flex-1 p-4 md:p-8 overflow-y-auto ${isDark ? 'bg-gray-800' : 'bg-gray-50'}`}>
            <div className="max-w-6xl mx-auto">
              <div className="mb-8">
                <h1 className={`text-2xl font-bold mb-2 ${isDark ? 'text-white' : 'text-gray-900'}`}>
                  Locked Time Capsules
                </h1>
                <p className={`text-sm ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                  {closedCapsules.length} capsule{closedCapsules.length !== 1 ? 's' : ''} waiting to be opened
                </p>
              </div>
  
              {closedCapsules.length === 0 ? (
                <div className={`text-center py-12 ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                  <div className="inline-block p-6 rounded-full bg-gray-100 dark:bg-gray-700/50 mb-4">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12 text-gray-400 dark:text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                  </div>
                  <h3 className="text-xl font-medium mb-2">No locked capsules</h3>
                  <p className="max-w-md mx-auto">Lock a capsule to see it appear here</p>
                </div>
              ) : (
                <div className="space-y-6">
                  {closedCapsules.map(capsule => (
                    <div 
                    key={capsule.id} 
                    className={`relative rounded-xl overflow-hidden transition-all ${isDark ? 'bg-gray-700/50 text-gray-100 border border-gray-600' : 'bg-white text-gray-800 border border-gray-200'}`}
                  >
                    {/* Options dropdown */}
                    <div className="absolute top-4 right-4" ref={openMenuId === capsule.id ? menuRef : null}>
                      <button 
                        className={`p-1 rounded-full ${isDark ? 'text-gray-400 hover:text-gray-300 hover:bg-gray-600' : 'text-gray-500 hover:text-gray-700 hover:bg-gray-200'} transition-colors`}
                        onClick={(e) => handleToggleMenu(e, capsule.id)}
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                        <path d="M6 10a2 2 0 11-4 0 2 2 0 014 0zM12 10a2 2 0 11-4 0 2 2 0 014 0zM16 12a2 2 0 100-4 2 2 0 000 4z" />
                        </svg>
                      </button>
                      
                      {openMenuId === capsule.id && (
                        <div 
                          className={`absolute right-0 mt-1 w-48 rounded-lg shadow-xl z-50 overflow-hidden ${isDark ? 'bg-gray-700 border border-gray-600' : 'bg-white border border-gray-200'}`}
                          style={{ top: '100%', right: 0 }}
                        >
                          <div className="py-1">
                            <button
                              onClick={(e) => {
                                e.stopPropagation();
                                handleUnlockCapsule(capsule.id);
                              }}
                              className={`block w-full text-left px-4 py-2 text-sm flex items-center ${
                                isDark ? 'text-amber-400 hover:bg-gray-600' : 'text-amber-600 hover:bg-gray-100'
                              } transition-colors`}
                            >
                              <svg className="mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 11V7a4 4 0 118 0m-4 8v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2z" />
                              </svg>
                              Unlock Capsule
                            </button>
                          </div>
                        </div>
                      )}
                    </div>
  
                      <div className="p-5">
                        <div className="mb-4">
                          <h3 className={`text-xl font-bold mb-1 ${isDark ? 'text-white' : 'text-gray-900'}`}>
                            {capsule.title}
                          </h3>
                          <p className={`text-xs ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                            Created by {capsule.ownerName || 'You'}
                          </p>
                        </div>
                        
                        <div className="grid grid-cols-2 gap-4 mb-4 pt-4 border-t border-gray-200 dark:border-gray-600">
                          <div>
                            <p className={`text-xs mb-1 ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>SEALED ON</p>
                            <p className={isDark ? 'text-gray-300' : 'text-gray-700'}>{formatDate(capsule.lockedDate)}</p>
                          </div>
                          <div>
                            <p className={`text-xs mb-1 ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>OPENS ON</p>
                            <p className={isDark ? 'text-gray-300' : 'text-gray-700'}>{formatDate(capsule.openDate)}</p>
                          </div>
                        </div>
                        
                        <div className="pt-4 border-t border-gray-200 dark:border-gray-600">
                          <p className={`text-xs mb-2 ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>TIME REMAINING</p>
                          <div className="text-[#AF3535] font-medium">
                            <CountdownService 
                              capsuleId={capsule.id} 
                              openDate={capsule.openDate} 
                            />
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </main>
        </div>
      </div>
    </div>
  );
};

export default CountdownTimerPage;