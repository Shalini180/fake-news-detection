package com.fakenews.graph;

/** Directed, labeled, weighted edge. Weight should represent a cost for Dijkstra (lower = better). */
public class GraphEdge {
    private final String from;
    private final String to;
    private final String relationship; // CONTAINS, PUBLISHED_BY, HAS_ARTICLE, SUPPORTS, CONTRADICTS, ...
    private final double weight;

    public GraphEdge(String from, String to, String relationship, double weight) {
        this.from = from;
        this.to = to;
        this.relationship = relationship;
        this.weight = weight;
    }

    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getRelationship() { return relationship; }
    public double getWeight() { return weight; }
}
