// components/Profile/SearchResult.jsx
import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import apiService from './apiService';
import ProfilePictureSample from '../../assets/ProfilePictureSample.png';
import Header from '../Header';
import { useAuth } from '../AuthProvider';
import { useThemeMode } from '../../context/ThemeContext';

const SearchResult = () => {
  const { user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [results, setResults] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const { isDark } = useThemeMode();

  const queryParams = new URLSearchParams(location.search);
  const searchQuery = queryParams.get('q') || '';

  useEffect(() => {
    const fetchResults = async () => {
      try {
        setIsLoading(true);
        const response = await apiService.get(`/api/profiles/search?query=${encodeURIComponent(searchQuery)}`);
        setResults(response.data.results);
        setError(null);
      } catch (err) {
        console.error('Search error:', err);
        setError('Failed to load search results');
        setResults([]);
      } finally {
        setIsLoading(false);
      }
    };

    if (searchQuery) {
      fetchResults();
    }
  }, [searchQuery]);

  const handleProfileClick = async (userId) => {
    try {
      const response = await apiService.get(`/api/profiles/view/${userId}`);
      navigate(`/profile/${userId}`, { state: { profile: response.data } });
    } catch (error) {
      console.error('Error fetching profile:', error);
      navigate(`/profile/${userId}`);
    }
  };

  return (
    <div className={`min-h-screen ${isDark ? 'dark bg-gray-800' : 'bg-gray-50'}`}>
      <Header userData={user} />
      
      <main className="container mx-auto px-4 py-8">
        <h1 className={`text-2xl font-bold mb-6 ${isDark ? 'text-gray-100' : 'text-gray-800'}`}>
          Search Results for "{searchQuery}"
        </h1>

        {isLoading ? (
          <div className="flex justify-center items-center py-12">
            <div className={`animate-spin rounded-full h-12 w-12 ${isDark ? 'border-t-2 border-b-2 border-red-400' : 'border-t-2 border-b-2 border-red-700'}`}></div>
          </div>
        ) : error ? (
          <div className={`border-l-4 p-4 mb-6 ${isDark ? 'bg-red-900/50 border-red-400 text-red-200' : 'bg-red-100 border-red-500 text-red-700'}`}>
            <p>{error}</p>
          </div>
        ) : results.length === 0 ? (
          <div className={`rounded-lg p-6 text-center ${isDark ? 'bg-gray-900 text-gray-300' : 'bg-gray-100 text-gray-600'}`}>
            <p>No users found matching your search</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {results.map((user) => (
              <div 
                key={user.userId}
                className={`rounded-lg shadow p-4 flex items-center space-x-4 hover:shadow-md transition-shadow cursor-pointer ${
                  isDark ? 'bg-gray-900 hover:bg-gray-700' : 'bg-white hover:bg-gray-50'
                }`}
                onClick={() => handleProfileClick(user.userId)}
              >
                <img 
                  src={user.profilePicture || ProfilePictureSample} 
                  alt={user.username}
                  className={`w-16 h-16 rounded-full border-2 object-cover ${
                    isDark ? 'border-red-400' : 'border-red-200'
                  }`}
                  onError={(e) => {
                    e.target.onerror = null;
                    e.target.src = ProfilePictureSample;
                  }}
                />
                <div>
                  <h3 className={`font-semibold ${isDark ? 'text-gray-100' : 'text-gray-800'}`}>
                    {user.username}
                  </h3>
                  {user.name && (
                    <p className={isDark ? 'text-gray-400' : 'text-gray-600'}>
                      {user.name}
                    </p>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
};

export default SearchResult;