create database if not exists `gt-review-assistant`
  default character set utf8mb4
  collate utf8mb4_unicode_ci;

use `gt-review-assistant`;

create table if not exists gt_project (
  id bigint primary key auto_increment,
  project_key varchar(100) not null unique,
  name varchar(200) not null,
  year_label varchar(50),
  dataset_key varchar(200),
  version_label varchar(200),
  status varchar(50) not null,
  created_by varchar(100),
  created_at datetime not null,
  updated_at datetime not null
) engine=InnoDB default charset=utf8mb4;

create table if not exists structure_note (
  id bigint primary key auto_increment,
  project_id bigint not null,
  side varchar(20) not null,
  note_no varchar(50) not null,
  note_name varchar(300),
  source_file_path varchar(1000),
  source_locator varchar(500),
  created_at datetime not null,
  updated_at datetime not null,
  unique key uk_note_side (project_id, side, note_no),
  key idx_note_project (project_id, note_no)
) engine=InnoDB default charset=utf8mb4;

create table if not exists structure_table (
  id bigint primary key auto_increment,
  note_id bigint not null,
  project_id bigint not null,
  side varchar(20) not null,
  note_no varchar(50) not null,
  table_title varchar(500),
  table_order int,
  source_locator varchar(500),
  created_at datetime not null,
  updated_at datetime not null,
  key idx_table_note (project_id, side, note_no),
  key idx_table_note_id (note_id),
  key idx_table_project_note_side_order (project_id, note_no, side, table_order, id)
) engine=InnoDB default charset=utf8mb4;

create table if not exists structure_row (
  id bigint primary key auto_increment,
  table_id bigint not null,
  project_id bigint not null,
  side varchar(20) not null,
  note_no varchar(50) not null,
  table_title varchar(500),
  row_key varchar(1000),
  row_path text,
  row_leaf varchar(500),
  item_key varchar(2000),
  row_order int,
  source_locator varchar(500),
  created_at datetime not null,
  updated_at datetime not null,
  key idx_row_table (table_id),
  key idx_row_note (project_id, side, note_no),
  key idx_row_key (project_id, side, note_no, row_key(200)),
  key idx_row_project_note_side_order (project_id, note_no, side, table_title(120), row_order, id),
  key idx_row_project_note_key (project_id, note_no, table_title(120), row_key(160)),
  key idx_row_project_note_item_key (project_id, note_no, item_key(500))
) engine=InnoDB default charset=utf8mb4;

create table if not exists structure_column (
  id bigint primary key auto_increment,
  table_id bigint not null,
  project_id bigint not null,
  side varchar(20) not null,
  note_no varchar(50) not null,
  table_title varchar(500),
  column_key varchar(1000),
  column_path text,
  column_leaf varchar(500),
  item_key varchar(2000),
  column_order int,
  source_locator varchar(500),
  created_at datetime not null,
  updated_at datetime not null,
  key idx_col_table (table_id),
  key idx_col_note (project_id, side, note_no),
  key idx_col_key (project_id, side, note_no, column_key(200)),
  key idx_col_project_note_side_order (project_id, note_no, side, table_title(120), column_order, id),
  key idx_col_project_note_key (project_id, note_no, table_title(120), column_key(160)),
  key idx_col_project_note_item_key (project_id, note_no, item_key(500))
) engine=InnoDB default charset=utf8mb4;

create table if not exists structure_cell (
  id bigint primary key auto_increment,
  table_id bigint not null,
  row_id bigint,
  column_id bigint,
  project_id bigint not null,
  side varchar(20) not null,
  note_no varchar(50) not null,
  table_title varchar(500),
  row_key varchar(1000),
  column_key varchar(1000),
  row_path text,
  column_path text,
  item_key varchar(2000),
  value_text varchar(1000),
  normalized_value varchar(1000),
  source_locator varchar(500),
  screenshot_path varchar(1000),
  created_at datetime not null,
  updated_at datetime not null,
  key idx_cell_table (table_id),
  key idx_cell_note (project_id, side, note_no),
  key idx_cell_axis (project_id, side, note_no, row_key(160), column_key(160)),
  key idx_cell_project_note_side_axis (project_id, note_no, side, table_title(120), row_key(120), column_key(120), id),
  key idx_cell_project_note_axis (project_id, note_no, table_title(120), row_key(120), column_key(120)),
  key idx_cell_project_note_item_key (project_id, note_no, item_key(500))
) engine=InnoDB default charset=utf8mb4;

create table if not exists structure_diff (
  id bigint primary key auto_increment,
  project_id bigint not null,
  note_no varchar(50),
  note_name varchar(300),
  diff_level varchar(50) not null,
  diff_type varchar(50) not null,
  pdf_ref_type varchar(50),
  pdf_ref_id bigint,
  excel_ref_type varchar(50),
  excel_ref_id bigint,
  pdf_text text,
  excel_text text,
  plain_summary text,
  impact_summary text,
  status varchar(50) not null,
  created_at datetime not null,
  key idx_diff_project (project_id),
  key idx_diff_note (project_id, note_no),
  key idx_diff_level (project_id, diff_level),
  key idx_diff_type (project_id, diff_type),
  key idx_diff_project_note_level (project_id, note_no, diff_level, id)
) engine=InnoDB default charset=utf8mb4;

create table if not exists runtime_run (
  id bigint primary key auto_increment,
  project_id bigint not null,
  run_key varchar(300) not null,
  run_root varchar(1000),
  run_type varchar(50),
  run_status varchar(50),
  dataset_key varchar(200),
  version_label varchar(300),
  case_count int,
  artifact_completeness varchar(50),
  confidence_level varchar(50),
  source_host varchar(200),
  source_sample_count int,
  target_sample_count int,
  source_structured_cell_count int,
  target_structured_cell_count int,
  table_count int,
  created_at datetime not null,
  updated_at datetime not null,
  unique key uk_runtime_run_project_key (project_id, run_key),
  key idx_runtime_run_project (project_id, updated_at, id)
) engine=InnoDB default charset=utf8mb4;

create table if not exists runtime_structure_item (
  id bigint primary key auto_increment,
  project_id bigint not null,
  run_id bigint not null,
  case_id varchar(50),
  note_no varchar(50),
  note_name varchar(300),
  level varchar(50) not null,
  runtime_side varchar(20),
  item_key varchar(2000),
  table_title varchar(500),
  table_profile_id varchar(200),
  row_key varchar(1000),
  row_label varchar(500),
  row_path text,
  column_key varchar(1000),
  column_label varchar(500),
  column_path text,
  value_text varchar(1000),
  normalized_value varchar(1000),
  value_signature varchar(1000),
  value_type varchar(100),
  source_locator varchar(500),
  cell_coordinate varchar(200),
  quote_text text,
  source_artifact varchar(500),
  source_json_path varchar(1000),
  locator_method varchar(200),
  confidence_level varchar(50),
  raw_payload_json longtext,
  created_at datetime not null,
  key idx_runtime_item_run (run_id),
  key idx_runtime_item_match (project_id, run_id, note_no, level, item_key(500)),
  key idx_runtime_item_side (project_id, run_id, note_no, level, runtime_side)
) engine=InnoDB default charset=utf8mb4;

create table if not exists structure_quality_decision (
  id bigint primary key auto_increment,
  project_id bigint not null,
  issue_key varchar(300) not null,
  issue_type varchar(80),
  severity varchar(30),
  side varchar(20),
  note_no varchar(50),
  table_id bigint,
  table_title varchar(500),
  level varchar(50),
  ref_id bigint,
  decision varchar(80) not null,
  comment text,
  reviewer varchar(100),
  reviewed_at datetime not null,
  created_at datetime not null,
  updated_at datetime not null,
  unique key uk_quality_decision_issue (project_id, issue_key),
  key idx_quality_decision_project (project_id, decision, updated_at),
  key idx_quality_decision_note (project_id, note_no, side)
) engine=InnoDB default charset=utf8mb4;

