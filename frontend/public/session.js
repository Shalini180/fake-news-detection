/**
 * Session Management Module
 * Handles localStorage-based session history for research/analysis tracking
 */

const SessionManager = (function () {
    const STORAGE_KEY = 'fakeNews:sessionHistory';
    const MAX_HISTORY_SIZE = 50;

    // Get session history from localStorage
    function getHistory() {
        try {
            const data = localStorage.getItem(STORAGE_KEY);
            return data ? JSON.parse(data) : { analyses: [], metadata: { created: Date.now() } };
        } catch (e) {
            console.error('Failed to load session history:', e);
            return { analyses: [], metadata: { created: Date.now() } };
        }
    }

    // Save session history to localStorage
    function saveHistory(history) {
        try {
            // Enforce size limit
            if (history.analyses.length > MAX_HISTORY_SIZE) {
                history.analyses = history.analyses.slice(-MAX_HISTORY_SIZE);
            }
            localStorage.setItem(STORAGE_KEY, JSON.stringify(history));
            return true;
        } catch (e) {
            console.error('Failed to save session history:', e);
            // If quota exceeded, remove oldest half and try again
            if (e.name === 'QuotaExceededError') {
                history.analyses = history.analyses.slice(Math.floor(MAX_HISTORY_SIZE / 2));
                try {
                    localStorage.setItem(STORAGE_KEY, JSON.stringify(history));
                    return true;
                } catch (e2) {
                    console.error('Failed to save even after cleanup:', e2);
                    return false;
                }
            }
            return false;
        }
    }

    // Add new analysis to history
    function addAnalysis(result) {
        const history = getHistory();
        const analysis = {
            id: `analysis-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
            timestamp: Date.now(),
            title: result.title || 'Untitled Article',
            source: result.source || 'Unknown source',
            credibilityScore: result.credibilityScore || 0,
            classification: result.classification || 'UNKNOWN',
            riskLevel: getRiskLevel(result.credibilityScore || 0),
            result: result // Full result object
        };

        history.analyses.push(analysis);
        saveHistory(history);
        return analysis;
    }

    // Get risk level from credibility score
    function getRiskLevel(score) {
        if (score > 0.7) return { level: 'HIGH', icon: '🚨', color: '#ef4444' };
        if (score > 0.5) return { level: 'MEDIUM', icon: '⚠️', color: '#f59e0b' };
        if (score > 0.3) return { level: 'LOW', icon: '⚡', color: '#eab308' };
        return { level: 'SAFE', icon: '✅', color: '#10b981' };
    }

    // Get all analyses, optionally filtered
    function getAnalyses(filter = null) {
        const history = getHistory();
        if (!filter) return history.analyses;

        return history.analyses.filter(a => {
            if (filter.riskLevel && a.riskLevel.level !== filter.riskLevel) return false;
            if (filter.minScore !== undefined && a.credibilityScore < filter.minScore) return false;
            if (filter.maxScore !== undefined && a.credibilityScore > filter.maxScore) return false;
            if (filter.searchTerm) {
                const term = filter.searchTerm.toLowerCase();
                return a.title.toLowerCase().includes(term) ||
                    (a.source && a.source.toLowerCase().includes(term));
            }
            return true;
        });
    }

    // Get analysis by ID
    function getAnalysisById(id) {
        const history = getHistory();
        return history.analyses.find(a => a.id === id);
    }

    // Delete analysis by ID
    function deleteAnalysis(id) {
        const history = getHistory();
        history.analyses = history.analyses.filter(a => a.id !== id);
        saveHistory(history);
    }

    // Clear all history
    function clearHistory() {
        const history = { analyses: [], metadata: { created: Date.now() } };
        saveHistory(history);
    }

    // Get session statistics
    function getStatistics() {
        const analyses = getAnalyses();
        if (analyses.length === 0) {
            return {
                total: 0,
                avgScore: 0,
                distribution: { HIGH: 0, MEDIUM: 0, LOW: 0, SAFE: 0 },
                timeRange: null
            };
        }

        const distribution = { HIGH: 0, MEDIUM: 0, LOW: 0, SAFE: 0 };
        let totalScore = 0;

        analyses.forEach(a => {
            distribution[a.riskLevel.level]++;
            totalScore += a.credibilityScore;
        });

        return {
            total: analyses.length,
            avgScore: totalScore / analyses.length,
            distribution,
            timeRange: {
                earliest: Math.min(...analyses.map(a => a.timestamp)),
                latest: Math.max(...analyses.map(a => a.timestamp))
            }
        };
    }

    // Export session as JSON file
    function exportSession() {
        const history = getHistory();
        const stats = getStatistics();

        const exportData = {
            metadata: {
                exportDate: new Date().toISOString(),
                totalAnalyses: history.analyses.length,
                statistics: stats
            },
            analyses: history.analyses
        };

        const jsonString = JSON.stringify(exportData, null, 2);
        const blob = new Blob([jsonString], { type: 'application/json' });
        const url = URL.createObjectURL(blob);

        const a = document.createElement('a');
        a.href = url;
        a.download = `fake-news-session-${new Date().toISOString().split('T')[0]}.json`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    }

    // Get time series data for charting
    function getTimeSeriesData() {
        const analyses = getAnalyses().sort((a, b) => a.timestamp - b.timestamp);
        return analyses.map(a => ({
            timestamp: a.timestamp,
            date: new Date(a.timestamp),
            score: a.credibilityScore,
            title: a.title
        }));
    }

    // Public API
    return {
        addAnalysis,
        getAnalyses,
        getAnalysisById,
        deleteAnalysis,
        clearHistory,
        getStatistics,
        exportSession,
        getTimeSeriesData,
        getRiskLevel
    };
})();

// Export for use in main app
window.SessionManager = SessionManager;
