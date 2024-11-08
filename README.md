# AI/联网对战五子棋——基于Java与Mysql
<p align="center">
  <a href="#安装说明"><img src="https://img.shields.io/badge/版本-2.0.0-blue.svg" alt="版本"></a>
  <a href="#开发环境"><img src="https://img.shields.io/badge/JDK-17+-green.svg" alt="JDK版本"></a>
  <a href="#许可证"><img src="https://img.shields.io/badge/许可证-MIT-yellow.svg" alt="许可证"></a>
</p>

## 项目简介

### 背景
本项目是一个基于Java与Mysql开发的网络五子棋对战平台。项目采用现代化的软件架构和设计模式，实现了完整的游戏功能和网络通信机制。

### 项目特点
- 采用Java语言开发，跨平台运行
- 基于Socket的可靠网络通信
- 美观的Swing图形界面
- 完善的游戏逻辑和规则实现
- 支持多人同时在线对战

## 功能特性

### 核心游戏功能
1. 游戏对战系统
    - 标准五子棋规则实现
    - 实时对战功能
    - 胜负判定系统
    - 计时器功能
    - 悔棋功能
    - 认输功能

2. 棋盘系统
    - 15×15标准棋盘
    - 落子动画效果
    - 最后落子标记
    - 获胜路径显示
    - 鼠标悬停提示

### 网络功能

1. 房间系统
    - 创建私人房间
    - 快速匹配
    - 房间列表显示
    - 房间状态监控

### 其他功能

- 历史对局回放
- 基于Kimi大模型的棋局点评

## 系统架构

### 整体架构

![图片1.png](https://img.picui.cn/free/2024/11/08/672ddf58a05d5.png)


### 技术架构

1. 后端技术栈
    - 核心框架：Java Socket
    - 数据库：MySQL 8.0
    - 缓存：Redis
    - 消息队列：RabbitMQ
    - 日志框架：Log4j2

2. 前端技术栈
    - GUI框架：Java Swing
    - 图形引擎：Java 2D

3. 网络通信
    - TCP/IP协议
    - 自定义应用层协议
    - WebSocket支持

## 安装说明

### 环境要求
- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+
- 最小内存要求：2GB
- 推荐系统：Windows 10

### 安装步骤
1. 克隆项目
```bash
git clone git@github.com:Ethan-Zheng-Zhou/Gomoku.git
```
2. 导入 JDBC 依赖
3. 修改 DatabaseConfig.java 文件
```Java
//注意，这里的端口号（3306）改为您本机的端口号
private static final String URL = "jdbc:mysql://localhost:3306/gomoku?useSSL=false&serverTimezone=UTC";  
private static final String USER = "输入您自己的用户名";  
private static final String PASSWORD = "输入您自己的密码";
```
4. 修改 kimiAPI.java 文件
```Java
private static final String API_KEY = "输入您自己的Api-key";  
private static final String BASE_URL = "https://api.moonshot.cn/v1/chat/completions";  
private static final String MODEL = "moonshot-v1-8k";  
```

## 关于作者

### 开发团队
- 项目负责人：【周正】
- AI对战、玩家对战、对局复盘、数据库、大模型棋局点评：【周正】
- 联网对战、Maven项目构建：【周陇】
- 测试人员：【周正】、【周陇】

### 联系方式
- 邮箱：ethan.zheng.zhou@outlook.com
- QQ: 577517234

### 项目地址
- Github：[Ethan-Zheng-Zhou/Gomoku: 基于Java和Swing的人机对战联网五子棋游戏](https://github.com/Ethan-Zheng-Zhou/Gomoku)

## 许可证
本项目采用 MIT 许可证，详情请参见 [LICENSE](LICENSE) 文件。

---
© 2024 南京邮电大学计算机学院、软件学院、网络空间安全学院. All Rights Reserved.