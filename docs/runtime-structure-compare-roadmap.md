# Runtime Structure Compare Roadmap

目标：把原始文件结构和 audit-engine 准召运行过程中的结构识别结果放在同一个界面里横向对比，找出运行过程中没有识别出来的附注、表、行、列和单元格。

## 当前版本

当前后端已经有 `structure_note/table/row/column/cell/diff`：

- `PDF`：从 PDF 原文或解析结果拆出来的原文件结构。
- `EXCEL`：从 Excel 原文拆出来的原文件结构。
- `structure_diff`：当前只做 PDF vs Excel 的结构差异。

## 终极版本要增加的第三侧

新增 side：

```text
RUNTIME
```

含义：audit-engine 运行准召时实际识别出来的结构，不是原始文件结构。

一条 runtime 单元应该能记录：

```text
run_root
case_id / note_no / note_name
table_path / section_path
row_path / row_leaf
column_path / column_leaf
value_text / normalized_value
source_locator / target_locator
runtime_stage
raw_evidence_id
```

## 建议新增表

### runtime_run

记录一次准召运行。

```sql
create table runtime_run (
  id bigint primary key auto_increment,
  project_id bigint not null,
  run_name varchar(200) not null,
  run_root varchar(1000),
  dataset_key varchar(200),
  version_label varchar(200),
  created_at datetime not null
);
```

### runtime_structure_cell

记录 audit-engine 运行过程中识别出的结构单元。

```sql
create table runtime_structure_cell (
  id bigint primary key auto_increment,
  project_id bigint not null,
  runtime_run_id bigint not null,
  side varchar(20),
  note_no varchar(50),
  note_name varchar(300),
  table_title varchar(500),
  row_key varchar(1000),
  column_key varchar(1000),
  row_path text,
  column_path text,
  value_text varchar(1000),
  normalized_value varchar(1000),
  source_locator varchar(500),
  raw_evidence_id varchar(300),
  runtime_stage varchar(100),
  created_at datetime not null
);
```

### runtime_structure_gap

记录“原文件有，但运行过程中没识别出来”的结构缺口。

```sql
create table runtime_structure_gap (
  id bigint primary key auto_increment,
  project_id bigint not null,
  runtime_run_id bigint not null,
  note_no varchar(50),
  note_name varchar(300),
  gap_level varchar(50),
  gap_type varchar(50),
  original_ref_type varchar(50),
  original_ref_id bigint,
  runtime_ref_id bigint,
  plain_summary text,
  impact_summary text,
  status varchar(50) not null,
  created_at datetime not null
);
```

## 对比口径

第一阶段不要直接判 GT，只做结构覆盖：

```text
原文件结构全集 - runtime 已识别结构 = 运行未识别结构
```

按粒度分层：

- 附注是否识别
- 表/小节是否识别
- 行是否识别
- 列名是否识别
- 单元格是否识别

## 前端展示方向

横向三列：

```text
PDF 原文件结构 | Excel 原文件结构 | audit-engine 运行识别结构
```

然后单独一块显示：

```text
运行过程中没识别出来的结构
```

给非技术同学看的说明必须是大白话：

```text
原文件里有“租赁负债 / 一年以内 / 2025-06-30”这个单元格，
但 audit-engine 运行结果里没有找到同样的行列定位。
```

## 下一步落地顺序

1. Worker 继续稳定拆 PDF/Excel 原文件结构。
2. 新增 runtime 导入 worker，从 audit-engine run root 提取运行识别结构。
3. 后端新增 runtime run / runtime cell / runtime gap 表。
4. 后端增加 original vs runtime 对比接口。
5. 前端第三列从占位改成真实 runtime 结构。
6. 差异卡片从 PDF vs Excel 扩展为 original vs runtime。
