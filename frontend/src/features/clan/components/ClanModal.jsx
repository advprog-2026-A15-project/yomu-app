import { useEffect, useMemo, useState } from 'react';
import { clanService } from '../services/clanService';
import '../styles/clan.css';

export function ClanModal({ isOpen, onClose, userId }) {
  const [clans, setClans] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isCreating, setIsCreating] = useState(false);
  const [joiningClanId, setJoiningClanId] = useState(null);
  const [newClanName, setNewClanName] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const joinableClans = useMemo(
    () => clans.filter((clan) => clan.joinable && clan.memberCount < clan.maxMembers),
    [clans],
  );

  const loadClans = async () => {
    setIsLoading(true);
    setError('');
    try {
      const data = await clanService.listClans();
      setClans(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    const timer = setTimeout(() => {
      void loadClans();
    }, 0);

    return () => clearTimeout(timer);
  }, [isOpen]);

  const handleCreateClan = async (event) => {
    event.preventDefault();
    if (!userId) {
      setError('Kamu harus login untuk membuat clan.');
      return;
    }
    const trimmedName = newClanName.trim();
    if (!trimmedName) {
      setError('Nama clan wajib diisi.');
      return;
    }

    setIsCreating(true);
    setError('');
    setSuccess('');
    try {
      await clanService.createClan(userId, trimmedName);
      setNewClanName('');
      setSuccess('Clan berhasil dibuat.');
      await loadClans();
    } catch (err) {
      setError(err.message);
    } finally {
      setIsCreating(false);
    }
  };

  const handleJoinClan = async (clanId) => {
    if (!userId) {
      setError('Kamu harus login untuk bergabung ke clan.');
      return;
    }

    setJoiningClanId(clanId);
    setError('');
    setSuccess('');
    try {
      await clanService.joinClan(clanId, userId);
      setSuccess('Berhasil bergabung ke clan.');
      await loadClans();
    } catch (err) {
      setError(err.message);
    } finally {
      setJoiningClanId(null);
    }
  };

  if (!isOpen) {
    return null;
  }

  return (
    <div className="clan-modal-overlay" role="dialog" aria-modal="true" aria-labelledby="clan-modal-title">
      <div className="clan-modal">
        <div className="clan-modal-header">
          <h2 id="clan-modal-title">Join a Clan</h2>
          <button type="button" className="clan-close-btn" onClick={onClose} aria-label="Close clan modal">
            x
          </button>
        </div>

        {error ? <p className="clan-alert clan-alert-error">{error}</p> : null}
        {success ? <p className="clan-alert clan-alert-success">{success}</p> : null}

        <section className="clan-section">
          <h3>Daftar Clan</h3>
          {isLoading ? <p>Memuat clan...</p> : null}

          {!isLoading && joinableClans.length === 0 ? (
            <p className="clan-empty-state">Belum ada clan yang bisa di-join.</p>
          ) : null}

          {!isLoading && joinableClans.length > 0 ? (
            <ul className="clan-list">
              {joinableClans.map((clan) => (
                <li key={clan.clanId} className="clan-list-item">
                  <div>
                    <strong>{clan.clanName}</strong>
                    <p>
                      Anggota: {clan.memberCount}/{clan.maxMembers}
                    </p>
                  </div>
                  <button
                    type="button"
                    className="btn-primary clan-join-btn"
                    disabled={joiningClanId === clan.clanId}
                    onClick={() => handleJoinClan(clan.clanId)}
                  >
                    {joiningClanId === clan.clanId ? 'Joining...' : 'Join'}
                  </button>
                </li>
              ))}
            </ul>
          ) : null}
        </section>

        <section className="clan-section">
          <h3>Buat Clan Baru</h3>
          <form onSubmit={handleCreateClan} className="clan-create-form">
            <input
              type="text"
              value={newClanName}
              onChange={(event) => setNewClanName(event.target.value)}
              placeholder="Masukkan nama clan"
              maxLength={40}
            />
            <button type="submit" className="btn-primary" disabled={isCreating}>
              {isCreating ? 'Creating...' : 'Create Clan'}
            </button>
          </form>
        </section>
      </div>
    </div>
  );
}
