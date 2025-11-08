package com.fakenews.datastructures;

import java.util.*;

/**
 * Min-heap keyed by score.
 * NOTE: In this project, "least credible" == HIGHEST fake score.
 * To avoid semantic confusion, getTopKLeastCredible() explicitly returns
 * the top-K by DESCENDING score (i.e., most fake first), regardless of heap internals.
 */
public class CredibilityMinHeap {
    private final List<HeapNode> heap = new ArrayList<>();
    private final Map<String, Integer> pos = new HashMap<>();

    // ---- core heap ops (min-heap on score) ----

    public void insert(String articleId, double score) {
        HeapNode n = new HeapNode(articleId, score);
        heap.add(n);
        pos.put(articleId, heap.size() - 1);
        heapifyUp(heap.size() - 1);
    }

    public HeapNode extractMin() {
        if (heap.isEmpty()) return null;
        HeapNode min = heap.get(0);
        HeapNode last = heap.remove(heap.size() - 1);
        pos.remove(min.articleId);
        if (!heap.isEmpty()) {
            heap.set(0, last);
            pos.put(last.articleId, 0);
            heapifyDown(0);
        }
        return min;
    }

    public HeapNode peek() {
        return heap.isEmpty() ? null : heap.get(0);
    }

    public void updateScore(String articleId, double newScore) {
        Integer i = pos.get(articleId);
        if (i == null) return;
        double old = heap.get(i).score;
        heap.get(i).score = newScore;
        if (newScore < old) heapifyUp(i); else heapifyDown(i);
    }

    public boolean isEmpty() { return heap.isEmpty(); }
    public int size()        { return heap.size();    }

    // ---- "least credible" view (DESC by score) ----

    /**
     * Returns top-K by DESCENDING score (i.e., highest fake scores first).
     * Uses a copy + partial sort to avoid changing heap invariants.
     */
    public List<HeapNode> getTopKLeastCredible(int k) {
        k = Math.max(0, k);
        if (k == 0 || heap.isEmpty()) return Collections.emptyList();

        List<HeapNode> copy = new ArrayList<>(heap.size());
        for (HeapNode n : heap) copy.add(new HeapNode(n.articleId, n.score));

        // Sort by score descending
        copy.sort((a, b) -> Double.compare(b.score, a.score));
        if (copy.size() > k) copy = copy.subList(0, k);
        return copy;
    }

    // ---- internals ----
    private void heapifyUp(int i) {
        while (i > 0) {
            int p = (i - 1) / 2;
            if (heap.get(i).score >= heap.get(p).score) break;
            swap(i, p); i = p;
        }
    }

    private void heapifyDown(int i) {
        int n = heap.size();
        while (true) {
            int smallest = i, l = 2 * i + 1, r = 2 * i + 2;
            if (l < n && heap.get(l).score < heap.get(smallest).score) smallest = l;
            if (r < n && heap.get(r).score < heap.get(smallest).score) smallest = r;
            if (smallest == i) break;
            swap(i, smallest); i = smallest;
        }
    }

    private void swap(int i, int j) {
        HeapNode a = heap.get(i), b = heap.get(j);
        heap.set(i, b); heap.set(j, a);
        pos.put(b.articleId, i);
        pos.put(a.articleId, j);
    }

    // ---- dto ----
    public static class HeapNode {
        private final String articleId;
        private double score;

        public HeapNode(String articleId, double credibilityScore) {
            this.articleId = articleId;
            this.score = credibilityScore;
        }
        public String getArticleId()     { return articleId; }
        public double getCredibilityScore() { return score; }
        public void setCredibilityScore(double s) { this.score = s; }
    }
}
