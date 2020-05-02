package com.xsearch.entity;

public class Article {

    public Article() {
    }

    public Article(int index, String title, String content) {
        this.index = index;
        this.title = title;
        this.content = content;
    }

    int index;

    String title;

    String content;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
