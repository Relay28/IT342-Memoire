import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useAuth } from '../../components/AuthProvider';

const VideoPlayer = ({ src, className }) => {
  const { authToken } = useAuth();
  const [objectUrl, setObjectUrl] = useState(null);

  useEffect(() => {
    let url;
    const fetchBlob = async () => {
      try {
        const res = await axios.get(src, {
          headers: { Authorization: `Bearer ${authToken}` },
          responseType: 'blob'
        });
        url = URL.createObjectURL(res.data);
        setObjectUrl(url);
      } catch (err) {
        console.error('Video load failed', err);
      }
    };

    fetchBlob();
    return () => {
      if (url) URL.revokeObjectURL(url);
    };
  }, [src, authToken]);

  if (!objectUrl) {
    return <div className={`w-full h-full bg-gray-200 animate-pulse ${className}`}></div>;
  }

  return (
    <video controls className={className}>
      <source src={objectUrl} type="video/mp4" />
      Your browser doesn’t support this video.
    </video>
  );
};

export default VideoPlayer;
