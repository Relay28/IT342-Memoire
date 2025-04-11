import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useAuth } from '../AuthProvider';

const ImageDisplay = ({ src, className, alt }) => {
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
        console.error('Image load failed', err);
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

  return <img src={objectUrl} alt={alt || ''} className={className} />;
};

export default ImageDisplay;
