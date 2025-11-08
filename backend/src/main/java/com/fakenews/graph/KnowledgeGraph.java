package com.fakenews.graph;

import java.util.*;

/**
 * Lightweight in-memory knowledge graph with BFS/DFS/Dijkstra utilities.
 * Notes:
 * - addEdge() is defensive (ensures adjacency lists exist, avoids NPEs).
 * - Traversals guard unknown start nodes (return empty).
 * - Dijkstra treats edge weight as a COST; pass lower values for "stronger" edges,
 *   or convert strengths to costs (e.g., cost = 1.0 / (epsilon + strength)).
 */
public class KnowledgeGraph {

    private final Map<String, GraphNode> nodes = new HashMap<>();
    private final Map<String, List<GraphEdge>> adjacencyList = new HashMap<>();

    // ---- Node / Edge management ------------------------------------------------

    public void addNode(String id, String type, Object data) {
        GraphNode node = new GraphNode(id, type, data);
        nodes.put(id, node);
        adjacencyList.putIfAbsent(id, new ArrayList<>());
    }

    public void addEdge(String from, String to, String relationship, double weight) {
        adjacencyList.putIfAbsent(from, new ArrayList<>());
        adjacencyList.putIfAbsent(to, new ArrayList<>()); // makes traversals simpler
        // (Optional) avoid duplicate edges with same (to, relationship)
        List<GraphEdge> edges = adjacencyList.get(from);
        for (GraphEdge e : edges) {
            if (e.getTo().equals(to) && e.getRelationship().equals(relationship)) {
                return; // already present
            }
        }
        edges.add(new GraphEdge(from, to, relationship, weight));
    }

    public GraphNode getNode(String id) {
        return nodes.get(id);
    }

    public List<GraphEdge> getEdges(String nodeId) {
        return adjacencyList.getOrDefault(nodeId, Collections.emptyList());
    }

    // ---- Traversals ------------------------------------------------------------

    /** Breadth-first traversal from startId (returns node ids in visit order). */
    public List<String> bfs(String startId) {
        if (!nodes.containsKey(startId)) return Collections.emptyList();
        List<String> order = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Deque<String> q = new ArrayDeque<>();
        q.offer(startId);
        visited.add(startId);

        while (!q.isEmpty()) {
            String cur = q.poll();
            order.add(cur);
            for (GraphEdge e : getEdges(cur)) {
                String v = e.getTo();
                if (visited.add(v)) q.offer(v);
            }
        }
        return order;
    }

    /** Depth-first traversal from startId (returns node ids in pre-order). */
    public List<String> dfs(String startId) {
        if (!nodes.containsKey(startId)) return Collections.emptyList();
        List<String> order = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        dfsRec(startId, visited, order);
        return order;
    }

    private void dfsRec(String u, Set<String> visited, List<String> order) {
        visited.add(u);
        order.add(u);
        for (GraphEdge e : getEdges(u)) {
            String v = e.getTo();
            if (!visited.contains(v)) dfsRec(v, visited, order);
        }
    }

    /**
     * Dijkstra shortest path distances from startId using edge weight as COST.
     * Returns a map of nodeId -> distance. Unknown start returns empty map.
     */
    public Map<String, Double> dijkstra(String startId) {
        Map<String, Double> dist = new HashMap<>();
        if (!nodes.containsKey(startId)) return dist;

        for (String id : nodes.keySet()) dist.put(id, Double.POSITIVE_INFINITY);
        dist.put(startId, 0.0);

        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.distance));
        pq.offer(new NodeDistance(startId, 0.0));
        Set<String> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            NodeDistance cur = pq.poll();
            if (!visited.add(cur.nodeId)) continue;

            for (GraphEdge e : getEdges(cur.nodeId)) {
                double nd = dist.get(cur.nodeId) + e.getWeight();
                if (nd < dist.get(e.getTo())) {
                    dist.put(e.getTo(), nd);
                    pq.offer(new NodeDistance(e.getTo(), nd));
                }
            }
        }
        return dist;
    }

    // ---- Related article discovery --------------------------------------------

    /**
     * Find ARTICLE nodes reachable within maxDepth from a given articleId.
     * Works best when you also add reverse edges SOURCE -> ARTICLE.
     */
    public List<String> findRelatedArticles(String articleId, int maxDepth) {
        if (!nodes.containsKey(articleId)) return Collections.emptyList();

        List<String> related = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Deque<NodeDepth> q = new ArrayDeque<>();
        q.offer(new NodeDepth(articleId, 0));
        visited.add(articleId);

        while (!q.isEmpty()) {
            NodeDepth cur = q.poll();
            if (cur.depth >= maxDepth) continue;

            for (GraphEdge e : getEdges(cur.nodeId)) {
                String v = e.getTo();
                if (!visited.add(v)) continue;

                GraphNode n = nodes.get(v);
                if (n != null && "ARTICLE".equals(n.getType())) {
                    related.add(v);
                }
                q.offer(new NodeDepth(v, cur.depth + 1));
            }
        }
        return related;
    }

    // ---- Internal helper structs ----------------------------------------------

    static class NodeDistance {
        final String nodeId;
        final double distance;
        NodeDistance(String nodeId, double distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }
    }

    static class NodeDepth {
        final String nodeId;
        final int depth;
        NodeDepth(String nodeId, int depth) {
            this.nodeId = nodeId;
            this.depth = depth;
        }
    }
}
