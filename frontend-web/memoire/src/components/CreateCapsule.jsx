import React, { useState } from 'react';
import Header from '../components/Header';
import Sidebar from '../components/Sidebar';
import { FiLock, FiShare2, FiUsers, FiEye, FiPlus } from 'react-icons/fi';

export default function CreateCapsule() {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [content, setContent] = useState('');

  return (
    <div className="flex flex-col h-screen bg-gray-50">
      <Header />
      
      <div className="flex flex-1 h-screen overflow-hidden">
        <Sidebar />
        
        {/* Main Content Area */}
        <main className="flex-1 overflow-y-auto p-6">
          {/* Top Section with Title and Action Buttons */}
          <div className="flex justify-between items-center mb-6">
            <h1 className="text-2xl font-bold text-gray-800">Create your capsule</h1>
            
            <div className="flex items-center space-x-4">
              {/* Access Indicators */}
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
              
              {/* Lock Icon */}
              <button className="p-2 text-gray-600 hover:text-red-600 transition-colors">
                <FiLock className="text-lg" />
              </button>
              
              {/* Share Button */}
              <button className="flex items-center space-x-2 bg-red-600 text-white px-4 py-2 rounded-md hover:bg-red-700 transition-colors">
                <FiShare2 />
                <span>Share</span>
              </button>
            </div>
          </div>
          
         

          <div className="border-t border-gray-200 my-4"></div>

          {/* Capsule Content */}
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
                placeholder="Desc"
                className="w-full text-lg p-2 border-b border-gray-200 focus:outline-none focus:border-red-600"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </div>
            
            <div>
              <textarea
                placeholder="Content Area"
                className="w-full h-40 p-3 border border-gray-200 rounded focus:outline-none focus:border-red-600 focus:ring-1 focus:ring-red-100"
                value={content}
                onChange={(e) => setContent(e.target.value)}
              />
            </div>
            
            {/* Media Upload Area */}
            <div className="border border-dashed border-gray-300 rounded-lg p-4 text-center">
              <button className="flex items-center justify-center space-x-2 text-red-600 hover:text-red-700 mx-auto">
                <FiPlus />
                <span>Add media</span>
              </button>
            </div>
            
            {/* Media Placeholders */}
            <div className="space-y-3">
              <div className="p-3 border border-gray-200 rounded bg-gray-50 flex justify-between items-center">
                <span>sample.mp4</span>
                <button className="text-gray-400 hover:text-red-600">
                  ×
                </button>
              </div>
              <div className="p-3 border border-gray-200 rounded bg-gray-50 flex justify-between items-center">
                <span>sample.mp4</span>
                <button className="text-gray-400 hover:text-red-600">
                  ×
                </button>
              </div>
            </div>
            
            {/* Save Button */}
            <div className="pt-4">
              <button className="flex items-center space-x-2 bg-red-600 text-white px-6 py-2 rounded-md hover:bg-red-700 transition-colors">
                <FiPlus />
                <span>Save Capsule</span>
              </button>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}