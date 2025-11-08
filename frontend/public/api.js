<script>
(() => {
  const DEFAULT_BASE = 'https://fake-news-detection-18.onrender.com/api/v1';

  const readBase = () => (localStorage.getItem('apiBase') || DEFAULT_BASE).replace(/\/+$/, '');
  const saveBase = (url) => {
    if (!url) return;
    const clean = url.trim().replace(/\/+$/, '');
    if (!/^https?:\/\//i.test(clean)) throw new Error('API base must start with http(s)://');
    localStorage.setItem('apiBase', clean);
  };

  // tiny fetch with timeout helper
  async function fx(url, opts = {}, timeoutMs = 15000) {
    const ctrl = new AbortController();
    const t = setTimeout(() => ctrl.abort(), timeoutMs);
    try {
      const res = await fetch(url, { ...opts, signal: ctrl.signal });
      const body = await (res.headers.get('content-type')?.includes('application/json') ? res.json() : res.text());
      if (!res.ok) {
        const detail = typeof body === 'string' ? body : JSON.stringify(body);
        throw new Error(`HTTP ${res.status} ${res.statusText} :: ${detail}`);
      }
      return body;
    } finally { clearTimeout(t); }
  }

  async function analyze({ title, content, source }) {
    const base = readBase();
    return fx(`${base}/analyze`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title, content, source })
    });
  }

  async function stats() {
    const base = readBase();
    return fx(`${base}/stats`);
  }

  function setBase(url) { saveBase(url); }
  function getBase() { return readBase(); }

  // Expose API
  window.api = { analyze, stats, setBase, getBase };

  // Optional: wire a “set API” button if present
  window.addEventListener('DOMContentLoaded', () => {
    console.log('[UI] API base =', getBase());
    const btn = document.getElementById('btnApi');
    if (btn) {
      btn.addEventListener('click', () => {
        const next = prompt('Set API base URL (e.g., https://fake-news-detection-18.onrender.com/api/v1):', getBase());
        if (next !== null) {
          try { setBase(next); alert(`API base set to: ${getBase()}`); }
          catch (e) { alert(e.message); }
        }
      });
    }
  });
})();
</script>
