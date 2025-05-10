import React, { useState } from 'react';
import { FaLock, FaCalendarAlt, FaClock } from 'react-icons/fa';
import { useTimeCapsule } from '../../hooks/useTimeCapsule';
import capsuleAccessService from '../../services/capsuleAccessService';
import { useAuth } from '../AuthProvider';

const LockDateModal = ({ isOpen, onClose, timeCapsuleId, onSuccess }) => {
  const [openDate, setOpenDate] = useState('');
  const [openTime, setOpenTime] = useState('12:00');
  const [accessLevel, setAccessLevel] = useState('only-me');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const { lockTimeCapsule } = useTimeCapsule();
  const { authToken } = useAuth();

  // Calculate minimum date (tomorrow)
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate()); // Fixed: actually set to tomorrow
  const minDate = tomorrow.toISOString().split('T')[0];

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!openDate) {
      setError('Please select an unlock date');
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      // Combine date and time into a single ISO string
      const [hours, minutes] = openTime.split(':');
      const dateObj = new Date(openDate);
      dateObj.setHours(parseInt(hours, 10));
      dateObj.setMinutes(parseInt(minutes, 10));

      // First lock the time capsule
      await lockTimeCapsule(timeCapsuleId, dateObj.toISOString());

      // Then set the access level with retry logic
      let accessError = null;
      let retryCount = 0;
      const maxRetries = 2;
      
      while (retryCount < maxRetries) {
        try {
          if (accessLevel === 'public') {
            await capsuleAccessService.grantPublicAccess(timeCapsuleId, authToken);
          } else if (accessLevel === 'friends-only') {
            await capsuleAccessService.grantAccessToAllFriends(timeCapsuleId, 'VIEWER', authToken);
          } else if (accessLevel === 'only-me') {
            await capsuleAccessService.restrictAccessToOwner(timeCapsuleId, authToken);
          }
          accessError = null;
          break;
        } catch (err) {
          accessError = err;
          retryCount++;
          if (retryCount < maxRetries) {
            await new Promise(resolve => setTimeout(resolve, 1000)); // wait 1 second before retry
          }
        }
      }

      if (accessError) {
        console.error('Error setting access level:', accessError);
        setError({
          title: 'Partial Success',
          message: 'Time capsule was locked successfully, but there was an error setting the access level.',
          details: 'You can try changing the access level later in the capsule settings.',
          isWarning: true
        });
        if (onSuccess) onSuccess(); // Still consider this a success since the capsule is locked
        onClose();
        return;
      }

      if (onSuccess) onSuccess();
      onClose();
    } catch (err) {
      setError({
        title: 'Error',
        message: err.response?.data?.message || 'Failed to lock time capsule',
        isWarning: false
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-md mx-4 overflow-hidden">
        {/* Header */}
        <div className="bg-red-700 text-white px-6 py-4 flex items-center">
          <FaLock className="mr-2" />
          <h2 className="text-xl font-semibold">Lock Time Capsule</h2>
        </div>

        {/* Body */}
        <div className="p-6">
          <p className="text-gray-600 mb-4">
            Once locked, this time capsule will be sealed until the specified date and time. 
            You won't be able to modify its contents until it's automatically unlocked.
          </p>

          <form onSubmit={handleSubmit}>
            <div className="mb-4">
              <label htmlFor="openDate" className="block text-sm font-medium text-gray-700 mb-1">
                Unlock Date
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <FaCalendarAlt className="text-gray-400" />
                </div>
                <input
                  type="date"
                  id="openDate"
                  className="pl-10 block w-full border-gray-300 rounded-md shadow-sm focus:ring-red-500 focus:border-red-500 sm:text-sm"
                  value={openDate}
                  onChange={(e) => setOpenDate(e.target.value)}
                  min={minDate}
                  required
                />
              </div>
            </div>

            <div className="mb-4">
              <label htmlFor="openTime" className="block text-sm font-medium text-gray-700 mb-1">
                Unlock Time
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <FaClock className="text-gray-400" />
                </div>
                <input
                  type="time"
                  id="openTime"
                  className="pl-10 block w-full border-gray-300 rounded-md shadow-sm focus:ring-red-500 focus:border-red-500 sm:text-sm"
                  value={openTime}
                  onChange={(e) => setOpenTime(e.target.value)}
                  required
                />
              </div>
              <p className="mt-1 text-sm text-gray-500">
                Select a future date and time when the time capsule should be unlocked.
              </p>
            </div>

            <div className="mb-4">
              <label htmlFor="accessLevel" className="block text-sm font-medium text-gray-700 mb-1">
                Access Level
              </label>
              <select
                id="accessLevel"
                className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-red-500 focus:border-red-500 sm:text-sm"
                value={accessLevel}
                onChange={(e) => setAccessLevel(e.target.value)}
              >
                <option value="only-me">Only Me</option>
                <option value="friends-only">Friends Only</option>
                <option value="public">Public</option>
              </select>
            </div>

            {error && (
              <div className={`mb-4 p-3 rounded-md ${error.isWarning ? 'bg-yellow-50 border border-yellow-200' : 'bg-red-50 border border-red-200'}`}>
                <h3 className={`text-sm font-medium ${error.isWarning ? 'text-yellow-800' : 'text-red-800'}`}>
                  {error.title}
                </h3>
                <p className={`text-sm ${error.isWarning ? 'text-yellow-600' : 'text-red-600'} mt-1`}>
                  {error.message}
                </p>
                {error.details && (
                  <p className={`text-xs ${error.isWarning ? 'text-yellow-600' : 'text-red-600'} mt-1`}>
                    {error.details}
                  </p>
                )}
              </div>
            )}

            <div className="mt-6 flex justify-end space-x-3">
              <button
                type="button"
                className="px-4 py-2 bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500"
                onClick={onClose}
                disabled={isSubmitting}
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-4 py-2 bg-red-700 text-white rounded-md hover:bg-red-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 flex items-center"
                disabled={isSubmitting}
              >
                {isSubmitting ? (
                  <>
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin mr-2"></div>
                    Locking...
                  </>
                ) : (
                  <>
                    <FaLock className="mr-2" />
                    Lock Capsule
                  </>
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default LockDateModal;