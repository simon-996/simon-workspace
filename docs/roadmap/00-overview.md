# Simon Workspace 阶段路线图总览

最后更新：2026-07-04

## 文档目的

`docs/roadmap` 是后续开发的阶段 Todo。每个阶段文件只维护该阶段要做什么、已经完成什么、还剩什么。完成一项后，把 `- [ ]` 改成 `- [x]`，并在对应阶段的进度记录里写明日期和结果。

设计风格基准单独维护在 [docs/design-style.md](../design-style.md)。

## 项目定位

Simon Workspace 是一个“个人主页 + 教学工作台”项目：

- 访客访问时，是简洁的个人主页。
- 授权账号登录后，可以进入课程、班级、学期、模板、文件、生成记录和权限管理工作台。
- 后续继续补齐教案/教学日历生成、AI 生成和部署闭环。

## 仓库结构

```text
simon-workspace/
├── simon-workspace-api/      Spring Boot API
├── simon-workspace-web/      Vue 3 + Vite frontend
├── deploy/                   Docker / Nginx / deployment config
├── docs/                     Roadmap, design style, plans
├── Jenkinsfile
├── README.md
└── simon-workspace-doc.md    Product and architecture summary
```

## 阶段划分

| 阶段 | 名称 | 当前判断 |
| --- | --- | --- |
| 01 | Foundation | 基础已可用，异常处理、Redis 验证、测试覆盖仍需补强 |
| 02 | Public Site | 首页已重构，博客和项目公开页未完成 |
| 03 | Workspace Core | 工作台核心页面和权限基础已完成，测试覆盖需补 |
| 04 | Document Generation | 后端 preview 基础已有，导出闭环和前端向导未完成 |
| 05 | AI Integration | 未开始 |
| 06 | Polish and Deploy | 视觉统一已推进，部署闭环和运维文档未完成 |

## 当前已完成的主要代码能力

- 首页个人主页风格、语言切换、站点配置加载骨架屏、滚动动画。
- 登录页、工作台路由守卫、Token 登录、当前用户恢复、权限导航。
- 工作台外壳：桌面侧栏、移动端底部导航、语言切换、退出登录。
- 工作台页面：总览、课程、班级、学期、模板、文件、生成记录、权限、站点配置。
- 后端接口：认证、站点配置、课程、班级、学期/周历、模板/字段、文件、生成任务、权限角色。
- 全局视觉系统：浅色个人站风格、Naive UI 主题覆盖、指标条/表格/工具条/弹窗/骨架屏统一样式。

## 当前最重要的缺口

- 公开博客和项目展示还没有独立页面与数据模型。
- 教案生成和教学日历生成还没有完整前端向导。
- Word / Excel 最终导出闭环还没有完成。
- AI 服务、Prompt、JSON 校验和 AI 生成流程还没有开始。
- 后端业务测试覆盖不足。
- 服务器部署、日志、备份和回滚文档还需要补齐。

## 通用验收标准

每个阶段收尾前至少检查：

- `npm run build --prefix simon-workspace-web`
- `mvn -f simon-workspace-api/pom.xml test`
- 相关页面在桌面和手机宽度不重叠、不溢出
- 权限边界清晰，访客不能访问工作台功能
- Todo 和进度记录已同步
- 有用修改已按功能提交

## 推荐下一步

优先级建议：

1. 补齐 04 阶段：教案/教学日历生成向导和导出闭环。
2. 补齐 02 阶段：博客列表、博客详情、项目展示。
3. 补齐 03 阶段：后端业务测试和权限边界测试。
4. 再进入 05 阶段：AI 生成。
