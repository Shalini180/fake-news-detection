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
}) ();
</script >
