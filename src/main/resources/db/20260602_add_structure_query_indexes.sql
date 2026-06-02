use `gt-review-assistant`;

drop procedure if exists add_index_if_missing;

delimiter //
create procedure add_index_if_missing(
  in p_table_name varchar(64),
  in p_index_name varchar(64),
  in p_ddl text
)
begin
  if not exists (
    select 1
    from information_schema.statistics
    where table_schema = database()
      and table_name = p_table_name
      and index_name = p_index_name
  ) then
    set @add_index_sql = p_ddl;
    prepare add_index_stmt from @add_index_sql;
    execute add_index_stmt;
    deallocate prepare add_index_stmt;
  end if;
end//
delimiter ;

call add_index_if_missing(
  'structure_table',
  'idx_table_project_note_side_order',
  'alter table structure_table add index idx_table_project_note_side_order (project_id, note_no, side, table_order, id)'
);

call add_index_if_missing(
  'structure_row',
  'idx_row_project_note_side_order',
  'alter table structure_row add index idx_row_project_note_side_order (project_id, note_no, side, table_title(120), row_order, id)'
);

call add_index_if_missing(
  'structure_row',
  'idx_row_project_note_key',
  'alter table structure_row add index idx_row_project_note_key (project_id, note_no, table_title(120), row_key(160))'
);

call add_index_if_missing(
  'structure_column',
  'idx_col_project_note_side_order',
  'alter table structure_column add index idx_col_project_note_side_order (project_id, note_no, side, table_title(120), column_order, id)'
);

call add_index_if_missing(
  'structure_column',
  'idx_col_project_note_key',
  'alter table structure_column add index idx_col_project_note_key (project_id, note_no, table_title(120), column_key(160))'
);

call add_index_if_missing(
  'structure_cell',
  'idx_cell_project_note_side_axis',
  'alter table structure_cell add index idx_cell_project_note_side_axis (project_id, note_no, side, table_title(120), row_key(120), column_key(120), id)'
);

call add_index_if_missing(
  'structure_cell',
  'idx_cell_project_note_axis',
  'alter table structure_cell add index idx_cell_project_note_axis (project_id, note_no, table_title(120), row_key(120), column_key(120))'
);

call add_index_if_missing(
  'structure_diff',
  'idx_diff_project_note_level',
  'alter table structure_diff add index idx_diff_project_note_level (project_id, note_no, diff_level, id)'
);

drop procedure if exists add_index_if_missing;
