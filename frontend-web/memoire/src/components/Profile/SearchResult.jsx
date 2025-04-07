// components/Profile/SearchResult.jsx
import React, { useState, useEffect, useContext } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import apiService  from './apiService';
import ProfilePictureSample from '../../assets/ProfilePictureSample.png';
import Header from '../Header';
import { PersonalInfoContext } from '../PersonalInfoContext';

const SearchResult = () => {
  const { personalInfo } = useContext(PersonalInfoContext);
  const location = useLocation();
  const navigate = useNavigate();
  const [results, setResults] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const queryParams = new URLSearchParams(location.search);
  const searchQuery = queryParams.get('q') || 

  console.log(searchQuery)
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
    <div className="min-h-screen bg-gray-50">
      <Header  />
      
      <main className="container mx-auto px-4 py-8">
        <h1 className="text-2xl font-bold text-gray-800 mb-6">
          Search Results for "{searchQuery}"
        </h1>

        {isLoading ? (
          <div className="flex justify-center items-center py-12">
            <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-red-700"></div>
          </div>
        ) : error ? (
          <div className="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6">
            <p>{error}</p>
          </div>
        ) : results.length === 0 ? (
          <div className="bg-gray-100 rounded-lg p-6 text-center">
            <p className="text-gray-600">No users found matching your search</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {results.map((user) => (
              <div 
                key={user.userId}
                className="bg-white rounded-lg shadow p-4 flex items-center space-x-4 hover:shadow-md transition-shadow cursor-pointer"
                onClick={() => handleProfileClick(user.userId)}
              >
                <img 
                  src={user.profilePicture || ProfilePictureSample} 
                  alt={user.username}
                  className="w-16 h-16 rounded-full border-2 border-red-200 object-cover"
                  onError={(e) => {
                    e.target.onerror = null;
                    e.target.src = ProfilePictureSample;
                  }}
                />
                <div>
                  <h3 className="font-semibold text-gray-800">{user.username}</h3>
                  {user.name && <p className="text-gray-600">{user.name}</p>}
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