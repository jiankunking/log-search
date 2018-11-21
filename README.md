# 架构设计 #
[https://blog.csdn.net/jiankunking/article/details/81806573](https://blog.csdn.net/jiankunking/article/details/81806573)

# 支持功能 #
1. **支持检索filebeat收集到elasticsearch中的日志信息(可以根据业务中项目、应用、实例的维度进行选择检索、下载)** 
2. **支持查看、下载日志上下文** 
3. **支持日志下载（在线、离线）** 
4. **支持通过ansible-ext部署filebeat,部署过程、进度可视化**
5. **单实例支持多elasticsearch集群检索、下载**
6. **打通多来源日志与业务中项目、应用、实例的映射关系**
7. **支持Elasticsearch Query String查询**


 <h1 class="curproject-name"> log-search </h1> 

1. 如果需要启用多行合并，日志格式需要统一（或者重新写一个pipeline）为：

<pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n</pattern>

样例：

2018-07-16 15:06:37.684 [DubboServerHandler-10.138.227.55:20808-thread-100] DEBUG com.jiankunking.dao.pda.SpSnParkODao.pdaAddOutMessage - <==    Updates: 1

该格式对应的多行正则：'^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}.\d* '

2. 目前支持：文本、docker、k8s

3. 目前支持的应用名称截取

docker:支持老paas，自动截取866_1934_qd-promext qd-promext

k8s:支持容器云，自动截取 infra-mongo-mongo-v1-0-b74b68988 v数字前面的部分

文本：这类日志，可以根据路径、文件来区分，从而添加不同的项目、应用标识。也就是说，即使同一台机器部署了多个项目、多个应用，只要日志路径不同，就可以区分出来。



# 查询集群、应用、实例信息

## 查询某个项目下的es集群
<a id=查询某个项目下的es集群> </a>
### 基本信息

**Path：** /api/v1/uni/projects/{project}/clusters

**Method：** GET

### 返回数据

```javascript
[
    {
        "address": "10.138.12.12:9200,10.138.12.13:9200,10.138.12.14:9200",
        "name": "TEST",
        "description": "监控es集群",
        "project": "TEST",
        "id": "e76cca75-2d8d-4b7b-a71e-8eb177fa52be"
    }
]
```
## 获取集群中所有某个应用下的实例列表
<a id=获取集群中所有某个应用下的实例列表> </a>
### 基本信息

**Path：** /api/v1/clusters/{cluster}/projects/{project}/apps/{app}/instances

**Method：** GET



### 请求参数
  |
**Query**

| 参数名称  |  是否必须 | 示例  | 备注  |
| ------------ | ------------ | ------------ | ------------ |
| fromTime | 是  |  1511971200000 |  起始时间（包含） |
| toTime | 是  |  1543507200000 |  结束时间（包含） |

### 返回数据

```javascript
[
    "TEST"
]
```
## 获取集群中某个项目下应用简称列表
<a id=获取集群中某个项目下应用简称列表> </a>
### 基本信息

**Path：** /api/v1/clusters/{cluster}/projects/{project}/apps

**Method：** GET

### 请求参数
**Query**

| 参数名称  |  是否必须 | 示例  | 备注  |
| ------------ | ------------ | ------------ | ------------ |
| fromTime | 是  |  1511971200000 |  起始时间（包含） |
| toTime | 是  |  1543507200000 |  结束时间（包含） |

### 返回数据

```javascript
[
    "jiankunking-server-server",
    "endpoint-admin-admin",
    "cluster-admin-admin",
    "console-web-redis-redis",
    "kube-dns",
    "exporters-node-node",
    "exporters-gpu-gpu",
    "infra-mongo-mongo",
    "config-sync-sync",
    "kube-dns-autoscaler",
    "license-server-server",
    "jiankunking-kubeflow-admin-kubeflow-admin",
    "jiankunking-postgres-postgres",
    "jiankunking-admin-admin",
    "alerting-notifier-notifier",
    "TESTing-metrics-server-metrics-server",
    "logging-fluentd-fluentd",
    "jiankunking-storage-manager-storage-manager",
    "logging-elasticsearch-elasticsearch",
    "config-gc-gc"
]
```
# 日志检索

## 搜索
<a id=搜索> </a>
### 基本信息

**Path：** /api/v1/clusters/{cluster}/projects/{project}/apps/{app}/instances/{instance}/logs

**Method：** GET

**接口描述：**
<pre><code>根据关键字查询
默认是按照时间升序加载即即时间最新的在最下面
当加载更多的时候 会加载时间晚一些的

</code></pre>


### 请求参数
**路径参数**

| 参数名称 | 示例  | 备注  |
| ------------ | ------------ | ------------ | ------------ | ------------ |
| cluster |   |   |
| project |   |   |
| app |   |  查询所有的时候，传递all |
| instance |   |  查询所有的时候，传递all |
**Query**

| 参数名称  |  是否必须 | 示例  | 备注  |
| ------------ | ------------ | ------------ | ------------ |
| fromTime | 是  |  1511971200000 |  起始时间（包含） |
| toTime | 是  |  1543507200000 |  结束时间（包含） |
| startID | 是  |  eyJkb2NJRCI6IkFXVVhMUHVvcWhmMUo0ZGtKU1hSIiwidGltZVN0YW1wU29ydCI6MTUzMzY5MjcwMzgxNCwiaW5kZXgiOiJmaWxlYmVhdC02LjMuMi0yMDE4LjA4LjA4In0= |  日志id，首次检索不需要传递，页面鼠标滑动，显示更多的时候粗腰传递，以便告知从那条开始继续展示 |
| keyword | 是  |   |  搜索关键字 |
| pageSize | 否  |  100 |  每次返回数据条数，不传默认100 |

### 返回数据

```javascript
{
    "metadata": {
        "total": 6
    },
    "items": [
        {
            "index": "filebeat-7.0.0-alpha1-2018.07.31",
            "project": "TEST",
            "time": "1533053365153",
            "id": "eyJkb2NJRCI6IkFXVXhQaGc1cWhmMUo0ZGszdlVtIiwidGltZVN0YW1wU29ydCI6MTUzMzA1MzM2NTE1MywiaW5kZXgiOiJmaWxlYmVhdC03LjAuMC1hbHBoYTEtMjAxOC4wNy4zMSJ9",
            "message": "2018-08-01 00:09:25.152  INFO 1 --- [ain-EventThread] org.I0Itec.zkclient.ZkClient             : zookeeper state changed (<logHighlight>SyncConnected</logHighlight>)",
            "type": "docker"
        },
        {
            "index": "filebeat-7.0.0-alpha1-2018.07.31",
            "project": "TEST",
            "time": "1533053325127",
            "id": "eyJkb2NJRCI6IkFXVXhQaGZxcWhmMUo0ZGszdlVPIiwidGltZVN0YW1wU29ydCI6MTUzMzA1MzMyNTEyNywiaW5kZXgiOiJmaWxlYmVhdC03LjAuMC1hbHBoYTEtMjAxOC4wNy4zMSJ9",
            "message": "2018-08-01 00:08:45.127  INFO 1 --- [ain-EventThread] org.I0Itec.zkclient.ZkClient             : zookeeper state changed (<logHighlight>SyncConnected</logHighlight>)",
            "type": "docker"
        }
    ]
}
```
## 某条日志上下文
<a id=某条日志上下文> </a>
### 基本信息

**Path：** /api/v1/clusters/{cluster}/projects/{project}/apps/{app}/instances/{instance}/contexts

**Method：** GET

**接口描述：**
<p>根据id获取对应日志文件中某行的上下文<br>
首次加载的时候应该是某行的前后多少行（这时需要包含当前行）<br>
当再次滚动加载更多的时候 应该只加载前多少行或者后多少行（这时不需要包含当前行）</p>


### 请求参数
**路径参数**

| 参数名称 | 示例  | 备注  |
| ------------ | ------------ | ------------ | ------------ | ------------ |
| cluster |   |   |
| project |   |   |
| app |   |  查询所有的时候，传递all |
| instance |   |  查询所有的时候，传递all |
**Query**

| 参数名称  |  是否必须 | 示例  | 备注  |
| ------------ | ------------ | ------------ | ------------ |
| fromTime | 是  |  1511971200000 |  起始时间（包含） |
| toTime | 是  |  1543507200000 |  结束时间（包含） |
| startID | 是  |  eyJkb2NJRCI6IkFXVVhMUEdIcWhmMUo0ZGtKU0VwIiwidGltZVN0YW1wU29ydCI6MTUzMzY5MjcwMDI4MywiaW5kZXgiOiJmaWxlYmVhdC02LjMuMi0yMDE4LjA4LjA4In0= |  日志id |
| beforeLines | 是  |  10 |  startID前多少行 |
| afterLines | 是  |  10 |  startID后多少行 |

### 返回数据

```javascript
{
    "metadata": {
        "total": 7325
    },
    "items": [
        {
            "index": "filebeat-7.0.0-alpha1-2018.07.31",
            "project": "TEST",
            "id": "eyJkb2NJRCI6IkFXVXhQaGZxcWhmMUo0ZGszdlVTIiwidGltZVN0YW1wU29ydCI6MTUzMzA1MzM0MDA0OSwiaW5kZXgiOiJmaWxlYmVhdC03LjAuMC1hbHBoYTEtMjAxOC4wNy4zMSJ9",
            "time": "1533053340049",
            "message": "2018-08-01 00:09:00.048  INFO 1 --- [0.138.8.62:2181] c.a.d.r.zookeeper.ZookeeperRegistry      :  [DUBBO] Notify urls for subscribe url provider://172.17.0.3:36684/com.jiankunking.interconn.TEST.app.service.services.UserService?anyhost=true&application=app-service&category=configurators&check=false&dubbo=2.5.3&heartbeat=10000&interface=com.jiankunking.interconn.TEST.app.service.services.UserService&logger=slf4j&methods=findById&pid=1&retries=0&revision=1.0.0-SNAPSHOT&side=provider&threads=100&timestamp=1522718594649, urls: [empty://172.17.0.3:36684/com.jiankunking.interconn.TEST.app.service.services.UserService?anyhost=true&application=app-service&category=configurators&check=false&dubbo=2.5.3&heartbeat=10000&interface=com.jiankunking.interconn.TEST.app.service.services.UserService&logger=slf4j&methods=findById&pid=1&retries=0&revision=1.0.0-SNAPSHOT&side=provider&threads=100&timestamp=1522718594649], dubbo version: 2.5.3, current host: 127.0.0.1",
            "type": "docker"
        },
        {
            "index": "filebeat-7.0.0-alpha1-2018.07.31",
            "project": "TEST",
            "id": "eyJkb2NJRCI6IkFXVXhQaGZxcWhmMUo0ZGszdlVRIiwidGltZVN0YW1wU29ydCI6MTUzMzA1MzMyNjY4NCwiaW5kZXgiOiJmaWxlYmVhdC03LjAuMC1hbHBoYTEtMjAxOC4wNy4zMSJ9",
            "time": "1533053326684",
            "message": "2018-08-01 00:08:46.684  INFO 1 --- [yTimer-thread-1] c.a.d.r.zookeeper.ZookeeperRegistry      :  [DUBBO] Retry register [dubbo://172.17.0.3:36684/com.jiankunking.interconn.TEST.app.service.services.AppMngService?anyhost=true&application=app-service&dubbo=2.5.3&heartbeat=10000&interface=com.jiankunking.interconn.TEST.app.service.services.AppMngService&logger=slf4j&methods=findProjects,findAppsByParam,findProjectsAlmRelationByDomainId,findDomains&pid=1&retries=0&revision=1.0.0-SNAPSHOT&side=provider&threads=100&timestamp=1522718595930, dubbo://172.17.0.3:36684/com.jiankunking.interconn.TEST.app.service.services.UserService?anyhost=true&application=app-service&dubbo=2.5.3&heartbeat=10000&interface=com.jiankunking.interconn.TEST.app.service.services.UserService&logger=slf4j&methods=findById&pid=1&retries=0&revision=1.0.0-SNAPSHOT&side=provider&threads=100&timestamp=1522718594649], dubbo version: 2.5.3, current host: 127.0.0.1",
            "type": "docker"
        }
    ]
}
```
# filebeat部署

## filebeat卸载
<a id=filebeat卸载> </a>
### 基本信息

**Path：** /api/v1/projects/{project}/ip/{ip}/version/{version}

**Method：** DELETE

**接口描述：**
<p>实时流显示</p>


## 批量部署filebeat
<a id=批量部署filebeat> </a>
### 基本信息

**Path：** /api/v1/projects/{project}/batch

**Method：** POST

**接口描述：**
<p>实时流显示</p>


## 查询agent版本
<a id=查询agent版本> </a>
### 基本信息

**Path：** /api/v1/projects/{project}/agent/version

**Method：** GET

### 返回数据

```javascript
{
   "version": [
      {
         "latestVersion": "6.3.1",
         "agentName": "fileBeatVersion"
      }
   ]
}
```
## 查询项目下安装信息
<a id=查询项目下安装信息> </a>
### 基本信息

**Path：** /api/v1/projects/{project}

**Method：** GET

### 返回数据

```javascript
[
    {
       //略
    }
]
```
## 给某个项目某台机器部署filebeat
<a id=给某个项目某台机器部署filebeat> </a>
### 基本信息

**Path：** /api/v1/projects/{project}/ip/{ip}

**Method：** POST

### 返回数据

```javascript
{"result":{"job":"filebeat","type":"PLAY","name":"filebeat deploy"}}
{"result":{"job":"filebeat","type":"TASK","name":"Gathering Facts"}}
{"result":{"job":"filebeat","type":"HOST","host":"10.138.25.181","step":1,"name":"Gathering Facts","status":"ok","progress":7}}
{"result":{"job":"filebeat","type":"TASK","name":"mkdir -p /data/download/filebeat/TEST_filebeat_yml_10.138.25.181"}}
{"result":{"job":"filebeat","type":"HOST","host":"10.138.25.34 -\u003e localhost","step":2,"name":"mkdir -p /data/download/filebeat/TEST_filebeat_yml_10.138.25.34","status":"changed","progress":14}}
{"result":{"job":"filebeat","type":"TASK","name":"download python script"}}
{"result":{"job":"filebeat","type":"HOST","host":"10.138.25.34 -\u003e localhost","step":3,"name":"download python script","status":"changed","progress":21}}
{"result":{"job":"filebeat","type":"TASK","name":"mkdir -p /usr/sbin/filebeat"}}
{"result":{"job":"filebeat","type":"HOST","host":"10.138.25.34","step":4,"name":"mkdir -p /usr/sbin/filebeat","status":"changed","progress":28}}
{"result":{"job":"filebeat","type":"TASK","name":"generate filebeat config flie"}}
{"result":{"job":"filebeat","type":"HOST","host":"10.138.25.34 -\u003e localhost","step":5,"name":"generate filebeat config flie","status":"changed","progress":35}}
{"result":{"job":"filebeat","type":"TASK","name":"copy filebeat service file"}}
{"result":{"job":"filebeat","type":"HOST","host":"10.138.25.34","step":6,"name":"copy filebeat service file","status":"ok","progress":42}}
{"result":{"job":"filebeat","type":"TASK","name":"create Downloads dir"}}
{"result":{"job":"filebeat","type":"HOST","host":"10.138.25.34","step":7,"name":"create Downloads dir","status":"ok","progress":50}}
{"result":{"job":"filebeat","type":"TASK","name":"download filebeat agent"}}
{"result":{"job":"filebeat","type":"HOST","host":"10.138.25.34","step":8,"name":"download filebeat agent","status":"ok","progress":57}}
{"result":{"job":"filebeat","type":"TASK","name":"copy yml"}}
{"result":{"job":"filebeat","type":"HOST","host":"10.138.25.34","step":9,"name":"copy yml","status":"ok","progress":64}}
{"result":{"job":"filebeat","type":"TASK","name":"create filebeats configure dir"}}
{"result":{"job":"filebeat","type":"HOST","host":"10.138.25.34","step":10,"name":"create filebeats configure dir","status":"ok","progress":71}}
{"result":{"job":"filebeat","type":"TASK","name":"remove filebeat files"}}
{"result":{"job":"filebeat","type":"HOST","host":"10.138.25.34 -\u003e localhost","step":11,"name":"remove filebeat files","status":"changed","progress":78}}
{"result":{"job":"filebeat","type":"TASK","name":"move yml"}}
{"result":{"job":"filebeat","type":"HOST","host":"10.138.25.34","step":12,"name":"move yml","status":"changed","progress":85}}
{"result":{"job":"filebeat","type":"TASK","name":"enable filebeat"}}
{"result":{"job":"filebeat","type":"HOST","host":"10.138.25.34","step":13,"name":"enable filebeat","status":"ok","progress":92}}
{"result":{"job":"filebeat","type":"TASK","name":"reload all service"}}
{"result":{"job":"filebeat","type":"HOST","host":"10.138.25.34","step":14,"name":"reload all service","status":"changed","progress":99}}
{"result":{"job":"filebeat","type":"TASK","name":"start filebeat"}}
{"result":{"job":"filebeat","type":"HOST","host":"10.138.25.34","step":15,"name":"start filebeat","status":"changed","progress":99}}
{"result":{"job":"filebeat","type":"PLAY","name":"RECAP"}}
{"result":{"job":"filebeat","type":"RECAP","host":"10.138.25.34","status":"ok","ok":15,"changed":8,"progress":100}}

```
# 日志在线下载

## 下个某条日志所在文件的日志
<a id=下个某条日志所在文件的日志> </a>
### 基本信息

**Path：** /api/v1/clusters/{cluster}/projects/{project}/apps/{app}/instances/{instance}/source/download

**Method：** GET

**Query**

| 参数名称  |  是否必须 | 示例  | 备注  |
| ------------ | ------------ | ------------ | ------------ |
| fromTime | 是  |  1511971200000 |  起始时间（包含） |
| toTime | 是  |  1543507200000 |  结束时间（包含） |
| startID | 是  |  eyJkb2NJRCI6IkFXVVhMUEdIcWhmMUo0ZGtKU0VwIiwidGltZVN0YW1wU29ydCI6MTUzMzY5MjcwMDI4MywiaW5kZXgiOiJmaWxlYmVhdC02LjMuMi0yMDE4LjA4LjA4In0= |  日志id |

## 下载符合搜索条件的日志
<a id=下载符合搜索条件的日志> </a>
### 基本信息

**Path：** /api/v1/clusters/{cluster}/projects/{project}/apps/{app}/instances/{instance}/keyword/download

**Method：** GET

### 请求参数
**路径参数**

| 参数名称 | 示例  | 备注  |
| ------------ | ------------ | ------------ | ------------ | ------------ |
| cluster |   |   |
| project |   |   |
| app |   |  查询所有的时候，传递all |
| instance |   |  查询所有的时候，传递all |
**Query**

| 参数名称  |  是否必须 | 示例  | 备注  |
| ------------ | ------------ | ------------ | ------------ |
| fromTime | 是  |  1511971200000 |  起始时间（包含） |
| toTime | 是  |  1543507200000 |  结束时间（包含） |
| keyword | 是  |   |  搜索关键字 |

# 日志离线下载

## 查询某个项目离线日志下载任务列表
<a id=查询某个项目离线日志下载任务列表> </a>
### 基本信息

**Path：** /api/v1/clusters/{cluster}/projects/{project}/offline_task

**Method：** GET

### 返回数据

```javascript
[
    {
        "creator": "jiankunking",
        "downLoadStatus": "success",
        "creatTime": 1538286271641,
        "queryCondition": {
            "app": "btbrrs-rm",
            "cluster": "TEST",
            "instance": "tt-rm-app-v1-0-2418099246-237qw",
            "logDownLoadType": "keyword",
            "fromTime": 1511971200000,
            "project": "TEST",
            "keyword": "host.ip: 10.138.12.14 AND fields.app: tt-rm AND message:BJ0VR30A8",
            "toTime": 1543507200000
        },
        "url": "http://127.0.0.1:8080/downloads/TEST/1538286271641/TEST_TEST_tt-rm_tt-rm-app-v1-0-2418099246-237qw_fd62af37f590cda30df4e7b31ed1ae94.log"
    },
    {
        "creator": "jiankunking",
        "downLoadStatus": "fail",
        "creatTime": 1538286304052,
        "queryCondition": {
            "app": "tt-rm",
            "cluster": "TEST",
            "instance": "tt-rm-app-v1-0-2418099246-237qw",
            "logDownLoadType": "keyword",
            "fromTime": 1511971200000,
            "project": "TEST",
            "keyword": "GIN",
            "toTime": 1543507200000
        },
        "url": ""
    },
    {
        "creator": "jiankunking",
        "downLoadStatus": "downloading",
        "creatTime": 1538286449331,
        "queryCondition": {
            "app": "tt-rm",
            "cluster": "TEST",
            "instance": "tt-rm-app-v1-0-2418099246-237qw",
            "logDownLoadType": "keyword",
            "fromTime": 1511971200000,
            "project": "TEST",
            "keyword": "GIN",
            "toTime": 1543507200000
        },
        "url": ""
    },
    {
        "creator": "jiankunking",
        "downLoadStatus": "success",
        "creatTime": 1538288030738,
        "queryCondition": {
            "app": "tt-rm",
            "cluster": "TEST",
            "instance": "tt-rm-app-v1-0-2418099246-237qw",
            "logDownLoadType": "keyword",
            "fromTime": 1511971200000,
            "project": "TEST",
            "keyword": "GIN",
            "toTime": 1543507200000
        },
        "url": "http://127.0.0.1:8080/downloads/TEST/1538288030738/TEST_TEST_tt-rm_tt-rm-app-v1-0-2418099246-237qw_56e1064e7628003b5d7398ae5229a60e.log"
    },
    {
        "creator": "jiankunking",
        "downLoadStatus": "success",
        "creatTime": 1538290273208,
        "queryCondition": {
            "app": "tt-rm",
            "cluster": "TEST",
            "instance": "tt-rm-app-v1-0-2418099246-237qw",
            "logDownLoadType": "keyword",
            "fromTime": 1537977600000,
            "project": "TEST",
            "keyword": "GIN",
            "toTime": 1538064000000
        },
        "url": "http://127.0.0.1:8080/downloads/TEST/1538290273208/TEST_TEST_tt-rm_tt-rm-app-v1-0-2418099246-237qw_644c22725e36861ebcd1d87092a49c7a.log"
    },
    {
        "creator": "jiankunking",
        "downLoadStatus": "success",
        "creatTime": 1538290673238,
        "queryCondition": {
            "app": "tt-rm",
            "cluster": "TEST",
            "instance": "tt-rm-app-v1-0-2418099246-237qw",
            "logDownLoadType": "keyword",
            "fromTime": 1537977600000,
            "project": "TEST",
            "keyword": "GIN",
            "toTime": 1538013600000
        },
        "url": "http://127.0.0.1:8080/downloads/TEST/1538290673238/TEST_TEST_tt-rm_tt-rm-app-v1-0-2418099246-237qw_a973d0c6ef97ca9afb2ef7ca7c9a14d7.log"
    },
    {
        "creator": "jiankunking",
        "downLoadStatus": "success",
        "creatTime": 1538290692555,
        "queryCondition": {
            "app": "tt-rm",
            "cluster": "TEST",
            "instance": "tt-rm-app-v1-0-2418099246-237qw",
            "logDownLoadType": "keyword",
            "fromTime": 1537977600000,
            "project": "TEST",
            "keyword": "GIN",
            "toTime": 1538064000000
        },
        "url": "http://127.0.0.1:8080/downloads/TEST/1538290692555/TEST_TEST_tt-rm_tt-rm-app-v1-0-2418099246-237qw_644c22725e36861ebcd1d87092a49c7a.log"
    },
    {
        "creator": "jiankunking",
        "downLoadStatus": "downloading",
        "creatTime": 1538292045359,
        "queryCondition": {
            "app": "tt-rm",
            "cluster": "TEST",
            "instance": "tt-rm-app-v1-0-2418099246-237qw",
            "logDownLoadType": "keyword",
            "fromTime": 1537977600000,
            "project": "TEST",
            "keyword": "GIN",
            "toTime": 1538064000000
        },
        "url": ""
    },
    {
        "creator": "jiankunking",
        "downLoadStatus": "downloading",
        "creatTime": 1538292417283,
        "queryCondition": {
            "app": "tt-rm",
            "cluster": "TEST",
            "instance": "tt-rm-app-v1-0-2418099246-237qw",
            "logDownLoadType": "keyword",
            "fromTime": 1537977600000,
            "project": "TEST",
            "keyword": "GIN",
            "toTime": 1538064000000
        },
        "url": ""
    }
]
```
## 离线下载某个实例的日志
<a id=离线下载某个实例的日志> </a>
### 基本信息

**Path：** /api/v1/clusters/{cluster}/projects/{project}/apps/{app}/instances/{instance}/source/offline_download

**Method：** GET



### 请求参数
**Headers**

| 参数名称  | 参数值  |  是否必须 | 示例  | 备注  |
| ------------ | ------------ | ------------ | ------------ | ------------ |
| X-UName  |  登陆人账号 | 是  |  jiankunking |   |

**Query**

| 参数名称  |  是否必须 | 示例  | 备注  |
| ------------ | ------------ | ------------ | ------------ |
| fromTime | 是  |  1511971200000 |  起始时间（包含） |
| toTime | 是  |  1543507200000 |  结束时间（包含） |
| startID | 是  |  eyJkb2NJRCI6IkFXVVhMUEdIcWhmMUo0ZGtKU0VwIiwidGltZVN0YW1wU29ydCI6MTUzMzY5MjcwMDI4MywiaW5kZXgiOiJmaWxlYmVhdC02LjMuMi0yMDE4LjA4LjA4In0= |  日志id |

## 离线下载符合搜索条件的日志
<a id=离线下载符合搜索条件的日志> </a>
### 基本信息

**Path：** /api/v1/clusters/{cluster}/projects/{project}/apps/{app}/instances/{instance}/keyword/offline_download

**Method：** GET

### 请求参数
**Headers**

| 参数名称  | 参数值  |  是否必须 | 示例  | 备注  |
| ------------ | ------------ | ------------ | ------------ | ------------ |
| X-UName  |  登陆人账号 | 是  |  jiankunking |   |

**Query**

| 参数名称  |  是否必须 | 示例  | 备注  |
| ------------ | ------------ | ------------ | ------------ |
| fromTime | 是  |  1511971200000 |  起始时间（包含） |
| toTime | 是  |  1543507200000 |  结束时间（包含） |
| keyword | 是  |  error |  日志下载搜索关键字 |

# 日志条数

## 查询上下文日志总条数
<a id=查询上下文日志总条数> </a>
### 基本信息

**Path：** /api/v1/clusters/{cluster}/projects/{project}/apps/{app}/instances/{instance}/contexts/total

**Method：** GET

**Query**

| 参数名称  |  是否必须 | 示例  | 备注  |
| ------------ | ------------ | ------------ | ------------ |
| fromTime | 是  |  1511971200000 |  起始时间（包含） |
| toTime | 是  |  1543507200000 |  结束时间（包含） |

### 返回数据

```javascript
{
    "total":1222
}
```
## 查询符合条件的日志总条数
<a id=查询符合条件的日志总条数> </a>
### 基本信息

**Path：** /api/v1/clusters/{cluster}/projects/{project}/apps/{app}/instances/{instance}/logs/total

**Method：** GET

**Query**

| 参数名称  |  是否必须 | 示例  | 备注  |
| ------------ | ------------ | ------------ | ------------ |
| fromTime | 是  |  1511971200000 |  起始时间（包含） |
| toTime | 是  |  1543507200000 |  结束时间（包含） |
| 结束时间（包含） | 是  |  error |  日志下载搜索关键字 |

### 返回数据

```javascript
{
    "total":1222
}
```
