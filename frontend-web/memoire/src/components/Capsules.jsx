import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../components/AuthProvider';
import { useTimeCapsule } from '../hooks/useTimeCapsule';
import Header from '../components/Header';
import Sidebar from '../components/Sidebar';
import ProfilePictureSample from '../assets/ProfilePictureSample.png';
import { format } from 'date-fns';

const Capsules = () => {
  const { authToken } = useAuth();
  const { getUserTimeCapsules, loading, error } = useTimeCapsule();
  const [capsules, setCapsules] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchCapsules = async () => {
      try {
        const data = await getUserTimeCapsules();
        setCapsules(data);
      } catch (err) {
        console.error('Failed to fetch capsules:', err);
      }
    };
    
    if (authToken) {
      fetchCapsules();
    }
  }, [authToken, getUserTimeCapsules]);

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return format(new Date(dateString), 'MMMM d, yyyy');
  };

  const getStatusLabel = (status, locked) => {
    if (locked) return 'Locked';
    if (status === 'ACTIVE') return 'Active';
    if (status === 'ARCHIVED') return 'Archived';
    return status;
  };

  const handleCapsuleClick = (capsule) => {
    if (capsule.locked) {
      alert('This capsule is locked and cannot be edited');
      return;
    }
    navigate(`/edit/${capsule.id}`);
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-100">
        <Header />
        <div className="flex flex-1 h-screen overflow-hidden">
          <Sidebar />
          <section className="flex-1 p-8 overflow-y-auto flex items-center justify-center">
            <div className="text-center">Loading your capsules...</div>
          </section>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-100">
        <Header />
        <div className="flex flex-1 h-screen overflow-hidden">
          <Sidebar />
          <section className="flex-1 p-8 overflow-y-auto flex items-center justify-center">
            <div className="text-red-500">Error: {error}</div>
          </section>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <div className="flex flex-col h-screen">
        <Header />
        
        <div className="flex flex-1 h-screen overflow-hidden">
          <Sidebar />

          <section className="flex-1 p-8 overflow-y-auto">
            <div className="max-w-4xl mx-auto">
              {/* Header Row */}
              <div className="grid grid-cols-6 items-center gap-4 font-bold pb-4 mb-2 border-b border-gray-200">
                <div className="col-span-2 pl-4">Name</div>
                <div className="text-center">Owner</div>
                <div className="text-center">Modified last</div>
                <div className="text-center">Status</div>
                <div className="w-8"></div>
              </div>

              {capsules.length === 0 ? (
                <div className="text-center py-8 text-gray-500">
                  You don't have any capsules yet. Create your first one!
                </div>
              ) : (
                capsules.map((capsule) => (
                  <div 
                    key={capsule.id} 
                    className="grid grid-cols-6 items-center gap-4 p-4 mb-3 bg-white rounded-lg shadow-md hover:bg-gray-50 transition-colors cursor-pointer"
                    onClick={() => handleCapsuleClick(capsule)}
                  >
                    <div className="col-span-2 font-semibold truncate pl-4">
                      {capsule.title}
                    </div>
                    <div className="flex justify-center">
                      <img 
                        src={ProfilePictureSample} 
                        alt="Owner" 
                        className="h-8 w-8 rounded-full object-cover"
                      />
                    </div>
                    <div className="text-center">
                      {formatDate(capsule.createdAt)}
                    </div>
                    <div className="text-center">
                      <span className={`px-2 py-1 rounded-full text-xs ${
                        capsule.locked ? 'bg-yellow-100 text-yellow-800' :
                        capsule.status === 'ACTIVE' ? 'bg-green-100 text-green-800' :
                        'bg-gray-100 text-gray-800'
                      }`}>
                        {getStatusLabel(capsule.status, capsule.locked)}
                      </span>
                    </div>
                    <div className="flex justify-center">
                      <button 
                        className="text-gray-400 hover:text-gray-600 focus:outline-none"
                        onClick={(e) => {
                          e.stopPropagation();
                          // Handle menu click here
                        }}
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                          <path d="M10 6a2 2 0 110-4 2 2 0 010 4zM10 12a2 2 0 110-4 2 2 0 010 4zM10 18a2 2 0 110-4 2 2 0 010 4z" />
                        </svg>
                      </button>
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

export default Capsules;