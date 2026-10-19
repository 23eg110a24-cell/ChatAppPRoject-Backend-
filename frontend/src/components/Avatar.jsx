import { useState } from 'react';
import { assetUrl } from '../api';

// Circle with the user's picture (png, jpg, webp or gif) or, without one, their initials.
// `online` is true/false for people and undefined for channels (no status dot).
export default function Avatar({ initials, online, size = 36, src }) {
  const [failed, setFailed] = useState(null);
  const url = assetUrl(src);
  const showImage = Boolean(url) && failed !== url;

  return (
    <span className="avatar" style={{ width: size, height: size, fontSize: Math.round(size * 0.31) }}>
      {showImage ? (
        <img className="avatar-img" src={url} alt="" draggable={false} onError={() => setFailed(url)} />
      ) : (
        initials
      )}
      {online !== undefined && <span className={`dot ${online ? 'dot-on' : 'dot-off'}`} />}
    </span>
  );
}