use `gt-review-assistant`;

create table if not exists structure_gt_decision (
  id bigint primary key auto_increment,
  project_id bigint not null,
  decision_key varchar(64) not null,
  view_mode varchar(50) not null,
  run_id bigint,
  note_no varchar(50),
  level varchar(50),
  item_key varchar(2000),
  runtime_side varchar(20),
  decision varchar(80) not null,
  alias_text varchar(500),
  merge_target_key varchar(500),
  comment text,
  reviewer varchar(100),
  reviewed_at datetime not null,
  created_at datetime not null,
  updated_at datetime not null,
  unique key uk_structure_gt_decision_key (project_id, decision_key),
  key idx_structure_gt_decision_project (project_id, decision, updated_at),
  key idx_structure_gt_decision_note (project_id, note_no, level, decision)
) engine=InnoDB default charset=utf8mb4;
