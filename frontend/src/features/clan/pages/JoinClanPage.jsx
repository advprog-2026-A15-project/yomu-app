import { useEffect, useMemo, useState } from 'react';
import { clanService } from '../services/clanService';
import { useAuth } from '../../auth';
import '../styles/joinClan.css';

export function JoinClanPage() {
  const { user } = useAuth();
  const [searchQuery, setSearchQuery] = useState('');
  const [clans, setClans] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [joiningClanId, setJoiningClanId] = useState(null);
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

  return (
    <main className="join-clan-page">
      <header className="join-clan-header">
        <h1>Join a Clan</h1>
        {myClan ? <span className="join-clan-my-clan-label">My Clan</span> : null}
      </header>

      <section className="join-clan-search-section">
        <label htmlFor="clan-search">Search Clan</label>
        <input
          id="clan-search"
          type="text"
          value={searchQuery}
          onChange={(event) => setSearchQuery(event.target.value)}
          placeholder="Cari nama clan"
          autoComplete="off"
        />
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
    </main>
  );
}
