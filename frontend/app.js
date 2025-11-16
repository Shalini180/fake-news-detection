// Configuration
const API_URL = 'http://localhost:8080/api/v1';

// State
let currentResults = null;

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    initializeApp();
});

function initializeApp() {
    // Theme toggle
    const themeToggle = document.getElementById('themeToggle');
    themeToggle.addEventListener('click', toggleTheme);

    // Load saved theme
    const savedTheme = localStorage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
    updateThemeIcon(savedTheme);

    // Form submission
    const form = document.getElementById('analysisForm');
    form.addEventListener('submit', handleFormSubmit);

    // Tab switching
    const tabs = document.querySelectorAll('.tab');
    tabs.forEach(tab => {
        tab.addEventListener('click', () => switchTab(tab.dataset.tab));
    });

    // Character counter
    const contentTextarea = document.getElementById('content');
    contentTextarea.addEventListener('input', updateCharacterCount);
}

// Theme Management
function toggleTheme() {
    const current = document.documentElement.getAttribute('data-theme');
    const newTheme = current === 'light' ? 'dark' : 'light';
    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
    updateThemeIcon(newTheme);
}

function updateThemeIcon(theme) {
    const sunIcon = document.querySelector('.sun-icon');
    const moonIcon = document.querySelector('.moon-icon');

    if (theme === 'dark') {
        sunIcon.classList.add('hidden');
        moonIcon.classList.remove('hidden');
    } else {
        sunIcon.classList.remove('hidden');
        moonIcon.classList.add('hidden');
    }
}

// Form Handling
async function handleFormSubmit(e) {
    e.preventDefault();

    const title = document.getElementById('title').value.trim();
    const content = document.getElementById('content').value.trim();
    const source = document.getElementById('source').value.trim();

    // Validation
    if (!title || !content) {
        showNotification('Please fill in all required fields', 'error');
        return;
    }

    if (content.length < 10) {
        showNotification('Content must be at least 10 characters', 'error');
        return;
    }

    // Show loading
    showLoading();
    hideResults();

    try {
        const response = await fetch(`${API_URL}/analyze`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                title,
                content,
                source: source || null
            })
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        currentResults = data;

        // Display results
        displayResults(data);

        // Smooth scroll to results
        setTimeout(() => {
            document.getElementById('resultsSection').scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }, 300);

    } catch (error) {
        console.error('Analysis error:', error);
        showNotification(
            'Failed to analyze article. Please try again.',
            'error'
        );
    } finally {
        hideLoading();
    }
}

// Display Results
function displayResults(data) {
    // Show results section
    const resultsSection = document.getElementById('resultsSection');
    resultsSection.classList.remove('hidden');

    // Update risk badge
    updateRiskBadge(data);

    // Update classification
    document.getElementById('classification').textContent = data.classification;

    // Update credibility score with animation
    animateScore(data.finalCredibilityScore);

    // Update overall assessment
    document.getElementById('overallAssessment').textContent = data.overallAssessment;

    // Update key reasons
    displayKeyReasons(data.keyReasons);

    // Update suspicious phrases
    displaySuspiciousPhrases(data.suspiciousPhrases);

    // Update top tokens
    displayTopTokens(data.topTokens);

    // Update claims
    displayClaims(data);

    // Update deep analysis
    displayDeepAnalysis(data);

    // Update processing time
    document.getElementById('processingTime').textContent =
        `Analyzed in ${data.processingTimeMs.toFixed(0)}ms`;
}

function updateRiskBadge(data) {
    const riskIcon = document.getElementById('riskIcon');
    const riskText = document.getElementById('riskText');
    const riskBadge = document.getElementById('riskBadge');

    riskIcon.textContent = data.riskIcon;
    riskText.textContent = data.riskLevel;

    // Update badge color based on risk
    riskBadge.className = 'risk-badge';
    switch(data.riskLevel) {
        case 'HIGH':
            riskBadge.classList.add('risk-high');
            break;
        case 'MEDIUM':
            riskBadge.classList.add('risk-medium');
            break;
        case 'LOW':
            riskBadge.classList.add('risk-low');
            break;
        case 'SAFE':
            riskBadge.classList.add('risk-safe');
            break;
    }
}

function animateScore(score) {
    const scoreValue = document.getElementById('scoreValue');
    const scoreCircle = document.getElementById('scoreCircle');
    const circumference = 565.48;

    // Animate number
    let current = 0;
    const target = Math.round(score * 100);
    const increment = target / 50;

    const numberInterval = setInterval(() => {
        current += increment;
        if (current >= target) {
            current = target;
            clearInterval(numberInterval);
        }
        scoreValue.textContent = Math.round(current);
    }, 20);

    // Animate circle
    const offset = circumference - (score * circumference);
    scoreCircle.style.strokeDashoffset = offset;

    // Update color based on score
    if (score >= 0.75) {
        scoreCircle.style.stroke = '#10b981'; // Green
    } else if (score >= 0.55) {
        scoreCircle.style.stroke = '#f59e0b'; // Yellow
    } else if (score >= 0.35) {
        scoreCircle.style.stroke = '#f97316'; // Orange
    } else {
        scoreCircle.style.stroke = '#ef4444'; // Red
    }
}

function displayKeyReasons(reasons) {
    const list = document.getElementById('keyReasonsList');
    list.innerHTML = '';

    if (!reasons || reasons.length === 0) {
        list.innerHTML = '<li class="no-data">No specific issues identified</li>';
        return;
    }

    reasons.forEach((reason, index) => {
        const li = document.createElement('li');
        li.className = 'reason-item';
        li.innerHTML = `
            <div class="reason-icon">${index + 1}</div>
            <div class="reason-text">${escapeHtml(reason)}</div>
        `;
        list.appendChild(li);
    });
}

function displaySuspiciousPhrases(phrases) {
    const container = document.getElementById('suspiciousPhrases');
    container.innerHTML = '';

    if (!phrases || phrases.length === 0) {
        container.innerHTML = '<p class="no-data">No suspicious phrases detected</p>';
        return;
    }

    phrases.forEach(phrase => {
        const badge = document.createElement('span');
        badge.className = 'phrase-badge';
        badge.textContent = phrase;
        container.appendChild(badge);
    });
}

function displayTopTokens(tokens) {
    const container = document.getElementById('topTokens');
    container.innerHTML = '';

    if (!tokens || tokens.length === 0) {
        container.innerHTML = '<p class="no-data">No significant tokens identified</p>';
        return;
    }

    // Take top 10 tokens
    const topTokens = tokens.slice(0, 10);
    const maxImportance = topTokens[0].importance;

    topTokens.forEach(item => {
        const tokenCard = document.createElement('div');
        tokenCard.className = 'token-card';

        const barWidth = (item.importance / maxImportance) * 100;

        tokenCard.innerHTML = `
            <div class="token-word">${escapeHtml(item.token)}</div>
            <div class="token-bar">
                <div class="token-bar-fill" style="width: ${barWidth}%"></div>
            </div>
            <div class="token-score">${item.importance.toFixed(3)}</div>
        `;

        container.appendChild(tokenCard);
    });
}

function displayClaims(data) {
    const totalClaims = data.claimCount || 0;
    const verifiedClaims = data.verifiedClaims || 0;
    const verificationRate = totalClaims > 0
        ? Math.round((verifiedClaims / totalClaims) * 100)
        : 0;

    document.getElementById('totalClaims').textContent = totalClaims;
    document.getElementById('verifiedClaims').textContent = verifiedClaims;
    document.getElementById('verificationRate').textContent = verificationRate + '%';

    const claimsList = document.getElementById('extractedClaimsList');
    claimsList.innerHTML = '';

    if (!data.extractedClaims || data.extractedClaims.length === 0) {
        claimsList.innerHTML = '<li class="no-data">No factual claims extracted</li>';
        return;
    }

    data.extractedClaims.forEach((claim, index) => {
        const li = document.createElement('li');
        li.className = 'claim-item';

        // Determine if verified (simplified - in production, track individual claims)
        const isVerified = index < verifiedClaims;

        li.innerHTML = `
            <div class="claim-status ${isVerified ? 'verified' : 'unverified'}">
                ${isVerified ? '✓' : '?'}
            </div>
            <div class="claim-text">${escapeHtml(claim)}</div>
        `;

        claimsList.appendChild(li);
    });
}

function displayDeepAnalysis(data) {
    // RoBERTa confidence
    const robertaConf = Math.round(data.robertaConfidence * 100);
    document.getElementById('robertaConfidence').textContent = robertaConf + '%';
    document.getElementById('robertaProgress').style.width = robertaConf + '%';

    // Domain score
    const domainScoreValue = Math.round(data.domainScore * 100);
    document.getElementById('domainScore').textContent = domainScoreValue + '%';
    document.getElementById('domainProgress').style.width = domainScoreValue + '%';

    // Sentiment
    const sentiment = data.sentimentScore || 0;
    const sentimentPercent = ((sentiment + 1) / 2) * 100; // Convert -1 to 1 → 0 to 100
    document.getElementById('sentimentScore').textContent =
        sentiment > 0 ? 'Positive' : sentiment < 0 ? 'Negative' : 'Neutral';
    document.getElementById('sentimentBar').style.left = sentimentPercent + '%';

    // Writing quality
    displayWritingQuality(data.writingQuality);

    // Component scores
    if (data.componentScores) {
        document.getElementById('contentScore').textContent =
            Math.round(data.componentScores.contentAnalysis * 100) + '%';
        document.getElementById('domainScoreBreakdown').textContent =
            Math.round(data.componentScores.domainCredibility * 100) + '%';
        document.getElementById('claimScore').textContent =
            Math.round(data.componentScores.claimVerification * 100) + '%';
        document.getElementById('crossRefScore').textContent =
            Math.round(data.componentScores.crossReference * 100) + '%';
    }
}

function displayWritingQuality(quality) {
    const container = document.getElementById('writingQualityDetails');

    if (!quality) {
        container.innerHTML = '<p class="no-data">No quality data available</p>';
        return;
    }

    container.innerHTML = `
        <div class="quality-item">
            <span>Exclamation marks:</span>
            <span class="${quality.exclamation_count > 3 ? 'warning' : ''}">${quality.exclamation_count}</span>
        </div>
        <div class="quality-item">
            <span>Capitalization ratio:</span>
            <span class="${quality.caps_ratio > 0.3 ? 'warning' : ''}">${(quality.caps_ratio * 100).toFixed(1)}%</span>
        </div>
        <div class="quality-item">
            <span>Clickbait score:</span>
            <span class="${quality.clickbait_score >= 3 ? 'warning' : ''}">${quality.clickbait_score}/10</span>
        </div>
        <div class="quality-item">
            <span>Avg sentence length:</span>
            <span>${quality.avg_sentence_length.toFixed(0)} chars</span>
        </div>
    `;
}

// Tab Management
function switchTab(tabName) {
    // Update tab buttons
    document.querySelectorAll('.tab').forEach(tab => {
        tab.classList.remove('active');
    });
    document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');

    // Update tab panels
    document.querySelectorAll('.tab-panel').forEach(panel => {
        panel.classList.add('hidden');
    });
    document.getElementById(tabName + 'Tab').classList.remove('hidden');
}

// Loading States
function showLoading() {
    document.getElementById('loadingState').classList.remove('hidden');
    document.getElementById('analyzeBtn').disabled = true;
}

function hideLoading() {
    document.getElementById('loadingState').classList.add('hidden');
    document.getElementById('analyzeBtn').disabled = false;
}

function hideResults() {
    document.getElementById('resultsSection').classList.add('hidden');
}

// Notifications
function showNotification(message, type = 'info') {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;

    // Add to page
    document.body.appendChild(notification);

    // Animate in
    setTimeout(() => notification.classList.add('show'), 10);

    // Remove after 3 seconds
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// Utilities
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function updateCharacterCount() {
    const content = document.getElementById('content');
    const count = content.value.length;
    const hint = content.nextElementSibling;
    hint.textContent = `${count} / 10,000 characters`;

    if (count > 10000) {
        hint.classList.add('error');
    } else {
        hint.classList.remove('error');
    }
}