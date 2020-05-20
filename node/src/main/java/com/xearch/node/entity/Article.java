package com.xearch.node.entity;

import com.sun.xml.internal.ws.developer.Serialization;
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
