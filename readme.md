## 产品介绍

### 项目背景

本项目为 MeetingLib 应用程序的后端服务，提供了人员身份控制、设备状态控制、踢出用户等接口能力。
MeetingLib 是在融云 IM、RTC 能力之上封装的提供实时音视频会议场景中常见会控功能的能力库，可帮助开发者快速实现会控常见功能（踢人、人员身份转让、设备状态控制等）。

### 项目介绍

Meeting-Server 只维护了会议中关于会控的基本信息(主持人Id、主讲Id等)，不提供会议、会议成员等详细信息的管理与维护，这些信息需由客户自己的 AppServer 进行维护管理。

Meeting-Server 提供了会议创建、会议删除接口，AppServer 在会议创建或删除时需要调用该接口，完成会控信息的同步。

#### 关于鉴权

访问 MeetingServer 提供的 API 接口，均需要经过鉴权才可访问，Meeting-Server 采用基于 jwt token 的鉴权机制。AppServer 需要调用 登录接口 获取鉴权 token,
每次请求 API 接口时，均需要在 HTTP Request Header 里面携带 RCMT-Token 信息。

![avatar](docs/images/authentication.png)

##### 登录校验

MeetingServer 对外封装了登录参数（userId + token + extra）校验接口，接口默认实现返回 true，具体校验逻辑，需由客户根据自己的具体业务场景进行二开实现。

#### 技术栈

* Meeting-Server 基于 SpringBoot 框架实现
* 依赖于 Mysql 进行数据存储、Redis 进行数据缓存
* 依赖于融云 IM 服务进行收发信令

#### 接口文档

文档目录: ${path}/meeting-server/docs/apidoc/，直接在浏览器打开 index.html 即可查看。

### 快速集成

#### AppServer 客户需要做的事情

AppServer 是客户自己的应用 Server， 在会议创建或删除时需要调用 MeetingServer 提供的创建会议、删除接口，完成会控信息的同步。接口说明详见接口文档中会议创建、会议删除接口。

#### Meeting-Server 需要客户修改的地方

下载 MeetingServer 源码之后，客户需要自定义实现用户名、密码的校验鉴权逻辑，代码位置：UserAuthProviderImpl.java 类中 doCredentialsMatch() 方法

```
    @Override
    public boolean doCredentialsMatch(String userId, String token, String extra) {
        //TODO 校验用户身份是否合法
        return true;
    }
```

#### Meeting-Server 服务部署

> 请提前准备好 Meeting-Server 所依赖的 Mysql、Redis (version >= 4.0.0) 等基础服务。

##### 服务配置

服务所用到的配置信息统一在 application.yml 文件中管理维护。

* 融云 IM 服务配置

请前往 [融云官网](https://www.rongcloud.cn/) 注册、申请 AppKey 和 Secret, 并替换 application.yml 文件中的`融云 IM 配置`，配置如下:

```
im:
  appKey: kkkkkk
  secret: ssssss
  host: http://api-cn.ronghub.com
```
* 数据库连接配置

请您将实际数据库连接配置替换 application.yml 文件中的 `数据库连接配置`，配置如下：

```
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/meetingdb?useSSL=false
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
```

* Redis 连接配置

请您将实际 Redis 连接配置替换 application.yml 文件中的 `Redis 连接配置`，配置如下：

```
spring:
  redis:
    database: 0
    host: 127.0.0.1
    port: 6379
    password:
```
##### 数据库初始化

执行如下命令，创建数据库:

```
create database meetingdb;
```

##### 服务打包运行

通过 mvn package 编译出 jar 或者 IntelliJ IDE 直接运行工程

*  java -jar meeting-server-2.0.0-SNAPSHOT.jar --spring.config.location=application.yml 启动服务
*  默认启用 8080 端口，默认 http 请求
