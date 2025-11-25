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

    // Create uncertainty gauge (entropy visualization)
    function createUncertaintyGauge(canvasId, entropy) {
        if (!isChartJsAvailable()) return null;

        destroyChart(canvasId);

        const percentage = Math.round(entropy * 100);
        const remaining = 100 - percentage;

        // Color based on entropy: 0-0.2 = green (confident), 0.2-0.5 = yellow, 0.5-1.0 = red (uncertain)
        let color;
        let label;
        if (entropy < 0.2) {
            color = '#10b981'; // green
            label = 'Highly Confident';
        } else if (entropy < 0.5) {
            color = '#eab308'; // yellow
            label = 'Moderately Confident';
        } else {
            color = '#ef4444'; // red
            label = 'Uncertain';
        }

        const ctx = document.getElementById(canvasId).getContext('2d');
        const chart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: [`Uncertainty (${percentage}%)`, 'Certainty'],
                datasets: [{
                    data: [percentage, remaining],
                    backgroundColor: [color, 'rgba(200, 200, 200, 0.2)'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                cutout: '70%',
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: (context) => {
                                return context.label + ': ' + context.parsed + '%';
                            }
                        }
                    }
                }
            },
            plugins: [{
                id: 'centerText',
                beforeDraw: (chart) => {
                    const width = chart.width;
                    const height = chart.height;
                    const ctx = chart.ctx;
                    ctx.restore();
                    ctx.font = 'bold 16px sans-serif';
                    ctx.textBaseline = 'middle';
                    ctx.fillStyle = color;
                    const text = label;
                    const textX = Math.round((width - ctx.measureText(text).width) / 2);
                    const textY = height / 2;
                    ctx.fillText(text, textX, textY);
                    ctx.save();
                }
            }]
        });

        chartInstances[canvasId] = chart;
        return chart;
    }

    // Create confidence interval band chart
    function createConfidenceBandChart(canvasId, credibilityScore, ciLower, ciUpper) {
        if (!isChartJsAvailable() || ciLower === undefined || ciUpper === undefined) return null;

        destroyChart(canvasId);

        const ctx = document.getElementById(canvasId).getContext('2d');
        const chart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Credibility Score'],
                datasets: [{
                    label: '95% Confidence Interval',
                    data: [[ciLower * 100, ciUpper * 100]],
                    backgroundColor: 'rgba(139, 92, 246, 0.3)',
                    borderColor: '#8b5cf6',
                    borderWidth: 2,
                    borderSkipped: false
                }, {
                    label: 'Point Estimate',
                    data: [credibilityScore * 100],
                    backgroundColor: '#6366f1',
                    borderWidth: 0,
                    barThickness: 8
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: true, position: 'bottom' },
                    tooltip: {
                        callbacks: {
                            label: (context) => {
                                if (context.datasetIndex === 0) {
                                    return `CI: [${ciLower.toFixed(2)}, ${ciUpper.toFixed(2)}]`;
                                }
                                return `Score: ${credibilityScore.toFixed(2)}`;
                            }
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

    // Create uncertainty over time chart
    function createUncertaintyTimeSeriesChart(canvasId, points) {
        if (!isChartJsAvailable() || !points || points.length === 0) return null;

        destroyChart(canvasId);

        const labels = points.map((p, i) => {
            const date = new Date(p.timestamp);
            return `#${i + 1} ${date.toLocaleTimeString()}`;
        });

        const credibilityData = points.map(p => p.credibilityScore * 100);
        const entropyData = points.map(p => (p.entropy !== null && p.entropy !== undefined) ? p.entropy * 100 : null);

        const datasets = [{
            label: 'Credibility Risk %',
            data: credibilityData,
            borderColor: '#8b5cf6',
            backgroundColor: 'rgba(139, 92, 246, 0.1)',
            fill: true,
            tension: 0.4,
            pointRadius: 4,
            yAxisID: 'y'
        }];

        // Only add entropy if we have data
        if (entropyData.some(v => v !== null)) {
            datasets.push({
                label: 'Uncertainty (Entropy) %',
                data: entropyData,
                borderColor: '#f59e0b',
                backgroundColor: 'rgba(245, 158, 11, 0.1)',
                fill: false,
                tension: 0.4,
                pointRadius: 4,
                borderDash: [5, 5],
                yAxisID: 'y'
            });
        }

        const ctx = document.getElementById(canvasId).getContext('2d');
        const chart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: datasets
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: {
                    mode: 'index',
                    intersect: false
                },
                plugins: {
                    legend: { display: true, position: 'top' }
                },
                scales: {
                    y: {
                        type: 'linear',
                        display: true,
                        position: 'left',
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
        createUncertaintyGauge,
        createConfidenceBandChart,
        createUncertaintyTimeSeriesChart,
        destroyChart,
        destroyAllCharts
    };
})();

// Export for use in main app
window.Visualizations = Visualizations;
