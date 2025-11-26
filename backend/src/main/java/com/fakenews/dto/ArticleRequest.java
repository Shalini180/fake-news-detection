package com.fakenews.dto;

public class ArticleRequest {
    private String title;
    private String content;
    private String source;

    public ArticleRequest() {}

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getSource() { return source; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setSource(String source) { this.source = source; }
}
