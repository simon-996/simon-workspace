# 02 Public Site 阶段文档

## 阶段目标

完成公开访问区域，让访问者能看到项目的核心气质：极简极客风首页、终端特色入口、博客浏览和项目展示。

本阶段强调“第一眼有记忆点”，但不追求复杂动效。

## 功能范围

```text
公开首页
顶部导航
终端展示与基础命令
博客列表
博客详情
项目展示
关于我
联系入口
移动端基础适配
```

## 不做什么

```text
不做博客后台编辑器
不做评论系统
不做复杂 Three.js 场景
不做完整命令补全
不做 AI 生成
不做登录后工作台业务
```

## 前端任务

### 1. 首页结构

首页需要包含：

```text
品牌名 Simon Workspace
一句清晰定位
进入工作台按钮
查看博客按钮
终端窗口
功能流程预览
```

首页视觉：

```text
深色背景
克制的蓝色、青色辅助
代码/终端字体
8px 或更小圆角
避免过多卡片堆叠
```

### 2. 终端 MVP

第一版终端只做前端命令映射：

```text
help
clear
about
blog
blog latest
blog search <keyword>
projects
login
workspace
generate lesson
generate calendar
```

终端命令行为：

```text
公开命令直接跳转或输出信息
工作台命令未登录时打开登录入口
生成命令只打开表单，不直接生成
```

### 3. 博客浏览

第一版可以先使用前端静态 mock 数据，也可以接后端只读接口。

页面：

```text
/blog
/blog/:slug
```

字段：

```text
title
slug
summary
category
tags
publishedTime
readingTime
content
```

### 4. 项目展示

页面：

```text
/projects
```

内容：

```text
项目名称
项目简介
技术栈
亮点
GitHub 地址
在线预览地址
```

### 5. 移动端适配

移动端首页：

```text
单列布局
终端默认压缩为小面板
主按钮明确
导航折叠
文字不能溢出
```

## 后端任务

本阶段后端可轻量：

```text
博客只读接口
项目只读接口
健康检查
公开内容无需登录
```

接口建议：

```text
GET /api/public/articles
GET /api/public/articles/{slug}
GET /api/public/projects
```

如果暂时没有后台，可以使用初始化 SQL 或代码内 mock 数据。

## 数据模型

可先创建：

```text
article
article_category
article_tag
article_tag_relation
article_view
```

评论 `comment` 可以后置。

## 测试与验收

前端：

```text
npm run build --prefix simon-workspace-web
```

人工验收：

```text
首页能进入
终端 help 能输出命令
终端 blog 能跳转博客
终端 workspace 能跳转或打开登录入口
博客列表和详情能浏览
项目页能浏览
移动端 375px 宽度无明显布局错乱
```

后端：

```text
mvn -f simon-workspace-api/pom.xml test
```

至少覆盖：

```text
公开文章列表接口
公开文章详情接口
公开项目接口
```

## 推荐提交

```text
feat(web): build public home page
feat(web): add terminal command dispatcher
feat(web): add blog and project pages
feat(api): add public article and project endpoints
test(api): cover public content endpoints
docs: update public site progress
```

## 阶段完成标准

```text
公开区域无需登录即可访问
首页具备终端记忆点
公开命令和页面跳转可用
博客和项目展示具备基础内容
桌面端和移动端都能正常浏览
构建和测试通过
```
