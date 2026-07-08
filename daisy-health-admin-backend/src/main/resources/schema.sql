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

set @add_user_emergency_contact = if(
  (select count(*) from information_schema.columns where table_schema = database() and table_name = 'user' and column_name = 'emergency_contact') = 0,
  'alter table `user` add column emergency_contact varchar(20) null after diet_preference',
  'select 1'
);
prepare stmt from @add_user_emergency_contact;
execute stmt;
deallocate prepare stmt;

set @add_user_emergency_phone = if(
  (select count(*) from information_schema.columns where table_schema = database() and table_name = 'user' and column_name = 'emergency_phone') = 0,
  'alter table `user` add column emergency_phone varchar(20) null after emergency_contact',
  'select 1'
);
prepare stmt from @add_user_emergency_phone;
execute stmt;
deallocate prepare stmt;

create table if not exists health_settings (
  id bigint primary key auto_increment,
  user_id bigint not null unique,
  heart_rate_upper int null,
  heart_rate_lower int null,
  blood_pressure_sys_upper int null,
  blood_pressure_dias_upper int null,
  blood_sugar_upper decimal(4,1) null,
  blood_sugar_lower decimal(4,1) null,
  step_goal int default 6000,
  sleep_goal decimal(3,1) default 7.0,
  medication_reminder tinyint default 1,
  checkup_interval int null,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp
) default charset = utf8mb4;

create table if not exists device (
  id bigint primary key auto_increment,
  user_id bigint not null,
  device_name varchar(50) not null,
  device_type varchar(20) not null,
  device_code varchar(50) null,
  bind_time datetime null,
  last_sync_time datetime null,
  status tinyint default 1,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists report (
  id bigint primary key auto_increment,
  user_id bigint not null,
  title varchar(100) not null,
  report_type varchar(20) not null,
  report_date date not null,
  file_url varchar(255) null,
  summary text null,
  doctor_name varchar(50) null,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists coupon (
  id bigint primary key auto_increment,
  user_id bigint null,
  coupon_no varchar(30) not null unique,
  name varchar(100) not null,
  type varchar(20) not null,
  discount decimal(10,2) null,
  min_amount decimal(10,2) null,
  status varchar(20) not null,
  expire_date date null,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists user_points (
  id bigint primary key auto_increment,
  user_id bigint not null unique,
  points int default 0,
  total_earned int default 0,
  total_spent int default 0,
  level varchar(20) default '普通',
  growth_value int default 0,
  updated_at datetime not null default current_timestamp on update current_timestamp
) default charset = utf8mb4;

create table if not exists points_record (
  id bigint primary key auto_increment,
  user_id bigint not null,
  change_value int not null,
  reason varchar(100) not null,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists member_level (
  id bigint primary key auto_increment,
  name varchar(50) not null unique,
  min_growth int not null,
  max_growth int null,
  icon varchar(255) null,
  benefits text null,
  status tinyint default 1,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists points_rule (
  id bigint primary key auto_increment,
  action_type varchar(50) not null unique,
  description varchar(200) null,
  points int not null,
  growth int not null,
  daily_limit int null,
  status tinyint default 1,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists product_category (
  id bigint primary key auto_increment,
  name varchar(30) not null unique,
  code varchar(20) not null unique,
  description varchar(200) null,
  sort_order int default 0,
  status tinyint default 1,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists service_item (
  id bigint primary key auto_increment,
  product_id bigint not null,
  name varchar(100) not null,
  description varchar(500) null,
  duration int null,
  price decimal(10,2) null,
  status tinyint default 1,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists banner (
  id bigint primary key auto_increment,
  title varchar(100) null,
  image_url varchar(255) not null,
  link_url varchar(255) null,
  sort_order int default 0,
  location varchar(20) default 'home',
  status tinyint default 1,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists activity (
  id bigint primary key auto_increment,
  title varchar(100) not null,
  cover_url varchar(255) null,
  location varchar(200) null,
  start_time datetime not null,
  end_time datetime null,
  quota int not null,
  enrolled int default 0,
  status varchar(20) default 'draft',
  content text null,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists activity_enroll (
  id bigint primary key auto_increment,
  activity_id bigint not null,
  user_id bigint not null,
  enroll_time datetime not null default current_timestamp,
  status varchar(20) default 'enrolled',
  remark varchar(200) null
) default charset = utf8mb4;

create table if not exists topic (
  id bigint primary key auto_increment,
  name varchar(50) not null unique,
  description varchar(200) null,
  icon varchar(255) null,
  post_count int default 0,
  status tinyint default 1,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists recipe (
  id bigint primary key auto_increment,
  name varchar(100) not null,
  category varchar(30) not null,
  ingredients text not null,
  steps text not null,
  calories int null,
  suitable_for varchar(200) null,
  cover_url varchar(255) null,
  status tinyint default 1,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists article (
  id bigint primary key auto_increment,
  title varchar(100) not null,
  summary varchar(500) null,
  content text null,
  cover_url varchar(255) null,
  author varchar(50) null,
  category varchar(30) null,
  tags varchar(200) null,
  view_count int default 0,
  status tinyint default 1,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists disease (
  id bigint primary key auto_increment,
  name varchar(100) not null,
  category varchar(30) null,
  summary varchar(500) null,
  symptoms text null,
  prevention text null,
  diet_advice text null,
  risk_factors text null,
  status tinyint default 1,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists institution (
  id bigint primary key auto_increment,
  name varchar(100) not null,
  address varchar(200) null,
  phone varchar(20) null,
  type varchar(30) not null,
  rating decimal(2,1) null,
  capacity int null,
  price_range varchar(50) null,
  services text null,
  images text null,
  status tinyint default 1,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists video (
  id bigint primary key auto_increment,
  title varchar(100) not null,
  description varchar(500) null,
  cover_url varchar(255) null,
  video_url varchar(255) not null,
  duration int null,
  lecturer varchar(50) null,
  category varchar(30) null,
  view_count int default 0,
  status tinyint default 1,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists food (
  id bigint primary key auto_increment,
  name varchar(100) not null,
  category varchar(30) null,
  calories int null,
  protein decimal(5,1) null,
  fat decimal(5,1) null,
  carbs decimal(5,1) null,
  fiber decimal(5,1) null,
  sodium decimal(5,1) null,
  suitable_for varchar(200) null,
  status tinyint default 1,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists assessment (
  id bigint primary key auto_increment,
  title varchar(100) not null,
  description text null,
  type varchar(30) not null,
  questions text not null,
  scoring_rules text null,
  status tinyint default 1,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists assessment_result (
  id bigint primary key auto_increment,
  assessment_id bigint not null,
  user_id bigint not null,
  score int not null,
  result text null,
  answers text null,
  created_at datetime not null default current_timestamp
) default charset = utf8mb4;

create table if not exists resource_detail (
  id bigint primary key auto_increment,
  resource_name varchar(60) not null,
  resource_id bigint not null,
  detail_title varchar(120) null,
  owner_name varchar(60) null,
  detail_status varchar(40) null,
  detail_content text null,
  remark text null,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  unique key uk_resource_detail (resource_name, resource_id)
) default charset = utf8mb4;
