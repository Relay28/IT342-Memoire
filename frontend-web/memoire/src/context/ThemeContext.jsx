// context/ThemeContext.jsx
import { createContext, useContext, useState, useEffect, useMemo } from "react";

const ThemeContext = createContext();

export const ThemeProvider = ({ children }) => {
  // Check localStorage for saved theme or use system preference as fallback
  const getInitialTheme = () => {
    // Check if we're in the browser environment
    if (typeof window === 'undefined') return 'light';
    
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme) return savedTheme;
    
    // Use system preference as fallback
    return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches 
      ? 'dark' 
      : 'light';
  };

  const [mode, setMode] = useState(getInitialTheme);

  // Apply theme class to document root element for Tailwind dark mode
  useEffect(() => {
    const root = window.document.documentElement;
    
    if (mode === 'dark') {
      root.classList.add('dark');
    } else {
      root.classList.remove('dark');
    }
    
    // Save theme preference to localStorage
    localStorage.setItem('theme', mode);
  }, [mode]);

  const toggleTheme = () => {
    setMode(prevMode => prevMode === 'light' ? 'dark' : 'light');
  };

  const value = useMemo(() => ({ 
    mode, 
    setMode,
    toggleTheme,
    isDark: mode === 'dark'
  }), [mode]);

  return (
    <ThemeContext.Provider value={value}>
      {children}
    </ThemeContext.Provider>
  );
};

// Custom hook to use the theme context
export const useThemeMode = () => {
  const context = useContext(ThemeContext);
  if (context === undefined) {
    throw new Error('useThemeMode must be used within a ThemeProvider');
  }
  return context;
};