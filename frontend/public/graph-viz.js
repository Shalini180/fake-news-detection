/**
 * Simple SVG Graph Visualization
 * Lightweight knowledge graph renderer without heavy dependencies
 */

const GraphViz = (function () {

    // Create a simple node-link SVG graph
    function createSimpleGraph(containerId, data) {
        const container = document.getElementById(containerId);
        if (!container) return;

        // Clear existing content
        container.innerHTML = '';

        const { article, claims, evidenceSources } = data;

        // Create SVG
        const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
        svg.setAttribute('width', '100%');
        svg.setAttribute('height', '400');
        svg.setAttribute('viewBox', '0 0 600 400');
        svg.style.background = 'var(--bg-elev)';
        svg.style.borderRadius = '12px';

        // Define marker for arrow
        const defs = document.createElementNS('http://www.w3.org/2000/svg', 'defs');
        const marker = document.createElementNS('http://www.w3.org/2000/svg', 'marker');
        marker.setAttribute('id', 'arrowhead');
        marker.setAttribute('markerWidth', '10');
        marker.setAttribute('markerHeight', '7');
        marker.setAttribute('refX', '9');
        marker.setAttribute('refY', '3.5');
        marker.setAttribute('orient', 'auto');
        const polygon = document.createElementNS('http://www.w3.org/2000/svg', 'polygon');
        polygon.setAttribute('points', '0 0, 10 3.5, 0 7');
        polygon.setAttribute('fill', 'var(--muted)');
        marker.appendChild(polygon);
        defs.appendChild(marker);
        svg.appendChild(defs);

        // Layout positions
        const centerX = 300;
        const articleY = 80;
        const claimsY = 200;
        const evidenceY = 320;

        // Draw article node (center top)
        createNode(svg, centerX, articleY, article.title, '#8b5cf6', 60);

        // Draw claim nodes
        const claimCount = Math.min(claims.length, 5);
        const claimSpacing = 500 / (claimCount + 1);

        claims.slice(0, 5).forEach((claim, i) => {
            const x = 50 + claimSpacing * (i + 1);
            const color = getClaimColor(claim.status);

            // Draw edge from article to claim
            drawEdge(svg, centerX, articleY + 30, x, claimsY - 30, 'contains');

            // Draw claim node
            createNode(svg, x, claimsY, `Claim ${i + 1}`, color, 40, claim.text);

            // Draw edge to evidence (simplified - all to one evidence node)
            if (evidenceSources && evidenceSources.count > 0) {
                drawEdge(svg, x, claimsY + 30, centerX, evidenceY - 30, 'verified by');
            }
        });

        // Draw evidence source node (bottom center)
        if (evidenceSources && evidenceSources.count > 0) {
            createNode(svg, centerX, evidenceY, `${evidenceSources.count} Sources`, '#10b981', 50);
        }

        container.appendChild(svg);
    }

    // Create a node (circle with text)
    function createNode(svg, x, y, label, color, radius, tooltip = '') {
        const group = document.createElementNS('http://www.w3.org/2000/svg', 'g');
        group.style.cursor = tooltip ? 'pointer' : 'default';

        // Circle
        const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
        circle.setAttribute('cx', x);
        circle.setAttribute('cy', y);
        circle.setAttribute('r', radius);
        circle.setAttribute('fill', color);
        circle.setAttribute('opacity', '0.8');
        circle.setAttribute('stroke', 'var(--surface)');
        circle.setAttribute('stroke-width', '3');

        // Text
        const text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        text.setAttribute('x', x);
        text.setAttribute('y', y + 5);
        text.setAttribute('text-anchor', 'middle');
        text.setAttribute('fill', 'white');
        text.setAttribute('font-size', '12');
        text.setAttribute('font-weight', 'bold');
        text.textContent = truncateText(label, 10);

        group.appendChild(circle);
        group.appendChild(text);

        // Tooltip
        if (tooltip) {
            const title = document.createElementNS('http://www.w3.org/2000/svg', 'title');
            title.textContent = tooltip;
            group.appendChild(title);
        }

        svg.appendChild(group);
    }

    // Draw an edge (line with arrow and label)
    function drawEdge(svg, x1, y1, x2, y2, label) {
        // Line
        const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
        line.setAttribute('x1', x1);
        line.setAttribute('y1', y1);
        line.setAttribute('x2', x2);
        line.setAttribute('y2', y2);
        line.setAttribute('stroke', 'var(--muted)');
        line.setAttribute('stroke-width', '2');
        line.setAttribute('stroke-dasharray', '5,5');
        line.setAttribute('marker-end', 'url(#arrowhead)');
        line.setAttribute('opacity', '0.5');

        // Label
        const midX = (x1 + x2) / 2;
        const midY = (y1 + y2) / 2;
        const text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        text.setAttribute('x', midX);
        text.setAttribute('y', midY - 5);
        text.setAttribute('text-anchor', 'middle');
        text.setAttribute('fill', 'var(--muted)');
        text.setAttribute('font-size', '10');
        text.setAttribute('opacity', '0.7');
        text.textContent = label;

        svg.appendChild(line);
        svg.appendChild(text);
    }

    // Get color based on claim status
    function getClaimColor(status) {
        const colors = {
            'VERIFIED': '#10b981',
            'SUPPORTED': '#3b82f6',
            'PENDING': '#f59e0b',
            'CONTRADICTED': '#ef4444',
            'UNVERIFIED': '#6b7280'
        };
        return colors[status] || colors.UNVERIFIED;
    }

    // Truncate text to max length
    function truncateText(text, maxLength) {
        if (text.length <= maxLength) return text;
        return text.substring(0, maxLength - 2) + '...';
    }

    // Public API
    return {
        createSimpleGraph
    };
})();

// Export for use in main app
window.GraphViz = GraphViz;
