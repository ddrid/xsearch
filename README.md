# xsearch
参考 Lucene/Elasticsearch 实现的简易分布式搜索引擎



## 实现功能

1. 倒排索引生成
2. 近实时搜索（动态更新索引）
3. 段合并
4. 分布式检索（向量空间模型）
5. 爬虫



## TODO

1. 动态扩容
2. 事务日志
3. 数据副本管理
4. 段合并策略优化



## API

### PUT /article

新增一篇文档

**Request Body**

| name       | description | kind   |
| ---------- | ----------- | ------ |
| title      | 文档标题    | string |
| url        | 原链接地址  | string |
| content    | 文档内容    | string |
| updateTime | 更新时间    | int    |

**Response Body**

| name    | description     | kind    |
| ------- | --------------- | ------- |
| id      | 文档对应唯一 id | string  |
| created | 是否成功创建    | boolean |



### GET /query

搜索

**Request Parameters**

| name  | description          | kind   |
| ----- | -------------------- | ------ |
| query | 查询字串             | string |
| from  | 分页参数，结果集起点 | int    |
| size  | 分页参数，结果集大小 | int    |

**Response Body**

| name       | description              | kind   |
| ---------- | ------------------------ | ------ |
| id         | 文档对应唯一 id          | string |
| score      | 评分                     | double |
| highlight  | 高亮内容                | string    |
| content    | 文档内容                 | string |
| title      | 文档标题                 | string |
| url        | 原链接地址               | string |
| updateTime | 更新时间                 | int    |
| segment    | 文档所在段号             | int    |

