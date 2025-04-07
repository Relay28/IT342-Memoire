import React, {useContext} from 'react';
import ProfilePictureSample from '../assets/ProfilePictureSample.png';
import { PersonalInfoContext } from '../components/PersonalInfoContext';
import Header from '../components/Header';
import Sidebar from '../components/Sidebar';

const Capsules = () => {
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
        <Header userData={userData} />
        
        <div className="flex flex-1 h-screen overflow-hidden">
          <Sidebar />

          <section className="flex-1 p-8 overflow-y-auto">
            <div className="max-w-4xl mx-auto">
              {/* Header Row */}
              <div className="grid grid-cols-6 items-center gap-4 font-bold pb-4 mb-2">
                <div className="col-span-2 pl-4">Name</div>
                <div className="text-center">Owner</div>
                <div className="text-center">Modified last</div>
                <div className="text-center">Type</div>
                <div className="w-8"></div> {/* Space for ellipsis */}
              </div>

              {/* Memories of 2024 Row */}
              <div className="grid grid-cols-6 items-center gap-4 p-4 mb-3 bg-white rounded-lg shadow-md hover:bg-gray-50 transition-colors">
                <div className="col-span-2 font-semibold truncate pl-4">Memories of 2024</div>
                <div className="flex justify-center">
                  <img 
                    src={ProfilePictureSample} 
                    alt="Owner" 
                    className="h-8 w-8 rounded-full object-cover"
                  />
                </div>
                <div className="text-center">February 28, 2025</div>
                <div className="text-gray-500 text-center">Closed</div>
                <div className="flex justify-center">
                  <button className="text-gray-400 hover:text-gray-600 focus:outline-none">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                      <path d="M10 6a2 2 0 110-4 2 2 0 010 4zM10 12a2 2 0 110-4 2 2 0 010 4zM10 18a2 2 0 110-4 2 2 0 010 4z" />
                    </svg>
                  </button>
                </div>
              </div>

              {/* Memories of 2025 Row */}
              <div className="grid grid-cols-6 items-center gap-4 p-4 bg-white rounded-lg shadow-md hover:bg-gray-50 transition-colors">
                <div className="col-span-2 font-semibold truncate pl-4">Memories of 2025</div>
                <div className="flex justify-center">
                  <img 
                    src={ProfilePictureSample} 
                    alt="Owner" 
                    className="h-8 w-8 rounded-full object-cover"
                  />
                </div>
                <div className="text-center">March 18, 2026</div>
                <div className="text-gray-500 text-center">Unpublished</div>
                <div className="flex justify-center">
                  <button className="text-gray-400 hover:text-gray-600 focus:outline-none">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                      <path d="M10 6a2 2 0 110-4 2 2 0 010 4zM10 12a2 2 0 110-4 2 2 0 010 4zM10 18a2 2 0 110-4 2 2 0 010 4z" />
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  );
};

export default Capsules;