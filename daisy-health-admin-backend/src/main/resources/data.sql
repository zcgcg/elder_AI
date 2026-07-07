insert ignore into role(id, name, description, permissions, created_at) values
(1, '超级管理员', '全部模块与全部操作', '{"*":["*"]}', now()),
(2, '管理员', '日常运营管理', '{"dashboard":["view"],"users":["view","edit"],"orders":["view"]}', now()),
(3, '客服专员', '工单、订单、售后处理', '{"workOrders":["view","edit"],"orders":["view"],"afterSales":["view","edit"]}', now());

insert ignore into staff(id, staff_no, name, phone, password_hash, role_id, remark, status, updater, created_at) values
(1, 'S0001', '系统管理员', '13800000000', 'admin123', 1, '初始化管理员', 1, '系统', now()),
(2, 'S0002', '运营专员', '13800000001', 'admin123', 2, '内容运营', 1, '系统', now()),
(3, 'S0003', '客服专员', '13800000002', 'admin123', 3, '客户服务', 1, '系统', now());

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

update staff set avatar_url = 'https://api.dicebear.com/7.x/initials/svg?seed=DAISY', remark = coalesce(remark, '初始化管理员') where id = 1;

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
(12, 'WO20260702002', 12, '助浴护理', 199.00, 4, 10011, 'cancelled', date_sub(now(), interval 7 day), date_sub(now(), interval 7 day), null, date_sub(now(), interval 7 day));

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
