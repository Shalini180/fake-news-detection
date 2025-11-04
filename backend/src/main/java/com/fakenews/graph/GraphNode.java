package com.fakenews.graph;

/** Simple immutable node wrapper for the knowledge graph. */
public class GraphNode {
    private final String id;
    private final String type; // ARTICLE, CLAIM, SOURCE, EVIDENCE, ENTITY, ...
    private final Object data;

    public GraphNode(String id, String type, Object data) {
        this.id = id;
        this.type = type;
        this.data = data;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public Object getData() { return data; }
}
