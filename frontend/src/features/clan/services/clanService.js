const API_URL = 'http://localhost:8080/api/clans';

const getAuthHeaders = () => {
  const storedUser = localStorage.getItem('yomu_user');
  const baseHeaders = {
    'Content-Type': 'application/json',
  };

  if (!storedUser) {
    return baseHeaders;
  }

  try {
    const user = JSON.parse(storedUser);
    return user.token
      ? { ...baseHeaders, Authorization: `Bearer ${user.token}` }
      : baseHeaders;
  } catch {
    return baseHeaders;
  }
};

const readJsonOrThrow = async (response, fallbackMessage) => {
  if (response.ok) {
    return response.json();
  }

  const errorData = await response.json().catch(() => null);
  throw new Error(errorData?.message || fallbackMessage);
};

export const clanService = {
  listClans: async () => {
    const response = await fetch(API_URL, {
      headers: getAuthHeaders(),
    });
    return readJsonOrThrow(response, 'Gagal memuat daftar clan.');
  },

  createClan: async (creatorUserId, clanName) => {
    const response = await fetch(API_URL, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ creatorUserId, clanName }),
    });
    return readJsonOrThrow(response, 'Gagal membuat clan.');
  },

  joinClan: async (clanId, userId) => {
    const response = await fetch(`${API_URL}/${encodeURIComponent(clanId)}/members`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ userId }),
    });
    return readJsonOrThrow(response, 'Gagal bergabung ke clan.');
  },
};
