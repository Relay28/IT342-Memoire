import React, {useContext} from 'react';
import bgmemoire from '../assets/bgmemoire.jpg';
import ProfilePictureSample from '../assets/ProfilePictureSample.png';
import { PersonalInfoContext } from './AuthProvider';
import Header from '../components/Header';
import Sidebar from '../components/Sidebar';

const ArchivedCapsules = () => {
  const { personalInfo } = useContext(PersonalInfoContext);
  const userData = personalInfo || {
    username: "",
    email: "",
    bio: "",
    profilePicture: ProfilePictureSample
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <div className="flex flex-col h-screen">
        {/* Header */}
        <Header userData={userData} />
        
        <div className="flex flex-1 h-screen overflow-hidden">
          {/* Sidebar */}
          <Sidebar />

          {/* Main Content */}
          <section className="flex-1 p-8 overflow-y-auto">
            <div className="max-w-4xl mx-auto">
              {/* Archived Capsules Title */}
              <h1 className="text-3xl font-bold mb-8">Archived Capsules</h1>

              {/* Capsule Card */}
              <div className="bg-white rounded-lg shadow-md overflow-hidden mb-8">
                <div className="p-6">
                  {/* User Info */}
                  <div className="flex items-center mb-4">
                    <img src={ProfilePictureSample} alt="user" className="h-12 w-12 rounded-full mr-4" />
                    <div>
                      <strong className="block">Georgia Santos</strong>
                      <p className="text-sm text-gray-500">Opened on February 24, 2025</p>
                    </div>
                  </div>

                  {/* Capsule Content */}
                  <div className="mb-4">
                    <p className="mb-4">Its been a year...</p>
                    <hr className="my-2" />
                    <div className="my-4">
                      <div className="text-xl font-semibold">Memories of 2024</div>
                      <div className="text-sm text-gray-500">Created on February 24, 2025</div>
                    </div>
                    <p>Hi Self! Open this after a year to reminisce wompwomp</p>
                  </div>

                  {/* Memory Images */}
                  <div className="grid grid-cols-3 gap-2 mt-4">
                    <img src={bgmemoire} alt="memory" className="h-32 w-full object-cover rounded" />
                    <img src={bgmemoire} alt="memory" className="h-32 w-full object-cover rounded" />
                    <img src={bgmemoire} alt="memory" className="h-32 w-full object-cover rounded" />
                  </div>
                </div>
              </div>

              {/* Additional Archived Capsules would go here */}
            </div>
          </section>
        </div>
      </div>
    </div>
  );
};

export default ArchivedCapsules;