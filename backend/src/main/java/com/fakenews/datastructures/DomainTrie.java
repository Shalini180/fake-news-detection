package com.fakenews.datastructures;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Trie for domain -> credibility/fake score lookup.
 * Store scores in [0..1], where higher = more fake (lower credibility).
 */
public class DomainTrie {
    private final TrieNode root;

    public DomainTrie() {
        this.root = new TrieNode();
    }

    /** Insert or update a domain's score. Input is normalized to lowercase/trimmed. */
    public void insert(String domain, double credibilityScore) {
        if (domain == null) return;
        domain = domain.trim().toLowerCase(Locale.ROOT);
        if (domain.isEmpty()) return;

        TrieNode cur = root;
        for (char c : domain.toCharArray()) {
            cur.children.putIfAbsent(c, new TrieNode());
            cur = cur.children.get(c);
        }
        cur.isEndOfWord = true;
        cur.credibilityScore = clamp01(credibilityScore);
    }

    /** Exact lookup. Returns null if not found. */
    public Double search(String domain) {
        if (domain == null) return null;
        domain = domain.trim().toLowerCase(Locale.ROOT);
        TrieNode node = searchNode(domain);
        return (node != null && node.isEndOfWord) ? node.credibilityScore : null;
    }

    /**
     * Lookup with fallback: try exact, then progressively strip leftmost subdomains.
     * e.g., news.reuters.com -> reuters.com
     */
    public Double searchWithFallback(String domain) {
        if (domain == null) return null;
        String d = domain.trim().toLowerCase(Locale.ROOT);
        Double v = search(d);
        if (v != null) return v;

        String[] parts = d.split("\\.");
        for (int i = 1; i + 1 < parts.length; i++) {
            String candidate = String.join(".", Arrays.copyOfRange(parts, i, parts.length));
            v = search(candidate);
            if (v != null) return v;
        }
        return null;
    }

    /** All domains starting with prefix (already normalized). */
    public List<DomainScore> getAllWithPrefix(String prefix) {
        List<DomainScore> results = new ArrayList<>();
        if (prefix == null) return results;
        prefix = prefix.trim().toLowerCase(Locale.ROOT);
        TrieNode node = searchNode(prefix);
        if (node != null) collectAllDomains(node, prefix, results);
        return results;
    }

    // ---- internals ----
    private TrieNode searchNode(String domain) {
        TrieNode current = root;
        for (char c : domain.toCharArray()) {
            TrieNode nxt = current.children.get(c);
            if (nxt == null) return null;
            current = nxt;
        }
        return current;
    }

    private void collectAllDomains(TrieNode node, String prefix, List<DomainScore> results) {
        if (node.isEndOfWord) results.add(new DomainScore(prefix, node.credibilityScore));
        for (Map.Entry<Character, TrieNode> e : node.children.entrySet()) {
            collectAllDomains(e.getValue(), prefix + e.getKey(), results);
        }
    }

    private static double clamp01(double v) { return Math.max(0.0, Math.min(1.0, v)); }

    // ---- node + dto ----
    static class TrieNode {
        final Map<Character, TrieNode> children = new ConcurrentHashMap<>();
        volatile boolean isEndOfWord;
        volatile double credibilityScore;
    }

    public static class DomainScore {
        private final String domain;
        private final double score;
        public DomainScore(String domain, double score) { this.domain = domain; this.score = score; }
        public String getDomain() { return domain; }
        public double getScore()  { return score;  }
    }
}
