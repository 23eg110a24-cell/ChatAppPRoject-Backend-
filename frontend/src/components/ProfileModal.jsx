import { useRef, useState } from 'react';
import { api, assetUrl } from '../api';
import Avatar from './Avatar';

const TYPES = ['image/png', 'image/jpeg', 'image/webp', 'image/gif'];
const LIMITS = { avatar: { bytes: 2 * 1024 * 1024, label: '2 MB' }, banner: { bytes: 5 * 1024 * 1024, label: '5 MB' } };

// Profile card with banner and picture. Editable only for your own profile.
export default function ProfileModal({ user, editable, online, onClose, onChange }) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const avatarInput = useRef(null);
  const bannerInput = useRef(null);

  async function run(action) {
    setError('');
    setBusy(true);
    try {
      onChange(await action());
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  function pick(kind) {
    return (event) => {
      const file = event.target.files[0];
      event.target.value = '';
      if (!file) return;
      if (!TYPES.includes(file.type)) {
        setError('Use a PNG, JPG, WEBP or GIF image.');
        return;
      }
      if (file.size > LIMITS[kind].bytes) {
        setError(`That file is too big (max ${LIMITS[kind].label}).`);
        return;
      }
      run(() => (kind === 'avatar' ? api.uploadAvatar(file) : api.uploadBanner(file)));
    };
  }

  const banner = assetUrl(user.bannerUrl);

  return (
    <div className="overlay" onClick={onClose}>
      <div className="modal modal-profile" role="dialog" aria-label="Profile" onClick={(event) => event.stopPropagation()}>
        <div className="modal-head">
          <h2>Profile</h2>
          <button type="button" className="btn btn-ghost" onClick={onClose}>
            Close
          </button>
        </div>

        <div className="profile-banner">{banner && <img src={banner} alt="" draggable={false} />}</div>
        <div className="profile-head">
          <Avatar initials={user.initials} src={user.avatarUrl} online={online} size={72} />
        </div>

        <div className="profile-text">
          <p className="profile-name">{user.displayName || user.username}</p>
          <p className="muted">@{user.username}</p>
          {user.bio && <p className="profile-bio">{user.bio}</p>}
        </div>

        {editable && (
          <>
            <div className="profile-actions">
              <button type="button" className="btn" disabled={busy} onClick={() => avatarInput.current.click()}>
                Change picture
              </button>
              {user.avatarUrl && (
                <button type="button" className="btn btn-ghost" disabled={busy} onClick={() => run(api.removeAvatar)}>
                  Remove picture
                </button>
              )}
              <button type="button" className="btn" disabled={busy} onClick={() => bannerInput.current.click()}>
                Change banner
              </button>
              {user.bannerUrl && (
                <button type="button" className="btn btn-ghost" disabled={busy} onClick={() => run(api.removeBanner)}>
                  Remove banner
                </button>
              )}
            </div>
            <input ref={avatarInput} type="file" accept={TYPES.join(',')} hidden onChange={pick('avatar')} />
            <input ref={bannerInput} type="file" accept={TYPES.join(',')} hidden onChange={pick('banner')} />
            <p className="muted small">PNG, JPG, WEBP or GIF. Pictures up to 2 MB, banners up to 5 MB. GIFs play.</p>
            {busy && <p className="muted small">Uploading...</p>}
            {error && <p className="error">{error}</p>}
          </>
        )}
      </div>
    </div>
  );
}