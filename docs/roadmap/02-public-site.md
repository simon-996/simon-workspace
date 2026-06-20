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

## 阶段执行 Todo

当前状态：早期进行中。最后核查时间：2026-06-20。

### 首页

- [x] 创建公开首页路由 `/`
- [x] 首页展示品牌名 `Simon Workspace`
- [x] 首页展示一句项目定位
- [x] 首页提供进入工作台按钮
- [x] 首页展示终端风格窗口
- [x] 首页包含顶部导航
- [ ] 将 `Read Blog` 按钮连接到 `/blog` 或博客锚点
- [ ] 增加功能流程预览：公开浏览、进入工作台、生成资料、下载文件
- [ ] 增加关于个人/技术方向的简短区块
- [ ] 增加联系入口
- [ ] 检查首页移动端 375px 宽度文字不溢出
- [ ] 检查首页桌面端首屏是否露出下一段内容

### 终端 MVP

- [x] 创建静态终端展示组件
- [ ] 将终端改为可输入命令的组件
- [ ] 支持 `help`
- [ ] 支持 `clear`
- [ ] 支持 `about`
- [ ] 支持 `blog`
- [ ] 支持 `blog latest`
- [ ] 支持 `blog search <keyword>`
- [ ] 支持 `projects`
- [ ] 支持 `login`
- [ ] 支持 `workspace`
- [ ] 支持 `generate lesson`
- [ ] 支持 `generate calendar`
- [ ] 未登录时工作台命令打开登录入口或给出明确提示
- [ ] 生成类命令只打开表单，不直接生成文件
- [ ] 命令输出和页面跳转都可测试

### 博客浏览

- [ ] 创建 `/blog` 路由
- [ ] 创建 `/blog/:slug` 路由
- [ ] 定义文章字段：`title`、`slug`、`summary`、`category`、`tags`、`publishedTime`、`readingTime`、`content`
- [ ] 准备第一批静态文章数据或后端只读接口
- [ ] 实现博客列表页
- [ ] 实现博客详情页
- [ ] 实现分类或标签筛选
- [ ] 实现空状态
- [ ] 实现加载状态
- [ ] 实现文章不存在状态
- [ ] 终端 `blog` 命令能跳转博客列表
- [ ] 终端 `blog latest` 命令能打开最新文章

### 项目展示

- [ ] 创建 `/projects` 路由
- [ ] 定义项目字段：名称、简介、技术栈、亮点、GitHub 地址、在线预览地址
- [ ] 准备第一批静态项目数据或后端只读接口
- [ ] 实现项目列表页
- [ ] 实现项目详情或展开视图
- [ ] 实现项目链接缺失时的安全展示
- [ ] 终端 `projects` 命令能跳转项目页

### 公开后端接口

- [ ] 决定本阶段使用前端静态数据还是后端公开接口
- [ ] 如使用后端接口，创建文章列表接口 `GET /api/public/articles`
- [ ] 如使用后端接口，创建文章详情接口 `GET /api/public/articles/{slug}`
- [ ] 如使用后端接口，创建项目列表接口 `GET /api/public/projects`
- [ ] 如使用数据库，创建文章和项目相关迁移
- [ ] 为公开接口增加测试

### 前端构建与体验

- [x] `npm run build --prefix simon-workspace-web` 在 2026-06-20 验证通过
- [ ] 移动端导航折叠或简化
- [ ] 首页终端在移动端默认压缩或适配单列
- [ ] 所有公开页面有明确标题和返回路径
- [ ] 所有公开页面没有无效按钮或死链接
- [ ] 检查颜色、字号、间距是否与极简极客风一致

## 阶段验收 Todo

- [ ] 公开首页无需登录即可访问
- [ ] 终端 `help` 能输出命令
- [ ] 终端 `blog` 能跳转博客
- [ ] 终端 `workspace` 能跳转或打开登录入口
- [ ] 博客列表和详情能浏览
- [ ] 项目页能浏览
- [ ] 移动端 375px 宽度无明显布局错乱
- [ ] `npm run build --prefix simon-workspace-web` 通过
- [ ] `mvn -f simon-workspace-api/pom.xml test` 通过

## 进度记录

```text
2026-06-20:
- 已有首页、顶部导航、进入工作台按钮、静态终端展示
- 尚未实现真实终端命令、博客页面、项目页面和公开内容接口
```
