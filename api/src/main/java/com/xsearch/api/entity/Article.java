package com.xsearch.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Article {

    /**
     * 文档唯一 id
     * 于 ArticleController 模块生成
     */
    String id;

    /**
     * 参考 Lucene 设计  https://www.jianshu.com/p/4d33705f37e5
     * 该字段表示文档所处在当前节点中的段号
     * -1 表示未 commit，不可被搜索
     * >=0 表示可以被搜索
     */
    int segment;

    /**
     * 最后更新时间，即知乎上的 "最近编辑于"
     * 表示UNIX时间戳至今的格林尼治时间秒数
     */
    int updateTime;

    /**
     * 原链接地址
     */
    String url;

    /**
     * 文档标题（知乎原问题标题）
     */
    String title;

    /**
     * 文档内容（知乎回答内容）
     */
    String content;

    /**
     * 此字段为搜索返回结果专有
     * 表示本条结果对于本次搜索的评分
     * 使用向量空间算法计算而得
     * https://blog.csdn.net/weixin_34205826/article/details/92088479
     */
    double score;

    /**
     * 此字段为搜索返回结果专有
     * 表示包含关键词的高亮语句，关键词以 HTML 标签 <em></em> 封装
     */
    String highlight;

    /**
     * 关键词偏移量 ，用于高亮显示
     */
    int offset;
}