package com.daisy.health.service;

import com.daisy.health.common.PageResult;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Profile("mysql")
public class JdbcAdminDataService implements AdminDataService {
    private final JdbcTemplate jdbcTemplate;
    private final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("MM-dd");

    public JdbcAdminDataService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<String, Object> login(Map<String, Object> payload) {
        String phone = stringValue(payload.get("phone"));
        String password = stringValue(payload.get("password"));
        if (phone.length() == 0 || password.length() == 0) {
            throw new IllegalArgumentException("账号或密码不能为空");
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select s.id, s.staff_no as staffNo, s.name, s.phone, s.avatar_url as avatarUrl, s.remark, r.name as role " +
                        "from staff s left join role r on s.role_id = r.id where s.phone = ? and s.status = 1 limit 1",
                phone
        );
        Map<String, Object> user = rows.isEmpty()
                ? record("id", 1, "staffNo", "S0001", "name", "系统管理员", "phone", phone, "role", "超级管理员", "avatarUrl", "", "remark", "初始化管理员")
                : rows.get(0);
        return record("token", "db-token-admin", "user", user);
    }

    @Override
    public Map<String, Object> profile() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select s.id, s.staff_no as staffNo, s.name, s.phone, s.avatar_url as avatarUrl, s.remark, r.name as role, date_format(s.updated_at, '%Y-%m-%d %H:%i:%s') as updatedAt " +
                        "from staff s left join role r on s.role_id = r.id where s.status = 1 order by s.id limit 1"
        );
        if (rows.isEmpty()) {
            return record("id", 1, "staffNo", "S0001", "name", "系统管理员", "phone", "13800000000", "role", "超级管理员", "avatarUrl", "", "remark", "初始化管理员");
        }
        return rows.get(0);
    }

    @Override
    public Map<String, Object> updateProfile(Map<String, Object> payload) {
        Long id = longValue(payload, "id", firstId("staff"));
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        putIfPresent(values, "staff_no", payload, "staffNo");
        putIfPresent(values, "name", payload, "name");
        putIfPresent(values, "phone", payload, "phone");
        putIfPresent(values, "remark", payload, "remark");
        putIfPresent(values, "avatar_url", payload, "avatarUrl");
        if (payload.containsKey("roleId")) {
            values.put("role_id", longValue(payload, "roleId", firstId("role")));
        } else if (payload.containsKey("role")) {
            values.put("role_id", roleIdByName(text(payload, "role", "超级管理员")));
        }
        values.put("updater", "系统管理员");
        updateById("staff", id, values);
        accepted("updateProfile:" + id);
        return profile();
    }

    @Override
    public Map<String, Object> dashboard() {
        return record(
                "metrics", list(
                        metric("新增用户", count("select count(*) from `user` where created_at >= date_sub(curdate(), interval 7 day)"), "+12.4%", "success"),
                        metric("新增工单", count("select count(*) from work_order where created_at >= date_sub(curdate(), interval 7 day)"), "+8.7%", "warning"),
                        metric("新增订单", count("select count(*) from service_order where created_at >= date_sub(curdate(), interval 7 day)"), "+16.1%", "success"),
                        metric("新增动态", count("select count(*) from operation_content where type = 'posts' and created_at >= date_sub(curdate(), interval 7 day)"), "-3.2%", "danger")
                ),
                "tagDistribution", jdbcTemplate.queryForList(
                        "select t.name, count(rel.user_id) as value " +
                                "from user_tag t left join user_tag_rel rel on t.id = rel.tag_id " +
                                "where t.status = 1 group by t.id, t.name order by value desc, t.id limit 8"
                ),
                "serviceShare", jdbcTemplate.queryForList(
                        "select service_type as name, count(*) as value from service_order group by service_type order by value desc"
                ),
                "trend", trend()
        );
    }

    @Override
    public List<Map<String, Object>> appointments() {
        return jdbcTemplate.queryForList(
                "select w.id, hour(w.service_time) as hour, w.service_item as serviceName, " +
                        "concat(date_format(w.service_time, '%H:%i'), '-', date_format(coalesce(w.complete_time, date_add(w.service_time, interval 1 hour)), '%H:%i')) as timeRange, " +
                        "u.real_name as userName, " +
                        "case w.status when 'pending' then '待服务' when 'service_in' then '服务中' when 'completed' then '已完成' when 'cancelled' then '已取消' else w.status end as status " +
                        "from work_order w left join `user` u on w.customer_id = u.id " +
                        "where date(w.service_time) = curdate() order by w.service_time"
        );
    }

    @Override
    public PageResult<Map<String, Object>> users(String keyword) {
        String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select u.id, u.nickname, u.real_name as realName, u.phone, u.avatar_url as avatarUrl, date_format(u.created_at, '%Y-%m-%d %H:%i:%s') as createdAt, " +
                        "group_concat(t.id order by t.id separator ',') as tagIds, group_concat(t.name order by t.id separator ',') as tags " +
                        "from `user` u " +
                        "left join user_tag_rel rel on u.id = rel.user_id " +
                        "left join user_tag t on rel.tag_id = t.id " +
                        "where (? = '%%' or u.nickname like ? or u.real_name like ? or u.phone like ?) " +
                        "group by u.id, u.nickname, u.real_name, u.phone, u.avatar_url, u.created_at order by u.created_at desc",
                like, like, like, like
        );
        normalizeTags(rows);
        normalizeTagIds(rows);
        return new PageResult<Map<String, Object>>(rows.size(), rows);
    }

    @Override
    public Map<String, Object> createUser(Map<String, Object> payload) {
        require(payload, "realName", "真实姓名不能为空");
        String realName = text(payload, "realName", "");
        String nickname = text(payload, "nickname", realName);
        String phone = text(payload, "phone", "138" + uniqueDigits(8));
        Map<String, Object> values = record(
                "nickname", nickname,
                "real_name", realName,
                "gender", genderCode(text(payload, "gender", "未知")),
                "birthday", nullIfBlank(text(payload, "birthday", "")),
                "phone", phone,
                "address", text(payload, "address", "上海市"),
                "bio", text(payload, "bio", "后台新增用户"),
                "height", decimal(payload, "height", BigDecimal.valueOf(160)),
                "weight", decimal(payload, "weight", BigDecimal.valueOf(60)),
                "ethnicity", text(payload, "ethnicity", "汉族"),
                "education", text(payload, "education", "高中"),
                "blood_type", text(payload, "bloodType", "A"),
                "rh_negative", 0,
                "chronic_disease", text(payload, "chronicDisease", "无"),
                "sleep_quality", text(payload, "sleepQuality", "良好"),
                "smoking_freq", text(payload, "smokingFreq", "不吸烟"),
                "drinking_freq", text(payload, "drinkingFreq", "不饮酒"),
                "exercise_freq", text(payload, "exerciseFreq", "每周2次"),
                "diet_preference", text(payload, "dietPreference", "清淡"),
                "avatar_url", text(payload, "avatarUrl", ""),
                "last_login_time", null,
                "last_buy_time", null,
                "status", 1
        );
        Number id = insert("user", values);
        Object tagsValue = payload.get("tags");
        if (tagsValue != null) {
            attachTags(id.longValue(), stringValue(tagsValue));
            updateAllTagCounts();
        }
        accepted("createUser:" + id);
        return record("accepted", true, "id", id.longValue(), "resource", "users");
    }

    @Override
    public Map<String, Object> updateUser(Long id, Map<String, Object> payload) {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        putIfPresent(values, "nickname", payload, "nickname");
        putIfPresent(values, "real_name", payload, "realName");
        putIfPresent(values, "phone", payload, "phone");
        putIfPresent(values, "avatar_url", payload, "avatarUrl");
        if (payload.containsKey("gender")) {
            values.put("gender", genderCode(text(payload, "gender", "未知")));
        }
        putIfPresent(values, "birthday", payload, "birthday");
        putIfPresent(values, "address", payload, "address");
        putIfPresent(values, "ethnicity", payload, "ethnicity");
        putIfPresent(values, "education", payload, "education");
        putIfPresent(values, "height", payload, "height");
        putIfPresent(values, "weight", payload, "weight");
        putIfPresent(values, "blood_type", payload, "bloodType");
        putIfPresent(values, "chronic_disease", payload, "chronicDisease");
        putIfPresent(values, "sleep_quality", payload, "sleepQuality");
        putIfPresent(values, "smoking_freq", payload, "smokingFreq");
        putIfPresent(values, "drinking_freq", payload, "drinkingFreq");
        putIfPresent(values, "exercise_freq", payload, "exerciseFreq");
        putIfPresent(values, "diet_preference", payload, "dietPreference");
        putIfPresent(values, "emergency_contact", payload, "emergencyContact");
        putIfPresent(values, "emergency_phone", payload, "emergencyPhone");
        if (!values.isEmpty()) {
            updateById("user", id, values);
        }
        if (payload.containsKey("tags")) {
            jdbcTemplate.update("delete from user_tag_rel where user_id = ?", id);
            attachTags(id, stringValue(payload.get("tags")));
            updateAllTagCounts();
        }
        accepted("updateUser:" + id);
        return record("accepted", true, "id", id, "resource", "users");
    }

    @Override
    public Map<String, Object> deleteUser(Long id) {
        jdbcTemplate.update("delete from user_tag_rel where user_id = ?", id);
        jdbcTemplate.update("delete from health_data where user_id = ?", id);
        jdbcTemplate.update("delete from medication_record where user_id = ?", id);
        jdbcTemplate.update("delete from `user` where id = ?", id);
        updateAllTagCounts();
        accepted("deleteUser:" + id);
        return record("accepted", true, "id", id, "resource", "users");
    }

    @Override
    public Map<String, Object> user(Long id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select u.id, u.nickname, u.real_name as realName, case u.gender when 1 then '男' when 2 then '女' else '未知' end as gender, " +
                        "date_format(u.birthday, '%Y-%m-%d') as birthday, u.phone, u.address, u.ethnicity, u.education, u.avatar_url as avatarUrl, u.height, u.weight, u.blood_type as bloodType, " +
                        "u.chronic_disease as chronicDisease, u.sleep_quality as sleepQuality, u.exercise_freq as exerciseFreq, " +
                        "u.emergency_contact as emergencyContact, u.emergency_phone as emergencyPhone, " +
                        "date_format(u.last_buy_time, '%Y-%m-%d') as lastBuyTime, date_format(u.last_login_time, '%Y-%m-%d %H:%i') as lastLoginTime, " +
                        "date_format(u.created_at, '%Y-%m-%d') as createdAt, group_concat(t.name order by t.id separator ',') as tags " +
                        "from `user` u left join user_tag_rel rel on u.id = rel.user_id left join user_tag t on rel.tag_id = t.id " +
                        "where u.id = ? group by u.id",
                id
        );
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }
        Map<String, Object> row = rows.get(0);
        normalizeTags(Arrays.asList(row));
        row.put("medications", jdbcTemplate.queryForList(
                "select id, period, drug_name as drugName, frequency, date_format(take_time, '%H:%i') as takeTime, dosage, reminder_enabled = 1 as reminderEnabled from medication_record where user_id = ? order by take_time",
                id
        ));
        row.put("healthData", jdbcTemplate.queryForList(
                "select min(id) as id, date_format(record_date, '%m-%d') as day, date_format(record_date, '%Y-%m-%d') as recordDate, " +
                        "max(case when data_type = 'weight' then cast(value as decimal(8,2)) end) as weight, " +
                        "max(case when data_type = 'heart_rate' then cast(value as unsigned) end) as heartRate " +
                        "from health_data where user_id = ? group by record_date order by record_date",
                id
        ));
        row.put("devices", jdbcTemplate.queryForList("select id, device_name as deviceName, device_type as deviceType, device_code as deviceCode, if(status = 1, '绑定', '解绑') as status from device where user_id = ? order by id", id));
        row.put("reports", jdbcTemplate.queryForList("select id, title, report_type as reportType, date_format(report_date, '%Y-%m-%d') as reportDate, doctor_name as doctorName, summary from report where user_id = ? order by report_date desc", id));
        row.put("orders", jdbcTemplate.queryForList("select id, order_no as orderNo, product_name as productName, amount, status, service_type as serviceType from service_order where buyer_id = ? order by id desc", id));
        row.put("coupons", jdbcTemplate.queryForList("select id, coupon_no as couponNo, name, type, discount, status, date_format(expire_date, '%Y-%m-%d') as expireDate from coupon where user_id = ? order by id desc", id));
        row.put("points", jdbcTemplate.queryForList("select id, points, total_earned as totalEarned, total_spent as totalSpent, level, growth_value as growthValue from user_points where user_id = ?", id));
        row.put("contents", jdbcTemplate.queryForList("select id, title, type, publisher, author, if(status = 1, '已发布', '草稿') as status from operation_content where publisher = ? order by id desc", row.get("realName")));
        row.put("serviceRecords", jdbcTemplate.queryForList("select id, order_no as orderNo, service_item as serviceItem, amount, status, date_format(service_time, '%Y-%m-%d %H:%i:%s') as serviceTime, date_format(complete_time, '%Y-%m-%d %H:%i:%s') as completeTime from work_order where customer_id = ? order by id desc", id));
        return row;
    }

    @Override
    public PageResult<Map<String, Object>> tags() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select id, name, type, color, user_count as userCount, if(status = 1, '启用', '禁用') as status, date_format(updated_at, '%Y-%m-%d %H:%i:%s') as updatedAt " +
                        "from user_tag order by id"
        );
        return new PageResult<Map<String, Object>>(rows.size(), rows);
    }

    @Override
    public Map<String, Object> createTag(Map<String, Object> payload) {
        require(payload, "name", "标签名称不能为空");
        Number id = insert("user_tag", record(
                "name", text(payload, "name", ""),
                "type", text(payload, "type", "manual"),
                "color", text(payload, "color", "green"),
                "user_count", 0,
                "status", statusCode(text(payload, "status", "启用")),
                "updater", "系统管理员"
        ));
        accepted("createTag:" + id);
        return record("accepted", true, "id", id.longValue(), "resource", "tags");
    }

    @Override
    public Map<String, Object> updateTag(Long id, Map<String, Object> payload) {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        putIfPresent(values, "name", payload, "name");
        putIfPresent(values, "type", payload, "type");
        putIfPresent(values, "color", payload, "color");
        if (payload.containsKey("status")) values.put("status", statusCode(text(payload, "status", "启用")));
        values.put("updater", "系统管理员");
        updateById("user_tag", id, values);
        accepted("updateTag:" + id);
        return record("accepted", true, "id", id, "resource", "tags");
    }

    @Override
    public Map<String, Object> deleteTag(Long id) {
        jdbcTemplate.update("delete from user_tag_rel where tag_id = ?", id);
        jdbcTemplate.update("delete from user_tag where id = ?", id);
        updateAllTagCounts();
        accepted("deleteTag:" + id);
        return record("accepted", true, "id", id, "resource", "tags");
    }

    @Override
    public Map<String, Object> updateUserTags(Long userId, Map<String, Object> payload) {
        jdbcTemplate.update("delete from user_tag_rel where user_id = ?", userId);
        List<Long> tagIds = toLongList(payload == null ? null : payload.get("tagIds"));
        if (tagIds.isEmpty() && payload != null && payload.containsKey("tags")) {
            attachTags(userId, stringValue(payload.get("tags")));
        } else {
            for (Long tagId : tagIds) {
                jdbcTemplate.update("insert ignore into user_tag_rel(user_id, tag_id) values(?, ?)", userId, tagId);
            }
        }
        updateAllTagCounts();
        accepted("updateUserTags:" + userId);
        return record("accepted", true, "id", userId, "resource", "userTags");
    }

    @Override
    public PageResult<Map<String, Object>> resource(String name) {
        List<Map<String, Object>> rows;
        if ("personnel".equals(name)) {
            rows = jdbcTemplate.queryForList("select id, name, service_type as serviceType, area, if(status = 1, '启用', '禁用') as status, date_format(created_at, '%Y-%m-%d %H:%i') as updatedAt from service_personnel order by id");
        } else if ("audits".equals(name)) {
            rows = jdbcTemplate.queryForList("select id, name, service_type as serviceType, audit_status as auditStatus, concat(substr(phone, 1, 3), '****', substr(phone, 8)) as phone, date_format(created_at, '%Y-%m-%d %H:%i') as updatedAt from service_personnel order by id");
        } else if ("workOrders".equals(name)) {
            rows = jdbcTemplate.queryForList("select w.id, w.order_no as orderNo, w.service_item as serviceItem, u.real_name as customer, case w.status when 'pending' then '待服务' when 'service_in' then '服务中' when 'completed' then '已完成' when 'cancelled' then '已取消' else w.status end as status, date_format(w.dispatch_time, '%Y-%m-%d %H:%i') as updatedAt from work_order w left join `user` u on w.customer_id = u.id order by w.id");
        } else if ("products".equals(name)) {
            rows = jdbcTemplate.queryForList("select id, name, category, price, if(status = 1, '上架', '下架') as status, date_format(updated_at, '%Y-%m-%d %H:%i') as updatedAt from product order by id");
        } else if ("orders".equals(name)) {
            rows = jdbcTemplate.queryForList("select o.id, o.order_no as orderNo, o.product_name as productName, u.real_name as buyer, o.amount, case o.status when 'pending_accept' then '待接单' when 'pending_service' then '待服务' when 'completed' then '已完成' when 'closed' then '已关闭' when 'after_sale' then '售后中' else o.status end as status from service_order o left join `user` u on o.buyer_id = u.id order by o.id");
        } else if ("afterSales".equals(name)) {
            rows = jdbcTemplate.queryForList("select a.id, o.order_no as orderNo, u.real_name as applicant, a.reason, a.status from after_sale a left join service_order o on a.order_id = o.id left join `user` u on a.applicant_id = u.id order by a.id");
        } else if ("reviews".equals(name)) {
            rows = jdbcTemplate.queryForList("select r.id, p.name as productName, u.real_name as user, r.rating, if(r.visible = 1, '已显示', '已隐藏') as status from review r left join product p on r.product_id = p.id left join `user` u on r.user_id = u.id order by r.id");
        } else if ("staffs".equals(name)) {
            rows = jdbcTemplate.queryForList("select s.id, s.staff_no as staffNo, s.name, r.name as role, if(s.status = 1, '启用', '禁用') as status from staff s left join role r on s.role_id = r.id order by s.id");
        } else if ("roles".equals(name)) {
            rows = jdbcTemplate.queryForList("select id, name, description, '启用' as status from role order by id");
        } else if ("logs".equals(name)) {
            rows = jdbcTemplate.queryForList("select id, operator, action_type as actionType, target, date_format(created_at, '%Y-%m-%d %H:%i') as createdAt from operation_log order by id desc");
        } else if ("agreements".equals(name)) {
            rows = jdbcTemplate.queryForList("select id, title, type, if(status = 1, '启用', '禁用') as status from agreement order by id");
        } else if (phaseTableName(name) != null) {
            rows = phaseRows(name);
        } else {
            rows = operationContent(name);
        }
        return new PageResult<Map<String, Object>>(rows.size(), rows);
    }

    @Override
    public Map<String, Object> createResource(String name, Map<String, Object> payload) {
        Number id;
        if ("personnel".equals(name)) {
            require(payload, "name", "服务人员姓名不能为空");
            id = insert("service_personnel", record(
                    "name", text(payload, "name", ""),
                    "phone", text(payload, "phone", "139" + uniqueDigits(8)),
                    "service_type", text(payload, "serviceType", "家政护理"),
                    "area", text(payload, "area", "浦东新区"),
                    "join_time", null,
                    "status", statusCode(text(payload, "status", "启用")),
                    "audit_status", text(payload, "auditStatus", "待审核")
            ));
        } else if ("products".equals(name)) {
            require(payload, "name", "商品名称不能为空");
            String category = text(payload, "category", "家政护理");
            id = insert("product", record(
                    "name", text(payload, "name", ""),
                    "code", text(payload, "code", "P-" + System.currentTimeMillis()),
                    "category", category,
                    "price", decimal(payload, "price", BigDecimal.valueOf(99)),
                    "status", statusCode(text(payload, "status", "上架")),
                    "updater", "系统管理员"
            ));
        } else if ("orders".equals(name)) {
            long productId = longValue(payload, "productId", firstId("product"));
            long buyerId = userId(payload, "buyerId", firstId("user"));
            Map<String, Object> product = firstRow("select name, price, category from product where id = ?", productId);
            String productName = text(payload, "productName", stringValue(product.get("name")));
            BigDecimal amount = decimal(payload, "amount", toDecimal(product.get("price"), BigDecimal.valueOf(99)));
            id = insert("service_order", record(
                    "order_no", text(payload, "orderNo", "OD" + System.currentTimeMillis()),
                    "product_id", productId,
                    "product_name", productName,
                    "amount", amount,
                    "buyer_id", buyerId,
                    "status", text(payload, "status", "pending_accept"),
                    "service_type", text(payload, "serviceType", stringValue(product.get("category")))
            ));
        } else if ("appointments".equals(name)) {
            long customerId = userId(payload, "customerId", firstId("user"));
            id = insert("work_order", record(
                    "order_no", text(payload, "orderNo", "WO" + System.currentTimeMillis()),
                    "order_id", longValue(payload, "orderId", firstId("service_order")),
                    "service_item", text(payload, "serviceName", "预约服务"),
                    "amount", decimal(payload, "amount", BigDecimal.valueOf(99)),
                    "personnel_id", longValue(payload, "personnelId", firstId("service_personnel")),
                    "customer_id", customerId,
                    "status", text(payload, "status", "pending"),
                    "dispatch_time", null,
                    "service_time", nullIfBlank(text(payload, "serviceTime", "")),
                    "complete_time", nullIfBlank(text(payload, "completeTime", "")),
                    "cancel_reason", null
            ));
        } else if ("workOrders".equals(name)) {
            long orderId = longValue(payload, "orderId", firstId("service_order"));
            long customerId = userId(payload, "customerId", firstId("user"));
            id = insert("work_order", record(
                    "order_no", text(payload, "orderNo", "WO" + System.currentTimeMillis()),
                    "order_id", orderId,
                    "service_item", text(payload, "serviceItem", "上门服务"),
                    "amount", decimal(payload, "amount", BigDecimal.valueOf(99)),
                    "personnel_id", longValue(payload, "personnelId", firstId("service_personnel")),
                    "customer_id", customerId,
                    "status", text(payload, "status", "pending"),
                    "dispatch_time", null,
                    "service_time", null,
                    "complete_time", null,
                    "cancel_reason", null
            ));
        } else if ("afterSales".equals(name)) {
            id = insert("after_sale", record(
                    "order_id", longValue(payload, "orderId", firstId("service_order")),
                    "applicant_id", userId(payload, "applicantId", firstId("user")),
                    "reason", text(payload, "reason", "服务时间变更"),
                    "status", text(payload, "status", "处理中")
            ));
        } else if ("reviews".equals(name)) {
            id = insert("review", record(
                    "order_id", longValue(payload, "orderId", firstId("service_order")),
                    "product_id", longValue(payload, "productId", firstId("product")),
                    "user_id", userId(payload, "userId", firstId("user")),
                    "rating", longValue(payload, "rating", 5),
                    "content", text(payload, "content", "服务体验良好"),
                    "visible", 1
            ));
        } else if ("staffs".equals(name)) {
            require(payload, "name", "员工姓名不能为空");
            id = insert("staff", record(
                    "staff_no", text(payload, "staffNo", "S" + uniqueDigits(4)),
                    "name", text(payload, "name", ""),
                    "phone", text(payload, "phone", "137" + uniqueDigits(8)),
                    "password_hash", text(payload, "password", "admin123"),
                    "role_id", longValue(payload, "roleId", firstId("role")),
                    "remark", text(payload, "remark", "后台新增员工"),
                    "status", statusCode(text(payload, "status", "启用")),
                    "updater", "系统管理员"
            ));
        } else if ("roles".equals(name)) {
            require(payload, "name", "角色名称不能为空");
            id = insert("role", record(
                    "name", text(payload, "name", ""),
                    "description", text(payload, "description", "后台新增角色"),
                    "permissions", text(payload, "permissions", "{}")
            ));
        } else if ("agreements".equals(name)) {
            id = insert("agreement", record(
                    "title", text(payload, "title", "新增协议"),
                    "type", text(payload, "type", "custom"),
                    "content", text(payload, "content", "协议内容"),
                    "status", statusCode(text(payload, "status", "启用"))
            ));
        } else if (phaseTableName(name) != null) {
            id = insert(phaseTableName(name), phaseCreateValues(name, payload));
        } else {
            id = insert("operation_content", record(
                    "type", name == null ? "posts" : name,
                    "title", text(payload, "title", "新增内容"),
                    "publisher", text(payload, "publisher", "系统管理员"),
                    "author", text(payload, "author", "运营中心"),
                    "location", text(payload, "location", ""),
                    "quota", longValue(payload, "quota", 0),
                    "likes", longValue(payload, "likes", 0),
                    "status", statusCode(text(payload, "status", "已发布"))
            ));
        }
        accepted("createResource:" + name + ":" + id);
        return record("accepted", true, "id", id.longValue(), "resource", name);
    }

    @Override
    public Map<String, Object> updateResource(String name, Long id, Map<String, Object> payload) {
        if (payload == null) {
            payload = new LinkedHashMap<String, Object>();
        }
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        if ("personnel".equals(name)) {
            putIfPresent(values, "name", payload, "name");
            putIfPresent(values, "phone", payload, "phone");
            putIfPresent(values, "service_type", payload, "serviceType");
            putIfPresent(values, "area", payload, "area");
            if (payload.containsKey("status")) values.put("status", statusCode(text(payload, "status", "启用")));
            putIfPresent(values, "audit_status", payload, "auditStatus");
            updateById("service_personnel", id, values);
        } else if ("products".equals(name)) {
            putIfPresent(values, "name", payload, "name");
            putIfPresent(values, "category", payload, "category");
            if (payload.containsKey("price")) values.put("price", decimal(payload, "price", BigDecimal.valueOf(99)));
            if (payload.containsKey("status")) values.put("status", statusCode(text(payload, "status", "上架")));
            values.put("updater", "系统管理员");
            updateById("product", id, values);
        } else if ("orders".equals(name)) {
            putIfPresent(values, "product_name", payload, "productName");
            if (payload.containsKey("amount")) values.put("amount", decimal(payload, "amount", BigDecimal.valueOf(99)));
            if (hasUserRef(payload, "buyerId")) values.put("buyer_id", userId(payload, "buyerId", firstId("user")));
            putIfPresent(values, "status", payload, "status");
            putIfPresent(values, "service_type", payload, "serviceType");
            updateById("service_order", id, values);
        } else if ("workOrders".equals(name) || "appointments".equals(name)) {
            putIfPresent(values, "service_item", payload, "serviceItem");
            putIfPresent(values, "service_item", payload, "serviceName");
            if (payload.containsKey("amount")) values.put("amount", decimal(payload, "amount", BigDecimal.valueOf(99)));
            if (hasUserRef(payload, "customerId")) values.put("customer_id", userId(payload, "customerId", firstId("user")));
            if (payload.containsKey("personnelId")) values.put("personnel_id", longValue(payload, "personnelId", firstId("service_personnel")));
            putIfPresent(values, "status", payload, "status");
            putIfPresent(values, "service_time", payload, "serviceTime");
            putIfPresent(values, "complete_time", payload, "completeTime");
            updateById("work_order", id, values);
        } else if ("afterSales".equals(name)) {
            if (hasUserRef(payload, "applicantId")) values.put("applicant_id", userId(payload, "applicantId", firstId("user")));
            putIfPresent(values, "reason", payload, "reason");
            putIfPresent(values, "status", payload, "status");
            updateById("after_sale", id, values);
        } else if ("reviews".equals(name)) {
            if (hasUserRef(payload, "userId")) values.put("user_id", userId(payload, "userId", firstId("user")));
            if (payload.containsKey("rating")) values.put("rating", longValue(payload, "rating", 5));
            putIfPresent(values, "content", payload, "content");
            updateById("review", id, values);
        } else if ("staffs".equals(name)) {
            putIfPresent(values, "staff_no", payload, "staffNo");
            putIfPresent(values, "name", payload, "name");
            putIfPresent(values, "phone", payload, "phone");
            if (payload.containsKey("roleId")) values.put("role_id", longValue(payload, "roleId", firstId("role")));
            putIfPresent(values, "remark", payload, "remark");
            if (payload.containsKey("status")) values.put("status", statusCode(text(payload, "status", "启用")));
            values.put("updater", "系统管理员");
            updateById("staff", id, values);
        } else if ("roles".equals(name)) {
            putIfPresent(values, "name", payload, "name");
            putIfPresent(values, "description", payload, "description");
            updateById("role", id, values);
        } else if ("agreements".equals(name)) {
            putIfPresent(values, "title", payload, "title");
            putIfPresent(values, "type", payload, "type");
            putIfPresent(values, "content", payload, "content");
            if (payload.containsKey("status")) values.put("status", statusCode(text(payload, "status", "启用")));
            updateById("agreement", id, values);
        } else if (phaseTableName(name) != null) {
            updateById(phaseTableName(name), id, phaseUpdateValues(name, payload));
        } else {
            putIfPresent(values, "title", payload, "title");
            putIfPresent(values, "publisher", payload, "publisher");
            putIfPresent(values, "author", payload, "author");
            putIfPresent(values, "location", payload, "location");
            if (payload.containsKey("quota")) values.put("quota", longValue(payload, "quota", 0));
            if (payload.containsKey("likes")) values.put("likes", longValue(payload, "likes", 0));
            if (payload.containsKey("status")) values.put("status", statusCode(text(payload, "status", "已发布")));
            updateById("operation_content", id, values);
        }
        accepted("updateResource:" + name + ":" + id);
        return record("accepted", true, "id", id, "resource", name);
    }

    @Override
    public Map<String, Object> deleteResource(String name, Long id) {
        String table = tableName(name);
        jdbcTemplate.update("delete from `" + table + "` where id = ?", id);
        accepted("deleteResource:" + name + ":" + id);
        return record("accepted", true, "id", id, "resource", name);
    }

    @Override
    public Map<String, Object> analyticsOverview() {
        return record("metrics", list(
                record("label", "活跃用户", "value", count("select count(*) from `user` where status = 1"), "delta", "+9.8%"),
                record("label", "成交金额", "value", "¥" + money(countAmount()), "delta", "+14.1%"),
                record("label", "完成工单", "value", count("select count(*) from work_order where status = 'completed'"), "delta", "+7.3%"),
                record("label", "好评率", "value", goodRate(), "delta", "+2.4%")
        ));
    }

    @Override
    public Map<String, Object> accepted(String action) {
        jdbcTemplate.update(
                "insert into operation_log(operator, action_type, target, content, ip_address) values('系统管理员', '操作', ?, ?, '127.0.0.1')",
                action, "接口操作已接收"
        );
        return record("accepted", true, "action", action, "operator", "系统管理员");
    }

    private List<Map<String, Object>> operationContent(String type) {
        String normalized = type == null ? "posts" : type;
        return jdbcTemplate.queryForList(
                "select id, title, publisher, likes, location, quota, author, if(status = 1, '已发布', '草稿') as status from operation_content where type = ? order by id",
                normalized
        );
    }

    private Number insert(String tableName, Map<String, Object> values) {
        final List<String> columns = new ArrayList<String>(values.keySet());
        final StringBuilder sql = new StringBuilder();
        sql.append("insert into `").append(tableName).append("` (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("`").append(columns.get(i)).append("`");
        }
        sql.append(") values (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("?");
        }
        sql.append(")");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < columns.size(); i++) {
                statement.setObject(i + 1, values.get(columns.get(i)));
            }
            return statement;
        }, keyHolder);
        return keyHolder.getKey();
    }

    private void attachTags(long userId, String tagsText) {
        String[] tags = tagsText.split("[,，]");
        for (String rawTag : tags) {
            String tag = rawTag.trim();
            if (tag.length() == 0) {
                continue;
            }
            List<Map<String, Object>> exists = jdbcTemplate.queryForList("select id from user_tag where name = ? limit 1", tag);
            long tagId;
            if (exists.isEmpty()) {
                tagId = insert("user_tag", record("name", tag, "type", "manual", "user_count", 0, "status", 1, "updater", "系统管理员")).longValue();
            } else {
                tagId = ((Number) exists.get(0).get("id")).longValue();
            }
            jdbcTemplate.update("insert ignore into user_tag_rel(user_id, tag_id) values(?, ?)", userId, tagId);
            jdbcTemplate.update("update user_tag set user_count = (select count(*) from user_tag_rel where tag_id = ?) where id = ?", tagId, tagId);
        }
    }

    private void updateAllTagCounts() {
        jdbcTemplate.update("update user_tag t set user_count = (select count(*) from user_tag_rel r where r.tag_id = t.id)");
    }

    private void updateById(String tableName, Long id, Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        StringBuilder sql = new StringBuilder("update `").append(tableName).append("` set ");
        List<Object> args = new ArrayList<Object>();
        int index = 0;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (index++ > 0) {
                sql.append(", ");
            }
            sql.append("`").append(entry.getKey()).append("` = ?");
            args.add(entry.getValue());
        }
        sql.append(" where id = ?");
        args.add(id);
        jdbcTemplate.update(sql.toString(), args.toArray());
    }

    private String tableName(String resourceName) {
        if ("personnel".equals(resourceName)) return "service_personnel";
        if ("products".equals(resourceName)) return "product";
        if ("orders".equals(resourceName)) return "service_order";
        if ("workOrders".equals(resourceName) || "appointments".equals(resourceName)) return "work_order";
        if ("afterSales".equals(resourceName)) return "after_sale";
        if ("reviews".equals(resourceName)) return "review";
        if ("staffs".equals(resourceName)) return "staff";
        if ("roles".equals(resourceName)) return "role";
        if ("agreements".equals(resourceName)) return "agreement";
        String phaseTable = phaseTableName(resourceName);
        if (phaseTable != null) return phaseTable;
        return "operation_content";
    }

    private String phaseTableName(String resourceName) {
        if ("healthSettings".equals(resourceName)) return "health_settings";
        if ("healthData".equals(resourceName)) return "health_data";
        if ("medications".equals(resourceName)) return "medication_record";
        if ("devices".equals(resourceName)) return "device";
        if ("reports".equals(resourceName)) return "report";
        if ("coupons".equals(resourceName)) return "coupon";
        if ("userPoints".equals(resourceName)) return "user_points";
        if ("pointsRecords".equals(resourceName)) return "points_record";
        if ("memberLevels".equals(resourceName)) return "member_level";
        if ("pointsRules".equals(resourceName)) return "points_rule";
        if ("productCategories".equals(resourceName)) return "product_category";
        if ("serviceItems".equals(resourceName)) return "service_item";
        if ("banners".equals(resourceName)) return "banner";
        if ("activities".equals(resourceName)) return "activity";
        if ("activityEnrolls".equals(resourceName)) return "activity_enroll";
        if ("topics".equals(resourceName)) return "topic";
        if ("recipes".equals(resourceName)) return "recipe";
        if ("articles".equals(resourceName)) return "article";
        if ("diseases".equals(resourceName)) return "disease";
        if ("institutions".equals(resourceName)) return "institution";
        if ("videos".equals(resourceName)) return "video";
        if ("foods".equals(resourceName)) return "food";
        if ("assessments".equals(resourceName)) return "assessment";
        if ("assessmentResults".equals(resourceName)) return "assessment_result";
        return null;
    }

    private List<Map<String, Object>> phaseRows(String name) {
        if ("healthSettings".equals(name)) return jdbcTemplate.queryForList("select h.id, u.real_name as userName, h.step_goal as stepGoal, h.sleep_goal as sleepGoal, if(h.medication_reminder = 1, '启用', '禁用') as status from health_settings h left join `user` u on h.user_id = u.id order by h.id");
        if ("healthData".equals(name)) return jdbcTemplate.queryForList("select h.id, u.real_name as userName, h.data_type as dataType, h.value, h.unit, date_format(h.record_date, '%Y-%m-%d') as recordDate, date_format(h.record_time, '%H:%i:%s') as recordTime, h.source from health_data h left join `user` u on h.user_id = u.id order by h.record_date desc, h.id desc");
        if ("medications".equals(name)) return jdbcTemplate.queryForList("select m.id, u.real_name as userName, m.period, m.drug_name as drugName, m.frequency, date_format(m.take_time, '%H:%i:%s') as takeTime, m.dosage, if(m.reminder_enabled = 1, '启用', '禁用') as status from medication_record m left join `user` u on m.user_id = u.id order by m.id");
        if ("devices".equals(name)) return jdbcTemplate.queryForList("select d.id, u.real_name as userName, d.device_name as deviceName, d.device_type as deviceType, d.device_code as deviceCode, if(d.status = 1, '绑定', '解绑') as status from device d left join `user` u on d.user_id = u.id order by d.id");
        if ("reports".equals(name)) return jdbcTemplate.queryForList("select r.id, u.real_name as userName, r.title, r.report_type as reportType, date_format(r.report_date, '%Y-%m-%d') as reportDate, r.doctor_name as doctorName from report r left join `user` u on r.user_id = u.id order by r.id");
        if ("coupons".equals(name)) return jdbcTemplate.queryForList("select id, coupon_no as couponNo, name, type, discount, status, date_format(expire_date, '%Y-%m-%d') as expireDate from coupon order by id");
        if ("userPoints".equals(name)) return jdbcTemplate.queryForList("select p.id, u.real_name as userName, p.points, p.level, p.growth_value as growthValue from user_points p left join `user` u on p.user_id = u.id order by p.id");
        if ("pointsRecords".equals(name)) return jdbcTemplate.queryForList("select r.id, u.real_name as userName, r.change_value as changeValue, r.reason, date_format(r.created_at, '%Y-%m-%d %H:%i') as createdAt from points_record r left join `user` u on r.user_id = u.id order by r.id desc");
        if ("memberLevels".equals(name)) return jdbcTemplate.queryForList("select id, name, min_growth as minGrowth, max_growth as maxGrowth, if(status = 1, '启用', '禁用') as status from member_level order by id");
        if ("pointsRules".equals(name)) return jdbcTemplate.queryForList("select id, action_type as actionType, description, points, growth, if(status = 1, '启用', '禁用') as status from points_rule order by id");
        if ("productCategories".equals(name)) return jdbcTemplate.queryForList("select id, name, code, description, sort_order as sortOrder, if(status = 1, '启用', '禁用') as status from product_category order by sort_order, id");
        if ("serviceItems".equals(name)) return jdbcTemplate.queryForList("select s.id, p.name as productName, s.name, s.duration, s.price, if(s.status = 1, '启用', '禁用') as status from service_item s left join product p on s.product_id = p.id order by s.id");
        if ("banners".equals(name)) return jdbcTemplate.queryForList("select id, title, image_url as imageUrl, location, sort_order as sortOrder, if(status = 1, '启用', '禁用') as status from banner order by sort_order, id");
        if ("activities".equals(name)) return jdbcTemplate.queryForList("select id, title, location, quota, enrolled, case status when 'published' then '已发布' when 'draft' then '草稿' when 'ended' then '已结束' else status end as status from activity order by id");
        if ("activityEnrolls".equals(name)) return jdbcTemplate.queryForList("select e.id, a.title as activityTitle, u.real_name as userName, e.status, e.remark from activity_enroll e left join activity a on e.activity_id = a.id left join `user` u on e.user_id = u.id order by e.id");
        if ("topics".equals(name)) return jdbcTemplate.queryForList("select id, name, description, post_count as postCount, if(status = 1, '启用', '禁用') as status from topic order by id");
        if ("recipes".equals(name)) return jdbcTemplate.queryForList("select id, name as title, category, calories, suitable_for as suitableFor, if(status = 1, '启用', '禁用') as status from recipe order by id");
        if ("articles".equals(name)) return jdbcTemplate.queryForList("select id, title, author, category, view_count as viewCount, if(status = 1, '已发布', '草稿') as status from article order by id");
        if ("diseases".equals(name)) return jdbcTemplate.queryForList("select id, name as title, category, summary, if(status = 1, '启用', '禁用') as status from disease order by id");
        if ("institutions".equals(name)) return jdbcTemplate.queryForList("select id, name as title, type, address, rating, capacity, if(status = 1, '启用', '禁用') as status from institution order by id");
        if ("videos".equals(name)) return jdbcTemplate.queryForList("select id, title, lecturer, category, duration, view_count as viewCount, if(status = 1, '已发布', '草稿') as status from video order by id");
        if ("foods".equals(name)) return jdbcTemplate.queryForList("select id, name as title, category, calories, protein, fat, carbs, if(status = 1, '启用', '禁用') as status from food order by id");
        if ("assessments".equals(name)) return jdbcTemplate.queryForList("select id, title, type, if(status = 1, '启用', '禁用') as status from assessment order by id");
        if ("assessmentResults".equals(name)) return jdbcTemplate.queryForList("select r.id, a.title as assessmentTitle, u.real_name as userName, r.score, r.result from assessment_result r left join assessment a on r.assessment_id = a.id left join `user` u on r.user_id = u.id order by r.id");
        return new ArrayList<Map<String, Object>>();
    }

    private Map<String, Object> phaseCreateValues(String name, Map<String, Object> payload) {
        if ("healthSettings".equals(name)) return record("user_id", userId(payload, "userId", firstId("user")), "heart_rate_upper", longValue(payload, "heartRateUpper", 110), "heart_rate_lower", longValue(payload, "heartRateLower", 55), "step_goal", longValue(payload, "stepGoal", 6000), "sleep_goal", decimal(payload, "sleepGoal", BigDecimal.valueOf(7)), "medication_reminder", statusCode(text(payload, "status", "启用")));
        if ("healthData".equals(name)) return record("user_id", userId(payload, "userId", firstId("user")), "data_type", text(payload, "dataType", "weight"), "value", text(payload, "value", "0"), "unit", text(payload, "unit", ""), "record_date", nullIfBlank(text(payload, "recordDate", LocalDate.now().toString())), "record_time", nullIfBlank(text(payload, "recordTime", "")), "source", text(payload, "source", "后台录入"), "device_id", nullableLong(payload, "deviceId"));
        if ("medications".equals(name)) return record("user_id", userId(payload, "userId", firstId("user")), "period", text(payload, "period", "早餐"), "drug_name", text(payload, "drugName", "药品"), "frequency", text(payload, "frequency", "每天"), "take_time", nullIfBlank(text(payload, "takeTime", "")), "dosage", text(payload, "dosage", ""), "reminder_enabled", statusCode(text(payload, "status", "启用")), "source", text(payload, "source", "后台录入"), "creator", text(payload, "creator", "系统管理员"));
        if ("devices".equals(name)) return record("user_id", userId(payload, "userId", firstId("user")), "device_name", text(payload, "deviceName", "智能设备"), "device_type", text(payload, "deviceType", "band"), "device_code", text(payload, "deviceCode", "DEV" + uniqueDigits(6)), "bind_time", null, "last_sync_time", null, "status", statusCode(text(payload, "status", "启用")));
        if ("reports".equals(name)) return record("user_id", userId(payload, "userId", firstId("user")), "title", text(payload, "title", "健康报告"), "report_type", text(payload, "reportType", "健康评估"), "report_date", nullIfBlank(text(payload, "reportDate", LocalDate.now().toString())), "file_url", text(payload, "fileUrl", ""), "summary", text(payload, "summary", ""), "doctor_name", text(payload, "doctorName", "系统医生"));
        if ("coupons".equals(name)) return record("user_id", nullableUserId(payload, "userId"), "coupon_no", text(payload, "couponNo", "CP" + System.currentTimeMillis()), "name", text(payload, "name", "优惠券"), "type", text(payload, "type", "满减"), "discount", decimal(payload, "discount", BigDecimal.valueOf(10)), "min_amount", decimal(payload, "minAmount", BigDecimal.ZERO), "status", text(payload, "status", "未使用"), "expire_date", nullIfBlank(text(payload, "expireDate", LocalDate.now().plusDays(30).toString())));
        if ("userPoints".equals(name)) return record("user_id", userId(payload, "userId", firstId("user")), "points", longValue(payload, "points", 0), "total_earned", longValue(payload, "totalEarned", 0), "total_spent", longValue(payload, "totalSpent", 0), "level", text(payload, "level", "普通"), "growth_value", longValue(payload, "growthValue", 0));
        if ("pointsRecords".equals(name)) return record("user_id", userId(payload, "userId", firstId("user")), "change_value", longValue(payload, "changeValue", 1), "reason", text(payload, "reason", "后台调整"));
        if ("memberLevels".equals(name)) return record("name", text(payload, "name", "新等级"), "min_growth", longValue(payload, "minGrowth", 0), "max_growth", nullableLong(payload, "maxGrowth"), "icon", text(payload, "icon", ""), "benefits", text(payload, "benefits", ""), "status", statusCode(text(payload, "status", "启用")));
        if ("pointsRules".equals(name)) return record("action_type", text(payload, "actionType", "action" + uniqueDigits(4)), "description", text(payload, "description", "积分规则"), "points", longValue(payload, "points", 1), "growth", longValue(payload, "growth", 1), "daily_limit", nullableLong(payload, "dailyLimit"), "status", statusCode(text(payload, "status", "启用")));
        if ("productCategories".equals(name)) return record("name", text(payload, "name", "新分类"), "code", text(payload, "code", "CAT" + uniqueDigits(4)), "description", text(payload, "description", ""), "sort_order", longValue(payload, "sortOrder", 0), "status", statusCode(text(payload, "status", "启用")));
        if ("serviceItems".equals(name)) return record("product_id", longValue(payload, "productId", firstId("product")), "name", text(payload, "name", "服务项目"), "description", text(payload, "description", ""), "duration", longValue(payload, "duration", 60), "price", decimal(payload, "price", BigDecimal.valueOf(99)), "status", statusCode(text(payload, "status", "启用")));
        if ("banners".equals(name)) return record("title", text(payload, "title", "轮播图"), "image_url", text(payload, "imageUrl", "https://example.com/banner.png"), "link_url", text(payload, "linkUrl", ""), "sort_order", longValue(payload, "sortOrder", 0), "location", text(payload, "location", "home"), "status", statusCode(text(payload, "status", "启用")));
        if ("activities".equals(name)) return record("title", text(payload, "title", "活动"), "cover_url", text(payload, "coverUrl", ""), "location", text(payload, "location", ""), "start_time", text(payload, "startTime", LocalDate.now().toString() + " 09:00:00"), "end_time", nullIfBlank(text(payload, "endTime", "")), "quota", longValue(payload, "quota", 50), "enrolled", longValue(payload, "enrolled", 0), "status", text(payload, "status", "published"), "content", text(payload, "content", ""));
        if ("activityEnrolls".equals(name)) return record("activity_id", longValue(payload, "activityId", firstId("activity")), "user_id", userId(payload, "userId", firstId("user")), "status", text(payload, "status", "enrolled"), "remark", text(payload, "remark", ""));
        if ("topics".equals(name)) return record("name", text(payload, "name", "新话题"), "description", text(payload, "description", ""), "icon", text(payload, "icon", ""), "post_count", longValue(payload, "postCount", 0), "status", statusCode(text(payload, "status", "启用")));
        if ("recipes".equals(name)) return record("name", text(payload, "title", text(payload, "name", "新菜谱")), "category", text(payload, "category", "午餐"), "ingredients", text(payload, "ingredients", "食材"), "steps", text(payload, "steps", "步骤"), "calories", longValue(payload, "calories", 300), "suitable_for", text(payload, "suitableFor", ""), "cover_url", text(payload, "coverUrl", ""), "status", statusCode(text(payload, "status", "启用")));
        if ("articles".equals(name)) return record("title", text(payload, "title", "新资讯"), "summary", text(payload, "summary", ""), "content", text(payload, "content", ""), "cover_url", text(payload, "coverUrl", ""), "author", text(payload, "author", "运营中心"), "category", text(payload, "category", "养生"), "tags", text(payload, "tags", ""), "view_count", longValue(payload, "viewCount", 0), "status", statusCode(text(payload, "status", "已发布")));
        if ("diseases".equals(name)) return record("name", text(payload, "title", text(payload, "name", "疾病")), "category", text(payload, "category", "常见病"), "summary", text(payload, "summary", ""), "symptoms", text(payload, "symptoms", ""), "prevention", text(payload, "prevention", ""), "diet_advice", text(payload, "dietAdvice", ""), "risk_factors", text(payload, "riskFactors", ""), "status", statusCode(text(payload, "status", "启用")));
        if ("institutions".equals(name)) return record("name", text(payload, "title", text(payload, "name", "机构")), "address", text(payload, "address", ""), "phone", text(payload, "phone", ""), "type", text(payload, "type", "养老院"), "rating", decimal(payload, "rating", BigDecimal.valueOf(4.5)), "capacity", longValue(payload, "capacity", 100), "price_range", text(payload, "priceRange", ""), "services", text(payload, "services", ""), "images", text(payload, "images", ""), "status", statusCode(text(payload, "status", "启用")));
        if ("videos".equals(name)) return record("title", text(payload, "title", "视频"), "description", text(payload, "description", ""), "cover_url", text(payload, "coverUrl", ""), "video_url", text(payload, "videoUrl", "https://example.com/video.mp4"), "duration", longValue(payload, "duration", 600), "lecturer", text(payload, "lecturer", ""), "category", text(payload, "category", "健康"), "view_count", longValue(payload, "viewCount", 0), "status", statusCode(text(payload, "status", "已发布")));
        if ("foods".equals(name)) return record("name", text(payload, "title", text(payload, "name", "食物")), "category", text(payload, "category", "主食"), "calories", longValue(payload, "calories", 100), "protein", decimal(payload, "protein", BigDecimal.ZERO), "fat", decimal(payload, "fat", BigDecimal.ZERO), "carbs", decimal(payload, "carbs", BigDecimal.ZERO), "fiber", decimal(payload, "fiber", BigDecimal.ZERO), "sodium", decimal(payload, "sodium", BigDecimal.ZERO), "suitable_for", text(payload, "suitableFor", ""), "status", statusCode(text(payload, "status", "启用")));
        if ("assessments".equals(name)) return record("title", text(payload, "title", "测评"), "description", text(payload, "description", ""), "type", text(payload, "type", "custom"), "questions", text(payload, "questions", "[]"), "scoring_rules", text(payload, "scoringRules", "{}"), "status", statusCode(text(payload, "status", "启用")));
        if ("assessmentResults".equals(name)) return record("assessment_id", longValue(payload, "assessmentId", firstId("assessment")), "user_id", userId(payload, "userId", firstId("user")), "score", longValue(payload, "score", 0), "result", text(payload, "result", ""), "answers", text(payload, "answers", "{}"));
        return record("title", text(payload, "title", "新增内容"));
    }

    private Map<String, Object> phaseUpdateValues(String name, Map<String, Object> payload) {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        Map<String, Object> created = phaseCreateValues(name, payload);
        for (Map.Entry<String, Object> entry : created.entrySet()) {
            values.put(entry.getKey(), entry.getValue());
        }
        return values;
    }

    private List<Map<String, Object>> trend() {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            Date sqlDate = Date.valueOf(day);
            rows.add(record(
                    "day", day.format(dayFormatter),
                    "users", count("select count(*) from `user` where date(created_at) = ?", sqlDate),
                    "orders", count("select count(*) from service_order where date(created_at) = ?", sqlDate)
            ));
        }
        return rows;
    }

    private Map<String, Object> metric(String label, long value, String delta, String tone) {
        return record("label", label, "value", value, "delta", delta, "tone", tone);
    }

    private long count(String sql, Object... args) {
        Number value = jdbcTemplate.queryForObject(sql, args, Number.class);
        return value == null ? 0 : value.longValue();
    }

    private long firstId(String tableName) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("select id from `" + tableName + "` order by id limit 1");
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("基础数据不存在：" + tableName);
        }
        return ((Number) rows.get(0).get("id")).longValue();
    }

    private boolean hasUserRef(Map<String, Object> payload, String idKey) {
        return payload != null && (payload.containsKey(idKey) || payload.containsKey("userRef") || payload.containsKey("userName") || payload.containsKey("customerName") || payload.containsKey("buyerName") || payload.containsKey("applicantName"));
    }

    private long userId(Map<String, Object> payload, String idKey, long fallback) {
        if (payload == null) {
            return fallback;
        }
        Long parsedId = parseLong(payload.get(idKey));
        if (parsedId != null && parsedId > 0) {
            return parsedId;
        }
        String ref = firstText(payload, "userRef", "userName", "customerName", "buyerName", "applicantName", idKey);
        if (ref.length() == 0) {
            return fallback;
        }
        Long refId = parseLong(ref);
        if (refId != null && refId > 0) {
            return refId;
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select id from `user` where real_name = ? or nickname = ? or phone = ? order by id limit 1",
                ref, ref, ref
        );
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("未找到用户：" + ref);
        }
        return ((Number) rows.get(0).get("id")).longValue();
    }

    private Object nullableUserId(Map<String, Object> payload, String idKey) {
        if (!hasUserRef(payload, idKey)) {
            return null;
        }
        Long parsedId = parseLong(payload.get(idKey));
        String ref = firstText(payload, "userRef", "userName", "customerName", "buyerName", "applicantName", idKey);
        if ((parsedId == null || parsedId <= 0) && ref.length() == 0) {
            return null;
        }
        return userId(payload, idKey, firstId("user"));
    }

    private String firstText(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            if (payload.containsKey(key)) {
                String value = stringValue(payload.get(key)).trim();
                if (value.length() > 0) {
                    return value;
                }
            }
        }
        return "";
    }

    private long roleIdByName(String roleName) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("select id from role where name = ? limit 1", roleName);
        if (!rows.isEmpty()) {
            return ((Number) rows.get(0).get("id")).longValue();
        }
        return insert("role", record("name", roleName, "description", "个人资料新增角色", "permissions", "{}")).longValue();
    }

    private Map<String, Object> firstRow(String sql, Object... args) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, args);
        if (rows.isEmpty()) {
            return new LinkedHashMap<String, Object>();
        }
        return rows.get(0);
    }

    private BigDecimal countAmount() {
        BigDecimal value = jdbcTemplate.queryForObject("select coalesce(sum(amount), 0) from service_order where status in ('pending_accept', 'pending_service', 'completed')", BigDecimal.class);
        return value == null ? BigDecimal.ZERO : value;
    }

    private String money(BigDecimal value) {
        return value.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    private String goodRate() {
        long total = count("select count(*) from review");
        if (total == 0) {
            return "0%";
        }
        long good = count("select count(*) from review where rating >= 4");
        return BigDecimal.valueOf(good * 100.0 / total).setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString() + "%";
    }

    private void normalizeTags(List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            Object tags = row.get("tags");
            if (tags == null || stringValue(tags).length() == 0) {
                row.put("tags", new ArrayList<String>());
            } else {
                row.put("tags", Arrays.asList(stringValue(tags).split(",")));
            }
        }
    }

    private void normalizeTagIds(List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            Object value = row.get("tagIds");
            row.put("tagIds", toLongList(value));
        }
    }

    private List<Long> toLongList(Object value) {
        List<Long> ids = new ArrayList<Long>();
        if (value == null) {
            return ids;
        }
        if (value instanceof Iterable) {
            for (Object item : (Iterable<?>) value) {
                Long parsed = parseLong(item);
                if (parsed != null) {
                    ids.add(parsed);
                }
            }
            return ids;
        }
        String text = stringValue(value);
        if (text.trim().length() == 0) {
            return ids;
        }
        String[] parts = text.split("[,，]");
        for (String part : parts) {
            Long parsed = parseLong(part);
            if (parsed != null) {
                ids.add(parsed);
            }
        }
        return ids;
    }

    private Long parseLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            String text = stringValue(value).trim();
            return text.length() == 0 ? null : Long.parseLong(text);
        } catch (Exception ex) {
            return null;
        }
    }

    private void require(Map<String, Object> payload, String key, String message) {
        if (text(payload, key, "").trim().length() == 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private void putIfPresent(Map<String, Object> values, String column, Map<String, Object> payload, String key) {
        if (payload != null && payload.containsKey(key)) {
            values.put(column, nullIfBlank(stringValue(payload.get(key))));
        }
    }

    private String text(Map<String, Object> payload, String key, String fallback) {
        Object value = payload == null ? null : payload.get(key);
        String text = stringValue(value).trim();
        return text.length() == 0 ? fallback : text;
    }

    private Object nullIfBlank(String value) {
        return value == null || value.trim().length() == 0 ? null : value.trim();
    }

    private int genderCode(String value) {
        if ("男".equals(value) || "1".equals(value)) {
            return 1;
        }
        if ("女".equals(value) || "2".equals(value)) {
            return 2;
        }
        return 0;
    }

    private int statusCode(String value) {
        return "禁用".equals(value) || "下架".equals(value) || "草稿".equals(value) ? 0 : 1;
    }

    private long longValue(Map<String, Object> payload, String key, long fallback) {
        Object value = payload == null ? null : payload.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            String text = stringValue(value).trim();
            return text.length() == 0 ? fallback : Long.parseLong(text);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private Object nullableLong(Map<String, Object> payload, String key) {
        Object value = payload == null ? null : payload.get(key);
        if (value == null || stringValue(value).trim().length() == 0) {
            return null;
        }
        return longValue(payload, key, 0);
    }

    private BigDecimal decimal(Map<String, Object> payload, String key, BigDecimal fallback) {
        return toDecimal(payload == null ? null : payload.get(key), fallback);
    }

    private BigDecimal toDecimal(Object value, BigDecimal fallback) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            String text = stringValue(value).trim();
            return text.length() == 0 ? fallback : new BigDecimal(text);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private String uniqueDigits(int length) {
        String raw = String.valueOf(System.currentTimeMillis());
        return raw.substring(Math.max(0, raw.length() - length));
    }

    private Map<String, Object> record(Object... values) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (int i = 0; i < values.length; i += 2) {
            map.put(String.valueOf(values[i]), values[i + 1]);
        }
        return map;
    }

    private List<Map<String, Object>> list(Map<String, Object>... rows) {
        return new ArrayList<Map<String, Object>>(Arrays.asList(rows));
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
