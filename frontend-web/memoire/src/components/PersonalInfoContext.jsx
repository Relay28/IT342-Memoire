import React, { createContext, useState, useEffect } from 'react';

// Create the context
export const PersonalInfoContext = createContext();

// Provider component
export const PersonalInfoProvider = ({ children }) => {
  const [personalInfo, setPersonalInfo] = useState(() => {
    // Retrieve initial value from localStorage
    const savedInfo = localStorage.getItem('personalInfo');
    return savedInfo ? JSON.parse(savedInfo) : null; // Parse JSON if data exists, else return null
  });

  useEffect(() => {
    // Save the personalInfo state to localStorage whenever it changes
    if (personalInfo) {
      localStorage.setItem('personalInfo', JSON.stringify(personalInfo));
    } else {
      localStorage.removeItem('personalInfo'); // Clean up if data is null
    }
  }, [personalInfo]);

  return (
    <PersonalInfoContext.Provider value={{ personalInfo, setPersonalInfo }}>
      {children}
    </PersonalInfoContext.Provider>
  );
};