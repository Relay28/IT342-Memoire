import React, { useState } from 'react';
import axios from 'axios';
import { useAuth } from '../components/AuthProvider';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
const BASE_URL = `${API_BASE_URL}/api/reports`;
 // Update this to match your Spring Boot backend URL

const ServiceReportCapsule = () => {
    const { authToken } = useAuth();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const createReport = async (reportedID, itemType) => {
        try {
            setLoading(true);
            setError(null);
            const response = await axios.post(`${BASE_URL}`, {
                reportedID,
                itemType,
                status: 'Pending'
            }, {
                headers: {
                    'Authorization': `Bearer ${authToken}`,
                    'Content-Type': 'application/json'
                }
            });
            return response.data;
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to create report');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const getReportWithDetails = async (reportId) => {
        try {
            setLoading(true);
            setError(null);
            const response = await axios.get(`${BASE_URL}/${reportId}`, {
                headers: {
                    'Authorization': `Bearer ${authToken}`
                }
            });
            return response.data;
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to fetch report details');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const getReportsByReporter = async (reporterId) => {
        try {
            setLoading(true);
            setError(null);
            const response = await axios.get(`${BASE_URL}/reporter/${reporterId}`, {
                headers: {
                    'Authorization': `Bearer ${authToken}`
                }
            });
            return response.data;
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to fetch reports by reporter');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const getReportsByItemType = async (itemType) => {
        try {
            setLoading(true);
            setError(null);
            const response = await axios.get(`${BASE_URL}/itemType/${itemType}`, {
                headers: {
                    'Authorization': `Bearer ${authToken}`
                }
            });
            return response.data;
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to fetch reports by item type');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const updateReportStatus = async (reportId, status) => {
        try {
            setLoading(true);
            setError(null);
            const response = await axios.put(`${BASE_URL}/${reportId}/status`, null, {
                params: { status },
                headers: {
                    'Authorization': `Bearer ${authToken}`
                }
            });
            return response.data;
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to update report status');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const deleteReport = async (reportId) => {
        try {
            setLoading(true);
            setError(null);
            await axios.delete(`${BASE_URL}/${reportId}`, {
                headers: {
                    'Authorization': `Bearer ${authToken}`
                }
            });
            return true;
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to delete report');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const getReportedEntity = async (reportId) => {
        try {
            setLoading(true);
            setError(null);
            const response = await axios.get(`${BASE_URL}/${reportId}/entity`, {
                headers: {
                    'Authorization': `Bearer ${authToken}`
                }
            });
            return response.data;
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to fetch reported entity');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    return {
        createReport,
        getReportWithDetails,
        getReportsByReporter,
        getReportsByItemType,
        updateReportStatus,
        deleteReport,
        getReportedEntity,
        loading,
        error,
        clearError: () => setError(null)
    };
};

export default ServiceReportCapsule;