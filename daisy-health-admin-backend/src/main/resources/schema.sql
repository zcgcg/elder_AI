create table if not exists `user` (
  id bigint primary key auto_increment,
  nickname varchar(50) not null,
  real_name varchar(50) not null,
  gender tinyint default 0,
  birthday date null,
  phone varchar(20) not null unique,
  id_card varchar(18) null,
  address varchar(200) null,
  bio varchar(500) null,
  height decimal(5,1) null,
  weight decimal(5,1) null,
  ethnicity varchar(20) null,
  education varchar(20) null,
  blood_type varchar(5) null,
  rh_negative tinyint default 0,
  chronic_disease varchar(200) null,
  sleep_quality varchar(10) null,
  smoking_freq varchar(20) null,
  drinking_freq varchar(20) null,
  exercise_freq varchar(20) null,
  diet_preference varchar(20) null,
  avatar_url varchar(255) null,
  created_at datetime not null default current_timestamp,
  last_login_time datetime null,
  last_buy_time datetime null,
  status tinyint not null default 1,
  updated_at datetime not null default current_timestamp on update current_timestamp
) default charset = utf8mb4;

create table if not exists health_data (
  id bigint primary key auto_increment,
  user_id bigint not null,
  data_type varchar(20) not null,
  value varchar(50) not null,
  unit varchar(10) null,
  record_date date not null,
  record_time time null,
  source varchar(20) null,
  device_id bigint null,
  created_at datetime not null default current_timestamp,
  index idx_health_user_type_date (user_id, data_type, record_date)
) default charset = utf8mb4;

create table if not exists medication_record (
  id bigint primary key auto_increment,
  user_id bigint not null,
  period varchar(10) not null,
  drug_name varchar(100) not null,
  frequency varchar(20) not null,
  take_time time null,
  dosage varchar(20) null,
  reminder_enabled tinyint not null default 1,
  source varchar(20) null,
  creator varchar(50) null,
  created_at datetime not null default current_timestamp,
  index idx_medication_user (user_id)
) default charset = utf8mb4;

create table if not exists user_tag (
  id bigint primary key auto_increment,
  name varchar(50) not null unique,
  type varchar(20) not null,
  color varchar(20) null,
  user_count int not null default 0,
  status tinyint not null default 1,
  updater varchar(50) null,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists user_tag_rel (
  id bigint primary key auto_increment,
  user_id bigint not null,
  tag_id bigint not null,
  unique key uk_user_tag (user_id, tag_id)
) default charset = utf8mb4;

create table if not exists service_personnel (
  id bigint primary key auto_increment,
  name varchar(50) not null,
  phone varchar(20) not null unique,
  service_type varchar(20) not null,
  area varchar(100) null,
  join_time datetime null,
  status tinyint not null default 1,
  audit_status varchar(20) not null default '待审核',
  avatar_url varchar(255) null,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists product (
  id bigint primary key auto_increment,
  name varchar(100) not null,
  code varchar(30) not null unique,
  category varchar(30) not null,
  price decimal(10,2) not null,
  status tinyint not null default 1,
  updater varchar(50) null,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists service_order (
  id bigint primary key auto_increment,
  order_no varchar(30) not null unique,
  product_id bigint not null,
  product_name varchar(100) not null,
  amount decimal(10,2) not null,
  buyer_id bigint not null,
  status varchar(20) not null,
  service_type varchar(20) not null,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists work_order (
  id bigint primary key auto_increment,
  order_no varchar(30) not null unique,
  order_id bigint not null,
  service_item varchar(100) not null,
  amount decimal(10,2) not null,
  personnel_id bigint null,
  customer_id bigint not null,
  status varchar(20) not null,
  dispatch_time datetime null,
  service_time datetime null,
  complete_time datetime null,
  cancel_reason varchar(200) null,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists after_sale (
  id bigint primary key auto_increment,
  order_id bigint not null,
  applicant_id bigint not null,
  reason varchar(200) not null,
  status varchar(20) not null,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists review (
  id bigint primary key auto_increment,
  order_id bigint not null,
  product_id bigint not null,
  user_id bigint not null,
  rating int not null,
  content varchar(500) null,
  visible tinyint not null default 1,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists operation_content (
  id bigint primary key auto_increment,
  type varchar(30) not null,
  title varchar(100) not null,
  publisher varchar(50) null,
  author varchar(50) null,
  location varchar(100) null,
  quota int null,
  likes int not null default 0,
  status tinyint not null default 1,
  created_at datetime not null default current_timestamp,
  index idx_operation_type (type)
) default charset = utf8mb4;

create table if not exists staff (
  id bigint primary key auto_increment,
  staff_no varchar(20) not null unique,
  name varchar(50) not null,
  phone varchar(20) not null unique,
  password_hash varchar(255) not null,
  role_id bigint not null,
  remark varchar(200) null,
  avatar_url varchar(255) null,
  status tinyint not null default 1,
  updater varchar(50) null,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

set @add_user_tag_color = if(
  (select count(*) from information_schema.columns where table_schema = database() and table_name = 'user_tag' and column_name = 'color') = 0,
  'alter table user_tag add column color varchar(20) null after type',
  'select 1'
);
prepare stmt from @add_user_tag_color;
execute stmt;
deallocate prepare stmt;

set @add_staff_avatar = if(
  (select count(*) from information_schema.columns where table_schema = database() and table_name = 'staff' and column_name = 'avatar_url') = 0,
  'alter table staff add column avatar_url varchar(255) null after remark',
  'select 1'
);
prepare stmt from @add_staff_avatar;
execute stmt;
deallocate prepare stmt;

create table if not exists role (
  id bigint primary key auto_increment,
  name varchar(50) not null unique,
  description varchar(200) null,
  permissions text null,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists operation_log (
  id bigint primary key auto_increment,
  operator varchar(50) not null,
  action_type varchar(20) not null,
  target varchar(100) not null,
  content varchar(500) null,
  ip_address varchar(50) null,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists agreement (
  id bigint primary key auto_increment,
  title varchar(100) not null,
  type varchar(30) not null,
  content text null,
  status tinyint not null default 1,
  updated_at datetime not null default current_timestamp on update current_timestamp
) default charset = utf8mb4;
