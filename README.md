# 一、架构设计 #
[https://www.jiankunking.com/log-service-architecture-design.html](https://www.jiankunking.com/log-service-architecture-design.html)

# 二、支持功能 #
## 1、实例无状态，支持水平拓展
## 2、部署、卸载日志收集agent
* 已对接ansible，通过ansible实现批量部署、卸载日志收集agent（filebeat）
* 部署、卸载日志收集agent（filebeat）过程可视化（stream）
## 3、多集群支持
* 单实例支持多elasticsearch集群
## 4、复杂查询
* 支持Query String
* 支持and or ()等组合查询
## 5、检索
* 支持滚动查询检索结果
* 支持检索结果分页
* 检索结果自动区分项目、应用、实例、日志来源、时间等
## 6、高亮
* 支持日志检索结果，高亮关键字
## 7、日志上下文
* 支持滚动查看某条日志上下文（前多少行、后多少行）
## 8、日志下载
* 支持日志在线下载（支持全文下载、支持检索结果下载）
* 支持日志离线下载（支持全文下载、支持检索结果下载）
* 支持定时清理服务端离线下载的日志
## 9、检索维度
* 支持按照具体业务维度进行检索，比如按照项目、应用、实例等具体业务维度
## 10、日志来源
* 支持普通文本日志（比如：某台机器某个文件夹下所有日志或者某个日志）
* 支持docker日志
* 支持kubernates日志
以上三种日志均支持自动收集并归类（比如：某条日志是某个项目、某个应用、某个实例的）
## 11、多行合并
* 支持按照实际情况进行多行日志合并

合并日志的好处：
* 检索时，可以看到日志全貌
* 根据异常或者某种规则告警的时，附带多行合并的日志更有意义

## 12、配置更新
* 支持动态更新配置

## 13、连接复用
* 应用启动自动加载es client（LRU）
* es client 复用


# 三、API 文档
[https://github.com/jiankunking/log-search/wiki/API-文档](https://github.com/jiankunking/log-search/wiki/API-文档)





