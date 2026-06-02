# GT Review Assistant 开发与优化记录

更新时间：2026-06-02

## 1. 项目背景

这个项目是为了辅助 GT 审核、PDF vs Excel 结构核对、以及后续 audit-engine runtime 结构识别分析。

核心目标不是做复杂业务系统，而是给审核人员和非技术同学提供一个清晰的网址界面：

- 能看到原始 Excel / PDF 被拆解出的结构。
- 能按附注、行、列、单元格粒度查看。
- 后续能接入 audit-engine 运行期间识别出的结构。
- 能明确看到哪些原文件结构没有被 runtime 识别到。
- 降低人工核对 GT 的成本，减少漏项、重复项和误判。

当前阶段重点是先完成 202506 的 PDF vs Excel 结构展示和分页核对 MVP。

## 2. 技术栈

用户已确认的技术栈：

- 后端：Spring Boot 3
- ORM：MyBatis，不使用 MyBatis-Plus
- 数据库：MySQL
- 分页：PageHelper
- 前端：Vue 3 + Vite + Element Plus
- Worker：Python
- 文件解析：openpyxl 解析 Excel，PyMuPDF 解析 PDF

当前项目目录：

```text
D:\audit-engine\gt-review-assistant
├─ java-backened\gt-review-assistant    # Spring Boot 后端，目录名沿用当前拼写
├─ vue-fronted                           # Vue 前端，目录名沿用当前拼写
├─ worker                                # Python 解析 worker
└─ workspace                             # 本地生成结构 JSON、临时数据
```

注意：`java-backened`、`vue-fronted` 目录名有拼写问题，但目前不影响运行，暂时没有重命名，避免影响已有配置。

## 3. 当前系统能力

### 3.1 后端能力

后端已经实现：

- 项目创建与查询。
- 导入 PDF / Excel 结构 JSON。
- 存储结构层级：
  - 附注：`structure_note`
  - 表 / 小节：`structure_table`
  - 行：`structure_row`
  - 列：`structure_column`
  - 单元格：`structure_cell`
  - PDF vs Excel 结构差异：`structure_diff`
- 重新生成 PDF vs Excel 结构差异。
- 按附注查询结构详情。
- 按附注、粒度分页查询结构覆盖清单。

主要接口：

```text
GET  /api/projects
POST /api/projects
GET  /api/projects/{projectId}

POST /api/projects/{projectId}/structures/import
POST /api/projects/{projectId}/structures/compare
GET  /api/projects/{projectId}/structures/overview
GET  /api/projects/{projectId}/structures/notes
GET  /api/projects/{projectId}/structures/notes/{noteNo}
GET  /api/projects/{projectId}/structures/notes/{noteNo}/coverage
GET  /api/projects/{projectId}/structures/diffs
```

分页接口参数：

```text
level=cell|row|column
keyword=
pageNum=1
pageSize=20
```

### 3.2 前端能力

前端已经实现：

- 项目首页。
- 结构台账页面。
- 顶部附注目录横向导航。
- 附注搜索。
- 按附注展示结构覆盖清单。
- 按单元格 / 行 / 列名切换粒度。
- 按关键词搜索行名、列名、值、表名。
- PageHelper 分页展示。
- 上半部分展示“原文件识别出的结构”。
- 下半部分展示“audit-engine 运行过程中识别出的结构”。
- runtime 尚未接入前，统一展示 `runtime 未识别到`。
- 辅助信息可展开查看 PDF / Excel / runtime 三栏结构树。
- 辅助信息可展开查看当前附注的 PDF vs Excel 差异。

### 3.3 Worker 能力

Python worker 已经实现：

- 从 Excel 自动拆结构 JSON。
- 从 PDF 自动拆结构 JSON。
- 可以把 JSON 写入本地目录。
- 可以调用后端接口导入结构。
- 可以触发后端 compare 生成差异。

示例命令：

```powershell
cd D:\audit-engine\gt-review-assistant\worker

python build_structure_json.py both `
  --excel-input "D:\data-annotation\2025.06\2025上半年-完整版-标黄-仅2025-复核-20260514-142853\20250630-A股主表及附注-标黄.xlsx" `
  --pdf-input "D:\data-annotation\2025.06\2025上半年-完整版-标黄-仅2025-复核-20260514-142853\20250630-中信证券报告A股-标黄.pdf" `
  --output-dir "D:\audit-engine\gt-review-assistant\workspace\202506_structure_json" `
  --include-text-lines `
  --backend "http://localhost:18081" `
  --project-id 1 `
  --compare
```

## 4. 已完成的关键开发

### 4.1 后端初版

完成了 Spring Boot + MyBatis + MySQL 基础结构：

- Controller
- Service
- Mapper
- Entity
- DTO
- MyBatis XML
- MySQL schema
- CORS 配置
- 全局异常处理

### 4.2 结构导入

支持导入两类来源：

```text
PDF
EXCEL
```

每次导入某一侧结构时，会先删除该项目该侧旧结构，再写入新结构。

当前结构存储粒度：

```text
项目 -> 来源侧 -> 附注 -> 表/小节 -> 行 -> 列 -> 单元格
```

### 4.3 PDF vs Excel 结构差异

后端支持生成初步结构差异，当前主要用于辅助展示和后续分析。

这不是最终 GT 决策逻辑，而是帮助人工发现：

- PDF 有，Excel 没有。
- Excel 有，PDF 没有。
- 行、列、单元格维度存在结构不一致。

### 4.4 横向结构覆盖清单

新增了核心展示功能：

```text
上半部分：原文件识别出的结构
下半部分：runtime 识别出的结构
```

当前 runtime 数据尚未真实接入，所以统一显示：

```text
runtime 未识别到
```

后续接入 audit-engine runtime 结构后，这里会变成真正的“原文件结构 vs 运行识别结构”。

## 5. 页面布局优化

最初左侧附注目录占用空间较大，下方展示区域不够宽。

已优化为：

- 附注目录移动到页面上方。
- 附注以横向卡片形式展示。
- 页面主体区域改成单列大宽度。
- 顶部标题缩小，减少首屏占用。
- 项目最大展示宽度从 `1500px` 提升到 `1760px`。
- 辅助信息默认收起，避免页面过长。

优化后效果：

- 下方结构展示区域更宽。
- 非技术用户更容易先选附注，再看明细。
- 页面视觉层级更清晰。

## 6. 后端性能优化

### 6.1 加数据库索引

发现分页接口慢的原因之一是：

旧索引多为：

```sql
(project_id, side, note_no)
```

但分页查询常用条件是：

```sql
where project_id = ?
  and note_no = ?
```

没有先限定 `side` 时，旧索引不能很好服务当前分页查询。

已新增适合当前查询模式的索引：

```text
idx_table_project_note_side_order
idx_row_project_note_side_order
idx_row_project_note_key
idx_col_project_note_side_order
idx_col_project_note_key
idx_cell_project_note_side_axis
idx_cell_project_note_axis
idx_diff_project_note_level
```

对应脚本：

```text
src/main/resources/db/20260602_add_structure_query_indexes.sql
```

### 6.2 materialized item_key

分页查询原来每次动态拼接：

```sql
concat(table_title, '|', row_key)
concat(table_title, '|', column_key)
concat(table_title, '|', row_key, '|', column_key)
```

同时还要：

```sql
group by concat(...)
order by item_key
```

这会导致 MySQL 每次分页都做临时计算、分组、排序。

已改为提前在数据库中存储 `item_key` 字段：

- `structure_row.item_key`
- `structure_column.item_key`
- `structure_cell.item_key`

新导入数据时直接写入 `item_key`。

旧数据已通过迁移脚本回填。

对应脚本：

```text
src/main/resources/db/20260602_add_item_key_columns.sql
```

本地已执行回填：

```text
structure_row    14207 行
structure_column 640 行
structure_cell   18021 行
```

直接测试数据库查询后，`附注36 / 单元格 / 第1页` 的 count + page 查询从秒级降到毫秒级。

## 7. 前端性能优化

页面切页仍有卡顿后，继续优化了前端渲染。

### 7.1 分页组件轻量化

文件：

```text
src/components/RuntimeCoverageLedger.vue
```

优化内容：

- 分页数据从 `reactive` 改为 `shallowRef`，避免 Vue 对每一条结构数据做深层响应式代理。
- `pageList` 使用 `Object.freeze`，减少无意义响应式追踪。
- 每行加 `v-memo`，减少重复渲染。
- 行内 `el-tag` 改为普通 `span.runtime-status-pill`，降低 Element Plus 组件渲染成本。
- 去掉整块 `v-loading` 遮罩，改成轻量加载提示。

### 7.2 前端页缓存

新增前端缓存：

```text
projectId + noteNo + level + keyword + pageNum + pageSize
```

组成缓存 key。

效果：

- 已看过的页再次切回，不再请求后端。
- 当前页加载后，会自动预加载下一页。
- 用户连续翻页时体感更流畅。

### 7.3 浏览器渲染优化

CSS 加了：

```css
content-visibility: auto;
contain-intrinsic-size: ...;
```

用于减少长列表中不可见区域的渲染压力。

## 8. 当前运行方式

### 8.1 启动后端

```powershell
cd D:\audit-engine\gt-review-assistant\java-backened\gt-review-assistant
mvn spring-boot:run
```

默认端口：

```text
http://localhost:18081
```

### 8.2 启动前端

```powershell
cd D:\audit-engine\gt-review-assistant\vue-fronted
npm install
npm run dev
```

默认地址：

```text
http://localhost:5173
```

### 8.3 页面入口

```text
http://localhost:5173/projects/1
```

## 9. 已验证内容

后端：

```powershell
mvn test
```

已通过。

前端：

```powershell
npm run build
```

已通过。

浏览器检查：

- 页面可打开。
- 附注目录在上方。
- 分页列表正常显示 20 条。
- runtime 状态标签正常显示。
- 切页正常。
- 已访问页面再次切回速度明显提升。

## 10. 当前限制

当前仍是 MVP，不是终极版本。

主要限制：

- runtime 结构还没有真实接入。
- PDF 解析目前使用文本行兜底，可能有噪声。
- 当前 `coverage` 查询仍然基于原始结构表做 group by，虽然已经用 `item_key` 优化，但还不是最终最优方案。
- 目前还没有用户权限、登录、审计日志，这一阶段暂时不需要。
- 当前没有 Redis，暂时也不建议加，避免部署复杂化。

## 11. 下一步建议

### 11.1 接入 audit-engine runtime 结构

目标：

```text
原文件结构 vs audit-engine 运行识别结构
```

需要从 audit-engine 运行结果中抽取：

- 附注
- 表 / 小节
- 行
- 列
- 单元格
- 坐标 / locator
- 识别来源 / evidence

然后导入本系统 runtime 结构表。

### 11.2 新增 runtime 表

建议后续新增：

```text
runtime_structure_note
runtime_structure_table
runtime_structure_row
runtime_structure_column
runtime_structure_cell
```

或者先用统一表：

```text
runtime_structure_item
```

字段包括：

```text
project_id
run_id
note_no
level
item_key
table_title
row_key
column_key
value_text
source_locator
recognized_status
```

### 11.3 预生成 coverage 汇总表

如果后续数据量变大，建议新增：

```text
structure_coverage_item
```

导入或 compare 后预生成覆盖清单。

前端分页时直接查该表，避免实时 group by。

这是比 Redis 更适合当前系统的性能优化方向。

### 11.4 审核工作台化

后续可以增加：

- 一键标记：确认缺失 / 误报 / 需要复核。
- 决策日志。
- 导出 CSV / Excel。
- 证据截图或源文件定位。
- GT candidate 生成。
- 最终 GT freeze manifest。

## 12. Git 状态说明

当前项目根目录不是一个 Git 仓库。

下面三个目录是独立 Git 仓库：

```text
D:\audit-engine\gt-review-assistant\java-backened\gt-review-assistant
D:\audit-engine\gt-review-assistant\vue-fronted
D:\audit-engine\gt-review-assistant\worker
```

当前本机没有安装 GitHub CLI：

```text
gh: command not found
```

当前 backend / frontend / worker 仓库也没有配置远端：

```powershell
git remote -v
```

没有输出。

因此，暂时不能直接 push 到 GitHub。

如果要推送，需要先在 GitHub 上创建对应仓库，然后执行：

```powershell
git remote add origin https://github.com/<your-user>/<repo-name>.git
git push -u origin master
```

或者如果你希望做成一个总仓库，可以把 `gt-review-assistant` 根目录初始化成一个 monorepo，再统一提交 backend / frontend / worker。

## 13. 当前推荐的 GitHub 方案

建议用一个总仓库，而不是三个分散仓库：

```text
gt-review-assistant
├─ backend
├─ frontend
├─ worker
└─ docs
```

但因为当前目录已经各自初始化为独立仓库，短期可以先分别推送：

```text
gt-review-assistant-backend
gt-review-assistant-frontend
gt-review-assistant-worker
```

长期再决定是否整理成 monorepo。
## 14. Runtime sample 接入 MVP（2026-06-02）

新增了 audit-engine 运行结果接入的第一版闭环。

当前口径：

```text
只读取 audit-engine 运行结果文件，不改 audit-engine 主流程。
worker 读取 runtime_eval.json / structure_qa.json。
worker 调后端接口导入 runtime_run / runtime_structure_item。
前端按 runId 展示“样例中找到了 / 样例中没找到”。
```

注意：当前接入的是 `runtime_eval.json` 中的：

```text
cases[*].debug_counts.direct_doc_materialization.clean_runtime_evidence.source_fact_samples
cases[*].debug_counts.direct_doc_materialization.clean_runtime_evidence.target_fact_samples
```

这是 sample-only 数据，不代表完整 runtime 识别率。因此后端 `runtime_run.artifact_completeness` 标记为：

```text
SAMPLE_ONLY
```

后端新增：

```text
runtime_run
runtime_structure_item
POST /api/projects/{projectId}/runtime-runs/import
GET  /api/projects/{projectId}/runtime-runs
GET  /api/projects/{projectId}/structures/notes/{noteNo}/coverage?runId=...
```

worker 新增：

```text
import_audit_engine_runtime.py
```

示例：

```powershell
python import_audit_engine_runtime.py `
  --run-root "D:\runtime\case23-resource-guard-textcache-smoke-20260512" `
  --output-dir "D:\audit-engine\gt-review-assistant\workspace\runtime_import_case23" `
  --backend "http://localhost:18081" `
  --project-id 1
```

前端新增：

- runtime run 下拉选择。
- 根据 run 的完整性显示 sample 提示。
- 分页接口传入 `runId` 和 `matchStatus`。
- 支持筛选：全部、已匹配、未匹配。
- sample-only run 显示“样例中找到了 / 样例中没找到”，避免误解为完整识别率。
