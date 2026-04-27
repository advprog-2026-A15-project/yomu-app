import { useEffect, useMemo, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../auth';
import { clanService } from '../services/clanService';
import { findMyClan } from '../utils/clanMembership';
import '../styles/myClan.css';

export function MyClanPage() {
  const { user } = useAuth();
  const [clans, setClans] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [hasLoaded, setHasLoaded] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadClans = async () => {
      setIsLoading(true);
      setError('');
      try {
        const data = await clanService.listClans();
        setClans(Array.isArray(data) ? data : []);
      } catch (err) {
        setError(err.message || 'Gagal memuat data clan.');
      } finally {
        setIsLoading(false);
        setHasLoaded(true);
      }
    };

    void loadClans();
  }, []);

  const myClan = useMemo(() => findMyClan(clans, user?.id), [clans, user?.id]);

  if (!user?.id) {
    return <Navigate to="/login" replace />;
  }

  if (hasLoaded && !isLoading && !error && !myClan) {
    return <Navigate to="/clans/join" replace />;
  }

  return (
    <main className="my-clan-page">
      <header className="my-clan-header">
        <h1>My Clan</h1>
      </header>

      {isLoading ? <p>Memuat data clan...</p> : null}
      {error ? <p className="my-clan-error">{error}</p> : null}

      {!isLoading && !error && myClan ? (
        <section className="my-clan-card">
          <div className="my-clan-summary">
            <h2>{myClan.clanName}</h2>
            <p>
              Members: {myClan.memberCount}/{myClan.maxMembers}
            </p>
          </div>

          <ul className="my-clan-members">
            {(Array.isArray(myClan.members) && myClan.members.length > 0
              ? myClan.members
              : myClan.memberUserIds.map((memberId) => ({
                  userId: memberId,
                  displayName: memberId,
                  owner: memberId === myClan.ownerUserId,
                }))).map((member) => (
              <li key={member.userId} className="my-clan-member-row">
                <span className="my-clan-member-name">{member.displayName}</span>
                {member.owner ? (
                  <span className="my-clan-owner-marker">(owner)</span>
                ) : null}
              </li>
            ))}
          </ul>
        </section>
      ) : null}
    </main>
  );
}
