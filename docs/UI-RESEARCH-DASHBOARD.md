# UI – Research Dashboard

## Overview

The frontend has been upgraded to a research-grade dashboard with advanced visualizations, temporal analytics, and session management. All features work in vanilla JavaScript + CSS + Chart.js (CDN).

## Key Features

### 1. Demo Mode

Load pre-configured scenarios for offline testing without backend:

- **Access:** Click "🎬 Demo" button in query panel
- **Scenarios:**
  1. High-Risk Fake News - Sensational language, no sources
  2. Low-Risk Credible News - Well-sourced, peer-reviewed
  3. Mixed Signals - Moderate issues
  4. Claims-Heavy Article - Multiple verifiable claims
  5. Suspicious Domain - Reasonable content, bad source

**Usage:**
1. Click Demo button
2. Select scenario from dropdown
3. Click "Load Demo"
4. Results render immediately without API call

### 2. Session History

All analyses automatically save to localStorage (last 50):

- **View:** Right sidebar shows chronological history
- **Reload:** Click any item to view past results
- **Persist:** History survives page refreshes
- **Clear:** Click trash icon to clear all history
- **Storage:** Uses `SessionManager` module

### 3. Temporal Analytics

Track credibility evolution across your session:

- **Navigate to:** "Temporal" tab in results workspace
- **Charts:**
  - **Time Series:** Line chart showing credibility scores over time
  - **Distribution:** Histogram of risk levels (SAFE/LOW/MEDIUM/HIGH)
- **Updates:** Charts auto-update after each analysis

### 4. Side-by-Side Comparison

Compare any 2 analyses from history:

1. **Double-click** 2 history items (or single-click to select)
2. Click "Compare Selected (2/2)" button in Temporal tab
3. Modal shows side-by-side metrics
4. Compare scores, classifications, timestamps

### 5. Session Export

Download complete session data as JSON:

- **Navigate to:** "System" tab
- **Click:** "📥 Download Session JSON"
- **File:** `fake-news-session-YYYY-MM-DD.json`
- **Contains:** All analyses, metadata, statistics

### 6. Advanced Visualizations

#### Credibility Gauge (Overview Tab)
- Radial donut chart (Chart.js)
- Color-coded: Green (safe) → Yellow → Orange → Red (high risk)
- Shows percentage and risk badge

#### Attention Heatmap (Explanations Tab)
- Horizontal scrollable strip
- Each token colored by attention weight
- Hover for exact weight value

#### Feature Importance Bars (Explanations Tab)
- Horizontal bar chart
- Shows: content analysis, domain credibility, claims verification
- Color-coded by risk level

#### Knowledge Graph (Claims Tab)
- SVG node-link diagram
- Article → Claims → Evidence flow
- Nodes colored by verification status

#### Time Series & Distribution (Temporal Tab)
- Line chart: credibility scores over session
- Bar chart: distribution of risk levels

## Layout Structure

### Desktop (>1400px)
```
┌─────────────────────────────────────────────────┐
│                   Header                        │
├──────────┬──────────────────────┬───────────────┤
│  Query   │   Results Workspace  │   History     │
│  Panel   │   (5 Tabs)          │   Panel       │
│          │                      │               │
│  (320px) │   (Flex 1)          │   (280px)     │
└──────────┴──────────────────────┴───────────────┘
```

### Tablet (900-1400px)
```
┌───────────────────────────────────────┐
│            Header                     │
├──────────┬────────────────────────────┤
│  Query   │   Results Workspace       │
│  Panel   │   (5 Tabs)                │
│          │                            │
│ (300px)  │   (Flex 1)                │
└──────────┴────────────────────────────┘

History panel becomes modal overlay (toggle button)
```

### Mobile (<900px)
```
┌────────────────────┐
│      Header        │
├────────────────────┤
│  Query Panel       │
│  (Collapsible)     │
├────────────────────┤
│  Results Workspace │
│  (Full Width)      │
└────────────────────┘

History panel: slide-out overlay
```

## Tabs Structure

### 1. Overview
- Radial credibility gauge
- Risk badge (emoji + level)
- Key reasons list

### 2. Explanations
- Token attention heatmap
- Feature importance chart
- Suspicious phrases list

### 3. Claims & Evidence
- Filter buttons (All/Verified/Supported/Pending/Unverified)
- Claims list with status badges
- Evidence density indicators
- Knowledge graph visualization

### 4. Temporal & Comparative
- Time series chart
- Distribution histogram
- Comparison controls
- Selection interface

### 5. System Metrics
- Total analyses count
- Average risk score
- High-risk count
- Model limitations panel
- Export session button

## Technical Details

### Modules
- `demo-data.js` - 5 realistic scenarios
- `session.js` - SessionManager (localStorage + export)
- `visualizations.js` - Chart.js wrappers
- `graph-viz.js` - SVG graph renderer
- `layout.css` - Responsive 3-panel grid
- `app.js` - Main dashboard logic (480 lines)

### Browser Support
- Modern browsers (Chrome, Firefox, Edge, Safari)
- Requires JavaScript enabled
- localStorage required for history
- Chart.js loaded via CDN

### Performance
- Session limit: 50 analyses (auto-cleanup)
- Charts destroy/recreate on update (no memory leaks)
- Lazy-load demo data (only when needed)

## Keyboard Shortcuts

*None implemented yet - future enhancement*

## Troubleshooting

**Charts not rendering:**
- Check console for Chart.js CDN loading
- Verify `Visualizations.isChartJsAvailable()` returns true

**History not persisting:**
- Check browser localStorage is enabled
- Check localStorage quota (5MB limit)
- SessionManager auto-cleans if quota exceeded

**Demo mode not working:**
- Verify `DEMO_DATA` object is loaded
- Check console for script loading errors

**API calls failing:**
- UI gracefully falls back to local simulation
- Check `api.js` configuration
- Verify CORS settings

## Development Workflow

### Running Locally
```bash
# Serve frontend
cd frontend/public
python -m http.server 3000

# Navigate to:
# http://localhost:3000
```

### Testing
1. Load each demo scenario
2. Verify all tabs render correctly
3. Test session history (refresh browser)
4. Test comparison (select 2 items)
5. Test export (check JSON file)
6. Test on mobile (resize browser)

### Debugging
- Open browser DevTools console
- Check for module loading errors
- Verify Chart.js loaded: `typeof Chart !== 'undefined'`
- Check SessionManager: `SessionManager.getStatistics()`

## Research Use Cases

### 1. Exploratory Analysis
- Load multiple articles
- View temporal trends
- Identify patterns in credibility

### 2. Demo/Presentation
- Use demo mode for offline presentations
- Switch between scenarios quickly
- Show different risk levels

### 3. Data Collection
- Analyze articles over time
- Export session for external analysis
- Compare specific articles

### 4. Model Evaluation
- Compare backend vs simulated results
- Track model confidence over time
- Identify edge cases

---

*Last Updated: 2025-11-25*
