\# 个人网站 + 极客终端首页 + AI 教学工作台规划文档



\## 1. 项目定位



本项目不是单纯的个人博客，也不是单纯的后台管理系统，而是一个结合个人品牌、技术记录、教学资料生成和 AI 工具能力的个人工作平台。



项目定位：



> \*\*极简极客风个人网站 + Apple 式动效首页 + 图形化教师 AI 工作台 + 终端快捷入口\*\*



核心价值：



\- 对外展示个人技术、教学、项目成果。

\- 对内沉淀博客、课程资料、教学模板。

\- 通过 AI 自动生成教案、教学日历、授课计划等教学文件。

\- 以终端交互作为网站特色入口，形成强记忆点。

\- 所有功能同时支持图形界面和终端命令。

\- 同时适配电脑端和手机端。



一句话概括：



> \*\*图形化工作台为主体，终端式交互为灵魂。\*\*



\---



\## 2. 目标用户



\### 第一阶段



主要服务自己：



\- 信息工程系大学教师。

\- 需要长期维护课程资料。

\- 需要生成每学期教案、教学日历、授课计划等文件。

\- 需要记录技术博客、教学日志和项目过程。

\- 熟悉 Spring Boot、Vue、MySQL、Redis 技术栈。



\### 后期扩展



可以扩展给其他教师使用：



\- 高校教师。

\- 职业院校教师。

\- 培训机构讲师。

\- 课程负责人。

\- 教研室成员。



\---



\## 3. 总体设计原则



\### 3.1 双入口设计



网站所有功能都必须同时支持：



1\. \*\*图形界面操作\*\*

2\. \*\*终端命令快捷操作\*\*



设计原则：



```text

图形界面保证易用性

终端界面提供个性化和效率

命令面板连接两者

```



重点要求：



\- 任何功能都不能只存在于终端中。

\- 终端命令必须映射到一个真实的图形页面、弹窗或表单。

\- 用户不会命令，也能完整使用网站。

\- 终端是特色入口，不是唯一入口。



\### 3.2 多端适配原则



网站需要同时适配：



\- 电脑端

\- 平板端

\- 手机端



设计原则：



```text

桌面端：完整工作台 + 终端交互 + 多栏布局

移动端：轻量浏览 + 快速生成 + 简化工作台

```



一句话：



> \*\*电脑端负责生产力，手机端负责随时可用。\*\*



\---



\## 4. 网站整体结构



```text

公开区

├── 首页

├── 博客

├── 项目展示

├── 关于我

└── 联系方式



登录区 / 工作台

├── 工作台首页

├── 教学资料生成

├── 教案生成

├── 教学日历生成

├── 授课计划生成

├── 课程管理

├── 班级管理

├── 学期管理

├── 模板管理

├── 文件中心

├── 生成记录

└── 系统设置

```



公开区负责展示和阅读。



工作台负责实际生产力。



\---



\## 5. 视觉风格



整体风格定义为：



> \*\*Minimal Geek × Apple Motion\*\*



中文描述：



> \*\*极简极客风 + Apple 式高级动效 + 教师工作台专业感\*\*



关键词：



```text

简洁

极客

克制

高级

科技感

专业

高效

可信

```



\---



\## 6. 首页设计



\### 6.1 首页气质



首页可以偏炫酷，负责形成记忆点。



建议元素：



\- 深色背景。

\- 大终端窗口。

\- 低饱和蓝紫光效。

\- 细线网格。

\- 轻微粒子。

\- 玻璃拟态卡片。

\- Apple 风滚动动画。

\- 文案逐层浮现。

\- 功能流程动画。



首页重点：



```text

惊艳

极客

高级

但不花哨

```



\### 6.2 首页布局



桌面端首页建议：



```text

顶部导航栏

├── 首页

├── 博客

├── 项目

├── 工作台

└── 登录 / 头像



首屏区域

├── 左侧：个人介绍 + 主要按钮

│   ├── 查看博客

│   ├── 进入工作台

│   └── 生成教案

│

└── 右侧：终端窗口

&#x20;   └── 支持命令操作

```



移动端首页建议：



```text

顶部 Logo + 菜单按钮

个人简介

主要按钮：

\- 查看博客

\- 进入工作台

\- 生成教案

终端折叠为一个卡片

```



移动端终端默认折叠：



```text

\[打开 Terminal]

```



点击后进入全屏终端。



\---



\## 7. 配色方案



\### 7.1 首页深色主题



```text

背景色：#020617

主文字：#F8FAFC

次文字：#94A3B8

主色：#3B82F6

辅助色：#8B5CF6

强调色：#06B6D4

边框线：rgba(255,255,255,0.08)

卡片背景：rgba(15,23,42,0.75)

```



\### 7.2 工作台浅色主题



```text

背景色：#F8FAFC

卡片色：#FFFFFF

主文字：#0F172A

次文字：#64748B

主色：#2563EB

辅助色：#7C3AED

边框色：#E5E7EB

成功色：#22C55E

警告色：#F59E0B

错误色：#EF4444

```



\### 7.3 字体建议



```text

中文：PingFang SC / Microsoft YaHei / Noto Sans SC

英文：Inter

代码：JetBrains Mono / Fira Code

```



\---



\## 8. 终端设计



\### 8.1 终端定位



终端是网站特色入口，用来体现极客风格和效率感。



但终端不是唯一入口。



核心原则：



```text

图形界面为主

终端快捷入口为辅

```



\### 8.2 首页终端示例



```bash

simon@workspace:\~$ help

```



输出：



```text

Available commands:



help                  查看命令帮助

about                 关于我

blog                  查看博客

blog latest           查看最新文章

blog search <keyword> 搜索博客

projects              查看项目

login                 登录工作台

logout                退出登录

whoami                查看当前用户

workspace             进入工作台

generate lesson       生成教案

generate calendar     生成教学日历

templates             查看模板

courses               查看课程

history               查看生成记录

clear                 清空终端

theme dark            切换深色模式

theme light           切换浅色模式

```



\### 8.3 终端交互能力



建议支持：



\- 命令输入。

\- 命令提示。

\- 自动补全。

\- 历史命令。

\- `help` 帮助。

\- 鼠标点击常用命令。

\- 复杂操作打开右侧图形化面板。



\### 8.4 终端输出类型



终端输出可以分为：



```text

普通文本

成功提示

错误提示

加载状态

文件下载

表单引导

卡片预览

```



示例：



```text

\[INFO] 正在读取课程信息...

\[AI] 正在生成第 3 周教案...

\[SUCCESS] 教案生成完成

\[FILE] Java Web-第3周教案.docx

```



\---



\## 9. 图形界面设计



\### 9.1 图形界面定位



图形界面是网站的主操作入口。



所有功能都要有完整的图形界面。



包括：



\- 登录。

\- 博客查看。

\- 博客搜索。

\- 工作台。

\- 课程管理。

\- 模板管理。

\- 教案生成。

\- 教学日历生成。

\- 文件下载。

\- 生成记录。



\### 9.2 图形界面与终端对应关系



| 功能         | 图形界面入口        | 终端入口            |

| ------------ | ------------------- | ------------------- |

| 查看博客     | 导航栏 / 博客列表   | `blog`              |

| 搜索博客     | 搜索框              | `blog search redis` |

| 查看项目     | 项目页面            | `projects`          |

| 登录         | 登录按钮 / 登录页   | `login`             |

| 进入工作台   | 导航栏 / 工作台按钮 | `workspace`         |

| 生成教案     | 工作台表单页面      | `generate lesson`   |

| 生成教学日历 | 工作台表单页面      | `generate calendar` |

| 上传模板     | 模板管理页面        | `templates upload`  |

| 查看生成记录 | 生成记录页面        | `history`           |

| 下载文件     | 文件中心按钮        | `download latest`   |



\---



\## 10. 工作台设计



\### 10.1 工作台定位



工作台不是炫酷展示页，而是日常生产力工具。



工作台重点：



```text

清晰

高效

稳定

低干扰

```



\### 10.2 工作台首页



示例：



```text

你好，Simon



常用功能

\- 生成教案

\- 生成教学日历

\- 生成授课计划

\- 上传模板



最近任务

\- Java Web 第3周教案.docx

\- 数据库原理教学日历.xlsx



课程资料

\- 本学期课程：4 门

\- 已上传模板：12 个

\- 已生成文件：36 个

```



\### 10.3 工作台导航



```text

工作台

├── 概览

├── 教学资料生成

│   ├── 生成教案

│   ├── 生成教学日历

│   ├── 生成授课计划

│   └── 批量生成

├── 课程管理

├── 班级管理

├── 学期管理

├── 模板管理

├── 文件中心

├── 生成记录

└── 系统设置

```



\### 10.4 工作台命令面板



工作台内部建议支持快捷命令面板：



```text

⌘ K / Ctrl K 打开命令面板

```



可以快速执行：



```text

生成教案

打开模板管理

搜索课程

下载最近文件

查看生成记录

搜索博客

```



\---



\## 11. 移动端适配



\### 11.1 移动端定位



移动端不强行复制完整后台，而是做成轻量移动工作台。



移动端适合：



\- 查看博客。

\- 查看生成记录。

\- 下载文件。

\- 快速生成单份教案。

\- 快速查看课程资料。

\- 修改少量文本。



移动端不适合：



\- 大量模板编辑。

\- 复杂表格维护。

\- 批量生成复杂文件。

\- 复杂校历配置。



\### 11.2 移动端工作台结构



推荐使用：



```text

底部 Tab

\+

卡片式功能入口

\+

分步骤表单

```



底部导航示例：



```text

首页 / 生成 / 文件 / 博客 / 我的

```



移动端工作台首页：



```text

常用功能

\[生成教案]

\[教学日历]

\[模板管理]

\[生成记录]



最近文件

Java Web 第3周教案.docx

数据库原理教学日历.xlsx

```



\### 11.3 移动端复杂表单处理



复杂功能使用分步骤流程：



```text

第 1 步：选择课程

第 2 步：选择学期

第 3 步：选择模板

第 4 步：填写主题

第 5 步：预览生成内容

第 6 步：导出文件

```



\### 11.4 移动端终端处理



电脑端：



```text

右侧大终端

支持完整命令输入

支持自动补全

支持历史命令

```



手机端：



```text

默认折叠

点击后全屏打开

命令输入框固定底部

提供快捷命令按钮

```



快捷按钮示例：



```text

help

blog

login

workspace

generate lesson

generate calendar

```



\---



\## 12. 响应式断点建议



```text

手机：< 768px

平板：768px - 1024px

桌面：> 1024px

大屏：> 1440px

```



Tailwind 对应断点：



```text

sm: 640px

md: 768px

lg: 1024px

xl: 1280px

2xl: 1536px

```



\### 桌面端布局特点



```text

多栏布局

表格展示

复杂筛选

批量操作

右侧预览

拖拽上传

完整终端

```



\### 移动端布局特点



```text

单栏布局

卡片列表

底部操作按钮

分步骤表单

点击上传

简化筛选

全屏终端

```



\---



\## 13. 核心功能模块



\## 13.1 博客模块



功能：



\- 发布文章。

\- 编辑文章。

\- Markdown 写作。

\- 分类管理。

\- 标签管理。

\- 搜索文章。

\- 文章归档。

\- 代码高亮。

\- 阅读统计。

\- 草稿保存。



文章分类建议：



```text

技术笔记

教学记录

AI 应用

项目总结

开发日志

课程建设

```



\---



\## 13.2 项目展示模块



展示自己的项目成果。



内容包括：



\- 项目名称。

\- 项目简介。

\- 技术栈。

\- 功能截图。

\- GitHub 地址。

\- 在线预览地址。

\- 开发日志。

\- 项目总结。



重点项目：



```text

AI 教学资料生成工作台

校园应用系统

Flutter 项目

Spring Boot 项目

DevOps 自动部署实践

```



\---



\## 13.3 教学资料生成模块



这是工作台的核心。



主要生成：



\- 教案。

\- 教学日历。

\- 授课计划。

\- 实验安排表。

\- 课程总结。

\- 教学反思。

\- 学生成绩分析。

\- 课程考核说明。

\- 课程思政元素。

\- 实验指导书。



第一版优先做：



```text

教案生成

教学日历生成

授课计划生成

Word 模板导出

Excel 模板导出

```



\---



\## 13.4 课程管理模块



保存长期课程数据。



字段包括：



```text

课程名称

课程代码

所属专业

授课班级

年级

总学时

理论学时

实验学时

周学时

教材

课程目标

教学重点

教学难点

考核方式

课程简介

课程大纲

```



\---



\## 13.5 学期管理模块



用于生成教学日历。



字段包括：



```text

学年

学期

开学日期

总周数

考试周

节假日

调课安排

单双周规则

每周上课次数

每次课学时

```



系统根据学期信息自动计算：



```text

第几周

日期范围

是否节假日

是否考试周

是否调课

实际上课日期

```



\---



\## 13.6 模板管理模块



支持上传学校固定模板。



模板类型：



```text

Word 模板

\- 教案模板

\- 授课计划模板

\- 实验指导书模板

\- 课程总结模板



Excel 模板

\- 教学日历模板

\- 学时分配表模板

\- 实验安排表模板

\- 成绩构成表模板

```



Word 模板占位符示例：



```text

课程名称：{{courseName}}

授课班级：{{className}}

授课教师：{{teacher}}

授课内容：{{topic}}



一、教学目标

{{teachingGoal}}



二、教学重点

{{keyPoint}}



三、教学难点

{{difficultPoint}}



四、教学过程

{{teachingProcess}}



五、作业布置

{{homework}}

```



Excel 模板占位符示例：



```text

{{week}}

{{dateRange}}

{{content}}

{{hours}}

{{method}}

{{homework}}

```



\---



\## 13.7 AI 内容生成模块



本项目第一阶段不做复杂 AI Agent，而是做：



```text

AI 内容生成 + 模板填充 + 文件导出

```



AI 负责生成内容。



程序负责填充模板。



流程：



```text

用户输入课程信息

↓

后端拼接 Prompt

↓

AI 返回结构化 JSON

↓

用户预览并修改

↓

后端填入 Word / Excel 模板

↓

导出文件

```



AI 不直接生成 docx 或 xlsx。



AI 输出 JSON，例如：



```json

{

&#x20; "teachingGoal": "通过本次课程，使学生理解 Spring Boot 的基本概念...",

&#x20; "keyPoint": "Spring Boot 项目结构、启动类、配置文件...",

&#x20; "difficultPoint": "自动配置机制与依赖管理...",

&#x20; "teachingMethod": "讲授法、案例演示法、任务驱动法",

&#x20; "teachingProcess": \[

&#x20;   {

&#x20;     "stage": "导入新课",

&#x20;     "time": "10分钟",

&#x20;     "content": "通过传统 Spring 项目配置复杂的问题引入 Spring Boot..."

&#x20;   },

&#x20;   {

&#x20;     "stage": "新课讲授",

&#x20;     "time": "60分钟",

&#x20;     "content": "讲解 Spring Boot 的核心特点、项目结构和启动流程..."

&#x20;   }

&#x20; ],

&#x20; "homework": "完成一个 Spring Boot Hello World 项目...",

&#x20; "ideologicalElement": "引导学生理解软件工程中的效率意识和规范意识...",

&#x20; "reflection": "本节课应关注学生对自动配置机制的理解情况..."

}

```



\---



\## 14. 技术栈



\## 14.1 前端技术栈



最终推荐：



```text

Vue 3

Vite

TypeScript

Naive UI

Tailwind CSS

Pinia

Vue Router

Axios

GSAP

ScrollTrigger

Three.js / TresJS

@vueuse/motion

Markdown 编辑器

代码高亮库

```



\### 选择 Naive UI 的原因



选择：



```text

Naive UI

```



原因：



\- 默认视觉更现代。

\- 更适合极简极客风。

\- 深色主题更自然。

\- TypeScript 体验好。

\- 主题定制方便。

\- 不容易做出传统后台模板味。



技术分工：



```text

Naive UI：表单、弹窗、表格、菜单、上传、通知

Tailwind CSS：整体布局、首页视觉、极客风细节

GSAP：Apple 式滚动动画

自定义组件：终端、命令面板、文件生成流程

```



首页动画建议：



```text

80% CSS + GSAP

20% Three.js

```



不要一开始全站 3D，避免性能和维护复杂。



\---



\## 14.2 后端技术栈



推荐：



```text

Spring Boot 3

Spring Security / Sa-Token

MyBatis-Plus

MySQL

Redis

MinIO / 本地文件存储

poi-tl

EasyExcel

Apache POI

AI API

Docker

Nginx

Jenkins

```



推荐说明：



\- Spring Boot：后端主框架。

\- MySQL：业务数据。

\- Redis：登录、缓存、任务状态、限流。

\- MinIO：模板文件和生成文件存储。

\- poi-tl：Word 模板填充。

\- EasyExcel：Excel 导入导出。

\- Apache POI：复杂 Office 文件处理。

\- AI API：生成教案正文和教学内容。

\- Docker + Nginx + Jenkins：部署和自动化发布。



\---



\## 14.3 AI 模型选择



第一版建议支持一种模型即可。



可选：



```text

OpenAI API

通义千问

DeepSeek

智谱 GLM

豆包

```



后端设计统一接口：



```java

public interface AiService {

&#x20;   String chat(String prompt);

}

```



后期可扩展：



```text

OpenAiServiceImpl

DeepSeekServiceImpl

QwenServiceImpl

DoubaoServiceImpl

```



\---



\## 15. 系统架构



整体架构：



```text

Vue 前端

&#x20;   ↓

Spring Boot API

&#x20;   ↓

MySQL：业务数据

Redis：登录状态、任务状态、缓存、限流

MinIO：模板文件、生成文件

AI API：生成教学内容

poi-tl：生成 Word

EasyExcel / POI：生成 Excel

```



第一版使用单体架构即可。



后期可以拆分：



```text

Spring Boot 主服务

Python 文档解析服务

向量检索服务

AI 工作流服务

```



第一版不建议过早微服务化。



\---



\## 16. 数据库表设计初稿



数据库设计优先服务第一阶段 MVP：登录、课程管理、学期管理、模板管理、AI 内容生成、Word / Excel 导出、生成记录、文件下载。



第一版建议采用单体业务库，所有表统一包含：



```text

id                  BIGINT 主键，雪花 ID 或数据库自增

created\_time        DATETIME 创建时间

updated\_time        DATETIME 更新时间

deleted             TINYINT 逻辑删除，0 未删除，1 已删除

```



命名约定：



```text

数据库表名使用 snake\_case

Java 实体使用 PascalCase

接口 DTO 使用明确的业务语义命名

状态字段统一使用 VARCHAR 存储枚举值

大文本字段使用 TEXT / LONGTEXT

金额、课时、进度等数字字段避免使用字符串

```



\### 16.1 用户与权限相关



第一版虽然主要给自己使用，但仍然建议把登录、角色和权限打好基础，避免教学资料和 AI 生成结果暴露到公网。



```text

user

role

permission

user\_role

role\_permission

login\_log

```



\#### user



保存系统用户。第一版可以只有管理员账号，但表结构保留多用户扩展能力。



| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | 是 | 用户 ID |
| username | VARCHAR(64) | 是 | 登录用户名，唯一 |
| password\_hash | VARCHAR(255) | 是 | 加密后的密码 |
| nickname | VARCHAR(64) | 是 | 显示名称 |
| avatar\_url | VARCHAR(512) | 否 | 头像地址 |
| email | VARCHAR(128) | 否 | 邮箱 |
| phone | VARCHAR(32) | 否 | 手机号 |
| status | VARCHAR(32) | 是 | ENABLED / DISABLED / LOCKED |
| last\_login\_time | DATETIME | 否 | 最近登录时间 |
| created\_time | DATETIME | 是 | 创建时间 |
| updated\_time | DATETIME | 是 | 更新时间 |
| deleted | TINYINT | 是 | 逻辑删除 |



索引建议：



```text

uk\_user\_username(username)

idx\_user\_status(status)

```



\#### role



保存角色信息。



| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | 是 | 角色 ID |
| role\_code | VARCHAR(64) | 是 | 角色编码，如 ADMIN、TEACHER |
| role\_name | VARCHAR(64) | 是 | 角色名称 |
| description | VARCHAR(255) | 否 | 角色说明 |
| created\_time | DATETIME | 是 | 创建时间 |
| updated\_time | DATETIME | 是 | 更新时间 |
| deleted | TINYINT | 是 | 逻辑删除 |



\#### permission



保存权限点。第一版可以内置权限，不必做复杂权限配置页面。



| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | 是 | 权限 ID |
| permission\_code | VARCHAR(128) | 是 | 权限编码，如 COURSE:READ |
| permission\_name | VARCHAR(64) | 是 | 权限名称 |
| resource\_type | VARCHAR(32) | 是 | MENU / BUTTON / API |
| description | VARCHAR(255) | 否 | 权限说明 |
| created\_time | DATETIME | 是 | 创建时间 |
| updated\_time | DATETIME | 是 | 更新时间 |
| deleted | TINYINT | 是 | 逻辑删除 |



\#### login\_log



记录登录行为，便于排查异常访问。



| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | 是 | 日志 ID |
| user\_id | BIGINT | 否 | 用户 ID，登录失败时可为空 |
| username | VARCHAR(64) | 是 | 登录用户名 |
| login\_ip | VARCHAR(64) | 否 | 登录 IP |
| user\_agent | VARCHAR(512) | 否 | 浏览器信息 |
| status | VARCHAR(32) | 是 | SUCCESS / FAILED |
| failure\_reason | VARCHAR(255) | 否 | 失败原因 |
| created\_time | DATETIME | 是 | 登录时间 |



\### 16.2 博客相关



博客模块第一版以文章发布、展示、分类、标签、搜索为主。



```text

article

article\_category

article\_tag

article\_tag\_relation

article\_view

comment

```



\#### article



保存博客文章。



| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | 是 | 文章 ID |
| title | VARCHAR(200) | 是 | 标题 |
| slug | VARCHAR(200) | 是 | URL 标识，唯一 |
| summary | VARCHAR(500) | 否 | 摘要 |
| cover\_url | VARCHAR(512) | 否 | 封面图 |
| content\_md | LONGTEXT | 是 | Markdown 正文 |
| content\_html | LONGTEXT | 否 | 渲染后的 HTML，可缓存 |
| category\_id | BIGINT | 否 | 分类 ID |
| status | VARCHAR(32) | 是 | DRAFT / PUBLISHED / ARCHIVED |
| visibility | VARCHAR(32) | 是 | PUBLIC / PRIVATE |
| view\_count | INT | 是 | 阅读量 |
| published\_time | DATETIME | 否 | 发布时间 |
| created\_time | DATETIME | 是 | 创建时间 |
| updated\_time | DATETIME | 是 | 更新时间 |
| deleted | TINYINT | 是 | 逻辑删除 |



索引建议：



```text

uk\_article\_slug(slug)

idx\_article\_status\_published(status, published\_time)

idx\_article\_category(category\_id)

```



\### 16.3 教学工作台相关



教学工作台是第一阶段核心，数据模型应围绕“基础资料长期维护 + 单次生成任务可追踪 + 文件可下载可复用”设计。



```text

course

class\_info

semester

semester\_calendar

template\_file

template\_field

generate\_task

document\_data

generate\_result

file\_resource

```



\#### course



保存课程长期信息。课程是教案、教学日历、授课计划生成的核心上下文。



| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | 是 | 课程 ID |
| course\_name | VARCHAR(128) | 是 | 课程名称 |
| course\_code | VARCHAR(64) | 否 | 课程代码 |
| major | VARCHAR(128) | 否 | 所属专业 |
| grade | VARCHAR(32) | 否 | 年级 |
| total\_hours | INT | 是 | 总学时 |
| theory\_hours | INT | 否 | 理论学时 |
| experiment\_hours | INT | 否 | 实验学时 |
| weekly\_hours | INT | 否 | 周学时 |
| credit | DECIMAL(4,1) | 否 | 学分 |
| textbook | VARCHAR(255) | 否 | 教材 |
| course\_goal | TEXT | 否 | 课程目标 |
| key\_point | TEXT | 否 | 教学重点 |
| difficult\_point | TEXT | 否 | 教学难点 |
| assessment\_method | TEXT | 否 | 考核方式 |
| syllabus | LONGTEXT | 否 | 课程大纲 |
| description | TEXT | 否 | 课程简介 |
| status | VARCHAR(32) | 是 | ACTIVE / ARCHIVED |
| created\_time | DATETIME | 是 | 创建时间 |
| updated\_time | DATETIME | 是 | 更新时间 |
| deleted | TINYINT | 是 | 逻辑删除 |



索引建议：



```text

idx\_course\_name(course\_name)

idx\_course\_status(status)

```



\#### class\_info



保存授课班级信息。课程与班级可以是一对多关系。



| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | 是 | 班级 ID |
| class\_name | VARCHAR(128) | 是 | 班级名称 |
| major | VARCHAR(128) | 否 | 专业 |
| grade | VARCHAR(32) | 否 | 年级 |
| student\_count | INT | 否 | 学生人数 |
| counselor | VARCHAR(64) | 否 | 辅导员 |
| remark | VARCHAR(500) | 否 | 备注 |
| created\_time | DATETIME | 是 | 创建时间 |
| updated\_time | DATETIME | 是 | 更新时间 |
| deleted | TINYINT | 是 | 逻辑删除 |



\#### semester



保存学期信息，用于计算教学周、考试周、节假日和教学日历。



| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | 是 | 学期 ID |
| academic\_year | VARCHAR(32) | 是 | 学年，如 2026-2027 |
| semester\_name | VARCHAR(32) | 是 | 第一学期 / 第二学期 |
| start\_date | DATE | 是 | 开学日期 |
| end\_date | DATE | 否 | 结束日期 |
| total\_weeks | INT | 是 | 总周数 |
| exam\_week | INT | 否 | 考试周 |
| holiday\_json | JSON | 否 | 节假日配置 |
| adjustment\_json | JSON | 否 | 调课配置 |
| remark | VARCHAR(500) | 否 | 备注 |
| status | VARCHAR(32) | 是 | PLANNED / ACTIVE / CLOSED |
| created\_time | DATETIME | 是 | 创建时间 |
| updated\_time | DATETIME | 是 | 更新时间 |
| deleted | TINYINT | 是 | 逻辑删除 |



索引建议：



```text

uk\_semester\_year\_name(academic\_year, semester\_name)

idx\_semester\_status(status)

```



\#### semester\_calendar



保存学期周次计算结果。可以由 semester 配置生成，也允许人工微调。



| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | 是 | 日历 ID |
| semester\_id | BIGINT | 是 | 学期 ID |
| week\_no | INT | 是 | 第几周 |
| start\_date | DATE | 是 | 本周开始日期 |
| end\_date | DATE | 是 | 本周结束日期 |
| is\_exam\_week | TINYINT | 是 | 是否考试周 |
| is\_holiday | TINYINT | 是 | 是否含节假日 |
| holiday\_note | VARCHAR(255) | 否 | 节假日说明 |
| adjustment\_note | VARCHAR(255) | 否 | 调课说明 |
| created\_time | DATETIME | 是 | 创建时间 |
| updated\_time | DATETIME | 是 | 更新时间 |
| deleted | TINYINT | 是 | 逻辑删除 |



索引建议：



```text

uk\_semester\_week(semester\_id, week\_no)

```



\#### template\_file



保存 Word / Excel 模板元信息。模板文件本身由 file\_resource 统一管理。



| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | 是 | 模板 ID |
| template\_name | VARCHAR(128) | 是 | 模板名称 |
| template\_type | VARCHAR(32) | 是 | LESSON / CALENDAR / PLAN / SUMMARY / EXPERIMENT |
| file\_resource\_id | BIGINT | 是 | 文件资源 ID |
| file\_ext | VARCHAR(16) | 是 | docx / xlsx |
| placeholder\_json | JSON | 否 | 占位符快照 |
| version | INT | 是 | 模板版本 |
| status | VARCHAR(32) | 是 | ENABLED / DISABLED |
| description | VARCHAR(500) | 否 | 模板说明 |
| created\_time | DATETIME | 是 | 创建时间 |
| updated\_time | DATETIME | 是 | 更新时间 |
| deleted | TINYINT | 是 | 逻辑删除 |



\#### template\_field



保存模板占位符字段，便于前端生成表单、后端校验 AI JSON。



| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | 是 | 字段 ID |
| template\_id | BIGINT | 是 | 模板 ID |
| field\_key | VARCHAR(128) | 是 | 占位符 key，如 teachingGoal |
| field\_label | VARCHAR(128) | 是 | 字段中文名 |
| field\_type | VARCHAR(32) | 是 | TEXT / RICH_TEXT / NUMBER / DATE / TABLE / LIST |
| required | TINYINT | 是 | 是否必填 |
| default\_value | TEXT | 否 | 默认值 |
| sort\_order | INT | 是 | 排序 |
| description | VARCHAR(500) | 否 | 字段说明 |
| created\_time | DATETIME | 是 | 创建时间 |
| updated\_time | DATETIME | 是 | 更新时间 |
| deleted | TINYINT | 是 | 逻辑删除 |



索引建议：



```text

uk\_template\_field(template\_id, field\_key)

```



\#### generate\_task



保存一次生成任务。所有 AI 生成、模板填充、导出文件都围绕任务流转。



| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | 是 | 任务 ID |
| task\_no | VARCHAR(64) | 是 | 任务编号，唯一 |
| task\_type | VARCHAR(32) | 是 | LESSON / CALENDAR / PLAN / SUMMARY |
| user\_id | BIGINT | 是 | 发起用户 ID |
| course\_id | BIGINT | 否 | 课程 ID |
| class\_id | BIGINT | 否 | 班级 ID |
| semester\_id | BIGINT | 否 | 学期 ID |
| template\_id | BIGINT | 否 | 模板 ID |
| week\_no | INT | 否 | 周次，教案生成常用 |
| topic | VARCHAR(255) | 否 | 授课主题 |
| input\_json | JSON | 否 | 用户输入参数 |
| status | VARCHAR(32) | 是 | 任务状态 |
| progress | INT | 是 | 进度，0-100 |
| error\_message | TEXT | 否 | 错误信息 |
| started\_time | DATETIME | 否 | 开始时间 |
| finished\_time | DATETIME | 否 | 完成时间 |
| created\_time | DATETIME | 是 | 创建时间 |
| updated\_time | DATETIME | 是 | 更新时间 |
| deleted | TINYINT | 是 | 逻辑删除 |



任务状态：



```text

PENDING         已创建，等待处理

GENERATING      AI 正在生成结构化内容

PREVIEW\_READY   AI 内容已生成，等待用户预览确认

FILLING\_TEMPLATE 正在填充 Word / Excel 模板

SUCCESS         生成成功

FAILED          生成失败

CANCELED        用户取消

```



索引建议：



```text

uk\_generate\_task\_no(task\_no)

idx\_generate\_task\_user(user\_id, created\_time)

idx\_generate\_task\_status(status)

idx\_generate\_task\_course(course\_id)

```



\#### document\_data



保存 AI 生成的结构化 JSON 和用户编辑后的最终 JSON。AI 不直接生成 docx / xlsx。



| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | 是 | 数据 ID |
| task\_id | BIGINT | 是 | 任务 ID |
| ai\_model | VARCHAR(64) | 否 | 使用的 AI 模型 |
| prompt | LONGTEXT | 否 | 实际发送给 AI 的 Prompt |
| raw\_response | LONGTEXT | 否 | AI 原始返回 |
| data\_json | JSON | 是 | 解析后的结构化内容 |
| edited\_json | JSON | 否 | 用户编辑确认后的内容 |
| version | INT | 是 | 编辑版本 |
| created\_time | DATETIME | 是 | 创建时间 |
| updated\_time | DATETIME | 是 | 更新时间 |
| deleted | TINYINT | 是 | 逻辑删除 |



索引建议：



```text

idx\_document\_data\_task(task\_id)

```



\#### generate\_result



保存任务生成出的最终文件。一个任务可以产生多个结果文件。



| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | 是 | 结果 ID |
| task\_id | BIGINT | 是 | 任务 ID |
| file\_resource\_id | BIGINT | 是 | 文件资源 ID |
| file\_name | VARCHAR(255) | 是 | 展示文件名 |
| file\_type | VARCHAR(32) | 是 | DOCX / XLSX / PDF / ZIP |
| result\_type | VARCHAR(32) | 是 | PREVIEW / FINAL |
| created\_time | DATETIME | 是 | 创建时间 |
| updated\_time | DATETIME | 是 | 更新时间 |
| deleted | TINYINT | 是 | 逻辑删除 |



\#### file\_resource



统一保存上传文件和生成文件的元信息。第一版可以使用本地磁盘，后续平滑切换到 MinIO。



| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | BIGINT | 是 | 文件 ID |
| original\_name | VARCHAR(255) | 是 | 原始文件名 |
| stored\_name | VARCHAR(255) | 是 | 存储文件名 |
| bucket | VARCHAR(64) | 否 | MinIO bucket，本地存储时可为空 |
| storage\_path | VARCHAR(1024) | 是 | 存储路径 |
| access\_url | VARCHAR(1024) | 否 | 访问地址 |
| file\_ext | VARCHAR(16) | 是 | 文件扩展名 |
| mime\_type | VARCHAR(128) | 否 | MIME 类型 |
| file\_size | BIGINT | 是 | 文件大小，字节 |
| sha256 | VARCHAR(128) | 否 | 文件哈希 |
| source | VARCHAR(32) | 是 | UPLOAD / GENERATED |
| owner\_id | BIGINT | 否 | 上传或生成用户 |
| created\_time | DATETIME | 是 | 创建时间 |
| updated\_time | DATETIME | 是 | 更新时间 |
| deleted | TINYINT | 是 | 逻辑删除 |



\### 16.4 表关系与业务边界



核心关系：



```text

user 1 - N generate\_task

course 1 - N generate\_task

class\_info 1 - N generate\_task

semester 1 - N semester\_calendar

semester 1 - N generate\_task

template\_file 1 - N template\_field

template\_file 1 - N generate\_task

generate\_task 1 - 1 document\_data

generate\_task 1 - N generate\_result

file\_resource 1 - N template\_file

file\_resource 1 - N generate\_result

```



第一版边界：



```text

课程、学期、模板是长期基础数据

生成任务是一次性业务流水

AI 生成内容必须先保存为 JSON，再进入模板填充

用户确认前的内容只能作为预览，不直接生成最终文件

生成文件和上传模板统一进入 file\_resource

文件访问必须经过权限校验，不直接暴露真实磁盘路径

```



\---



\## 17. 教案生成流程



单周教案生成流程：



```text

1\. 用户点击“生成教案”或输入 generate lesson

2\. 系统检查登录状态

3\. 打开教案生成页面或面板

4\. 用户选择课程

5\. 用户选择学期

6\. 用户选择周次

7\. 用户选择 Word 模板

8\. 用户输入授课主题或章节

9\. 后端生成 Prompt

10\. AI 返回教案 JSON

11\. 前端展示可编辑预览

12\. 用户确认

13\. 后端用 poi-tl 填充 Word 模板

14\. 保存生成文件

15\. 返回下载链接

```



终端示例：



```bash

simon@workspace:\~$ generate lesson --course "Java Web" --week 3 --topic "Spring Boot 入门"

```



输出：



```text

\[INFO] 正在读取课程信息...

\[AI] 正在生成第 3 周教案...

\[DOCX] 正在填充 Word 模板...

\[SUCCESS] 教案生成完成



File:

Java Web-第3周教案.docx

```



\---



\## 18. 教学日历生成流程



教学日历生成流程：



```text

1\. 用户点击“生成教学日历”或输入 generate calendar

2\. 选择课程

3\. 选择学期

4\. 设置总周数、周学时、考试周

5\. 输入课程大纲或章节列表

6\. AI 自动拆分每周教学内容

7\. 系统结合校历生成周次和日期

8\. 用户预览教学日历

9\. 后端填充 Excel 模板

10\. 导出 xlsx 文件

```



输出内容包括：



```text

周次

日期

教学内容

学时

教学方式

作业安排

实验安排

备注

```



\---



\## 19. 文件生成策略



核心原则：



```text

AI 生成内容

程序填充模板

```



不要让 AI 直接生成 Word 或 Excel 文件。



\### 19.1 Word 生成



推荐使用：



```text

poi-tl

```



适合：



```text

教案

授课计划

实验指导书

课程总结

教学反思

```



\### 19.2 Excel 生成



推荐使用：



```text

EasyExcel

Apache POI

```



适合：



```text

教学日历

实验安排表

成绩构成表

学时分配表

```



\### 19.3 文件存储



第一版可以本地存储。



后期建议使用：



```text

MinIO

```



保存：



```text

模板文件

生成文件

上传资料

附件

```



\---



\## 20. 权限设计



公开访问：



```text

首页

博客列表

博客详情

项目展示

关于我

```



登录访问：



```text

工作台

课程管理

模板管理

教案生成

教学日历生成

文件下载

生成记录

```



管理员权限：



```text

用户管理

角色管理

系统设置

模型配置

文件清理

```



第一版只有自己使用，也建议把登录和权限做好，避免教学资料暴露到公网。



\---



\## 21. Redis 使用场景



Redis 用于：



```text

登录 token

验证码

AI 任务状态

任务进度

防重复提交

接口限流

热点博客缓存

模板字段缓存

命令历史缓存

```



AI 生成任务状态示例：



```text

PENDING

GENERATING

PREVIEW\_READY

FILLING\_TEMPLATE

SUCCESS

FAILED

CANCELED

```



\---



\## 22. 终端命令设计



\### 22.1 终端定位



终端不是完整 Shell，也不是隐藏后台能力的唯一入口，而是网站的特色快捷入口。



第一版终端定位：



```text

快捷命令入口

页面跳转器

任务触发器

状态提示器

```



不做：



```text

真实系统命令执行

任意脚本执行

复杂管道操作

后台静默完成复杂业务

绕过图形界面的隐藏功能

```



核心原则：



```text

所有终端命令必须映射到真实图形界面能力

复杂命令只负责打开页面、弹窗或抽屉，并预填参数

涉及 AI 生成、文件导出、删除等操作时，必须进入图形化确认流程

不会命令的用户，也必须能通过图形界面完成同样功能

```



\### 22.2 MVP 命令范围



第一版只实现少量高频命令，保证稳定、好记、能形成首页记忆点。



\#### 公开命令



```bash

help

clear

about

blog

blog latest

blog search <keyword>

projects

contact

theme dark

theme light

```



\#### 登录命令



```bash

login

logout

whoami

```



\#### 工作台命令



```bash

workspace

courses

semesters

templates

files

tasks

history

```



\#### 教学资料命令



```bash

generate lesson

generate calendar

download latest

open file <fileId>

```



\### 22.3 命令与图形界面映射



| 命令 | 图形界面动作 | MVP 行为 |
| --- | --- | --- |
| help | 终端内输出帮助 | 直接输出命令列表 |
| clear | 清空终端输出 | 直接清空当前终端视图 |
| about | 关于我页面 | 跳转或滚动到关于区域 |
| blog | 博客列表页 | 跳转博客列表 |
| blog latest | 最新文章 | 打开最新文章或最新文章列表 |
| blog search <keyword> | 博客搜索页 | 跳转博客页并预填关键词 |
| projects | 项目展示页 | 跳转项目页 |
| login | 登录弹窗 / 登录页 | 打开登录界面 |
| logout | 退出确认 | 登录状态下执行退出 |
| whoami | 当前用户信息 | 终端输出当前登录状态 |
| workspace | 工作台首页 | 未登录先打开登录界面 |
| courses | 课程管理页 | 跳转课程管理 |
| semesters | 学期管理页 | 跳转学期管理 |
| templates | 模板管理页 | 跳转模板管理 |
| files | 文件中心 | 跳转文件中心 |
| tasks / history | 生成记录页 | 跳转生成记录 |
| generate lesson | 教案生成表单 | 打开表单，不直接生成 |
| generate calendar | 教学日历生成表单 | 打开表单，不直接生成 |
| download latest | 文件中心 | 定位到最近生成文件，由用户确认下载 |
| open file <fileId> | 文件预览 / 下载面板 | 打开文件详情 |



\### 22.4 命令参数规则



第一版支持简单参数解析，目标是预填图形化表单，不追求完整 CLI 能力。



示例：



```bash

generate lesson --course "Java Web" --week 3 --topic "Spring Boot 入门"

```



行为：



```text

1. 解析 course、week、topic 参数

2. 检查登录状态

3. 打开教案生成页面或右侧抽屉

4. 自动预填课程、周次、主题

5. 用户检查并点击“生成”

6. 生成完成后在终端输出任务状态和文件入口

```



参数解析边界：



```text

支持 --key value

支持带引号字符串

支持 keyword 关键词搜索

不支持管道

不支持重定向

不支持多命令串联

不支持任意 JavaScript 执行

```



\### 22.5 终端执行反馈



终端输出要短、明确、有状态感。



输出类型：



```text

INFO       普通信息

SUCCESS    成功

WARN       需要用户注意

ERROR      错误

AI         AI 生成中

FILE       文件入口

NAV        页面跳转

```



示例：



```text

[NAV] 已打开教案生成面板

[INFO] 已预填课程：Java Web

[INFO] 已预填周次：第 3 周

[AI] 等待你确认后开始生成

```



生成完成示例：



```text

[SUCCESS] 教案生成完成

[FILE] Java Web-第 3 周教案.docx

```



\### 22.6 错误处理



常见错误：



| 场景 | 终端输出 | 图形界面动作 |
| --- | --- | --- |
| 未登录访问工作台 | [WARN] 请先登录 | 打开登录弹窗 |
| 命令不存在 | [ERROR] 未识别命令，输入 help 查看帮助 | 保持当前页面 |
| 参数缺失 | [WARN] 缺少必要参数 | 打开对应表单让用户补齐 |
| 课程不存在 | [WARN] 未找到课程 | 打开课程选择框 |
| 文件不存在 | [ERROR] 文件不存在或无权限 | 打开文件中心 |
| AI 生成失败 | [ERROR] 生成失败，已保存任务记录 | 打开任务详情 |



\### 22.7 后续增强命令



第二阶段再考虑增强终端体验：



```bash

generate plan

generate summary

generate lesson --batch

templates upload

task retry <taskId>

task cancel <taskId>

download task <taskId>

```



后续增强能力：



```text

命令自动补全

命令历史上下切换

常用命令收藏

根据登录状态展示不同 help

根据当前页面推荐命令

移动端快捷命令按钮

```



不建议加入：



```text

复杂脚本语言

多命令事务

绕过确认的批量删除

直接暴露服务器文件路径

直接输出敏感 Token 或配置

```



\---



\## 23. 首页动画设计



\### 23.1 Hero 动画



```text

标题淡入

副标题上移

终端窗口展开

光效慢慢扩散

按钮延迟出现

```



\### 23.2 终端动画



```text

打字机效果

命令执行闪烁

输出逐行出现

成功状态高亮

```



\### 23.3 滚动叙事动画



展示教学资料生成流程：



```text

课程信息

→

AI 生成

→

Word 模板

→

Excel 模板

→

导出文件

```



\### 23.4 卡片动画



```text

hover 轻微上浮

边框渐变光

图标轻微移动

玻璃卡片浮动

```



注意：



```text

首页可以华丽

工作台要克制

移动端减少复杂动画

```



\---



\## 24. 第一阶段 MVP



第一阶段目标：



> \*\*先做出能自己用的个人网站和教学资料生成工具。\*\*



功能范围：



```text

1\. 首页终端

2\. 图形化首页导航

3\. 博客列表

4\. 博客详情

5\. 登录

6\. 工作台首页

7\. 课程管理

8\. 学期管理

9\. 模板上传

10\. 单周教案生成

11\. 教学日历生成

12\. Word 模板导出

13\. Excel 模板导出

14\. 生成记录

15\. 文件下载

16\. 电脑端和手机端基础适配

```



第一阶段暂不做：



```text

复杂 AI Agent

多用户协作

向量知识库

课程资料 RAG

自动读取 PDF

复杂工作流编排

多模型自动切换

```



\---



\## 25. 第二阶段功能



第二阶段增强：



```text

整学期批量生成教案

上传课程大纲自动拆分周计划

课程资料库

模板字段自动识别

AI 辅助写博客

生成课程总结

生成实验指导书

生成课程思政案例

文件打包下载 zip

移动端生成流程优化

工作台命令面板优化

```



\---



\## 26. 第三阶段功能



第三阶段再考虑 Agent 化：



```text

自动读取课程大纲

自动分析模板字段

自动规划 16 周教学内容

自动避开节假日

自动生成全部文档

自动检查模板完整性

自动发现缺失字段

自动修复生成结果

```



到这个阶段，项目才逐渐变成真正的 AI Agent。



\---



\## 27. 推荐开发顺序



\### 第一步：基础工程



```text

搭建 Vue 3 项目

接入 Naive UI

接入 Tailwind CSS

搭建 Spring Boot 项目

配置 MySQL

配置 Redis

完成登录

完成基础权限

```



\### 第二步：公开网站



```text

首页终端

图形化导航

博客列表

博客详情

项目展示

关于我

移动端首页适配

```



\### 第三步：工作台基础



```text

工作台布局

课程管理

学期管理

模板管理

文件上传

生成记录

移动端工作台卡片布局

```



\### 第四步：文档生成



```text

Word 模板填充

Excel 模板填充

文件下载

历史记录

```



\### 第五步：AI 接入



```text

AI 生成教案 JSON

AI 生成教学日历 JSON

前端预览编辑

确认后导出

```



\### 第六步：首页动效



```text

GSAP 动画

终端打字效果

滚动叙事

右侧动态预览

移动端动画降级

```



建议先把功能跑通，再慢慢打磨动效。



\---



\## 28. 最终产品形态



最终网站可以形成三个身份：



```text

个人品牌网站

技术与教学博客

AI 教学资料工作台

```



外部看到的是：



```text

一个简洁、极客、有高级动效的个人网站

```



自己使用的是：



```text

一个可以生成教案、教学日历、授课计划的私人工作台

```



后期扩展后，它可以变成：



```text

高校教师智能教学资料生成平台

```



\---



\## 29. 当前最终推荐方案



最终推荐路线：



```text

Vue 3 + Vite + TypeScript

\+

Naive UI + Tailwind CSS + GSAP

\+

Spring Boot 3 + MySQL + Redis

\+

MinIO + poi-tl + EasyExcel

\+

AI 内容生成

\+

极简极客风首页终端

\+

图形化工作台

\+

电脑端和手机端响应式适配

```



核心开发原则：



```text

先做实用功能

再做高级动效

先做模板填充

再做 AI 生成

先做图形界面完整可用

再做终端快捷体验

先做桌面端完整生产力

再优化移动端轻量体验

先做自己使用

再考虑产品化

```



当前版本的项目重点：



```text

首页有记忆点

博客能沉淀内容

工作台能解决真实教学资料生成问题

图形界面完整可用

终端入口体现极客风格

手机和电脑都能使用

```



\---



\## 30. 一句话总结



> 这个网站不是普通博客，而是一个以极简极客风为视觉特色、以图形化工作台为主体、以终端交互为灵魂、以 AI 教学资料生成为核心生产力的个人网站。

