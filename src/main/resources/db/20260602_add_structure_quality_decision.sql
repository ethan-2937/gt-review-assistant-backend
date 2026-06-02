use `gt-review-assistant`;

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
