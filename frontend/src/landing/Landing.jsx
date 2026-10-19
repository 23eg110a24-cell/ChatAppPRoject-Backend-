import TypeGarden from './TypeGarden';

// First screen of the chat app: the garden greets you, then Sign up / Log in, then chat.
// onRegister / onLogin are plain callbacks so this works with any router.
export default function Landing({ onRegister, onLogin }) {
  return (
    <TypeGarden
      greeting="Beacon"
      locked
      showHints={false}
      renderOverlay={pal => {
        const base = {
          font: 'inherit', fontSize: 12, letterSpacing: '0.04em', cursor: 'pointer',
          padding: '8px 16px', borderRadius: 999,
        };
        return (
          <div
            style={{ position: 'absolute', top: 'calc(16px + env(safe-area-inset-top))', right: 'calc(16px + env(safe-area-inset-right))', zIndex: 5, display: 'flex', gap: 8 }}
          >
            <button
              type="button"
              onClick={onLogin}
              style={{ ...base, background: 'transparent', color: pal.text, border: `1px solid ${pal.text}`, opacity: 0.85 }}
            >
              Log in
            </button>
            <button
              type="button"
              onClick={onRegister}
              style={{ ...base, background: pal.text, color: pal.bg, border: `1px solid ${pal.text}` }}
            >
              Sign up
            </button>
          </div>
        );
      }}
    />
  );
}