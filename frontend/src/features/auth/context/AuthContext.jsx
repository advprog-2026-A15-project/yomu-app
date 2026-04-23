import { createContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // Check for saved session on mount
  useEffect(() => {
    const checkSession = () => {
      try {
        const storedUser = localStorage.getItem('yomu_user');
        if (storedUser) {
          setUser(JSON.parse(storedUser));
        }
      } catch (err) {
        console.error("Failed to parse user session", err);
      } finally {
        setIsLoading(false);
      }
    };
    checkSession();
  }, []);

  // Sync user state to localStorage
  const saveSession = (userData) => {
    setUser(userData);
    localStorage.setItem('yomu_user', JSON.stringify(userData));
  };

  const clearSession = () => {
    setUser(null);
    localStorage.removeItem('yomu_user');
  };

  const login = async (identifier, password) => {
    setIsLoading(true);
    setError(null);
    try {
      const userData = await authService.login(identifier, password);
      saveSession(userData);
      return userData;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (userData) => {
    setIsLoading(true);
    setError(null);
    try {
      const newUserData = await authService.register(userData);
      saveSession(newUserData);
      return newUserData;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const googleLogin = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const userData = await authService.googleLogin();
      saveSession(userData);
      return userData;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const updateProfile = async (updateData) => {
    setIsLoading(true);
    setError(null);
    try {
      if (!user) throw new Error("Not logged in");
      const updatedUser = await authService.updateProfile(user.id, updateData);
      saveSession(updatedUser);
      return updatedUser;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const deleteAccount = async () => {
    setIsLoading(true);
    setError(null);
    try {
      if (!user) throw new Error("Not logged in");
      await authService.deleteAccount(user.id);
      clearSession();
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    clearSession();
  };

  const value = {
    user,
    isLoading,
    error,
    login,
    register,
    googleLogin,
    updateProfile,
    deleteAccount,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
