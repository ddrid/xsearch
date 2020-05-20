package com.xsearch.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Article {

    String id;

    int segment;

    int updateTime;

    String url;

    String title;

    String content;
}