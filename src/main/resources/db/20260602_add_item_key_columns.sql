use `gt-review-assistant`;

drop procedure if exists add_column_if_missing;
drop procedure if exists add_index_if_missing;

delimiter //
create procedure add_column_if_missing(
  in p_table_name varchar(64),
  in p_column_name varchar(64),
  in p_ddl text
)
begin
  if not exists (
    select 1
    from information_schema.columns
    where table_schema = database()
      and table_name = p_table_name
      and column_name = p_column_name
  ) then
    set @ddl_sql = p_ddl;
    prepare ddl_stmt from @ddl_sql;
    execute ddl_stmt;
    deallocate prepare ddl_stmt;
  end if;
end//

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
    set @ddl_sql = p_ddl;
    prepare ddl_stmt from @ddl_sql;
    execute ddl_stmt;
    deallocate prepare ddl_stmt;
  end if;
end//
delimiter ;

call add_column_if_missing(
  'structure_row',
  'item_key',
  'alter table structure_row add column item_key varchar(2000) null after row_leaf'
);

call add_column_if_missing(
  'structure_column',
  'item_key',
  'alter table structure_column add column item_key varchar(2000) null after column_leaf'
);

call add_column_if_missing(
  'structure_cell',
  'item_key',
  'alter table structure_cell add column item_key varchar(2000) null after column_path'
);

update structure_row
set item_key = left(concat(coalesce(table_title, 'default table'), '|', coalesce(row_key, coalesce(row_path, row_leaf, ''))), 2000)
where item_key is null or item_key = '';

update structure_column
set item_key = left(concat(coalesce(table_title, 'default table'), '|', coalesce(column_key, coalesce(column_path, column_leaf, ''))), 2000)
where item_key is null or item_key = '';

update structure_cell
set item_key = left(concat(coalesce(table_title, 'default table'), '|', coalesce(row_key, coalesce(row_path, '')), '|', coalesce(column_key, coalesce(column_path, ''))), 2000)
where item_key is null or item_key = '';

call add_index_if_missing(
  'structure_row',
  'idx_row_project_note_item_key',
  'alter table structure_row add index idx_row_project_note_item_key (project_id, note_no, item_key(500))'
);

call add_index_if_missing(
  'structure_column',
  'idx_col_project_note_item_key',
  'alter table structure_column add index idx_col_project_note_item_key (project_id, note_no, item_key(500))'
);

call add_index_if_missing(
  'structure_cell',
  'idx_cell_project_note_item_key',
  'alter table structure_cell add index idx_cell_project_note_item_key (project_id, note_no, item_key(500))'
);

drop procedure if exists add_column_if_missing;
drop procedure if exists add_index_if_missing;
