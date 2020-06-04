## 系统架构

![](https://i.loli.net/2019/05/08/5cd1d45a156f1.jpg)

-  各个组件均采用 `SpringBoot` 构建。
-  采用 `Netty` 构建底层通信。
-  `Redis` 存放各个客户端的路由信息、账号信息、在线状态等。
-  `Zookeeper` 用于 `IM-server` 服务的注册与发现。


### server

`IM` 服务端；用于接收 `client` 连接、消息透传、消息推送等功能。

**支持集群部署。**

### forward-route

消息路由服务器；用于处理消息路由、消息转发、用户登录、用户下线以及一些运营工具（获取在线用户数等）。

### client

`IM` 客户端；给用户使用的消息终端，一个命令即可启动并向其他人发起通讯（群聊、私聊）。

## 流程图

![](https://i.loli.net/2019/05/08/5cd1d45b982b3.jpg)

- 客户端向 `route` 发起登录。
- 登录成功从 `Zookeeper` 中选择可用 `IM-server` 返回给客户端，并保存登录、路由信息到 `Redis`。
- 客户端向 `IM-server` 发起长连接，成功后保持心跳。
- 客户端下线时通过 `route` 清除状态信息。

## 启动
### 部署 IM-server
```shell
cp server-1.0.0-SNAPSHOT.jar /xx/work/server0/
cd /xx/work/server0/
nohup java -jar  /root/work/server0/server-1.0.0-SNAPSHOT.jar --yiworld.server.port=9000 --app.zk.addr=zk地址  > /root/work/server0/log.file 2>&1 &
```

> server 集群部署同理，只要保证 Zookeeper 地址相同即可。

### 部署路由服务器(forward-route)

```shell
cp forward-route-1.0.0-SNAPSHOT.jar /xx/work/route0/
cd /xx/work/route0/
nohup java -jar /root/work/route0/forward-route-1.0.0-SNAPSHOT.jar --app.zk.addr=zk地址 --spring.redis.host=redis地址 --spring.redis.port=6379  > /root/work/route/log.file 2>&1 &
```

> forward-route 本身就是无状态，可以部署多台；使用 Nginx 代理即可。

### 启动客户端

```shell
cp client-1.0.0-SNAPSHOT.jar /xx/work/route0/
cd /xx/work/route0/
java -jar cim-client-1.0.0-SNAPSHOT.jar --server.port=8084 --yiworld.user.id=唯一客户端ID --yiworld.user.userName=用户名 --yiworld.route.url=http://路由服务器:8083/
```

#### 注册账号
```shell
curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{
  "reqNo": "1234567890",
  "timeStamp": 0,
  "userName": "zhangsan"
}' 'http://路由服务器:8083/registerAccount'
```

从返回结果中获取 `userId`

```json
{
    "code":"9000",
    "message":"成功",
    "reqNo":null,
    "dataBody":{
        "userId":1547028929407,
        "userName":"test"
    }
}
```
#### 启动本地客户端
```shell
# 启动本地客户端
cp client-1.0.0-SNAPSHOT.jar /xx/work/route0/
cd /xx/work/route0/
java -jar client-1.0.0-SNAPSHOT.jar --server.port=8084 --yiworld.user.id=上方返回的userId --yiworld.user.userName=用户名 --yiworld.route.url=http://路由服务器:8083/
```

## 客户端内置命令

| 命令 | 描述|
| ------ | ------ | 
| `:q!` | 退出客户端| 
| `:olu` | 获取所有在线用户信息 | 
| `:all` | 获取所有命令 | 
| `:q [option]` | 【:q 关键字】查询聊天记录 | 
| `:ai` | 开启 AI 模式 | 
| `:qai` | 关闭 AI 模式 | 
| `:pu` | 模糊匹配用户 | 
| `:info` | 获取客户端信息 | 
| `:emoji [option]` | 查询表情包 [option:页码] | 
| `:delay [msg] [delayTime]` | 发送延时消息 | 
| `:` | 更多命令正在开发中。。 | 

### 聊天记录查询

![](https://i.loli.net/2019/05/08/5cd1c310cb796.jpg)

使用命令 `:q 关键字` 即可查询与个人相关的聊天记录。

> 客户端聊天记录默认存放在 `/opt/logs/cim/`，所以需要这个目录的写入权限。也可在启动命令中加入 `--cim.msg.logger.path = /自定义` 参数自定义目录。


### AI 模式

![](https://i.loli.net/2019/05/08/5cd1c30e47d95.jpg)

使用命令 `:ai` 开启 AI 模式，之后所有的消息都会由 `AI` 响应。

`:qai` 退出 AI 模式。

### 前缀匹配用户名

![](https://i.loli.net/2019/05/08/5cd1c32ac3397.jpg)

使用命令 `:qu prefix` 可以按照前缀的方式搜索用户信息。

> 该功能主要用于在移动端中的输入框中搜索用户。 

### 群聊/私聊

#### 群聊
群聊只需要在控制台里输入消息回车后即可发送，同时所有在线客户端都可收到消息。

#### 私聊
私聊首先需要知道对方的 `userID` 才能进行。

输入命令 `:olu` 可列出所有在线用户。

接着使用 `userId;;消息内容` 的格式即可发送私聊消息。

同时另一个账号收不到消息。

### emoji 表情支持
使用命令 `:emoji 1` 查询出所有表情列表，使用表情别名即可发送表情。

![](https://tva1.sinaimg.cn/large/006y8mN6ly1g6j910cqrzj30dn05qjw9.jpg)
![](https://tva1.sinaimg.cn/large/006y8mN6ly1g6j99hazg6j30ax03hq35.jpg)
 
### 延时消息

发送 10s 的延时消息：

```shell
:delay delayMsg 10
```
![](https://tva1.sinaimg.cn/large/006y8mN6ly1g7brppmokqg30gn07gafj.gif)



