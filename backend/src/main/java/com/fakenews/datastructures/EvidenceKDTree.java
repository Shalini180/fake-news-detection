package com.fakenews.datastructures;

import java.util.*;

/**
 * 2D KD-tree (relevance, recency). Stores an opaque "data" blob per evidence id.
 */
public class EvidenceKDTree {
    private KDNode root;
    private static final int K = 2; // dimensions

    public void insert(String evidenceId, double relevance, double recency, Object data) {
        double[] point = new double[]{ clamp01(relevance), clamp01(recency) };
        root = insertRec(root, point, evidenceId, data, 0);
    }

    private KDNode insertRec(KDNode node, double[] point, String id, Object data, int depth) {
        if (node == null) return new KDNode(point, id, data);
        int cd = depth % K;
        if (point[cd] < node.point[cd]) node.left  = insertRec(node.left,  point, id, data, depth + 1);
        else                            node.right = insertRec(node.right, point, id, data, depth + 1);
        return node;
    }

    /** k-NN using a max-heap (largest distance at head). */
    public List<EvidenceResult> kNearestNeighbors(double relevance, double recency, int k) {
        k = Math.max(1, k);
        double[] target = new double[]{ clamp01(relevance), clamp01(recency) };
        PriorityQueue<EvidenceResult> pq = new PriorityQueue<>(
                (a, b) -> Double.compare(b.distance, a.distance) // max-heap by distance
        );
        knnRec(root, target, k, 0, pq);

        List<EvidenceResult> results = new ArrayList<>(pq);
        results.sort(Comparator.comparingDouble(er -> er.distance)); // low -> high
        return results;
    }

    private void knnRec(KDNode node, double[] target, int k, int depth, PriorityQueue<EvidenceResult> pq) {
        if (node == null) return;

        double dist = euclidean(node.point, target);
        if (pq.size() < k) pq.offer(new EvidenceResult(node.evidenceId, node.data, dist));
        else if (dist < pq.peek().distance) { pq.poll(); pq.offer(new EvidenceResult(node.evidenceId, node.data, dist)); }

        int cd = depth % K;
        KDNode first  = target[cd] < node.point[cd] ? node.left : node.right;
        KDNode second = target[cd] < node.point[cd] ? node.right : node.left;

        knnRec(first, target, k, depth + 1, pq);
        double planeDist = Math.abs(target[cd] - node.point[cd]);
        if (pq.size() < k || planeDist < pq.peek().distance) {
            knnRec(second, target, k, depth + 1, pq);
        }
    }

    /** Axis-aligned range search (inclusive). */
    public List<EvidenceResult> rangeSearch(double minRel, double maxRel, double minRec, double maxRec) {
        List<EvidenceResult> out = new ArrayList<>();
        rangeRec(root, clamp01(minRel), clamp01(maxRel), clamp01(minRec), clamp01(maxRec), 0, out);
        return out;
    }

    private void rangeRec(KDNode node, double minRel, double maxRel, double minRec, double maxRec,
                          int depth, List<EvidenceResult> out) {
        if (node == null) return;
        double rel = node.point[0], rec = node.point[1];
        if (rel >= minRel && rel <= maxRel && rec >= minRec && rec <= maxRec) {
            out.add(new EvidenceResult(node.evidenceId, node.data, 0.0));
        }
        int cd = depth % K;
        double min = cd == 0 ? minRel : minRec;
        double max = cd == 0 ? maxRel : maxRec;
        if (node.point[cd] >= min) rangeRec(node.left,  minRel, maxRel, minRec, maxRec, depth + 1, out);
        if (node.point[cd] <= max) rangeRec(node.right, minRel, maxRel, minRec, maxRec, depth + 1, out);
    }

    private static double euclidean(double[] a, double[] b) {
        double s = 0;
        for (int i = 0; i < K; i++) { double d = a[i] - b[i]; s += d * d; }
        return Math.sqrt(s);
    }
    private static double clamp01(double v) { return Math.max(0.0, Math.min(1.0, v)); }

    // ---- nodes + dto ----
    static class KDNode {
        final double[] point;
        final String evidenceId;
        final Object data;
        KDNode left, right;
        KDNode(double[] point, String evidenceId, Object data) {
            this.point = point; this.evidenceId = evidenceId; this.data = data;
        }
    }

    public static class EvidenceResult {
        private final String evidenceId;
        private final Object data;
        private final double distance;
        public EvidenceResult(String evidenceId, Object data, double distance) {
            this.evidenceId = evidenceId; this.data = data; this.distance = distance;
        }
        public String getEvidenceId() { return evidenceId; }
        public Object getData()       { return data; }
        public double getDistance()   { return distance; }
    }
}
