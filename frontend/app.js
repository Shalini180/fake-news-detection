/**
 * RESEARCH DASHBOARD UI - UX SPECIFICATION & ARCHITECTURE
 * 
 * LAYOUT ARCHITECTURE:
 * ├─ Query Panel (Left Sidebar - 320px)
 * │  ├─ Article input form (title, content, source)
 * │  ├─ Demo mode selector (5 pre-configured scenarios)
 * │  └─ Analysis trigger button
 * │
 * ├─ Results Workspace (Center - Flex 1, Tabbed Interface)
 * │  ├─ Overview Tab: Radial gauge, risk badge, key reasons summary
 * │  ├─ Explanations Tab: Token attention heatmap, feature importance bars, suspicious phrases
 * │  ├─ Claims & Evidence Tab: Extracted claims with verification status, knowledge graph viz
 * │  ├─ Temporal & Comparative Tab: Session time series, distribution charts, comparison controls
 * │  └─ System Metrics Tab: Statistics dashboard, model limitations, session export
 * │
 * └─ History Panel (Right Sidebar - 280px, Collapsible on Mobile)
 *    ├─ Session history list (localStorage, last 50 analyses)
 *    ├─ Clickable items to reload past results
 *    ├─ Selection for side-by-side comparison
 *    └─ Clear history button
 * 
 * KEY FEATURES:
 * - Session History: Auto-saves all analyses to localStorage, filterable, exportable
 * - Demo Mode: 5 realistic scenarios for offline testing/demos (high-risk fake, credible news, etc.)
 * - Advanced Viz: Chart.js gauges, time series, distributions; SVG knowledge graphs
 * - Temporal Analytics: Track credibility evolution across session
 * - Comparison: Side-by-side analysis of any 2 history items
 * - Responsive: Desktop 3-panel → Tablet 2-panel → Mobile stacked with overlays
 * - Theme: Persisted light/dark toggle
 * 
 * MODULES USED:
 * - demo-data.js: DEMO_DATA object with 5 scenarios
 * - session.js: SessionManager (addAnalysis, getAnalyses, exportSession, etc.)
 * - visualizations.js: Visualizations (Chart.js wrappers)
 * - graph-viz.js: GraphViz (SVG knowledge graph renderer)
 * - api.js: API client for backend communication
 */

// ============================================================================
// STATE MANAGEMENT
// ============================================================================

const AppState = {
    currentResult: null,
    selectedForComparison: [],
    activeClaimFilter: 'all'
};

// ============================================================================
// INITIALIZATION
// ============================================================================

document.addEventListener('DOMContentLoaded', () => {
    initializeDashboard();
});

function initializeDashboard() {
    console.log('🚀 Initializing Research Dashboard...');

    // Initialize tabs
    initializeTabs();

    // Initialize demo mode
    initializeDemoMode();

    // Initialize session history
    loadSessionHistory();

    // Initialize comparison functionality
    initializeComparison();

    // Initialize history panel (mobile toggle)
    initializeHistoryPanel();

    // Hook export button
    document.getElementById('btnExportSession').addEventListener('click', () => {
        SessionManager.exportSession();
        showToast('Session exported successfully!');
    });

    // Hook clear history button
    document.getElementById('btnClearHistory').addEventListener('click', () => {
        if (confirm('Clear all session history?')) {
            SessionManager.clearHistory();
            loadSessionHistory();
            updateTemporalAnalytics();
            showToast('History cleared');
        }
    });

    // Initialize theme toggle
    initializeTheme();

    // Initialize claim filters
    initializeClaimFilters();

    console.log('✅ Dashboard initialized');
}

// ============================================================================
// TAB MANAGEMENT
// ============================================================================

function initializeTabs() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabPanels = document.querySelectorAll('.tab-panel');

    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetTab = btn.getAttribute('data-tab');

            // Update button states
            tabButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            // Update panel visibility
            tabPanels.forEach(panel => {
                if (panel.id === `tab-${targetTab}`) {
                    panel.classList.add('active');
                } else {
                    panel.classList.remove('active');
                }
            });
        });
    });
}

// ============================================================================
// DEMO MODE
// ============================================================================

function initializeDemoMode() {
    const btnDemoMode = document.getElementById('btnDemoMode');
    const demoSelector = document.getElementById('demoSelector');
    const btnLoadDemo = document.getElementById('btnLoadDemo');

    btnDemoMode.addEventListener('click', () => {
        demoSelector.classList.toggle('hidden');
    });

    btnLoadDemo.addEventListener('click', () => {
        const scenarioKey = document.getElementById('demoSelect').value;
        loadDemoScenario(scenarioKey);
    });
}

function loadDemoScenario(scenarioKey) {
    const demo = window.DEMO_DATA[scenarioKey];
    if (!demo) {
        showToast('Demo scenario not found');
        return;
    }

    console.log('Loading demo scenario:', scenarioKey);

    // Populate form fields
    document.getElementById('articleTitle').value = demo.title;
    document.getElementById('articleContent').value = demo.content;
    document.getElementById('articleSource').value = demo.source;

    // Render results (demo response is already in the correct format)
    renderResults(demo.response);

    // Add to session history
    SessionManager.addAnalysis(demo.response);

    // Update history display and temporal analytics
    loadSessionHistory();
    updateTemporalAnalytics();

    showToast('✅ Demo data loaded');
}

// ============================================================================
// ARTICLE ANALYSIS (API Integration)
// ============================================================================

async function analyzeArticle() {
    const title = document.getElementById('articleTitle').value.trim();
    const content = document.getElementById('articleContent').value.trim();
    const source = document.getElementById('articleSource').value.trim();

    if (!content) {
        showToast('❌ Please enter article content');
        document.getElementById('articleContent').focus();
        return;
    }

});

if (!response.ok) {
    throw new Error(`API error: ${response.status}`);
}

const result = await response.json();
console.log('✅ API Result:', result);

// Render results
renderResults(result);

// Add to session
SessionManager.addAnalysis(result);

// Update UI
loadSessionHistory();
updateTemporalAnalytics();

showToast('✅ Analysis complete');

    } catch (error) {
    console.error('❌ API Error:', error);
    showToast('⚠️ API unavailable - using local simulation');

    // Fallback simulation
    const simulated = simulateAnalysis(title, content, source);
    renderResults(simulated);
    SessionManager.addAnalysis(simulated);
    loadSessionHistory();
    updateTemporalAnalytics();

} finally {
    btnAnalyze.disabled = false;
    btnAnalyze.textContent = '🚀 Analyze Article';
}
}

// Simple simulation fallback
function simulateAnalysis(title, content, source) {
    const score = Math.random() * 0.6 + 0.2; // Random score between 0.2-0.8
    const riskLevel = SessionManager.getRiskLevel(score);

    return {
        articleId: `sim-${Date.now()}`,
        title: title || 'Untitled',
        source: source || 'Unknown',
        credibilityScore: score,
        classification: riskLevel.level,
        explanation: 'Simulated analysis (backend unavailable)',
        featureScores: {
            content_analysis: score,
            domain_credibility: score * 0.9,
            claims_verification: score * 1.1,
            cross_reference: score * 0.8
        },
        keyReasons: ['Backend unavailable - showing simulated results'],
        riskLevel: riskLevel,
        extractedClaims: [],
        attentionTokens: [],
        topWords: [],
        suspiciousPhrases: []
    };
}

// ============================================================================
// RESULT RENDERING
// ============================================================================

function renderResults(result) {
    console.log('Rendering results:', result);
    AppState.currentResult = result;

    // 1. Render Overview Tab
    renderOverview(result);

    // 2. Render Explanations Tab
    renderExplanations(result);

    // 3. Render Claims & Evidence Tab
    renderClaimsAndEvidence(result);

    // 4. Update temporal analytics (charts)
    updateTemporalAnalytics();
}

function renderOverview(result) {
    const score = result.credibilityScore || 0;

    // Create credibility gauge using Visualizations module
    if (window.Visualizations && window.Visualizations.isChartJsAvailable()) {
        Visualizations.createCredibilityGauge('credibilityGauge', score);
    }

    // Update gauge text
    document.getElementById('gaugeScore').textContent = Math.round(score * 100) + '%';
    document.getElementById('gaugeLabel').textContent = getClassificationLabel(score);

    // Update risk badge
    const riskLevel = result.riskLevel || SessionManager.getRiskLevel(score);
    const riskBadge = document.getElementById('riskBadge');
    riskBadge.innerHTML = `
    <span class="risk-icon">${riskLevel.icon}</span>
    <span class="risk-text">${riskLevel.level} RISK</span>
  `;
    riskBadge.style.background = riskLevel.color;

    // Render key reasons
    const keyReasonsList = document.getElementById('keyReasonsList');
    const reasons = result.keyReasons || [];

    if (reasons.length > 0) {
        keyReasonsList.innerHTML = reasons.map(reason => `
      <li>${escapeHtml(reason)}</li>
    `).join('');
    } else {
        keyReasonsList.innerHTML = '<li class="hint">No specific reasons identified</li>';
    }

    // NEW: Render uncertainty metrics
    renderUncertaintyOverview(result);
}

function renderExplanations(result) {
    // 1. Attention heatmap
    const attentionHeatmap = document.getElementById('attentionHeatmap');
    const tokens = result.attentionTokens || [];

    if (tokens.length > 0) {
        attentionHeatmap.innerHTML = tokens.slice(0, 100).map(t => {
            const weight = t.weight || 0;
            const intensity = Math.floor(weight * 255);
            const bgColor = `rgba(${255 - intensity}, ${Math.floor((1 - weight) * 200)}, 0, ${weight * 0.8 + 0.2})`;

            return `<span class="attention-token" style="background: ${bgColor};" title="Attention: ${weight.toFixed(3)}">${escapeHtml(t.token)}</span>`;
        }).join(' ');
    } else {
        attentionHeatmap.innerHTML = '<span class="hint">No attention data available</span>';
    }

    // 2. Feature importance chart
    if (window.Visualizations && result.featureScores) {
        Visualizations.createFeatureChart('featureChart', result.featureScores);
    }

    // 3. Suspicious phrases
    const suspiciousPhrases = document.getElementById('suspiciousPhrases');
    const phrases = result.suspiciousPhrases || [];

    if (phrases.length > 0) {
        suspiciousPhrases.innerHTML = phrases.map(p => `
      <div class="suspicious-phrase-item">
        <div class="phrase-text">${escapeHtml(p.phrase)}</div>
        <div class="phrase-reason">${escapeHtml(p.reason)}</div>
      </div>
    `).join('');
    } else {
        suspiciousPhrases.innerHTML = '<div class="hint">No suspicious phrases detected</div>';
    }

    // NEW: Render uncertainty breakdown
    renderUncertaintyBreakdown(result.uncertainty);
}

function renderClaimsAndEvidence(result) {
    const claimsList = document.getElementById('claimsList');
    const claims = result.extractedClaims || [];

    if (claims.length > 0) {
        claimsList.innerHTML = claims.map((claim, i) => `
      <div class="claim-item" data-status="${claim.status || 'UNVERIFIED'}">
        <div class="claim-text">"${escapeHtml(claim.text)}"</div>
        <div class="claim-meta">
          <span class="claim-status status-${claim.status || 'UNVERIFIED'}">
            ${claim.status || 'UNVERIFIED'}
          </span>
          ${claim.evidenceDensity !== undefined ? `<span>Evidence: ${claim.evidenceDensity}/10</span>` : ''}
        </div>
      </div>
    `).join('');
    } else {
        claimsList.innerHTML = '<div class="hint">No claims extracted</div>';
    }

    // Apply current filter
    applyClaimFilter(AppState.activeClaimFilter);

    // Render knowledge graph
    if (window.GraphViz) {
        GraphViz.createSimpleGraph('knowledgeGraph', {
            article: { title: result.title || 'Article' },
            claims: claims,
            evidenceSources: { count: claims.reduce((sum, c) => sum + (c.evidenceDensity || 0), 0) }
        });
    }
}

// ============================================================================
// SESSION HISTORY
// ============================================================================

function loadSessionHistory() {
    const analyses = SessionManager.getAnalyses();
    const historyList = document.getElementById('historyList');

    if (analyses.length === 0) {
        historyList.innerHTML = '<p class="hint">No analyses yet</p>';
        return;
    }

    // Reverse to show most recent first
    const recentFirst = [...analyses].reverse();

    historyList.innerHTML = recentFirst.map(a => {
        const isSelected = AppState.selectedForComparison.includes(a.id);
        const selectedClass = isSelected ? 'selected' : '';

        return `
      <div class="history-item risk-${a.riskLevel.level.toLowerCase()} ${selectedClass}" 
           onclick="loadHistoryItem('${a.id}')"
           ondblclick="toggleComparisonSelection('${a.id}')">
        <div class="history-item-header">
          <span class="history-item-title" title="${escapeHtml(a.title)}">
            ${truncate(a.title, 30)}
          </span>
          <span class="history-item-badge">${a.riskLevel.icon}</span>
        </div>
        <div class="history-item-meta">
          <span class="history-item-timestamp">${formatTime(a.timestamp)}</span>
          <span class="history-item-score">${Math.round(a.credibilityScore * 100)}%</span>
        </div>
        ${isSelected ? '<div style="font-size: 10px; color: white; margin-top: 4px;">✓ Selected for comparison</div>' : ''}
      </div>
    `;
    }).join('');
}

function loadHistoryItem(id) {
    const analysis = SessionManager.getAnalysisById(id);
    if (!analysis) return;

    console.log('Loading history item:', id);
    renderResults(analysis.result);
    showToast('Loaded from history');
}

// ============================================================================
// TEMPORAL ANALYTICS
// ============================================================================

function updateTemporalAnalytics() {
    const timeSeriesData = SessionManager.getTimeSeriesData();
    const stats = SessionManager.getStatistics();

    // Update time series chart
    if (window.Visualizations && timeSeriesData.length > 0) {
        Visualizations.createTimeSeriesChart('timeSeriesChart', timeSeriesData);
        Visualizations.createDistributionChart('distributionChart', stats.distribution);
    }

    // Update system metrics
    document.getElementById('metricTotal').textContent = stats.total;
    document.getElementById('metricAvg').textContent = (stats.avgScore * 100).toFixed(1) + '%';
    document.getElementById('metricHigh').textContent = stats.distribution.HIGH || 0;
}

// ============================================================================
// COMPARISON
// ============================================================================

function initializeComparison() {
    const btnCompare = document.getElementById('btnCompare');
    const modal = document.getElementById('comparisonModal');
    const btnClose = document.getElementById('btnCloseComparison');

    btnCompare.addEventListener('click', showComparison);
    btnClose.addEventListener('click', () => modal.classList.remove('active'));

    // Close on overlay click
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.classList.remove('active');
        }
    });
}

function toggleComparisonSelection(id) {
    const index = AppState.selectedForComparison.indexOf(id);

    if (index > -1) {
        AppState.selectedForComparison.splice(index, 1);
    } else {
        if (AppState.selectedForComparison.length >= 2) {
            AppState.selectedForComparison.shift(); // Remove oldest
        }
        AppState.selectedForComparison.push(id);
    }

    // Update button and history display
    const btnCompare = document.getElementById('btnCompare');
    btnCompare.textContent = `Compare Selected (${AppState.selectedForComparison.length}/2)`;
    btnCompare.disabled = AppState.selectedForComparison.length !== 2;

    loadSessionHistory();
}

function showComparison() {
    if (AppState.selectedForComparison.length !== 2) return;

    const analysis1 = SessionManager.getAnalysisById(AppState.selectedForComparison[0]);
    const analysis2 = SessionManager.getAnalysisById(AppState.selectedForComparison[1]);

    if (!analysis1 || !analysis2) return;

    const grid = document.getElementById('comparisonGrid');
    grid.innerHTML = `
    ${renderComparisonColumn(analysis1)}
    ${renderComparisonColumn(analysis2)}
  `;

    document.getElementById('comparisonModal').classList.add('active');
}

function renderComparisonColumn(analysis) {
    const result = analysis.result;
    const score = result.credibilityScore || 0;

    return `
    <div class="comparison-column">
      <h3>${escapeHtml(analysis.title)}</h3>
      <div style="margin: 16px 0;">
        <div style="font-size: 48px; font-weight: 900; text-align: center;">${Math.round(score * 100)}%</div>
        <div style="text-align: center; color: var(--muted);">Risk Score</div>
      </div>
      <div style="margin: 12px 0;">
        <strong>Classification:</strong> ${result.classification || 'Unknown'}
      </div>
      <div style="margin: 12px 0;">
        <strong>Source:</strong> ${escapeHtml(analysis.source || 'Unknown')}
      </div>
      <div style="margin: 12px 0;">
        <strong>Claims:</strong> ${(result.extractedClaims || []).length}
      </div>
      <div style="margin: 12px 0;">
        <strong>Timestamp:</strong> ${new Date(analysis.timestamp).toLocaleString()}
      </div>
    </div>
  `;
}

// ============================================================================
// CLAIM FILTERS
// ============================================================================

function initializeClaimFilters() {
    const filterButtons = document.querySelectorAll('.claims-filter .filter-btn');

    filterButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const filter = btn.getAttribute('data-filter');

            // Update button states
            filterButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            // Apply filter
            AppState.activeClaimFilter = filter;
            applyClaimFilter(filter);
        });
    });
}

function applyClaimFilter(filter) {
    const claimItems = document.querySelectorAll('.claim-item');

    claimItems.forEach(item => {
        const status = item.getAttribute('data-status');

        if (filter === 'all' || status === filter) {
            item.style.display = 'block';
        } else {
            item.style.display = 'none';
        }
    });
}

// ============================================================================
// HISTORY PANEL (Mobile)
// ============================================================================

function initializeHistoryPanel() {
    const btnToggle = document.getElementById('btnToggleHistory');
    const historyPanel = document.querySelector('.history-panel');
    const overlay = document.getElementById('historyOverlay');

    if (!btnToggle) return;

    btnToggle.addEventListener('click', () => {
        historyPanel.classList.toggle('active');
        overlay.classList.toggle('active');
    });

    overlay.addEventListener('click', () => {
        historyPanel.classList.remove('active');
        overlay.classList.remove('active');
    });

    // Show toggle button only on small screens
    if (window.innerWidth < 1400) {
        btnToggle.style.display = 'inline-block';
    }

    window.addEventListener('resize', () => {
        if (window.innerWidth < 1400) {
            btnToggle.style.display = 'inline-block';
        } else {
            btnToggle.style.display = 'none';
            historyPanel.classList.remove('active');
            overlay.classList.remove('active');
        }
    });
}

// ============================================================================
// THEME TOGGLE
// ============================================================================

function initializeTheme() {
    const btnTheme = document.getElementById('btnTheme');

    // Load saved theme
    const savedTheme = localStorage.getItem('theme') || 'dark';
    document.documentElement.setAttribute('data-theme', savedTheme);

    btnTheme.addEventListener('click', () => {
        const current = document.documentElement.getAttribute('data-theme');
        const newTheme = current === 'dark' ? 'light' : 'dark';

        document.documentElement.setAttribute('data-theme', newTheme);
        localStorage.setItem('theme', newTheme);

        showToast(`Theme: ${newTheme} mode`);
    });
}

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

function showToast(message) {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.textContent = message;

    container.appendChild(toast);

    setTimeout(() => {
        toast.remove();
    }, 3000);
}

function truncate(text, maxLength) {
    if (!text) return '';
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength - 3) + '...';
}

function formatTime(timestamp) {
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now - date;

    // Less than 1 minute
    if (diff < 60000) return 'Just now';

    // Less than 1 hour
    if (diff < 3600000) {
        const mins = Math.floor(diff / 60000);
        return `${mins}m ago`;
    }

    // Less than 24 hours
    if (diff < 86400000) {
        const hours = Math.floor(diff / 3600000);
        return `${hours}h ago`;
    }

    // Otherwise, show date
    return date.toLocaleDateString();
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function getClassificationLabel(score) {
    if (score > 0.7) return 'Likely Fake';
    if (score > 0.5) return 'Suspicious';
    if (score > 0.3) return 'Mixed Signals';
    return 'Likely Credible';
}

// ============================================================================
// UNCERTAINTY RENDERING HELPERS
// ============================================================================

function renderUncertaintyOverview(result) {
    const uncertainty = result.uncertainty || null;
    const summaryEl = document.getElementById('uncertaintySummary');
    const hintEl = document.getElementById('ciTextHint');

    if (!uncertainty) {
        if (window.Visualizations) {
            Visualizations.destroyChart('uncertaintyGauge');
            Visualizations.destroyChart('confidenceBandChart');
        }
        if (summaryEl) summaryEl.textContent = '';
        if (hintEl) hintEl.textContent = '';
        return;
    }

    // Render uncertainty gauge
    if (window.Visualizations && uncertainty.entropy !== undefined) {
        Visualizations.createUncertaintyGauge('uncertaintyGauge', uncertainty.entropy);
    }

    // Render confidence interval chart
    if (window.Visualizations && uncertainty.confidenceInterval) {
        const [ciLower, ciUpper] = uncertainty.confidenceInterval;
        Visualizations.createConfidenceBandChart(
            'confidenceBandChart',
            result.credibilityScore,
            ciLower,
            ciUpper
        );
    }

    // Update summary text
    if (summaryEl) {
        const entropy = uncertainty.entropy || 0;
        let desc = entropy < 0.2 ? 'highly confident' : entropy < 0.5 ? 'moderately confident' : 'uncertain';
        summaryEl.textContent = `Entropy ${entropy.toFixed(3)} – model is ${desc} about this classification.`;
    }

    // Update CI hint
    if (hintEl && uncertainty.confidenceInterval) {
        const [lower, upper] = uncertainty.confidenceInterval;
        const width = upper - lower;
        if (width < 0.1) {
            hintEl.textContent = 'Narrow CI indicates precise estimate with low variance across MC samples.';
        } else if (width < 0.3) {
            hintEl.textContent = 'Moderate CI width suggests some model disagreement across dropout samples.';
        } else {
            hintEl.textContent = 'Wide CI indicates high uncertainty – model predictions vary significantly.';
        }
    }
}

function renderUncertaintyBreakdown(uncertainty) {
    const el = document.getElementById('uncertaintyBreakdown');
    if (!el) return;

    if (!uncertainty) {
        el.innerHTML = '<p class="hint">No uncertainty data available.</p>';
        return;
    }

    const { entropy, variance, stdDev, mcSamples, confidenceInterval } = uncertainty;
    const [ciLow, ciHigh] = confidenceInterval || [];

    el.innerHTML = `
        <ul style="list-style: none; padding: 0;">
            <li style="padding: 8px 0; border-bottom: 1px solid var(--muted);"><strong>Entropy:</strong> ${entropy?.toFixed(3) ?? 'N/A'}</li>
            <li style="padding: 8px 0; border-bottom: 1px solid var(--muted);"><strong>Variance:</strong> ${variance?.toExponential(3) ?? 'N/A'}</li>
            <li style="padding: 8px 0; border-bottom: 1px solid var(--muted);"><strong>Std Dev:</strong> ${stdDev?.toFixed(3) ?? 'N/A'}</li>
            <li style="padding: 8px 0; border-bottom: 1px solid var(--muted);"><strong>MC Samples:</strong> ${mcSamples ?? 'N/A'}</li>
            <li style="padding: 8px 0;"><strong>95% CI:</strong> [${ciLow?.toFixed(2) ?? '--'}, ${ciHigh?.toFixed(2) ?? '--'}]</li>
        </ul>
        <p class="hint" style="margin-top: 12px; font-size: 12px;">
            Lower entropy and variance indicate more reliable predictions. High entropy suggests the model is unsure.
        </p>
    `;
}

// ============================================================================
// GLOBAL ERROR HANDLING
// ============================================================================

window.addEventListener('error', (e) => {
    console.error('Global error:', e.error);
});

window.addEventListener('unhandledrejection', (e) => {
    console.error('Unhandled promise rejection:', e.reason);
});

console.log('✅ app.js loaded successfully');