import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import Sidebar from '../components/Sidebar';
import { FiLock, FiShare2, FiUsers, FiEye, FiPlus, FiArrowLeft } from 'react-icons/fi';
import { useTimeCapsule } from '../hooks/useTimeCapsule';
import { useAuth } from './AuthProvider';
import CapsuleContentGallery from './MediaShower/CapsuleContentGallery';

export default function CreateCapsule() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [capsuleData, setCapsuleData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const { isAuthenticated } = useAuth();
  const { getTimeCapsule } = useTimeCapsule();

  // Load capsule data
  useEffect(() => {
    const loadData = async () => {
      try {
        if (id) {
          const data = await getTimeCapsule(id);
          setCapsuleData(data);
          setTitle(data.title);
          setDescription(data.description);
        }
        setLoading(false);
      } catch (err) {
        setError(err.message);
        setLoading(false);
      }
    };

    if (isAuthenticated) {
      loadData();
    } else {
      setError('You must be logged in to view this page');
      setLoading(false);
    }
  }, [id, isAuthenticated]);

  if (loading) {
    return (
      <div className="flex flex-col h-screen bg-gray-50">
        <Header />
        <div className="flex flex-1 h-screen overflow-hidden">
          <Sidebar />
          <main className="flex-1 overflow-y-auto p-6 flex items-center justify-center">
            <div>Loading capsule details...</div>
          </main>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col h-screen bg-gray-50">
        <Header />
        <div className="flex flex-1 h-screen overflow-hidden">
          <Sidebar />
          <main className="flex-1 overflow-y-auto p-6 flex items-center justify-center">
            <div className="text-red-500">Error: {error}</div>
          </main>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-screen bg-gray-50">
      <Header />
      
      <div className="flex flex-1 h-screen overflow-hidden">
        <Sidebar />
        
        <main className="flex-1 overflow-y-auto p-6">
          <div className="flex justify-between items-center mb-6">
            <div className="flex items-center">
              <button 
                onClick={() => navigate('/capsules')}
                className="mr-4 p-2 text-gray-600 hover:text-red-600 rounded-full hover:bg-gray-100"
              >
                <FiArrowLeft className="text-lg" />
              </button>
              <h1 className="text-2xl font-bold text-gray-800">
                {capsuleData ? capsuleData.title : 'Create your capsule'}
              </h1>
            </div>
            
            <div className="flex items-center space-x-4">
              <div className="flex items-center space-x-2 text-gray-600">
                <div className="flex items-center space-x-1 bg-gray-100 px-2 py-1 rounded">
                  <FiUsers className="text-red-600" />
                  <span className="text-sm">3</span>
                </div>
                
                <div className="flex items-center space-x-1 bg-gray-100 px-2 py-1 rounded">
                  <FiEye className="text-red-600" />
                  <span className="text-sm">5</span>
                </div>
              </div>
              
              <button className="p-2 text-gray-600 hover:text-red-600 transition-colors">
                <FiLock className="text-lg" />
              </button>
              
              <button className="flex items-center space-x-2 bg-red-600 text-white px-4 py-2 rounded-md hover:bg-red-700 transition-colors">
                <FiShare2 />
                <span>Share</span>
              </button>
            </div>
          </div>
          
          <div className="border-t border-gray-200 my-4"></div>

          <div className="space-y-6 bg-white p-6 rounded-lg shadow-sm">
            <div>
              <input
                type="text"
                placeholder="Title"
                className="w-full text-2xl font-bold p-2 border-b border-gray-200 focus:outline-none focus:border-red-600"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
              />
            </div>
            
            <div>
              <input
                type="text"
                placeholder="Description"
                className="w-full text-lg p-2 border-b border-gray-200 focus:outline-none focus:border-red-600"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </div>
          
            {/* Integrated MediaViewer component */}
            <CapsuleContentGallery capsuleId={id} />
            
            {isAuthenticated && (
              <div className="pt-4">
                <button className="flex items-center space-x-2 bg-red-600 text-white px-6 py-2 rounded-md hover:bg-red-700 transition-colors">
                  <FiPlus />
                  <span>{capsuleData ? 'Update Capsule' : 'Save Capsule'}</span>
                </button>
              </div>
            )}
          </div>
        </main>
      </div>
    </div>
  );
}