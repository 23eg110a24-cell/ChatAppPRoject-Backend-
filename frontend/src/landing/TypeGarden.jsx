import { useEffect, useMemo, useRef, useState } from 'react';
import GardenEngine, { PALETTES, paletteColors } from './gardenEngine';

const FONT_HREF =
  'https://fonts.googleapis.com/css2?family=DM+Mono:wght@400;500&family=Playfair+Display:wght@700&display=swap';

function useFonts() {
  useEffect(() => {
    if (document.querySelector(`link[href="${FONT_HREF}"]`)) return;
    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = FONT_HREF;
    document.head.appendChild(link);
  }, []);
}

/**
 * Full-screen Type Garden. Type to grow, space cuts, backspace withers, enter clears.
 *
 * Props
 *  greeting        text that grows by itself until the visitor starts typing ('' to disable)
 *  greetingStart   ms before the first letter (default 300)
 *  greetingDelay   ms between letters (default 100)
 *  initialPalette  index into PALETTES
 *  density, boil, recoil   motion + garden tuning from the original
 *  showHints       bottom bar with hints + palette swatches
 *  locked          true = visitors can't type or retype the name (garden stays interactive)
 *  renderOverlay   (palette) => node, drawn above the garden (login / sign up buttons)
 */
export default function TypeGarden({
  greeting = 'Beacon',
  greetingStart = 300,
  greetingDelay = 100,
  initialPalette = 0,
  density = 1,
  boil = true,
  recoil = 20,
  showHints = true,
  locked = false,
  renderOverlay,
}) {
  useFonts();
  const rootRef = useRef(null);
  const cvRef = useRef(null);
  const inpRef = useRef(null);
  const engRef = useRef(null);
  const [pi, setPi] = useState(initialPalette);
  const pal = useMemo(() => paletteColors(pi), [pi]);

  useEffect(() => {
    const eng = new GardenEngine(cvRef.current, inpRef.current, {
      root: rootRef.current, density, boil, recoil, locked, palette: initialPalette,
    });
    engRef.current = eng;
    eng.start();
    if (greeting) eng.autotype(greeting, greetingDelay, greetingStart);
    return () => eng.destroy();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => { engRef.current && engRef.current.setPalette(pi); }, [pi]);
  useEffect(() => { engRef.current && engRef.current.setOptions({ density, boil, recoil, locked }); }, [density, boil, recoil, locked]);

  const mono = "'DM Mono', ui-monospace, monospace";
  const hint = { fontSize: 12, letterSpacing: '0.04em', color: pal.text, opacity: 0.6 };

  return (
    <div
      ref={rootRef}
      style={{ position: 'fixed', inset: 0, background: pal.bg, cursor: locked ? 'default' : 'text', fontFamily: mono, userSelect: 'none', WebkitUserSelect: 'none', touchAction: 'none', overflow: 'hidden' }}
    >
      <canvas ref={cvRef} style={{ position: 'absolute', inset: 0, width: '100%', height: '100%', display: 'block' }} />

      {/* hidden field: gets the keyboard up on phones and catches IME / predictive text */}
      <input
        ref={inpRef}
        aria-label="Type to grow a garden"
        autoCapitalize="off" autoComplete="off" autoCorrect="off" spellCheck={false}
        style={{ position: 'fixed', left: 0, top: 0, width: 1, height: 1, margin: 0, padding: 0, border: 0, opacity: 0, fontSize: 16, color: 'transparent', background: 'transparent', caretColor: 'transparent', pointerEvents: 'none' }}
      />

      {renderOverlay && renderOverlay(pal)}

      {showHints && (
        <div
          style={{ position: 'absolute', left: 24, right: 24, bottom: 20, display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 16, flexWrap: 'wrap', pointerEvents: 'none' }}
        >
          <div style={{ ...hint, display: 'flex', gap: 20, flexWrap: 'wrap' }}>
            <span>type to grow</span>
            <span>space cuts</span>
            <span>backspace withers</span>
            <span>enter clears</span>
          </div>
          <div
            style={{ display: 'flex', gap: 6, alignItems: 'center', padding: '4px 6px', border: '1px solid #3A3A3A', borderRadius: 999, background: pal.bg, pointerEvents: 'auto' }}
            onPointerDown={e => e.stopPropagation()}
          >
            {PALETTES.map((p, i) => (
              <button
                key={p[0]}
                type="button"
                aria-label={p[0]}
                title={p[0]}
                onClick={() => { setPi(i); }}
                style={{ width: 22, height: 22, padding: 0, borderRadius: 999, cursor: 'pointer', background: p[1], border: `1.5px solid ${i === pi ? pal.text : '#3A3A3A'}`, display: 'flex', alignItems: 'center', justifyContent: 'center', transform: i === pi ? 'scale(1.1)' : 'none' }}
              >
                <span style={{ width: 10, height: 10, borderRadius: 999, background: p[2] }} />
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}