insert ignore into role(id, name, description, permissions, created_at) values
(1, '超级管理员', '全部模块与全部操作', '{"*":["*"]}', now()),
(2, '管理员', '日常运营管理', '{"dashboard":["view"],"users":["view","edit"],"orders":["view"]}', now()),
(3, '客服专员', '工单、订单、售后处理', '{"workOrders":["view","edit"],"orders":["view"],"afterSales":["view","edit"]}', now());

insert ignore into staff(id, staff_no, name, phone, password_hash, role_id, remark, status, updater, created_at) values
(1, 'S0001', '系统管理员', '13800000000', '753951', 1, '初始化管理员', 1, '系统', now()),
(2, 'S0002', '运营专员', '13800000001', '753951', 2, '内容运营', 1, '系统', now()),
(3, 'S0003', '客服专员', '13800000002', '753951', 3, '客户服务', 1, '系统', now());

insert ignore into `user`(id, nickname, real_name, gender, birthday, phone, id_card, address, bio, height, weight, ethnicity, education, blood_type, rh_negative, chronic_disease, sleep_quality, smoking_freq, drinking_freq, exercise_freq, diet_preference, created_at, last_login_time, last_buy_time, status) values
(10001, '兰姨', '王秀兰', 2, '1952-03-12', '13800010001', '310101195203120021', '上海市浦东新区', '重点关怀用户', 158.0, 58.4, '汉族', '高中', 'A', 0, '高血压', '良好', '不吸烟', '偶尔', '每周3次', '清淡', date_sub(now(), interval 6 day), now(), date_sub(now(), interval 2 day), 1),
(10002, '建国叔', '陈建国', 1, '1949-09-08', '13800010002', '310101194909080019', '上海市徐汇区', '康复护理用户', 170.0, 72.0, '汉族', '大专', 'O', 0, '脑中风康复期', '一般', '已戒烟', '不饮酒', '每天', '低盐', date_sub(now(), interval 5 day), date_sub(now(), interval 1 day), date_sub(now(), interval 3 day), 1),
(10003, '桂英', '赵桂英', 2, '1945-11-20', '13800010003', '310101194511200028', '上海市静安区', '独居老人', 155.0, 52.5, '汉族', '初中', 'B', 0, '糖尿病', '较差', '不吸烟', '不饮酒', '每周1次', '控糖', date_sub(now(), interval 4 day), date_sub(now(), interval 1 day), date_sub(now(), interval 4 day), 1),
(10004, '爱华', '刘爱华', 2, '1956-05-18', '13800010004', '310101195605180033', '上海市杨浦区', '日常护理用户', 160.0, 61.0, '汉族', '高中', 'AB', 0, '骨质疏松', '良好', '不吸烟', '不饮酒', '每周2次', '均衡', date_sub(now(), interval 3 day), now(), date_sub(now(), interval 1 day), 1);

insert ignore into user_tag(id, name, type, user_count, status, updater, created_at) values
(1, '高血压', 'manual', 1, 1, '系统管理员', now()),
(2, '重点关怀', 'manual', 1, 1, '系统管理员', now()),
(3, '康复护理', 'manual', 1, 1, '系统管理员', now()),
(4, '独居老人', 'manual', 1, 1, '系统管理员', now()),
(5, '糖尿病', 'manual', 1, 1, '系统管理员', now()),
(6, '潜在客户', 'manual', 0, 1, '系统管理员', now()),
(7, '普通客户', 'manual', 0, 1, '系统管理员', now()),
(8, '多次消费客户', 'manual', 0, 1, '系统管理员', now()),
(9, '高血糖', 'manual', 0, 1, '系统管理员', now()),
(10, '高血脂', 'manual', 0, 1, '系统管理员', now());

update staff set avatar_url = coalesce(avatar_url, 'https://api.dicebear.com/7.x/initials/svg?seed=DAISY'), remark = coalesce(remark, '初始化管理员') where id = 1;

update user_tag set color = case name
  when '高血压' then 'green'
  when '糖尿病' then 'red'
  when '多次消费客户' then 'purple'
  when '重点关怀' then 'green'
  when '康复护理' then 'blue'
  when '独居老人' then 'orange'
  when '潜在客户' then 'gray'
  when '普通客户' then 'blue'
  when '高血糖' then 'orange'
  when '高血脂' then 'purple'
  else 'green'
end;

insert ignore into user_tag_rel(id, user_id, tag_id) values
(1, 10001, 1),
(2, 10001, 2),
(3, 10002, 3),
(4, 10003, 4),
(5, 10003, 5);

insert ignore into health_data(id, user_id, data_type, value, unit, record_date, record_time, source, created_at) values
(1, 10001, 'weight', '58.8', 'kg', date_sub(curdate(), interval 4 day), '08:00:00', '设备', now()),
(2, 10001, 'heart_rate', '76', 'bpm', date_sub(curdate(), interval 4 day), '08:00:00', '设备', now()),
(3, 10001, 'weight', '58.6', 'kg', date_sub(curdate(), interval 3 day), '08:00:00', '设备', now()),
(4, 10001, 'heart_rate', '74', 'bpm', date_sub(curdate(), interval 3 day), '08:00:00', '设备', now()),
(5, 10001, 'weight', '58.5', 'kg', date_sub(curdate(), interval 2 day), '08:00:00', '设备', now()),
(6, 10001, 'heart_rate', '78', 'bpm', date_sub(curdate(), interval 2 day), '08:00:00', '设备', now()),
(7, 10001, 'weight', '58.4', 'kg', date_sub(curdate(), interval 1 day), '08:00:00', '设备', now()),
(8, 10001, 'heart_rate', '75', 'bpm', date_sub(curdate(), interval 1 day), '08:00:00', '设备', now()),
(9, 10001, 'weight', '58.3', 'kg', curdate(), '08:00:00', '设备', now()),
(10, 10001, 'heart_rate', '73', 'bpm', curdate(), '08:00:00', '设备', now());

insert ignore into medication_record(id, user_id, period, drug_name, frequency, take_time, dosage, reminder_enabled, source, creator, created_at) values
(1, 10001, '早餐', '硝苯地平控释片', '每天', '08:00:00', '1片', 1, '手动', '系统管理员', now()),
(2, 10001, '晚餐', '阿司匹林', '每天', '19:00:00', '1片', 1, '手动', '系统管理员', now()),
(3, 10003, '早餐', '二甲双胍', '每天', '08:30:00', '1片', 1, '手动', '系统管理员', now());

insert ignore into service_personnel(id, name, phone, service_type, area, join_time, status, audit_status, created_at) values
(1, '张敏', '13900020001', '家政护理', '浦东新区', date_sub(now(), interval 20 day), 1, '已通过', date_sub(now(), interval 20 day)),
(2, '李华', '13900020002', '康复理疗', '徐汇区', date_sub(now(), interval 18 day), 1, '已通过', date_sub(now(), interval 18 day)),
(3, '周丽', '13900020003', '上门体检', '静安区', date_sub(now(), interval 1 day), 1, '待审核', date_sub(now(), interval 1 day));

insert ignore into product(id, name, code, category, price, status, updater, created_at) values
(1, '2小时日常清洁', 'P-HK-001', '家政护理', 129.00, 1, '运营专员', date_sub(now(), interval 6 day)),
(2, '脑中风康复训练', 'P-RH-001', '康复理疗', 299.00, 1, '运营专员', date_sub(now(), interval 5 day)),
(3, '上门基础体检', 'P-EX-001', '上门体检', 399.00, 1, '运营专员', date_sub(now(), interval 4 day)),
(4, '助浴护理', 'P-HK-002', '家政护理', 199.00, 1, '运营专员', date_sub(now(), interval 3 day));

insert ignore into service_order(id, order_no, product_id, product_name, amount, buyer_id, status, service_type, created_at) values
(1, 'OD20260706001', 3, '上门基础体检', 399.00, 10003, 'pending_accept', '上门体检', now()),
(2, 'OD20260706002', 4, '助浴护理', 199.00, 10001, 'pending_service', '家政护理', date_sub(now(), interval 1 day)),
(3, 'OD20260705001', 2, '脑中风康复训练', 299.00, 10002, 'completed', '康复理疗', date_sub(now(), interval 2 day)),
(4, 'OD20260704001', 1, '2小时日常清洁', 129.00, 10004, 'after_sale', '家政护理', date_sub(now(), interval 3 day));

insert ignore into work_order(id, order_no, order_id, service_item, amount, personnel_id, customer_id, status, dispatch_time, service_time, complete_time, created_at) values
(1, 'WO20260706001', 2, '助浴护理', 199.00, 1, 10001, 'pending', now(), timestamp(curdate(), '09:00:00'), null, now()),
(2, 'WO20260706002', 3, '肩颈康复', 299.00, 2, 10002, 'service_in', now(), timestamp(curdate(), '10:00:00'), null, now()),
(3, 'WO20260706003', 1, '上门基础体检', 399.00, 3, 10003, 'completed', date_sub(now(), interval 2 hour), timestamp(curdate(), '14:00:00'), timestamp(curdate(), '15:30:00'), now()),
(4, 'WO20260706004', 4, '日常清洁', 129.00, 1, 10004, 'cancelled', date_sub(now(), interval 1 day), timestamp(curdate(), '16:00:00'), null, date_sub(now(), interval 1 day));

insert ignore into after_sale(id, order_id, applicant_id, reason, status, created_at) values
(1, 4, 10004, '服务时间变更', '处理中', date_sub(now(), interval 1 day));

insert ignore into review(id, order_id, product_id, user_id, rating, content, visible, created_at) values
(1, 3, 2, 10002, 5, '服务人员专业，康复训练安排合理。', 1, date_sub(now(), interval 1 day)),
(2, 2, 4, 10001, 4, '护理过程细致。', 1, now());

insert ignore into operation_content(id, type, title, publisher, author, location, quota, likes, status, created_at) values
(1, 'posts', '社区晨练打卡', '王秀兰', null, null, null, 89, 1, now()),
(2, 'activities', '慢病管理讲座', null, null, '浦东社区中心', 60, 0, 1, date_add(now(), interval 3 day)),
(3, 'articles', '夏季老人补水指南', null, '运营中心', null, null, 0, 1, date_sub(now(), interval 2 day)),
(4, 'recipes', '低盐高蛋白菜谱', null, '营养师', null, null, 0, 1, date_sub(now(), interval 2 day)),
(5, 'diseases', '高血压日常管理', null, '健康编辑', null, null, 0, 1, date_sub(now(), interval 2 day)),
(6, 'institutions', '浦东康养中心', null, null, '浦东新区', 120, 0, 1, date_sub(now(), interval 2 day)),
(7, 'videos', '居家康复训练课', null, '健康讲堂', null, null, 0, 1, date_sub(now(), interval 2 day)),
(8, 'comments', '服务体验很好', '陈建国', null, null, null, 12, 1, date_sub(now(), interval 1 day)),
(9, 'foods', '燕麦', null, '营养数据库', null, null, 0, 1, date_sub(now(), interval 1 day)),
(10, 'assessments', '睡眠质量测评', null, '测评中心', null, null, 0, 1, date_sub(now(), interval 1 day));

insert ignore into operation_log(id, operator, action_type, target, content, ip_address, created_at) values
(1, '系统管理员', '登录', '后台系统', '管理员登录系统', '127.0.0.1', now()),
(2, '运营专员', '新增', '健康资讯', '发布夏季老人补水指南', '127.0.0.1', date_sub(now(), interval 1 day));

insert ignore into agreement(id, title, type, content, status, updated_at) values
(1, '用户隐私政策', 'privacy', '黛西健康用户隐私政策示例内容。', 1, now()),
(2, '服务协议', 'service', '黛西健康服务协议示例内容。', 1, now());

insert ignore into `user`(id, nickname, real_name, gender, birthday, phone, address, bio, height, weight, ethnicity, education, blood_type, rh_negative, chronic_disease, sleep_quality, smoking_freq, drinking_freq, exercise_freq, diet_preference, created_at, last_login_time, last_buy_time, status) values
(10005, '美玲', '沈美玲', 2, '1951-01-09', '13800010005', '上海市黄浦区', '高龄重点随访', 156.0, 55.0, '汉族', '初中', 'O', 0, '冠心病', '一般', '不吸烟', '不饮酒', '每周2次', '低脂', date_sub(now(), interval 8 day), date_sub(now(), interval 1 day), date_sub(now(), interval 5 day), 1),
(10006, '老周', '周国强', 1, '1948-07-22', '13800010006', '上海市长宁区', '康复训练用户', 168.0, 68.5, '汉族', '高中', 'A', 0, '膝关节术后', '良好', '已戒烟', '不饮酒', '每天', '高蛋白', date_sub(now(), interval 9 day), date_sub(now(), interval 2 day), date_sub(now(), interval 2 day), 1),
(10007, '秀珍', '吴秀珍', 2, '1954-12-02', '13800010007', '上海市虹口区', '慢病管理用户', 154.0, 57.8, '汉族', '小学', 'B', 0, '糖尿病,高血压', '较差', '不吸烟', '不饮酒', '每周1次', '控糖低盐', date_sub(now(), interval 10 day), now(), date_sub(now(), interval 6 day), 1),
(10008, '德明', '顾德明', 1, '1950-04-30', '13800010008', '上海市闵行区', '上门体检用户', 172.0, 70.1, '汉族', '大专', 'AB', 0, '高血脂', '良好', '不吸烟', '偶尔', '每周4次', '低脂', date_sub(now(), interval 11 day), date_sub(now(), interval 3 day), date_sub(now(), interval 1 day), 1),
(10009, '玉芬', '郑玉芬', 2, '1947-08-15', '13800010009', '上海市宝山区', '独居老人', 153.0, 50.2, '汉族', '初中', 'O', 0, '骨质疏松', '一般', '不吸烟', '不饮酒', '每周2次', '补钙', date_sub(now(), interval 12 day), date_sub(now(), interval 1 day), date_sub(now(), interval 7 day), 1),
(10010, '老陆', '陆建平', 1, '1953-10-03', '13800010010', '上海市普陀区', '家政护理用户', 169.0, 66.0, '汉族', '高中', 'A', 0, '腰椎间盘突出', '良好', '不吸烟', '不饮酒', '每周3次', '均衡', date_sub(now(), interval 13 day), now(), date_sub(now(), interval 2 day), 1),
(10011, '凤英', '马凤英', 2, '1946-06-28', '13800010011', '上海市嘉定区', '重点关怀用户', 152.0, 49.7, '汉族', '小学', 'B', 0, '阿尔茨海默早期', '较差', '不吸烟', '不饮酒', '少量', '易消化', date_sub(now(), interval 14 day), date_sub(now(), interval 4 day), date_sub(now(), interval 8 day), 1),
(10012, '长生', '孙长生', 1, '1955-02-17', '13800010012', '上海市松江区', '康复理疗用户', 171.0, 73.4, '汉族', '本科', 'O', 0, '肩周炎', '良好', '不吸烟', '偶尔', '每天', '均衡', date_sub(now(), interval 15 day), date_sub(now(), interval 1 day), date_sub(now(), interval 3 day), 1);

insert ignore into user_tag_rel(id, user_id, tag_id) values
(6, 10005, 2),
(7, 10005, 1),
(8, 10006, 3),
(9, 10007, 5),
(10, 10007, 1),
(11, 10008, 2),
(12, 10009, 4),
(13, 10010, 3),
(14, 10011, 2),
(15, 10011, 4),
(16, 10012, 3);

insert ignore into service_personnel(id, name, phone, service_type, area, join_time, status, audit_status, created_at) values
(4, '王芳', '13900020004', '家政护理', '黄浦区', date_sub(now(), interval 25 day), 1, '已通过', date_sub(now(), interval 25 day)),
(5, '赵强', '13900020005', '康复理疗', '长宁区', date_sub(now(), interval 22 day), 1, '已通过', date_sub(now(), interval 22 day)),
(6, '钱静', '13900020006', '上门体检', '虹口区', date_sub(now(), interval 10 day), 1, '已通过', date_sub(now(), interval 10 day)),
(7, '孙洁', '13900020007', '家政护理', '闵行区', date_sub(now(), interval 6 day), 1, '待审核', date_sub(now(), interval 6 day));

insert ignore into product(id, name, code, category, price, status, updater, created_at) values
(5, '助餐陪诊服务', 'P-HK-003', '家政护理', 169.00, 1, '运营专员', date_sub(now(), interval 7 day)),
(6, '肩周炎理疗套餐', 'P-RH-002', '康复理疗', 259.00, 1, '运营专员', date_sub(now(), interval 8 day)),
(7, '慢病随访体检', 'P-EX-002', '上门体检', 499.00, 1, '运营专员', date_sub(now(), interval 9 day)),
(8, '半日陪护服务', 'P-HK-004', '家政护理', 329.00, 1, '运营专员', date_sub(now(), interval 10 day)),
(9, '术后肌力训练', 'P-RH-003', '康复理疗', 359.00, 1, '运营专员', date_sub(now(), interval 11 day)),
(10, '心脑血管专项体检', 'P-EX-003', '上门体检', 699.00, 1, '运营专员', date_sub(now(), interval 12 day));

insert ignore into service_order(id, order_no, product_id, product_name, amount, buyer_id, status, service_type, created_at) values
(5, 'OD20260703001', 5, '助餐陪诊服务', 169.00, 10005, 'completed', '家政护理', date_sub(now(), interval 4 day)),
(6, 'OD20260703002', 6, '肩周炎理疗套餐', 259.00, 10012, 'pending_service', '康复理疗', date_sub(now(), interval 4 day)),
(7, 'OD20260702001', 7, '慢病随访体检', 499.00, 10007, 'completed', '上门体检', date_sub(now(), interval 5 day)),
(8, 'OD20260702002', 8, '半日陪护服务', 329.00, 10009, 'pending_accept', '家政护理', date_sub(now(), interval 5 day)),
(9, 'OD20260701001', 9, '术后肌力训练', 359.00, 10006, 'completed', '康复理疗', date_sub(now(), interval 6 day)),
(10, 'OD20260701002', 10, '心脑血管专项体检', 699.00, 10008, 'completed', '上门体检', date_sub(now(), interval 6 day)),
(11, 'OD20260630001', 1, '2小时日常清洁', 129.00, 10010, 'completed', '家政护理', date_sub(now(), interval 7 day)),
(12, 'OD20260630002', 4, '助浴护理', 199.00, 10011, 'after_sale', '家政护理', date_sub(now(), interval 7 day));

insert ignore into work_order(id, order_no, order_id, service_item, amount, personnel_id, customer_id, status, dispatch_time, service_time, complete_time, created_at) values
(5, 'WO20260705001', 5, '助餐陪诊服务', 169.00, 4, 10005, 'completed', date_sub(now(), interval 4 day), date_sub(now(), interval 4 day), date_sub(now(), interval 4 day), date_sub(now(), interval 4 day)),
(6, 'WO20260705002', 6, '肩周炎理疗套餐', 259.00, 5, 10012, 'pending', date_sub(now(), interval 2 day), date_add(now(), interval 1 day), null, date_sub(now(), interval 2 day)),
(7, 'WO20260704001', 7, '慢病随访体检', 499.00, 6, 10007, 'completed', date_sub(now(), interval 5 day), date_sub(now(), interval 5 day), date_sub(now(), interval 5 day), date_sub(now(), interval 5 day)),
(8, 'WO20260704002', 8, '半日陪护服务', 329.00, 7, 10009, 'pending', date_sub(now(), interval 1 day), date_add(now(), interval 2 day), null, date_sub(now(), interval 1 day)),
(9, 'WO20260703001', 9, '术后肌力训练', 359.00, 2, 10006, 'completed', date_sub(now(), interval 6 day), date_sub(now(), interval 6 day), date_sub(now(), interval 6 day), date_sub(now(), interval 6 day)),
(10, 'WO20260703002', 10, '心脑血管专项体检', 699.00, 3, 10008, 'completed', date_sub(now(), interval 6 day), date_sub(now(), interval 6 day), date_sub(now(), interval 6 day), date_sub(now(), interval 6 day)),
(11, 'WO20260702001', 11, '2小时日常清洁', 129.00, 1, 10010, 'completed', date_sub(now(), interval 7 day), date_sub(now(), interval 7 day), date_sub(now(), interval 7 day), date_sub(now(), interval 7 day)),
(12, 'WO20260702002', 12, '助浴护理', 199.00, 4, 10011, 'cancelled', date_sub(now(), interval 7 day), date_sub(now(), interval 7 day), null, date_sub(now(), interval 7 day)),
(201, 'WOSEEDTODAY001', 1, '晨间血压巡检', 89.00, 1, 10002, 'pending', now(), timestamp(curdate(), '08:30:00'), null, now()),
(202, 'WOSEEDTODAY002', 2, '康复训练陪护', 199.00, 2, 10004, 'service_in', now(), timestamp(curdate(), '11:30:00'), null, now()),
(203, 'WOSEEDTODAY003', 3, '午后用药提醒', 39.00, 3, 10005, 'pending', now(), timestamp(curdate(), '12:30:00'), null, now()),
(204, 'WOSEEDTODAY004', 4, '居家安全检查', 129.00, 4, 10006, 'pending', now(), timestamp(curdate(), '13:30:00'), null, now()),
(205, 'WOSEEDTODAY005', 5, '晚间健康随访', 69.00, 5, 10007, 'completed', now(), timestamp(curdate(), '18:30:00'), timestamp(curdate(), '19:10:00'), now()),
(206, 'WOSEEDTODAY006', 6, '睡前血糖记录', 49.00, 6, 10008, 'pending', now(), timestamp(curdate(), '21:00:00'), null, now());

insert ignore into work_order(order_no, order_id, product_id, service_item, amount, personnel_id, customer_id, status, dispatch_time, service_time, complete_time, created_at) values
('WOBOARD-DAY-0', 1, 3, '上门基础体检', 399.00, 3, 10003, 'pending', now(), timestamp(curdate(), '09:00:00'), null, now()),
('WOBOARD-DAY-1', 6, 6, '肩周炎理疗套餐', 259.00, 5, 10012, 'pending', now(), timestamp(date_add(curdate(), interval 1 day), '10:00:00'), null, now()),
('WOBOARD-DAY-2', 8, 8, '半日陪护服务', 329.00, 4, 10009, 'pending', now(), timestamp(date_add(curdate(), interval 2 day), '13:00:00'), null, now()),
('WOBOARD-DAY-3', 7, 7, '慢病随访体检', 499.00, 6, 10007, 'pending', now(), timestamp(date_add(curdate(), interval 3 day), '09:30:00'), null, now()),
('WOBOARD-DAY-4', 5, 5, '助餐陪诊服务', 169.00, 4, 10005, 'pending', now(), timestamp(date_add(curdate(), interval 4 day), '13:30:00'), null, now()),
('WOBOARD-DAY-5', 9, 9, '术后肌力训练', 359.00, 5, 10006, 'pending', now(), timestamp(date_add(curdate(), interval 5 day), '10:00:00'), null, now()),
('WOBOARD-DAY-6', 10, 10, '心脑血管专项体检', 699.00, 6, 10008, 'pending', now(), timestamp(date_add(curdate(), interval 6 day), '14:30:00'), null, now());

update work_order w
join service_order o on o.id = w.order_id
set w.product_id = o.product_id
where w.product_id is null;

insert ignore into after_sale(id, order_id, applicant_id, reason, status, created_at) values
(2, 12, 10011, '用户临时身体不适', '处理中', date_sub(now(), interval 2 day));

insert ignore into review(id, order_id, product_id, user_id, rating, content, visible, created_at) values
(3, 5, 5, 10005, 5, '陪诊非常耐心。', 1, date_sub(now(), interval 3 day)),
(4, 7, 7, 10007, 4, '体检流程清晰。', 1, date_sub(now(), interval 4 day)),
(5, 9, 9, 10006, 5, '训练后恢复明显。', 1, date_sub(now(), interval 5 day)),
(6, 10, 10, 10008, 5, '报告讲解细致。', 1, date_sub(now(), interval 5 day)),
(7, 11, 1, 10010, 4, '清洁服务准时。', 1, date_sub(now(), interval 6 day));

insert ignore into operation_content(id, type, title, publisher, author, location, quota, likes, status, created_at) values
(11, 'posts', '康复训练第30天', '周国强', null, null, null, 46, 1, date_sub(now(), interval 1 day)),
(12, 'posts', '今日低盐午餐', '吴秀珍', null, null, null, 58, 1, date_sub(now(), interval 2 day)),
(13, 'activities', '社区义诊日', null, null, '黄浦社区服务中心', 80, 0, 1, date_add(now(), interval 5 day)),
(14, 'activities', '银发健康运动会', null, null, '徐汇体育中心', 120, 0, 1, date_add(now(), interval 8 day)),
(15, 'articles', '老人夏季防暑指南', null, '运营中心', null, null, 0, 1, date_sub(now(), interval 3 day)),
(16, 'articles', '高血压用药提醒', null, '健康编辑', null, null, 0, 1, date_sub(now(), interval 4 day)),
(17, 'recipes', '控糖早餐搭配', null, '营养师', null, null, 0, 1, date_sub(now(), interval 3 day)),
(18, 'recipes', '高蛋白软食菜单', null, '营养师', null, null, 0, 1, date_sub(now(), interval 4 day)),
(19, 'diseases', '糖尿病足预防', null, '健康编辑', null, null, 0, 1, date_sub(now(), interval 5 day)),
(20, 'institutions', '静安护理院', null, null, '静安区', 90, 0, 1, date_sub(now(), interval 6 day)),
(21, 'videos', '肩颈放松课程', null, '健康讲堂', null, null, 0, 1, date_sub(now(), interval 3 day)),
(22, 'foods', '鸡蛋', null, '营养数据库', null, null, 0, 1, date_sub(now(), interval 2 day)),
(23, 'assessments', '跌倒风险测评', null, '测评中心', null, null, 0, 1, date_sub(now(), interval 2 day));

insert ignore into operation_log(id, operator, action_type, target, content, ip_address, created_at) values
(3, '系统管理员', '新增', '用户档案', '新增沈美玲用户档案', '127.0.0.1', date_sub(now(), interval 2 day)),
(4, '客服专员', '派单', '工单管理', '为肩周炎理疗套餐派单', '127.0.0.1', date_sub(now(), interval 1 day)),
(5, '运营专员', '新增', '活动管理', '发布社区义诊日活动', '127.0.0.1', now());

update user_tag t set user_count = (select count(*) from user_tag_rel r where r.tag_id = t.id);

insert ignore into health_settings(id, user_id, heart_rate_upper, heart_rate_lower, blood_pressure_sys_upper, blood_pressure_dias_upper, blood_sugar_upper, blood_sugar_lower, step_goal, sleep_goal, medication_reminder, checkup_interval) values
(1, 10001, 110, 55, 150, 95, 8.0, 3.9, 6000, 7.0, 1, 180),
(2, 10003, 105, 55, 145, 90, 7.8, 4.0, 5000, 7.5, 1, 90);

insert ignore into device(id, user_id, device_name, device_type, device_code, bind_time, last_sync_time, status) values
(1, 10001, '小米手环8', 'band', 'MI-BAND-10001', date_sub(now(), interval 20 day), now(), 1),
(2, 10003, '智能血糖仪', 'glucometer', 'GLU-10003', date_sub(now(), interval 12 day), date_sub(now(), interval 1 hour), 1);

insert ignore into report(id, user_id, title, report_type, report_date, file_url, summary, doctor_name) values
(1, 10001, '2026年7月慢病随访报告', '健康评估', curdate(), '', '血压整体稳定，建议继续低盐饮食。', '李医生'),
(2, 10003, '糖尿病管理月报', '月度总结', date_sub(curdate(), interval 3 day), '', '空腹血糖略高，建议复查。', '王医生');

insert ignore into coupon(id, user_id, coupon_no, name, type, discount, min_amount, status, expire_date) values
(1, 10001, 'CP202607001', '家政护理满减券', '满减', 30.00, 199.00, '未使用', date_add(curdate(), interval 30 day)),
(2, null, 'CP202607002', '康复理疗通用券', '折扣', 8.80, 0.00, '可发放', date_add(curdate(), interval 60 day));

insert ignore into user_points(id, user_id, points, total_earned, total_spent, level, growth_value) values
(1, 10001, 1260, 1800, 540, '银卡', 3200),
(2, 10003, 860, 900, 40, '普通', 1200);

insert ignore into points_record(id, user_id, change_value, reason, created_at) values
(1, 10001, 100, '完成服务订单', date_sub(now(), interval 2 day)),
(2, 10001, -40, '兑换优惠券', date_sub(now(), interval 1 day)),
(3, 10003, 80, '健康打卡', now());

insert ignore into member_level(id, name, min_growth, max_growth, icon, benefits, status) values
(1, '普通', 0, 999, '', '基础服务权益', 1),
(2, '银卡', 1000, 4999, '', '专属客服、优惠券礼包', 1),
(3, '金卡', 5000, null, '', '优先派单、生日礼遇', 1);

insert ignore into points_rule(id, action_type, description, points, growth, daily_limit, status) values
(1, 'signin', '每日签到', 5, 5, 1, 1),
(2, 'order', '完成订单', 100, 120, null, 1),
(3, 'review', '发布评价', 20, 20, 3, 1);

insert ignore into product_category(id, name, code, description, sort_order, status) values
(1, '家政护理', 'HK', '居家照护、清洁、助浴', 1, 1),
(2, '康复理疗', 'RH', '康复训练与理疗服务', 2, 1),
(3, '上门体检', 'EX', '居家健康检测服务', 3, 1);

insert ignore into service_item(id, product_id, name, description, duration, price, status) values
(1, 4, '助浴前评估', '上门前基础身体状态评估', 15, 0.00, 1),
(2, 4, '全程助浴护理', '助浴、擦洗、换衣及安全看护', 75, 199.00, 1),
(3, 2, '肩颈放松训练', '肩颈肌群康复训练', 45, 99.00, 1);

insert ignore into banner(id, title, image_url, link_url, sort_order, location, status) values
(1, '夏季慢病管理专题', 'https://example.com/banner-summer.png', '/articles/1', 1, 'home', 1),
(2, '社区义诊报名', 'https://example.com/banner-clinic.png', '/activities/1', 2, 'home', 1);

insert ignore into activity(id, title, cover_url, location, start_time, end_time, quota, enrolled, status, content) values
(1, '社区义诊日', '', '黄浦社区服务中心', date_add(now(), interval 5 day), date_add(now(), interval 5 day), 80, 12, 'published', '血压血糖检测与医生问诊。'),
(2, '银发健康运动会', '', '徐汇体育中心', date_add(now(), interval 8 day), date_add(now(), interval 8 day), 120, 28, 'published', '趣味运动与康复指导。');

insert ignore into activity_enroll(id, activity_id, user_id, status, remark) values
(1, 1, 10001, 'enrolled', '需要轮椅通道'),
(2, 1, 10003, 'attended', '已签到');

insert ignore into topic(id, name, description, icon, post_count, status) values
(1, '健康打卡', '每日健康生活分享', '', 23, 1),
(2, '康复训练', '康复训练记录与经验交流', '', 12, 1);

insert ignore into recipe(id, name, category, ingredients, steps, calories, suitable_for, cover_url, status) values
(1, '低盐蒸鱼', '午餐', '鱼,葱姜,低钠盐', '腌制;清蒸;调味', 260, '高血压', '', 1),
(2, '控糖燕麦粥', '早餐', '燕麦,牛奶,坚果', '熬煮;调味', 320, '糖尿病', '', 1);

insert ignore into article(id, title, summary, content, cover_url, author, category, tags, view_count, status) values
(1, '夏季老人补水指南', '高温天气下老人补水要点。', '少量多次补水，关注电解质。', '', '运营中心', '养生', '夏季,补水', 1260, 1),
(2, '高血压用药提醒', '规范用药和监测血压。', '遵医嘱用药，避免自行停药。', '', '健康编辑', '疾病', '高血压', 980, 1);

insert ignore into disease(id, name, category, summary, symptoms, prevention, diet_advice, risk_factors, status) values
(1, '高血压', '心血管', '常见慢性病，需要长期监测。', '头晕,心悸', '低盐饮食,规律运动', '低盐低脂', '年龄,肥胖,遗传', 1),
(2, '糖尿病', '内分泌', '血糖代谢异常疾病。', '多饮,多尿', '控糖饮食,定期监测', '低糖高纤维', '肥胖,家族史', 1);

insert ignore into institution(id, name, address, phone, type, rating, capacity, price_range, services, images, status) values
(1, '浦东康养中心', '上海市浦东新区康养路88号', '021-88880001', '养老院', 4.8, 120, '5000-9000/月', '护理,康复,餐饮', '', 1),
(2, '静安护理院', '上海市静安区安康路66号', '021-88880002', '护理院', 4.6, 90, '6000-11000/月', '医护,康复', '', 1);

insert ignore into video(id, title, description, cover_url, video_url, duration, lecturer, category, view_count, status) values
(1, '居家康复训练课', '适合老人每日练习的康复动作。', '', 'https://example.com/video-rehab.mp4', 900, '张康复师', '康复', 560, 1),
(2, '肩颈放松课程', '肩颈舒缓训练。', '', 'https://example.com/video-neck.mp4', 720, '李康复师', '康复', 430, 1);

insert ignore into food(id, name, category, calories, protein, fat, carbs, fiber, sodium, suitable_for, status) values
(1, '燕麦', '主食', 338, 10.1, 6.0, 66.9, 10.6, 3.7, '糖尿病,高血脂', 1),
(2, '鸡蛋', '蛋类', 144, 13.3, 8.8, 2.8, 0.0, 131.5, '康复护理', 1);

insert ignore into assessment(id, title, description, type, questions, scoring_rules, status) values
(1, '睡眠质量测评', '评估近一周睡眠质量。', 'sleep', '[\"入睡是否困难\",\"夜间醒来次数\"]', '{\"low\":\"良好\",\"high\":\"需关注\"}', 1),
(2, '跌倒风险测评', '评估居家跌倒风险。', 'fall', '[\"是否独居\",\"是否使用助行器\"]', '{\"high\":\"高风险\"}', 1);

insert ignore into assessment_result(id, assessment_id, user_id, score, result, answers) values
(1, 1, 10001, 18, '睡眠质量良好', '{}'),
(2, 2, 10003, 72, '跌倒风险较高', '{}');

insert ignore into role(id, name, description, permissions, created_at) values
(101, '运营主管', '运营模块管理', '{"operations":["view","edit"]}', now()),
(102, '商品主管', '商品和服务项目管理', '{"products":["view","edit"]}', now()),
(103, '服务主管', '服务人员和工单管理', '{"service":["view","edit"]}', now()),
(104, '数据分析员', '数据看板查看', '{"analytics":["view"]}', now()),
(105, '财务专员', '订单和资金查看', '{"trade":["view"]}', now()),
(106, '内容编辑', '资讯和视频维护', '{"articles":["view","edit"]}', now()),
(107, '健康顾问', '用户健康资料维护', '{"users":["view","edit"]}', now());

insert ignore into staff(id, staff_no, name, phone, password_hash, role_id, remark, avatar_url, status, updater, created_at) values
(101, 'S0101', '周运营', '13800000101', '753951', 101, '运营主管账号', 'https://api.dicebear.com/7.x/initials/svg?seed=S0101', 1, '系统', now()),
(102, 'S0102', '吴商品', '13800000102', '753951', 102, '商品主管账号', 'https://api.dicebear.com/7.x/initials/svg?seed=S0102', 1, '系统', now()),
(103, 'S0103', '郑服务', '13800000103', '753951', 103, '服务主管账号', 'https://api.dicebear.com/7.x/initials/svg?seed=S0103', 1, '系统', now()),
(104, 'S0104', '王数据', '13800000104', '753951', 104, '数据分析账号', 'https://api.dicebear.com/7.x/initials/svg?seed=S0104', 1, '系统', now()),
(105, 'S0105', '冯财务', '13800000105', '753951', 105, '财务账号', 'https://api.dicebear.com/7.x/initials/svg?seed=S0105', 1, '系统', now()),
(106, 'S0106', '陈编辑', '13800000106', '753951', 106, '内容编辑账号', 'https://api.dicebear.com/7.x/initials/svg?seed=S0106', 1, '系统', now()),
(107, 'S0107', '杨顾问', '13800000107', '753951', 107, '健康顾问账号', 'https://api.dicebear.com/7.x/initials/svg?seed=S0107', 1, '系统', now());

insert ignore into service_personnel(id, name, phone, service_type, area, join_time, status, audit_status, created_at) values
(101, '何洁', '13900020101', '家政护理', '普陀区', date_sub(now(), interval 13 day), 1, '已通过', date_sub(now(), interval 13 day)),
(102, '罗军', '13900020102', '康复理疗', '宝山区', date_sub(now(), interval 12 day), 1, '已通过', date_sub(now(), interval 12 day)),
(103, '许芳', '13900020103', '上门体检', '嘉定区', date_sub(now(), interval 11 day), 1, '已通过', date_sub(now(), interval 11 day));

insert ignore into after_sale(id, order_id, applicant_id, reason, status, created_at) values
(101, 1, 10003, '体检时间冲突', '处理中', date_sub(now(), interval 1 day)),
(102, 2, 10001, '服务体验反馈', '已完成', date_sub(now(), interval 2 day)),
(103, 3, 10002, '康复师改期', '已关闭', date_sub(now(), interval 3 day)),
(104, 5, 10005, '陪诊发票问题', '处理中', date_sub(now(), interval 4 day)),
(105, 6, 10012, '理疗套餐调整', '处理中', date_sub(now(), interval 5 day)),
(106, 7, 10007, '报告补发', '已完成', date_sub(now(), interval 6 day)),
(107, 8, 10009, '服务地点变更', '处理中', date_sub(now(), interval 7 day)),
(108, 10, 10008, '体检项目变更', '已关闭', date_sub(now(), interval 8 day));

insert ignore into review(id, order_id, product_id, user_id, rating, content, visible, created_at) values
(101, 1, 3, 10003, 4, '体检安排清晰。', 1, date_sub(now(), interval 2 day)),
(102, 6, 6, 10012, 5, '理疗体验不错。', 1, date_sub(now(), interval 2 day)),
(103, 8, 8, 10009, 4, '陪护服务准时。', 1, date_sub(now(), interval 3 day));

insert ignore into medication_record(id, user_id, period, drug_name, frequency, take_time, dosage, reminder_enabled, source, creator, created_at) values
(101, 10002, '早餐', '阿托伐他汀', '每天', '08:00:00', '1片', 1, '手动', '系统管理员', now()),
(102, 10004, '午餐', '钙片', '每天', '12:30:00', '1片', 1, '手动', '系统管理员', now()),
(103, 10005, '晚餐', '硝酸甘油', '按需', '19:00:00', '1片', 1, '手动', '系统管理员', now()),
(104, 10006, '早餐', '布洛芬缓释胶囊', '每天', '08:30:00', '1粒', 0, '手动', '系统管理员', now()),
(105, 10007, '早餐', '二甲双胍', '每天', '08:00:00', '1片', 1, '手动', '系统管理员', now()),
(106, 10008, '晚餐', '辛伐他汀', '每天', '20:00:00', '1片', 1, '手动', '系统管理员', now()),
(107, 10009, '睡前', '维生素D', '每天', '21:00:00', '1粒', 1, '手动', '系统管理员', now());

insert ignore into agreement(id, title, type, content, status, updated_at) values
(101, '健康数据授权书', 'health_data', '健康数据授权书示例内容。', 1, now()),
(102, '上门服务须知', 'service_notice', '上门服务须知示例内容。', 1, now()),
(103, '优惠券使用规则', 'coupon_rule', '优惠券使用规则示例内容。', 1, now()),
(104, '会员等级规则', 'member_rule', '会员等级规则示例内容。', 1, now()),
(105, '积分规则说明', 'points_rule', '积分规则说明示例内容。', 1, now()),
(106, '活动报名协议', 'activity', '活动报名协议示例内容。', 1, now()),
(107, '隐私补充条款', 'privacy_ext', '隐私补充条款示例内容。', 1, now()),
(108, '报告查看授权', 'report_auth', '报告查看授权示例内容。', 1, now());

insert ignore into operation_log(id, operator, action_type, target, content, ip_address, created_at) values
(101, '运营主管', '编辑', '轮播图管理', '更新首页轮播图排序', '127.0.0.1', date_sub(now(), interval 1 day)),
(102, '商品主管', '新增', '服务项目', '新增肩颈护理服务项目', '127.0.0.1', date_sub(now(), interval 2 day)),
(103, '服务主管', '审核', '服务人员', '审核服务人员资质', '127.0.0.1', date_sub(now(), interval 3 day)),
(104, '内容编辑', '发布', '健康资讯', '发布慢病管理资讯', '127.0.0.1', date_sub(now(), interval 4 day)),
(105, '健康顾问', '更新', '健康设置', '更新用户健康阈值', '127.0.0.1', date_sub(now(), interval 5 day));

insert ignore into health_settings(id, user_id, heart_rate_upper, heart_rate_lower, blood_pressure_sys_upper, blood_pressure_dias_upper, blood_sugar_upper, blood_sugar_lower, step_goal, sleep_goal, medication_reminder, checkup_interval) values
(101, 10002, 108, 54, 148, 92, 7.6, 3.8, 6500, 7.0, 1, 120),
(102, 10004, 112, 56, 150, 95, 8.1, 3.9, 6000, 7.2, 1, 180),
(103, 10005, 106, 52, 145, 90, 7.5, 3.8, 5200, 7.5, 1, 90),
(104, 10006, 115, 55, 150, 95, 8.0, 3.9, 7000, 7.0, 0, 180),
(105, 10007, 105, 50, 142, 88, 7.2, 3.7, 5000, 7.5, 1, 60),
(106, 10008, 110, 55, 148, 92, 7.8, 3.8, 7200, 7.0, 1, 120),
(107, 10009, 104, 50, 140, 88, 7.0, 3.7, 4800, 7.8, 1, 90),
(108, 10010, 112, 55, 148, 92, 7.8, 3.8, 6500, 7.0, 1, 150);

insert ignore into device(id, user_id, device_name, device_type, device_code, bind_time, last_sync_time, status) values
(101, 10002, '华为手表 GT4', 'watch', 'HW-GT4-10002', date_sub(now(), interval 16 day), now(), 1),
(102, 10004, '智能体脂秤', 'scale', 'SC-10004', date_sub(now(), interval 14 day), date_sub(now(), interval 2 hour), 1),
(103, 10005, '血压计 Pro', 'pressure', 'BP-10005', date_sub(now(), interval 13 day), now(), 1),
(104, 10006, '康复训练仪', 'rehab', 'RH-10006', date_sub(now(), interval 12 day), date_sub(now(), interval 3 hour), 1),
(105, 10007, '血糖仪 Plus', 'glucometer', 'GLU-10007', date_sub(now(), interval 11 day), now(), 1),
(106, 10008, '血氧夹', 'oxygen', 'OX-10008', date_sub(now(), interval 10 day), now(), 1),
(107, 10009, '定位手环', 'band', 'GPS-10009', date_sub(now(), interval 9 day), date_sub(now(), interval 1 hour), 1),
(108, 10010, '睡眠监测垫', 'sleep', 'SL-10010', date_sub(now(), interval 8 day), now(), 1);

insert ignore into report(id, user_id, title, report_type, report_date, file_url, summary, doctor_name) values
(101, 10002, '康复训练阶段报告', '康复评估', date_sub(curdate(), interval 1 day), '', '肩颈活动度改善。', '赵医生'),
(102, 10004, '骨密度复查报告', '体检', date_sub(curdate(), interval 2 day), '', '骨密度略低，建议补钙。', '刘医生'),
(103, 10005, '冠心病随访报告', '慢病随访', date_sub(curdate(), interval 3 day), '', '心率平稳，继续观察。', '陈医生'),
(104, 10006, '术后康复月报', '月度总结', date_sub(curdate(), interval 4 day), '', '膝关节恢复良好。', '李医生'),
(105, 10007, '血糖管理报告', '健康评估', date_sub(curdate(), interval 5 day), '', '餐后血糖仍需控制。', '王医生'),
(106, 10008, '血脂复查报告', '体检', date_sub(curdate(), interval 6 day), '', '低脂饮食有效。', '周医生'),
(107, 10009, '跌倒风险报告', '风险评估', date_sub(curdate(), interval 7 day), '', '建议增加居家扶手。', '许医生'),
(108, 10010, '腰椎护理报告', '康复评估', date_sub(curdate(), interval 8 day), '', '疼痛评分下降。', '黄医生');

insert ignore into coupon(id, user_id, coupon_no, name, type, discount, min_amount, status, expire_date) values
(101, 10002, 'CP202607101', '康复训练减免券', '满减', 40.00, 299.00, '未使用', date_add(curdate(), interval 25 day)),
(102, 10004, 'CP202607102', '日常护理券', '现金', 20.00, 0.00, '未使用', date_add(curdate(), interval 28 day)),
(103, 10005, 'CP202607103', '陪诊服务券', '满减', 30.00, 169.00, '已使用', date_add(curdate(), interval 18 day)),
(104, 10006, 'CP202607104', '理疗折扣券', '折扣', 8.50, 0.00, '未使用', date_add(curdate(), interval 40 day)),
(105, 10007, 'CP202607105', '体检专项券', '满减', 50.00, 499.00, '未使用', date_add(curdate(), interval 35 day)),
(106, 10008, 'CP202607106', '心脑专项券', '满减', 80.00, 699.00, '未使用', date_add(curdate(), interval 45 day)),
(107, 10009, 'CP202607107', '半日陪护券', '满减', 60.00, 329.00, '已过期', date_sub(curdate(), interval 1 day)),
(108, 10010, 'CP202607108', '清洁服务券', '现金', 15.00, 0.00, '未使用', date_add(curdate(), interval 20 day));

insert ignore into user_points(id, user_id, points, total_earned, total_spent, level, growth_value) values
(101, 10002, 960, 1200, 240, '普通', 980),
(102, 10004, 720, 900, 180, '普通', 760),
(103, 10005, 2200, 2600, 400, '银卡', 3600),
(104, 10006, 1560, 1900, 340, '银卡', 2800),
(105, 10007, 1180, 1400, 220, '银卡', 2100),
(106, 10008, 3280, 3800, 520, '金卡', 6200),
(107, 10009, 640, 760, 120, '普通', 680),
(108, 10010, 1460, 1720, 260, '银卡', 2400);

insert ignore into points_record(id, user_id, change_value, reason, created_at) values
(101, 10002, 60, '康复打卡', date_sub(now(), interval 1 day)),
(102, 10004, 30, '完成资料完善', date_sub(now(), interval 2 day)),
(103, 10005, 100, '完成陪诊订单', date_sub(now(), interval 3 day)),
(104, 10006, 120, '康复训练达标', date_sub(now(), interval 4 day)),
(105, 10007, 50, '血糖记录上传', date_sub(now(), interval 5 day)),
(106, 10008, 180, '完成专项体检', date_sub(now(), interval 6 day)),
(107, 10009, -30, '兑换护理券', date_sub(now(), interval 7 day));

insert ignore into member_level(id, name, min_growth, max_growth, icon, benefits, status) values
(101, '铜卡', 500, 999, '', '基础积分加速', 1),
(102, '铂金卡', 8000, 14999, '', '专属健康顾问', 1),
(103, '钻石卡', 15000, null, '', '年度体检礼包', 1),
(104, '关怀会员', 0, 1999, '', '慢病提醒服务', 1),
(105, '康复会员', 2000, 5999, '', '康复训练优惠', 1),
(106, '体检会员', 3000, 6999, '', '体检套餐优惠', 1),
(107, '家庭会员', 5000, 12000, '', '家庭账号权益', 1);

insert ignore into points_rule(id, action_type, description, points, growth, daily_limit, status) values
(101, 'health_record', '上传健康数据', 10, 10, 5, 1),
(102, 'medication_check', '用药打卡', 8, 8, 3, 1),
(103, 'activity_enroll', '报名活动', 20, 20, 2, 1),
(104, 'activity_attend', '参加活动', 50, 60, 1, 1),
(105, 'profile_complete', '完善资料', 30, 30, 1, 1),
(106, 'invite_family', '邀请家属', 80, 100, 1, 1),
(107, 'service_finish', '完成服务', 100, 120, null, 1);

delete from member_level where name not in ('普通', '银卡', '金卡');
delete from points_rule where action_type not in ('signin', 'order', 'review');

insert ignore into product_category(id, name, code, description, sort_order, status) values
(101, '助浴护理', 'HK-BATH', '助浴和清洁护理', 4, 1),
(102, '陪诊陪护', 'HK-CARE', '陪诊和陪护服务', 5, 1),
(103, '日常保洁', 'HK-CLEAN', '居家保洁服务', 6, 1),
(104, '术后康复', 'RH-POST', '术后康复训练', 7, 1),
(105, '肩颈理疗', 'RH-NECK', '肩颈理疗项目', 8, 1),
(106, '慢病体检', 'EX-CHRONIC', '慢病随访体检', 9, 1),
(107, '专项体检', 'EX-SPECIAL', '专项健康检查', 10, 1);

insert ignore into service_item(id, product_id, name, description, duration, price, status) values
(101, 5, '陪诊前沟通', '确认就诊信息与注意事项', 20, 0.00, 1),
(102, 5, '全程陪诊', '挂号、排队、取药陪同', 120, 169.00, 1),
(103, 6, '肩周炎评估', '活动度和疼痛评估', 20, 59.00, 1),
(104, 6, '肩周炎理疗', '热敷、拉伸和训练', 60, 259.00, 1),
(105, 7, '慢病指标采集', '血压血糖等基础采集', 40, 199.00, 1),
(106, 8, '半日陪护', '居家半日陪护服务', 240, 329.00, 1),
(107, 10, '心脑血管检测', '专项检测和报告解读', 90, 699.00, 1);

insert ignore into banner(id, title, image_url, link_url, sort_order, location, status) values
(101, '康复理疗套餐', 'https://example.com/banner-rehab.png', '/products/rehab', 3, 'home', 1),
(102, '助浴护理安心选', 'https://example.com/banner-bath.png', '/products/bath', 4, 'home', 1),
(103, '健康讲堂开课', 'https://example.com/banner-video.png', '/operations/videos', 5, 'home', 1),
(104, '银发运动会报名', 'https://example.com/banner-sport.png', '/operations/activities', 6, 'activity', 1),
(105, '慢病随访提醒', 'https://example.com/banner-chronic.png', '/articles/chronic', 7, 'home', 1),
(106, '体检套餐优惠', 'https://example.com/banner-checkup.png', '/products/checkup', 8, 'home', 1),
(107, '食谱推荐', 'https://example.com/banner-recipe.png', '/operations/recipes', 9, 'home', 1),
(108, '机构参观预约', 'https://example.com/banner-institution.png', '/operations/institutions', 10, 'home', 1);

insert ignore into activity(id, title, cover_url, location, start_time, end_time, quota, enrolled, status, content) values
(101, '慢病管理讲座', '', '浦东社区中心', date_add(now(), interval 2 day), date_add(now(), interval 2 day), 60, 22, 'published', '慢病用药和饮食管理。'),
(102, '营养早餐课堂', '', '静安活动室', date_add(now(), interval 3 day), date_add(now(), interval 3 day), 40, 18, 'published', '控糖早餐搭配。'),
(103, '防跌倒训练营', '', '徐汇康复室', date_add(now(), interval 4 day), date_add(now(), interval 4 day), 35, 16, 'published', '居家防跌倒训练。'),
(104, '智能设备教学', '', '线上直播', date_add(now(), interval 5 day), date_add(now(), interval 5 day), 100, 42, 'published', '手环和血压计使用教学。'),
(105, '家属照护课堂', '', '黄浦社区中心', date_add(now(), interval 6 day), date_add(now(), interval 6 day), 80, 33, 'published', '家庭照护注意事项。'),
(106, '康复经验分享', '', '长宁社区中心', date_add(now(), interval 7 day), date_add(now(), interval 7 day), 50, 24, 'published', '康复训练案例分享。'),
(107, '健康义剪日', '', '宝山社区驿站', date_add(now(), interval 8 day), date_add(now(), interval 8 day), 70, 31, 'published', '便民服务活动。'),
(108, '睡眠改善沙龙', '', '线上直播', date_add(now(), interval 9 day), date_add(now(), interval 9 day), 90, 29, 'published', '睡眠质量改善建议。');

insert ignore into activity_enroll(id, activity_id, user_id, status, remark) values
(101, 101, 10002, 'enrolled', '携带血压记录'),
(102, 102, 10003, 'enrolled', '关注控糖饮食'),
(103, 103, 10004, 'attended', '已签到'),
(104, 104, 10005, 'enrolled', '线上参加'),
(105, 105, 10006, 'cancelled', '时间冲突'),
(106, 106, 10007, 'enrolled', '康复训练咨询'),
(107, 107, 10008, 'attended', '已签到'),
(108, 108, 10009, 'enrolled', '睡眠较差');

insert ignore into elderly_message(id, user_id, content, status, created_at, updated_at) values
(201, 10001, '请帮忙确认本周助浴服务的上门时间。', 'pending', date_sub(now(), interval 2 hour), date_sub(now(), interval 2 hour)),
(202, 10001, '上次服务体验很好，谢谢工作人员。', 'resolved', date_sub(now(), interval 2 day), date_sub(now(), interval 1 day)),
(203, 10002, '康复训练工单想调整到下午。', 'processing', date_sub(now(), interval 1 hour), date_sub(now(), interval 30 minute));

insert ignore into topic(id, name, description, icon, post_count, status) values
(101, '低盐饮食', '低盐饮食经验交流', '', 18, 1),
(102, '控糖生活', '控糖饮食和运动', '', 21, 1),
(103, '居家康复', '居家康复训练分享', '', 15, 1),
(104, '睡眠改善', '睡眠质量改善交流', '', 9, 1),
(105, '家属陪护', '家属照护经验', '', 13, 1),
(106, '健康设备', '智能设备使用', '', 7, 1),
(107, '社区活动', '社区活动报名交流', '', 16, 1),
(108, '营养食谱', '营养菜谱分享', '', 19, 1);

insert ignore into recipe(id, name, category, ingredients, steps, calories, suitable_for, cover_url, status) values
(101, '番茄豆腐汤', '晚餐', '番茄,豆腐', '切块;炖煮', 180, '高血压', '', 1),
(102, '鸡胸蔬菜饭', '午餐', '鸡胸肉,西兰花,米饭', '蒸煮;装盘', 420, '康复护理', '', 1),
(103, '南瓜小米粥', '早餐', '南瓜,小米', '熬煮', 260, '消化不良', '', 1),
(104, '清炒菠菜', '午餐', '菠菜,蒜', '焯水;快炒', 120, '高血脂', '', 1),
(105, '银耳莲子羹', '加餐', '银耳,莲子', '泡发;慢炖', 210, '睡眠较差', '', 1),
(106, '低脂鸡蛋羹', '早餐', '鸡蛋,温水', '打散;蒸制', 160, '高血脂', '', 1),
(107, '紫薯燕麦杯', '早餐', '紫薯,燕麦', '蒸熟;混合', 300, '糖尿病', '', 1),
(108, '山药排骨汤', '午餐', '山药,排骨', '焯水;炖煮', 360, '康复护理', '', 1);

insert ignore into article(id, title, summary, content, cover_url, author, category, tags, view_count, status) values
(101, '老人防跌倒指南', '居家防跌倒要点。', '保持地面干燥，安装扶手。', '', '健康编辑', '护理', '防跌倒', 720, 1),
(102, '控糖饮食三原则', '控糖饮食基础建议。', '控制总量，选择低GI食物。', '', '营养师', '营养', '控糖', 830, 1),
(103, '康复训练注意事项', '训练强度和频率建议。', '循序渐进，避免过度疲劳。', '', '康复师', '康复', '训练', 650, 1),
(104, '高血脂日常管理', '饮食与运动管理。', '减少饱和脂肪摄入。', '', '健康编辑', '疾病', '高血脂', 590, 1),
(105, '老人睡眠改善', '改善睡眠质量。', '固定作息，减少午睡过长。', '', '健康编辑', '养生', '睡眠', 610, 1),
(106, '家属照护清单', '照护前准备事项。', '记录用药和紧急联系人。', '', '运营中心', '照护', '家属', 540, 1),
(107, '智能手环使用教程', '设备绑定与同步。', '打开蓝牙并完成授权。', '', '设备顾问', '设备', '手环', 470, 1),
(108, '夏季用药提醒', '高温天气用药注意。', '药品保存避免高温潮湿。', '', '药师', '用药', '夏季', 760, 1);

insert ignore into disease(id, name, category, summary, symptoms, prevention, diet_advice, risk_factors, status) values
(101, '高血脂', '心血管', '血脂异常需长期管理。', '乏力,头晕', '低脂饮食,运动', '少油少糖', '肥胖,饮酒', 1),
(102, '冠心病', '心血管', '冠状动脉供血不足。', '胸闷,胸痛', '控制血压血脂', '清淡饮食', '高血压,吸烟', 1),
(103, '骨质疏松', '骨科', '骨量降低导致骨折风险升高。', '腰背痛', '补钙,晒太阳', '高钙饮食', '高龄,缺钙', 1),
(104, '肩周炎', '骨科', '肩关节疼痛和活动受限。', '肩痛,抬手困难', '规律拉伸', '高蛋白', '久坐,劳损', 1),
(105, '脑卒中康复', '神经', '脑卒中后功能恢复。', '肢体无力', '康复训练', '低盐低脂', '高血压', 1),
(106, '阿尔茨海默', '神经', '认知功能下降。', '记忆下降', '认知训练', '均衡饮食', '高龄', 1),
(107, '慢阻肺', '呼吸', '慢性气道疾病。', '咳嗽,气短', '戒烟,肺康复', '高蛋白', '吸烟', 1),
(108, '失眠', '心理睡眠', '睡眠质量下降。', '入睡困难', '规律作息', '少咖啡因', '焦虑,作息紊乱', 1);

insert ignore into institution(id, name, address, phone, type, rating, capacity, price_range, services, images, status) values
(101, '徐汇日间照料中心', '徐汇区康健路12号', '021-88880101', '日间照料', 4.5, 60, '3000-6000/月', '照料,餐饮', '', 1),
(102, '长宁康复护理院', '长宁区虹桥路88号', '021-88880102', '护理院', 4.7, 100, '6000-12000/月', '护理,康复', '', 1),
(103, '黄浦社区养老站', '黄浦区人民路66号', '021-88880103', '社区', 4.3, 45, '2000-5000/月', '助餐,活动', '', 1),
(104, '宝山颐养中心', '宝山区友谊路99号', '021-88880104', '养老院', 4.6, 130, '4500-8500/月', '护理,娱乐', '', 1),
(105, '普陀康养驿站', '普陀区金沙江路18号', '021-88880105', '社区', 4.4, 50, '2500-5500/月', '助浴,助餐', '', 1),
(106, '嘉定护理之家', '嘉定区安亭路28号', '021-88880106', '护理院', 4.5, 80, '5000-9500/月', '医护,护理', '', 1),
(107, '闵行颐康院', '闵行区莘庄路56号', '021-88880107', '养老院', 4.2, 110, '4000-8000/月', '护理,康复', '', 1),
(108, '松江银龄中心', '松江区文诚路77号', '021-88880108', '养老院', 4.8, 140, '5500-10000/月', '护理,餐饮,康复', '', 1);

insert ignore into video(id, title, description, cover_url, video_url, duration, lecturer, category, view_count, status) values
(101, '防跌倒训练', '居家防跌倒训练动作。', '', 'https://example.com/video-fall.mp4', 840, '赵康复师', '康复', 390, 1),
(102, '控糖饮食课', '糖尿病饮食控制。', '', 'https://example.com/video-sugar.mp4', 960, '王营养师', '营养', 510, 1),
(103, '血压计使用教程', '家庭血压计正确使用。', '', 'https://example.com/video-bp.mp4', 480, '李护士', '设备', 620, 1),
(104, '助浴安全须知', '老人助浴安全要点。', '', 'https://example.com/video-bath.mp4', 720, '周护理师', '护理', 430, 1),
(105, '肩周炎拉伸', '肩周炎居家拉伸。', '', 'https://example.com/video-shoulder.mp4', 780, '罗康复师', '康复', 360, 1),
(106, '睡眠放松训练', '睡前放松练习。', '', 'https://example.com/video-sleep.mp4', 900, '心理顾问', '睡眠', 280, 1),
(107, '家属照护课程', '家属照护基础。', '', 'https://example.com/video-family.mp4', 1100, '陈护士', '照护', 330, 1),
(108, '低盐烹饪课', '低盐烹饪技巧。', '', 'https://example.com/video-salt.mp4', 640, '刘营养师', '营养', 450, 1);

insert ignore into food(id, name, category, calories, protein, fat, carbs, fiber, sodium, suitable_for, status) values
(101, '西兰花', '蔬菜', 36, 4.1, 0.6, 4.3, 2.6, 18.0, '高血压,糖尿病', 1),
(102, '鸡胸肉', '肉类', 133, 24.6, 1.9, 2.5, 0.0, 34.0, '康复护理', 1),
(103, '豆腐', '豆制品', 84, 6.6, 5.3, 3.4, 0.4, 7.2, '高血脂', 1),
(104, '紫薯', '主食', 106, 1.6, 0.2, 25.1, 3.0, 27.0, '糖尿病', 1),
(105, '菠菜', '蔬菜', 28, 2.6, 0.3, 4.5, 1.7, 85.0, '高血压', 1),
(106, '牛奶', '乳制品', 66, 3.2, 3.6, 5.0, 0.0, 37.0, '骨质疏松', 1),
(107, '三文鱼', '鱼类', 139, 17.2, 7.8, 0.0, 0.0, 50.0, '高血脂', 1),
(108, '苹果', '水果', 53, 0.4, 0.2, 13.7, 1.2, 1.6, '普通客户', 1);

insert ignore into assessment(id, title, description, type, questions, scoring_rules, status) values
(101, '慢病自评', '慢病管理能力自评。', 'chronic', '[\"是否按时服药\",\"是否定期监测\"]', '{}', 1),
(102, '营养风险测评', '饮食结构风险评估。', 'nutrition', '[\"每日蔬菜摄入\",\"蛋白质摄入\"]', '{}', 1),
(103, '认知风险测评', '认知能力初筛。', 'cognition', '[\"日期记忆\",\"词语回忆\"]', '{}', 1),
(104, '运动能力测评', '基础运动能力评估。', 'sport', '[\"步行距离\",\"起坐能力\"]', '{}', 1),
(105, '用药依从性测评', '用药规律性评估。', 'medication', '[\"是否漏服\",\"是否自行停药\"]', '{}', 1),
(106, '情绪状态测评', '近期情绪状态评估。', 'mood', '[\"是否焦虑\",\"是否低落\"]', '{}', 1),
(107, '照护需求测评', '居家照护需求评估。', 'care', '[\"是否需要助浴\",\"是否需要陪诊\"]', '{}', 1),
(108, '居家安全测评', '居家环境安全评估。', 'home', '[\"地面是否防滑\",\"夜间照明是否充足\"]', '{}', 1);

insert ignore into assessment_result(id, assessment_id, user_id, score, result, answers) values
(101, 101, 10002, 66, '慢病管理一般', '{}'),
(102, 102, 10004, 58, '营养风险中等', '{}'),
(103, 103, 10005, 42, '认知风险较低', '{}'),
(104, 104, 10006, 74, '运动能力良好', '{}'),
(105, 105, 10007, 61, '用药依从性一般', '{}'),
(106, 106, 10008, 36, '情绪状态稳定', '{}'),
(107, 107, 10009, 82, '照护需求较高', '{}'),
(108, 108, 10010, 69, '居家安全需改善', '{}');

update user_tag t set user_count = (select count(*) from user_tag_rel r where r.tag_id = t.id);

update staff
set phone = '13402832834',
    role_id = 1,
    status = 1
where id = 1;

update role set permissions = '{"*":["*"]}' where id = 1;
update role set permissions = '{"dashboard":["view","edit"],"users":["view","edit","delete"],"service":["view","edit"],"products":["view","edit"],"trade":["view","edit"],"operations":["view","edit"],"analytics":["view"],"system":["view"],"settings":["view","edit"]}' where id = 2;
update role set permissions = '{"dashboard":["view"],"users":["view"],"service":["view","edit"],"trade":["view","edit"],"products":["view"],"operations":["view"],"analytics":["view"]}' where id = 3;

insert into account(id, phone, password_hash, role_type, nickname, avatar_url, status, created_at, updated_at)
select id, phone, password_hash, 'staff', name, avatar_url, status, created_at, updated_at from staff
on duplicate key update
  phone = values(phone),
  nickname = values(nickname),
  avatar_url = values(avatar_url),
  status = values(status),
  updated_at = values(updated_at);

insert into admin_profile(account_id, staff_no, real_name, role_id, remark, updated_at)
select id, staff_no, name, role_id, remark, updated_at from staff
on duplicate key update
  staff_no = values(staff_no),
  real_name = values(real_name),
  role_id = values(role_id),
  remark = values(remark),
  updated_at = values(updated_at);

insert into account(id, phone, password_hash, role_type, nickname, avatar_url, status, last_login_time, created_at, updated_at)
select id, phone, '753951', 'elderly', nickname, avatar_url, status, last_login_time, created_at, updated_at from `user`
on duplicate key update
  phone = values(phone),
  nickname = values(nickname),
  avatar_url = values(avatar_url),
  status = values(status),
  last_login_time = values(last_login_time),
  updated_at = values(updated_at);

insert into elderly_profile(
  account_id, legacy_user_id, real_name, gender, birthday, id_card, address, bio, height, weight,
  ethnicity, education, blood_type, rh_negative, chronic_disease, sleep_quality, smoking_freq,
  drinking_freq, exercise_freq, diet_preference, emergency_contact, emergency_phone, last_buy_time
)
select id, id, real_name, gender, birthday, id_card, address, bio, height, weight,
       ethnicity, education, blood_type, rh_negative, chronic_disease, sleep_quality, smoking_freq,
       drinking_freq, exercise_freq, diet_preference, emergency_contact, emergency_phone, last_buy_time
from `user`
on duplicate key update
  real_name = values(real_name),
  gender = values(gender),
  birthday = values(birthday),
  address = values(address),
  bio = values(bio),
  height = values(height),
  weight = values(weight),
  chronic_disease = values(chronic_disease),
  emergency_contact = values(emergency_contact),
  emergency_phone = values(emergency_phone),
  last_buy_time = values(last_buy_time);

update service_personnel
set phone = '13900020001',
    status = 1,
    audit_status = '已通过'
where id = 1;

insert into account(id, phone, password_hash, role_type, nickname, avatar_url, status, created_at, updated_at)
select 200000 + id, phone, '753951', 'service', name, avatar_url, status, created_at, created_at from service_personnel
on duplicate key update
  phone = values(phone),
  role_type = values(role_type),
  nickname = values(nickname),
  avatar_url = values(avatar_url),
  status = values(status),
  updated_at = values(updated_at);

insert into service_profile(account_id, legacy_personnel_id, real_name, service_type, area, join_time, audit_status)
select 200000 + id, id, name, service_type, area, join_time, audit_status from service_personnel
on duplicate key update
  real_name = values(real_name),
  service_type = values(service_type),
  area = values(area),
  join_time = values(join_time),
  audit_status = values(audit_status);

insert into product(name, code, item_type, category, description, duration, price, status, updater, created_at)
select s.name, concat('SVC-', s.id), '服务', coalesce(p.category, '其他'), s.description, s.duration,
       coalesce(s.price, 0), s.status, '服务项目迁移', s.created_at
from service_item s
left join product p on p.id = s.product_id
on duplicate key update
  name = values(name),
  item_type = values(item_type),
  category = values(category),
  description = values(description),
  duration = values(duration),
  price = values(price),
  status = values(status),
  updater = values(updater);

update work_order w
join service_order o on o.id = w.order_id
set w.product_id = o.product_id
where w.product_id is null;

update work_order w
join product p on p.name = w.service_item
set w.product_id = p.id
where w.product_id is null;

-- 工单保存创建时的服务时长快照，避免以后修改商品时长影响历史预约。
update work_order w
left join product p on p.id = w.product_id
set w.service_duration = coalesce(nullif(p.duration, 0), 60)
where not exists (
  select 1 from data_migration m
  where m.migration_key = '20260715_snapshot_work_order_service_duration'
);

insert ignore into data_migration(migration_key)
values ('20260715_snapshot_work_order_service_duration');

update work_order w
set w.created_by_account_id = null,
    w.created_by_role = null
where w.created_by_role = 'elderly'
  and w.created_by_account_id = w.customer_id
  and not exists (
    select 1 from data_migration m
    where m.migration_key = '20260710_clear_inferred_work_order_creators'
  );

insert ignore into data_migration(migration_key)
values ('20260710_clear_inferred_work_order_creators');

-- 一单一工单迁移：同一交易订单下的额外工单各自获得一份完整订单副本。
drop temporary table if exists tmp_extra_work_orders;
create temporary table tmp_extra_work_orders as
select ranked.work_order_id,
       ranked.original_order_id,
       concat('ODM-', left(sha2(concat(
         'one-to-one:', ranked.work_order_id, ':', ranked.work_order_no, ':', ranked.original_order_id
       ), 256), 26)) as migration_order_no
from (
  select w.id as work_order_id,
         w.order_id as original_order_id,
         w.order_no as work_order_no,
         row_number() over (partition by w.order_id order by w.id) as row_number_in_order
  from work_order w
) ranked
where ranked.row_number_in_order > 1;

insert ignore into service_order(
  order_no, product_id, product_name, amount, buyer_id, status, service_type, created_at
)
select extra.migration_order_no,
       coalesce(w.product_id, original_order.product_id),
       w.service_item,
       w.amount,
       w.customer_id,
       case w.status
         when 'completed' then 'completed'
         when 'cancelled' then 'closed'
         else 'pending_service'
       end,
       original_order.service_type,
       w.created_at
from tmp_extra_work_orders extra
join work_order w on w.id = extra.work_order_id
join service_order original_order on original_order.id = extra.original_order_id;

update work_order w
join tmp_extra_work_orders extra on extra.work_order_id = w.id
join service_order migrated_order on migrated_order.order_no = extra.migration_order_no
set w.order_id = migrated_order.id;

drop temporary table if exists tmp_extra_work_orders;

-- 只有交易订单而没有履约工单时，补建一张待分派工单，不自动选择服务人员。
insert ignore into work_order(
  order_no, order_id, product_id, service_item, amount, personnel_id, customer_id,
  created_by_account_id, created_by_role, status, dispatch_time, service_time,
  complete_time, cancel_reason, created_at
)
select concat('WOM-', left(sha2(concat('one-to-one:', o.id, ':', o.order_no), 256), 26)),
       o.id,
       o.product_id,
       o.product_name,
       o.amount,
       null,
       o.buyer_id,
       null,
       null,
       case o.status
         when 'completed' then 'completed'
         when 'closed' then 'cancelled'
         else 'pending'
       end,
       o.created_at,
       null,
       case when o.status = 'completed' then o.created_at else null end,
       null,
       o.created_at
from service_order o
where not exists (
  select 1 from work_order w where w.order_id = o.id
);

-- 数据修复完成后由数据库兜底，禁止再次出现一张订单对应多张工单。
set @add_one_order_one_work_order = if(
  (select count(*) from information_schema.statistics
   where table_schema = database()
     and table_name = 'work_order'
     and index_name = 'uk_work_order_order_id') = 0,
  'alter table work_order add unique key uk_work_order_order_id(order_id)',
  'select 1'
);
prepare stmt from @add_one_order_one_work_order;
execute stmt;
deallocate prepare stmt;

insert ignore into data_migration(migration_key)
values ('20260714_one_order_one_work_order');

-- 看板日期只按稳定的工单编号刷新，不再改写已经修复的一对一订单关联。
update work_order
set service_time = case order_no
  when 'WOBOARD-DAY-0' then timestamp(curdate(), '09:00:00')
  when 'WOBOARD-DAY-1' then timestamp(date_add(curdate(), interval 1 day), '10:00:00')
  when 'WOBOARD-DAY-2' then timestamp(date_add(curdate(), interval 2 day), '13:00:00')
  when 'WOBOARD-DAY-3' then timestamp(date_add(curdate(), interval 3 day), '09:30:00')
  when 'WOBOARD-DAY-4' then timestamp(date_add(curdate(), interval 4 day), '13:30:00')
  when 'WOBOARD-DAY-5' then timestamp(date_add(curdate(), interval 5 day), '10:00:00')
  when 'WOBOARD-DAY-6' then timestamp(date_add(curdate(), interval 6 day), '14:30:00')
  else service_time
end
where order_no in (
  'WOBOARD-DAY-0', 'WOBOARD-DAY-1', 'WOBOARD-DAY-2', 'WOBOARD-DAY-3',
  'WOBOARD-DAY-4', 'WOBOARD-DAY-5', 'WOBOARD-DAY-6'
);
