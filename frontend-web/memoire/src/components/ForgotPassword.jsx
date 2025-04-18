import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaEnvelope, FaArrowLeft, FaCheckCircle } from 'react-icons/fa';
import { useAuth } from './AuthProvider';
import { useThemeMode } from '../context/ThemeContext';

const ForgotPassword = () => {
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const navigate = useNavigate();
  const { resetPassword } = useAuth();
  const { isDark } = useThemeMode();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!email) {
      setError('Please enter your email address');
      return;
    }

    if (!/\S+@\S+\.\S+/.test(email)) {
      setError('Please enter a valid email address');
      return;
    }

    try {
      setIsLoading(true);
      await resetPassword(email);
      setIsSuccess(true);
    } catch (err) {
      setError(err.message || 'Failed to reset password. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={`min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 ${
      isDark ? 'bg-gray-900' : 'bg-gray-50'
    }`}>
      <div className={`w-full max-w-md p-8 rounded-lg shadow-md ${
        isDark ? 'bg-gray-800 text-gray-100' : 'bg-white text-gray-800'
      }`}>
        <button
          onClick={() => navigate(-1)}
          className={`flex items-center mb-4 ${
            isDark ? 'text-gray-300 hover:text-white' : 'text-gray-600 hover:text-gray-900'
          }`}
        >
          <FaArrowLeft className="mr-2" /> Back
        </button>

        <div className="text-center mb-8">
          <h2 className="text-2xl font-bold">Reset your password</h2>
          <p className={`mt-2 text-sm ${
            isDark ? 'text-gray-400' : 'text-gray-600'
          }`}>
            {isSuccess
              ? 'Check your email for further instructions'
              : 'Enter your email to receive a password reset link'}
          </p>
        </div>

        {isSuccess ? (
          <div className={`p-4 rounded-md ${
            isDark ? 'bg-green-900/30 text-green-300' : 'bg-green-50 text-green-800'
          } text-center`}>
            <FaCheckCircle className="mx-auto text-4xl mb-3 text-green-500" />
            <p className="font-medium">Password reset email sent!</p>
            <p className="text-sm mt-1">
              We've sent instructions to {email}. Please check your inbox.
            </p>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-6">
            {error && (
              <div className={`p-3 rounded-md ${
                isDark ? 'bg-red-900/30 text-red-300' : 'bg-red-50 text-red-800'
              }`}>
                {error}
              </div>
            )}

            <div>
              <label htmlFor="email" className={`block text-sm font-medium ${
                isDark ? 'text-gray-300' : 'text-gray-700'
              }`}>
                Email address
              </label>
              <div className="mt-1 relative rounded-md shadow-sm">
                <div className={`absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none ${
                  isDark ? 'text-gray-400' : 'text-gray-500'
                }`}>
                  <FaEnvelope className="h-5 w-5" />
                </div>
                <input
                  id="email"
                  name="email"
                  type="email"
                  autoComplete="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className={`block w-full pl-10 pr-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-[#AF3535] ${
                    isDark
                      ? 'bg-gray-700 border-gray-600 text-white placeholder-gray-400'
                      : 'border-gray-300 placeholder-gray-500'
                  }`}
                  placeholder="you@example.com"
                />
              </div>
            </div>

            <div>
              <button
                type="submit"
                disabled={isLoading}
                className={`w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-[#AF3535] hover:bg-[#AF3535]/90 focus:outline-none focus:ring-2 focus:ring-offset-2 ${
                  isDark ? 'focus:ring-[#AF3535]' : 'focus:ring-[#AF3535]'
                } ${isLoading ? 'opacity-70 cursor-not-allowed' : ''}`}
              >
                {isLoading ? (
                  <>
                    <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Sending...
                  </>
                ) : (
                  'Send reset link'
                )}
              </button>
            </div>
          </form>
        )}

        <div className={`mt-6 text-center text-sm ${
          isDark ? 'text-gray-400' : 'text-gray-600'
        }`}>
          Remember your password?{' '}
          <button
            onClick={() => navigate('/login')}
            className={`font-medium ${
              isDark ? 'text-[#AF3535] hover:text-red-400' : 'text-[#AF3535] hover:text-red-600'
            }`}
          >
            Sign in
          </button>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;