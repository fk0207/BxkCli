BXKCLI
一个用 Java 实现的智能编程 Agent CLI，类似 Claude Code，支持 ReAct 推理、Plan-and-Execute 规划执行、多 Agent 协作、MCP 协议、RAG 代码检索、Skill 技能系统等。

核心特性
三种 Agent 模式
模式	命令	适用场景
ReAct	默认	简单任务，思考→行动→观察循环
Plan-and-Execute	/plan	复杂多步任务，先拆解成 DAG 再按拓扑序执行，支持并行和自动重新规划
Multi-Agent	/team	超复杂任务，多角色 Agent（Leader/Worker/Reviewer）协作
工具系统
文件操作：read_file、write_file、list_dir、glob_files、grep_code
命令执行：execute_command（带安全策略，禁止 sudo、rm -rf 等危险命令）
代码检索：search_code（RAG 语义检索，基于向量+关键词混合搜索）
网页能力：web_search、web_fetch（静态页面用 Jsoup 提取，SPA 自动 fallback 到浏览器 MCP）
记忆管理：save_memory（长期记忆持久化，支持项目级和全局级）
快照回滚：revert_turn（Side-Git 快照，每轮对话前自动创建快照）
MCP 协议支持
支持 stdio 和 Streamable HTTP 两种传输方式
通过 ~/.bxkcli/mcp.json 配置外部 MCP 服务器
自动注册 MCP 工具到 Agent 工具列表
支持 MCP Resource（@mention 引用外部资源）
内置 Chrome DevTools MCP 配置
Skill 技能系统
三层目录加载：内置 → 用户级 → 项目级（后者覆盖前者同名）
System Prompt 中只注入技能索引（节省 token），LLM 按需调用 load_skill 加载完整指引
技能内容走 User Message 注入（保留 Prompt Cache）
LRU 缓冲，最多同时加载 3 个技能
记忆系统
对话历史（conversationHistory）：每轮发给 LLM 的完整协议消息
短期记忆（ConversationMemory）：简化记忆条目，用于事实提取和 token 预算监控
长期记忆（LongTermMemory）：持久化到磁盘，跨会话保留，按项目隔离
历史压缩：接近窗口上限时自动 Map-Reduce 压缩旧消息为摘要
Token 预算管理
ContextProfile：根据模型窗口大小自动派生所有预算参数
AgentBudget：三道保险阀（死循环检测 + token 预算 + 轮数兜底）
TokenBudget：单次请求的 token 分配（system + tools + history + response）
Prompt Cache 优化：固定内容放 system prompt 前部，动态内容走 user message
其他能力
HITL 人工审批：危险操作（write_file / execute_command）需人工确认
Side-Git 快照：每轮对话前自动创建快照，支持回滚
LSP 诊断注入：写文件后自动检查语法错误
微信通道：通过 iLink 协议接入微信，终端扫码登录
多 LLM 支持：GLM、DeepSeek、Kimi、Step、Agnes、FreeLLMAPI、讯飞
流式输出：SSE 流式渲染，支持 Markdown 终端渲染和代码高亮
操作审计：危险工具调用按天写 JSONL 审计日志
技术栈
类别	技术
语言	Java 17
构建	Maven（shade 打 fat jar）
HTTP	OkHttp 4.12
JSON	Jackson 2.16
终端	JLine 4.0 + Lanterna 3.1
数据库	SQLite（向量存储 + 后台任务队列）
代码解析	JavaParser 3.28（AST 分析）
Git	JGit 7.6（Side-Git 快照）
HTML 解析	Jsoup 1.18
中文分词	Jieba-analysis 1.0.2
二维码	ZXing 3.5.3（微信登录）
日志	Logback 1.5
快速开始
1. 环境要求
JDK 17+
Maven 3.6+
一个 LLM API Key（推荐 GLM）
2. 编译

Bash

cd bxkcli-main
mvn clean package -DskipTests
3. 配置

Bash

cp .env.example .env
编辑 .env，至少填写一个 API Key：


env

GLM_API_KEY=your_api_key_here
4. 启动

Bash

java -jar target/bxkcli-1.0-SNAPSHOT.jar
项目结构

Plain Text

bxkcli-main/
├── src/main/java/com/bxkcli/
│   ├── agent/          # Agent 核心（ReAct / Plan-Execute / Multi-Agent）
│   ├── browser/        # 浏览器连接与安全策略
│   ├── cli/            # 命令行入口与交互
│   ├── config/         # 配置管理
│   ├── context/        # Token 预算计算（ContextProfile）
│   ├── hitl/           # 人工审批（Human-In-The-Loop）
│   ├── image/          # 剪贴板图片处理
│   ├── llm/            # 多 LLM 客户端（GLM/DeepSeek/Kimi/Step/...）
│   ├── lsp/            # LSP 诊断注入
│   ├── mcp/            # MCP 协议（传输层/JSON-RPC/工具/资源）
│   ├── memory/         # 记忆系统（短期/长期/压缩/检索）
│   ├── plan/           # 计划与任务（DAG/拓扑排序）
│   ├── policy/         # 安全策略与审计
│   ├── prompt/         # System Prompt 组装
│   ├── rag/            # RAG 代码检索（分块/向量/混合搜索）
│   ├── render/         # 终端渲染（Inline/Plain/Lanterna）
│   ├── runtime/        # 后台任务与 Runtime API
│   ├── skill/          # Skill 技能系统
│   ├── snapshot/       # Side-Git 快照与回滚
│   ├── tool/           # 工具注册与执行
│   ├── tui/            # 全屏 TUI（Lanterna）
│   ├── util/           # 工具类（ANSI/Markdown/Jieba）
│   ├── web/            # 网页搜索与抓取
│   └── wechat/         # 微信通道
├── src/main/resources/
│   ├── prompts/        # System Prompt 模板（base/mode/approvals/...）
│   └── skills/         # 内置 Skill（web-access）
└── pom.xml
常用命令
命令	说明
/plan	切换到 Plan-and-Execute 模式
/team	切换到 Multi-Agent 模式
/model glm	切换 LLM（glm/deepseek/kimi/step/...）
/skill list	列出所有技能
/skill on/off <name>	启用/禁用技能
/index	构建 RAG 代码索引
/search <query>	语义搜索代码
/snapshot list	查看快照列表
/revert	回滚到指定快照
/browser connect	连接 Chrome 调试
/clear	清空对话历史
/help	查看所有命令
架构总览

Plain Text

用户输入
    │
    ▼
┌─────────────────────────────────────────┐
│            CLI 主循环 (Main.java)        │
│  命令解析 → 模式选择 → Agent 调度        │
└──────────────────┬──────────────────────┘
                   │
     ┌─────────────┼─────────────┐
     ▼             ▼             ▼
  ReAct Agent  PlanExecute   Multi-Agent
     │             │             │
     │         Planner           │
     │        (DAG 拓扑)     Leader/Worker/Reviewer
     │             │             │
     ▼             ▼             ▼
┌─────────────────────────────────────────┐
│              Agent.run()                │
│  ┌───────────────────────────────────┐  │
│  │ 1. 构建 System Prompt             │  │
│  │    (base + mode + memory + skill) │  │
│  │ 2. 调 LLM (流式 SSE)              │  │
│  │ 3. 解析工具调用                    │  │
│  │ 4. 并行执行工具                    │  │
│  │ 5. 压缩历史 (超预算时)             │  │
│  │ 6. 循环 1-5 直到完成               │  │
│  └───────────────────────────────────┘  │
└──────────────────┬──────────────────────┘
                   │
    ┌──────────────┼──────────────┐
    ▼              ▼              ▼
 内置工具        MCP 工具       RAG 检索
 (文件/命令)    (外部服务)     (向量搜索)
License
MIT
