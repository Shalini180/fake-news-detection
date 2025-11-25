/**
 * Visualization Module
 * Advanced Chart.js-based visualizations for research dashboard
 * Requires Chart.js to be loaded via CDN
 */

const Visualizations = (function () {
    let chartInstances = {};

    // Check if Chart.js is available
    function isChartJsAvailable() {
        return typeof Chart !== 'undefined';
    }

    // Destroy existing chart if it exists
    function destroyChart(chartId) {
        if (chartInstances[chartId]) {
            chartInstances[chartId].destroy();
            delete chartInstances[chartId];
        }
    }

    // Create radial credibility gauge (doughnut chart)
    function createCredibilityGauge(canvasId, score) {
        if (!isChartJsAvailable()) {
            console.warn('Chart.js not loaded');
            return null;
        }

        destroyChart(canvasId);

        const percentage = Math.round(score * 100);
        const remaining = 100 - percentage;

        // Color based on score
        let color;
        if (score > 0.7) color = '#ef4444'; // red
        else if (score > 0.5) color = '#f59e0b'; // orange
        else if (score > 0.3) color = '#eab308'; // yellow
        else color = '#10b981'; // green

        const ctx = document.getElementById(canvasId).getContext('2d');
        const chart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Risk Score', 'Credibility'],
                datasets: [{
                    data: [percentage, remaining],
                    backgroundColor: [color, 'rgba(200, 200, 200, 0.2)'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                cutout: '75%',
                plugins: {
                    legend: { display: false },
                    tooltip: { enabled: false }
                }
            }
        });

        chartInstances[canvasId] = chart;
        return chart;
    }

    // Create feature importance bar chart
    function createFeatureChart(canvasId, features) {
        if (!isChartJsAvailable()) return null;

        destroyChart(canvasId);

        const labels = Object.keys(features);
        const data = Object.values(features).map(v => v * 100);
        const colors = data.map(v => {
            if (v > 70) return '#ef4444';
            if (v > 50) return '#f59e0b';
            if (v > 30) return '#eab308';
            return '#10b981';
        });

        const ctx = document.getElementById(canvasId).getContext('2d');
        const chart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Risk Score (%)',
                    data: data,
                    backgroundColor: colors,
                    borderRadius: 6
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: (context) => `${context.parsed.x.toFixed(1)}% risk`
                        }
                    }
                },
                scales: {
                    x: {
                        beginAtZero: true,
                        max: 100,
                        ticks: { callback: (value) => value + '%' }
                    }
                }
            }
        });

        chartInstances[canvasId] = chart;
        return chart;
    }

    // Create time series line chart for temporal analytics
    function createTimeSeriesChart(canvasId, timeSeriesData) {
        if (!isChartJsAvailable() || !timeSeriesData || timeSeriesData.length === 0) return null;

        destroyChart(canvasId);

        const labels = timeSeriesData.map((d, i) => {
            const date = new Date(d.timestamp);
            return `#${i + 1} ${date.toLocaleTimeString()}`;
        });
        const data = timeSeriesData.map(d => d.score * 100);

        const ctx = document.getElementById(canvasId).getContext('2d');
        const chart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Credibility Risk %',
                    data: data,
                    borderColor: '#8b5cf6',
                    backgroundColor: 'rgba(139, 92, 246, 0.1)',
                    fill: true,
                    tension: 0.4,
                    pointRadius: 4,
                    pointHoverRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    tooltip: {
                        callbacks: {
                            label: (context) => {
                                const title = timeSeriesData[context.dataIndex].title;
                                return `${context.parsed.y.toFixed(1)}% risk - ${title.substring(0, 40)}...`;
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 100,
                        ticks: { callback: (value) => value + '%' }
                    }
                }
            }
        });

        chartInstances[canvasId] = chart;
        return chart;
    }

    // Create distribution histogram
    function createDistributionChart(canvasId, distribution) {
        if (!isChartJsAvailable()) return null;

        destroyChart(canvasId);

        const labels = ['SAFE', 'LOW', 'MEDIUM', 'HIGH'];
        const data = [
            distribution.SAFE || 0,
            distribution.LOW || 0,
            distribution.MEDIUM || 0,
            distribution.HIGH || 0
        ];
        const colors = ['#10b981', '#eab308', '#f59e0b', '#ef4444'];

        const ctx = document.getElementById(canvasId).getContext('2d');
        const chart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Count',
                    data: data,
                    backgroundColor: colors,
                    borderRadius: 8
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: { stepSize: 1 }
                    }
                }
            }
        });

        chartInstances[canvasId] = chart;
        return chart;
    }

    // Destroy all charts
    function destroyAllCharts() {
        Object.keys(chartInstances).forEach(id => destroyChart(id));
    }

    // Public API
    return {
        isChartJsAvailable,
        createCredibilityGauge,
        createFeatureChart,
        createTimeSeriesChart,
        createDistributionChart,
        destroyChart,
        destroyAllCharts
    };
})();

// Export for use in main app
window.Visualizations = Visualizations;
