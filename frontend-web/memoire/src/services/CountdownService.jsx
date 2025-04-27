import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { parseISO, differenceInSeconds } from 'date-fns';

const CountdownService = ({ capsuleId, openDate }) => {
  const [countdown, setCountdown] = useState({
    days: 0,
    hours: 0,
    minutes: 0,
    seconds: 0,
    isReady: false
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const calculateCountdown = () => {
      try {
        const now = new Date();
        const open = parseISO(openDate);
        const diffInSeconds = differenceInSeconds(open, now);

        if (diffInSeconds <= 0) {
          return { days: 0, hours: 0, minutes: 0, seconds: 0, isReady: true };
        }

        const days = Math.floor(diffInSeconds / (3600 * 24));
        const hours = Math.floor((diffInSeconds % (3600 * 24)) / 3600);
        const minutes = Math.floor((diffInSeconds % 3600) / 60);
        const seconds = Math.floor(diffInSeconds % 60);

        return { days, hours, minutes, seconds, isReady: false };
      } catch (err) {
        console.error('Error calculating countdown:', err);
        return { days: 0, hours: 0, minutes: 0, seconds: 0, isReady: false };
      }
    };

    // Initial calculation
    setCountdown(calculateCountdown());
    setLoading(false);

    // Update every second
    const intervalId = setInterval(() => {
      setCountdown(calculateCountdown());
    }, 1000);

    return () => clearInterval(intervalId);
  }, [openDate]);

  const formatTime = () => {
    if (loading) return 'Loading...';
    if (error) return error;
    if (countdown.isReady) return 'Capsule is ready to open!';
    
    return `${countdown.days} days, ${countdown.hours} hours, ${countdown.minutes} minutes, ${countdown.seconds} seconds`;
  };

  return (
    <div className="countdown-display">
      {formatTime()}
    </div>
  );
};

export default CountdownService;