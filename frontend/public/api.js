<script>
  (function () {
    // Base URL for the backend API. You can change it via localStorage.setItem('apiBase', 'https://your-api.onrender.com/api/v1')
    const DEFAULT = 'http://localhost:8080/api/v1';
    const getBase = () => localStorage.getItem('apiBase') || DEFAULT;

    async function analyze({ title, content, source }) {
      const base = getBase();
      const res = await fetch(`${base}/analyze`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title, content, source }),
      });
      if (!res.ok) {
        const text = await res.text().catch(() => '');
        throw new Error(`API ${res.status}: ${text || res.statusText}`);
      }
      return res.json();
    }

    // simple helper to set base at runtime
    function setBase(url) {
      if (url && url.trim()) {
        localStorage.setItem('apiBase', url.trim().replace(/\/+$/, ''));
      }
    }

    // Expose to window
    window.api = { analyze, setBase, getBase };
  })();
</script>
