import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { clanService } from '../services/clanService';
import { useAuth } from '../../auth';
import '../styles/joinClan.css';

export function JoinClanPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [searchQuery, setSearchQuery] = useState('');
  const [clans, setClans] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [joiningClanId, setJoiningClanId] = useState(null);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [newClanName, setNewClanName] = useState('');
  const [isCreatingClan, setIsCreatingClan] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  const myClan = useMemo(() => {
    if (!user?.id) {
      return null;
    }
    return clans.find((clan) => Array.isArray(clan.memberUserIds) && clan.memberUserIds.includes(user.id)) ?? null;
  }, [clans, user]);

  const visibleClans = useMemo(() => {
    const normalizedSearch = searchQuery.trim().toLowerCase();
    if (!normalizedSearch) {
      return clans;
    }
    return clans.filter((clan) => clan.clanName.toLowerCase().includes(normalizedSearch));
  }, [clans, searchQuery]);

  useEffect(() => {
    const loadClans = async () => {
      setIsLoading(true);
      setError('');
      try {
        const data = await clanService.listClans();
        setClans(Array.isArray(data) ? data : []);
      } catch (err) {
        setError(err.message || 'Gagal memuat daftar clan.');
      } finally {
        setIsLoading(false);
      }
    };

    void loadClans();
  }, []);

  const handleJoinClan = async (clanId) => {
    if (!user?.id) {
      setError('Kamu harus login untuk bergabung ke clan.');
      return;
    }

    setJoiningClanId(clanId);
    setError('');
    setSuccessMessage('');
    try {
      await clanService.joinClan(clanId);
      setSuccessMessage('Berhasil bergabung ke clan.');
      const data = await clanService.listClans();
      setClans(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || 'Gagal bergabung ke clan.');
    } finally {
      setJoiningClanId(null);
    }
  };

  const handleCreateClan = async (event) => {
    event.preventDefault();

    if (!user?.id) {
      setError('Kamu harus login untuk membuat clan.');
      return;
    }

    const trimmedClanName = newClanName.trim();
    if (!trimmedClanName) {
      setError('Nama clan wajib diisi.');
      return;
    }

    setIsCreatingClan(true);
    setError('');
    setSuccessMessage('');
    try {
      await clanService.createClan(trimmedClanName);
      setSuccessMessage('Clan berhasil dibuat. Mengarahkan ke Home...');
      const data = await clanService.listClans();
      setClans(Array.isArray(data) ? data : []);
      setIsCreateModalOpen(false);
      setNewClanName('');
      navigate('/');
    } catch (err) {
      setError(err.message || 'Gagal membuat clan.');
    } finally {
      setIsCreatingClan(false);
    }
  };

  return (
    <main className="join-clan-page">
      <header className="join-clan-header">
        <h1>Join a Clan</h1>
        {myClan ? <span className="join-clan-my-clan-label">My Clan</span> : null}
      </header>

      <section className="join-clan-toolbar">
        <div className="join-clan-search-section">
          <label htmlFor="clan-search">Search Clan</label>
          <input
            id="clan-search"
            type="text"
            value={searchQuery}
            onChange={(event) => setSearchQuery(event.target.value)}
            placeholder="Cari nama clan"
            autoComplete="off"
          />
        </div>
        <button type="button" className="btn-primary join-clan-create-trigger" onClick={() => setIsCreateModalOpen(true)}>
          Create Clan
        </button>
      </section>

      {error ? <p className="join-clan-alert join-clan-alert-error">{error}</p> : null}
      {successMessage ? <p className="join-clan-alert join-clan-alert-success">{successMessage}</p> : null}

      <section className="join-clan-list-section" aria-live="polite">
        {isLoading ? <p>Memuat daftar clan...</p> : null}

        {!isLoading && clans.length === 0 ? <p>Belum ada clan yang tersedia.</p> : null}

        {!isLoading && clans.length > 0 && visibleClans.length === 0 ? (
          <p>Tidak ada clan yang cocok dengan pencarianmu.</p>
        ) : null}

        {!isLoading && visibleClans.length > 0 ? (
          <ul className="join-clan-list">
            {visibleClans.map((clan) => (
              <li key={clan.clanId} className="join-clan-list-item">
                <div>
                  <strong>{clan.clanName}</strong>
                  <p>
                    Anggota: {clan.memberCount}/{clan.maxMembers}
                  </p>
                </div>
                <button
                  type="button"
                  className="btn-primary"
                  disabled={
                    joiningClanId === clan.clanId ||
                    !clan.joinable ||
                    (Array.isArray(clan.memberUserIds) && clan.memberUserIds.includes(user?.id))
                  }
                  onClick={() => handleJoinClan(clan.clanId)}
                >
                  {joiningClanId === clan.clanId ? 'Joining...' : 'Join'}
                </button>
              </li>
            ))}
          </ul>
        ) : null}
      </section>

      {isCreateModalOpen ? (
        <div className="join-clan-modal-overlay" role="dialog" aria-modal="true" aria-labelledby="create-clan-title">
          <div className="join-clan-modal">
            <h2 id="create-clan-title">Create Clan</h2>
            <form onSubmit={handleCreateClan} className="join-clan-create-form">
              <label htmlFor="new-clan-name">Nama Clan</label>
              <input
                id="new-clan-name"
                type="text"
                value={newClanName}
                onChange={(event) => setNewClanName(event.target.value)}
                placeholder="Masukkan nama clan"
                maxLength={40}
              />
              <div className="join-clan-modal-actions">
                <button
                  type="button"
                  className="join-clan-cancel-btn"
                  onClick={() => {
                    setIsCreateModalOpen(false);
                    setNewClanName('');
                  }}
                  disabled={isCreatingClan}
                >
                  Cancel
                </button>
                <button type="submit" className="btn-primary" disabled={isCreatingClan}>
                  {isCreatingClan ? 'Creating...' : 'Create Clan'}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </main>
  );
}
