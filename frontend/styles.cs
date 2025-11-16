/* CSS Variables */
:root {
    --color-primary: #4F46E5;
    --color-primary-dark: #4338CA;
    --color-success: #10b981;
    --color-warning: #f59e0b;
    --color-danger: #ef4444;
    --color-info: #3b82f6;

    --color-bg: #ffffff;
    --color-bg-secondary: #f9fafb;
    --color-bg-tertiary: #f3f4f6;
    --color-text: #111827;
    --color-text-secondary: #6b7280;
    --color-border: #e5e7eb;

    --shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
    --shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1);
    --shadow-lg: 0 10px 15px -3px rgb(0 0 0 / 0.1);
    --shadow-xl: 0 20px 25px -5px rgb(0 0 0 / 0.1);

    --radius-sm: 0.375rem;
    --radius-md: 0.5rem;
    --radius-lg: 0.75rem;
    --radius-xl: 1rem;

    --transition: all 0.2s ease;
}

[data-theme="dark"] {
    --color-bg: #111827;
    --color-bg-secondary: #1f2937;
    --color-bg-tertiary: #374151;
    --color-text: #f9fafb;
    --color-text-secondary: #9ca3af;
    --color-border: #374151;
}

/* Reset & Base */
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

body {
    font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
    background: var(--color-bg);
    color: var(--color-text);
    line-height: 1.6;
    transition: var(--transition);
}

.container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 1.5rem;
}

/* Header */
.header {
    background: var(--color-bg);
    border-bottom: 1px solid var(--color-border);
    padding: 1rem 0;
    position: sticky;
    top: 0;
    z-index: 100;
    backdrop-filter: blur(8px);
}

.header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.logo {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    font-size: 1.25rem;
    font-weight: 600;
    color: var(--color-text);
}

.btn-icon {
    background: var(--color-bg-secondary);
    border: 1px solid var(--color-border);
    width: 40px;
    height: 40px;
    border-radius: var(--radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: var(--transition);
}

.btn-icon:hover {
    background: var(--color-bg-tertiary);
}

.hidden {
    display: none !important;
}

/* Hero Section */
.hero {
    text-align: center;
    padding: 4rem 0 2rem;
}

.hero h1 {
    font-size: 3rem;
    font-weight: 700;
    margin-bottom: 1rem;
    background: linear-gradient(135deg, var(--color-primary), #7c3aed);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.hero p {
    font-size: 1.25rem;
    color: var(--color-text-secondary);
    margin-bottom: 2rem;
}

.badge-container {
    display: flex;
    gap: 1rem;
    justify-content: center;
    flex-wrap: wrap;
}

.badge {
    padding: 0.5rem 1rem;
    background: var(--color-bg-secondary);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-lg);
    font-size: 0.875rem;
    font-weight: 500;
}

/* Card */
.card {
    background: var(--color-bg);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-xl);
    padding: 2rem;
    box-shadow: var(--shadow-md);
    margin-bottom: 2rem;
    transition: var(--transition);
}

.card:hover {
    box-shadow: var(--shadow-lg);
}

.card-header h2 {
    font-size: 1.5rem;
    font-weight: 600;
    margin-bottom: 0.5rem;
}

.card-header p {
    color: var(--color-text-secondary);
    margin-bottom: 1.5rem;
}

/* Form */
.analysis-form {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.form-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.form-group label {
    font-weight: 500;
    font-size: 0.875rem;
    color: var(--color-text);
}

.form-group input,
.form-group textarea {
    padding: 0.75rem;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
    font-size: 1rem;
    font-family: inherit;
    background: var(--color-bg);
    color: var(--color-text);
    transition: var(--transition);
}

.form-group input:focus,
.form-group textarea:focus {
    outline: none;
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}

.form-hint {
    font-size: 0.75rem;
    color: var(--color-text-secondary);
}

.form-hint.error {
    color: var(--color-danger);
}

/* Buttons */
.btn-primary {
    background: var(--color-primary);
    color: white;
    border: none;
    padding: 0.875rem 2rem;
    border-radius: var(--radius-md);
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    transition: var(--transition);
}

.btn-primary:hover:not(:disabled) {
    background: var(--color-primary-dark);
    transform: translateY(-1px);
    box-shadow: var(--shadow-md);
}

.btn-primary:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

/* Results */
.results-section {
    margin-top: 3rem;
}

.result-card {
    text-align: center;
}

.result-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
}

.risk-badge {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.5rem 1rem;
    border-radius: var(--radius-lg);
    font-weight: 600;
    font-size: 0.875rem;
}

.risk-badge.risk-safe {
    background: #d1fae5;
    color: #065f46;
}

.risk-badge.risk-low {
    background: #fef3c7;
    color: #92400e;
}

.risk-badge.risk-medium {
    background: #fed7aa;
    color: #9a3412;
}

.risk-badge.risk-high {
    background: #fee2e2;
    color: #991b1b;
}

.classification {
    font-size: 1.5rem;
    font-weight: 700;
    padding: 0.5rem 1.5rem;
    background: var(--color-bg-secondary);
    border-radius: var(--radius-lg);
}

/* Score Circle */
.score-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1rem;
    margin: 2rem 0;
}

.score-circle {
    position: relative;
    width: 200px;
    height: 200px;
}

.score-value {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    font-size: 3rem;
    font-weight: 700;
    color: var(--color-text);
}

.score-percent {
    font-size: 1.5rem;
    color: var(--color-text-secondary);
}

#scoreCircle {
    transition: stroke-dashoffset 1s ease;
}

.score-label {
    font-size: 1.125rem;
    font-weight: 500;
    color: var(--color-text-secondary);
}

.assessment {
    margin-top: 2rem;
    padding: 1.5rem;
    background: var(--color-bg-secondary);
    border-radius: var(--radius-lg);
    font-size: 1.125rem;
    line-height: 1.8;
}

/* Tabs */
.tabs {
    display: flex;
    gap: 0.5rem;
    border-bottom: 2px solid var(--color-border);
    margin-bottom: 2rem;
    overflow-x: auto;
}

.tab {
    background: none;
    border: none;
    padding: 1rem 1.5rem;
    font-size: 0.875rem;
    font-weight: 500;
    color: var(--color-text-secondary);
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    border-bottom: 2px solid transparent;
    transition: var(--transition);
    white-space: nowrap;
}

.tab:hover {
    color: var(--color-text);
}

.tab.active {
    color: var(--color-primary);
    border-bottom-color: var(--color-primary);
}

.tab-panel {
    animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
}

.tab-description {
    color: var(--color-text-secondary);
    margin-bottom: 1.5rem;
}

/* Reasons List */
.reasons-list {
    list-style: none;
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.reason-item {
    display: flex;
    gap: 1rem;
    padding: 1rem;
    background: var(--color-bg-secondary);
    border-radius: var(--radius-md);
    border-left: 3px solid var(--color-primary);
}

.reason-icon {
    width: 32px;
    height: 32px;
    background: var(--color-primary);
    color: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    flex-shrink: 0;
}

.reason-text {
    flex: 1;
}

/* Phrases */
.phrases-container {
    display: flex;
    flex-wrap: wrap;
    gap: 0.75rem;
    margin-bottom: 2rem;
}

.phrase-badge {
    padding: 0.5rem 1rem;
    background: #fee2e2;
    color: #991b1b;
    border-radius: var(--radius-md);
    font-size: 0.875rem;
    font-weight: 500;
}

/* Tokens Grid */
.tokens-grid {
    display: grid;
    gap: 1rem;
}

.token-card {
    padding: 1rem;
    background: var(--color-bg-secondary);
    border-radius: var(--radius-md);
    display: grid;
    grid-template-columns: 120px 1fr 80px;
    align-items: center;
    gap: 1rem;
}

.token-word {
    font-weight: 600;
    font-family: 'Courier New', monospace;
}

.token-bar {
    background: var(--color-bg-tertiary);
    height: 8px;
    border-radius: 4px;
    overflow: hidden;
}

.token-bar-fill {
    background: var(--color-primary);
    height: 100%;
    transition: width 0.5s ease;
}

.token-score {
    text-align: right;
    font-size: 0.875rem;
    color: var(--color-text-secondary);
    font-family: 'Courier New', monospace;
}

/* Claims */
.claims-stats {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 1.5rem;
    margin-bottom: 2rem;
}

.stat {
    text-align: center;
    padding: 1.5rem;
    background: var(--color-bg-secondary);
    border-radius: var(--radius-lg);
}

.stat-value {
    display: block;
    font-size: 2.5rem;
    font-weight: 700;
    color: var(--color-primary);
    margin-bottom: 0.5rem;
}

.stat-label {
    font-size: 0.875rem;
    color: var(--color-text-secondary);
}

.claims-list {
    list-style: none;
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.claim-item {
    display: flex;
    gap: 1rem;
    padding: 1rem;
    background: var(--color-bg-secondary);
    border-radius: var(--radius-md);