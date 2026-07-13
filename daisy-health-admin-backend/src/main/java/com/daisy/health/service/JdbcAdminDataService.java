package com.daisy.health.service;

import com.daisy.health.common.AuthenticatedUser;
import com.daisy.health.common.JwtAuthFilter;
import com.daisy.health.common.JwtService;
import com.daisy.health.common.PageResult;
import com.daisy.health.common.PermissionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Profile("mysql")
@Transactional
public class JdbcAdminDataService implements AdminDataService {
    private static final List<String> MEMBER_LEVEL_NAMES = Arrays.asList("普通", "银卡", "金卡");

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PermissionService permissionService;
    private final ObjectMapper objectMapper;
    private final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("MM-dd");

    public JdbcAdminDataService(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder, JwtService jwtService,
                                PermissionService permissionService, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.permissionService = permissionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> login(Map<String, Object> payload) {
        String phone = stringValue(payload.get("phone"));
        String password = stringValue(payload.get("password"));
        if (phone.length() == 0 || password.length() == 0) {
            throw new IllegalArgumentException("Account and password are required");
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select id, phone, password_hash as passwordHash, role_type as roleType, nickname, avatar_url as avatarUrl " +
                        "from account where phone = ? and status = 1 limit 1",
                phone
        );
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Account or password is incorrect");
        }
        Map<String, Object> account = rows.get(0);
        String storedPassword = stringValue(account.get("passwordHash"));
        if (!passwordEncoder.matches(password, storedPassword) && !password.equals(storedPassword)) {
            throw new IllegalArgumentException("Account or password is incorrect");
        }
        Long accountId = ((Number) account.get("id")).longValue();
        if (password.equals(storedPassword)) {
            String encoded = passwordEncoder.encode(password);
            jdbcTemplate.update("update account set password_hash = ? where id = ?", encoded, accountId);
            if ("staff".equals(stringValue(account.get("roleType")))) {
                jdbcTemplate.update("update staff set password_hash = ? where id = ?", encoded, accountId);
            }
        }
        jdbcTemplate.update("update account set last_login_time = now() where id = ?", accountId);
        Map<String, Object> user = loginUser(accountId, stringValue(account.get("roleType")));
        if (user.isEmpty()) {
            throw new IllegalArgumentException("Account profile is not configured");
        }
        user.remove("passwordHash");
        String roleType = stringValue(user.get("roleType"));
        user.put("permissions", "staff".equals(roleType) ? permissionService.loadPermissions(accountId) : Collections.emptyMap());
        String token = jwtService.createToken(accountId, roleType, phone);
        return record("token", token, "user", user);
    }

    @Override
    public Map<String, Object> profile() {
        AuthenticatedUser current = currentUser();
        if (current != null && !"staff".equals(current.getRoleType())) {
            Map<String, Object> profile = loginUser(current.getAccountId(), current.getRoleType());
            profile.put("permissions", Collections.emptyMap());
            return profile;
        }
        List<Map<String, Object>> rows = current == null
                ? jdbcTemplate.queryForList(staffProfileSql() + " where a.status = 1 and a.role_type = 'staff' order by a.id limit 1")
                : jdbcTemplate.queryForList(staffProfileSql() + " where a.id = ? and a.status = 1 limit 1", current.getAccountId());
        if (rows.isEmpty()) {
            return record("id", 1, "staffNo", "S0001", "name", "System Admin", "phone", "13800000000", "role", "Admin", "roleType", "staff", "avatarUrl", "", "remark", "", "permissions", Collections.emptyMap());
        }
        Map<String, Object> row = rows.get(0);
        row.put("permissions", permissionsMap(row.remove("permissions")));
        return row;
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
            values.put("role_id", roleIdByName(text(payload, "role", "Admin")));
        }
        values.put("updater", "System");
        updateById("staff", id, values);
        syncAdminAccount(id);
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
                        metric("New posts", count("select count(*) from operation_content where type = 'posts' and created_at >= date_sub(curdate(), interval 7 day)"), "-3.2%", "danger")
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
    public List<Map<String, Object>> appointments(ResourceQuery query) {
        LocalDate start = query.startDateOr(LocalDate.now());
        LocalDate end = query.endDateOr(start.plusDays(6));
        if (end.isBefore(start) || end.isAfter(start.plusDays(6))) {
            throw new IllegalArgumentException("预约看板最多查询连续 7 天");
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select w.id, date_format(w.service_time, '%Y-%m-%d') as serviceDate, w.product_id as productId, p.category, hour(w.service_time) as hour, w.service_item as serviceName, w.amount, " +
                        "concat(date_format(w.service_time, '%H:%i'), '-', date_format(coalesce(w.complete_time, date_add(w.service_time, interval 1 hour)), '%H:%i')) as timeRange, " +
                        "u.real_name as userName, " +
                        "case w.status when 'pending' then '待服务' when 'service_in' then '服务中' when 'completed' then '已完成' when 'cancelled' then '已取消' else w.status end as status " +
                        "from work_order w left join `user` u on w.customer_id = u.id left join product p on w.product_id = p.id " +
                        "where date(w.service_time) between ? and ? order by w.service_time",
                start,
                end
        );
        return query.filter(rows);
    }

    @Override
    public PageResult<Map<String, Object>> users(ResourceQuery query) {
        String like = "%" + (query.getKeyword() == null ? "" : query.getKeyword().trim()) + "%";
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
        rows = query.filter(rows);
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
                "id_card", nullIfBlank(text(payload, "idCard", "")),
                "address", text(payload, "address", "Shanghai"),
                "bio", text(payload, "bio", "Created from admin"),
                "height", decimal(payload, "height", BigDecimal.valueOf(160)),
                "weight", decimal(payload, "weight", BigDecimal.valueOf(60)),
                "ethnicity", text(payload, "ethnicity", "汉族"),
                "education", text(payload, "education", "高中"),
                "blood_type", text(payload, "bloodType", "A"),
                "rh_negative", booleanCode(payload.get("rhNegative")),
                "chronic_disease", text(payload, "chronicDisease", "None"),
                "sleep_quality", text(payload, "sleepQuality", "良好"),
                "smoking_freq", text(payload, "smokingFreq", "None"),
                "drinking_freq", text(payload, "drinkingFreq", "None"),
                "exercise_freq", text(payload, "exerciseFreq", "Weekly"),
                "diet_preference", text(payload, "dietPreference", "清淡"),
                "emergency_contact", text(payload, "emergencyContact", ""),
                "emergency_phone", text(payload, "emergencyPhone", ""),
                "avatar_url", text(payload, "avatarUrl", ""),
                "last_login_time", null,
                "last_buy_time", null,
                "status", 1
        );
        Number id = insert("user", values);
        syncElderlyAccount(id.longValue());
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
        putIfPresent(values, "id_card", payload, "idCard");
        putIfPresent(values, "address", payload, "address");
        putIfPresent(values, "bio", payload, "bio");
        putIfPresent(values, "ethnicity", payload, "ethnicity");
        putIfPresent(values, "education", payload, "education");
        putIfPresent(values, "height", payload, "height");
        putIfPresent(values, "weight", payload, "weight");
        putIfPresent(values, "blood_type", payload, "bloodType");
        if (payload.containsKey("rhNegative")) values.put("rh_negative", booleanCode(payload.get("rhNegative")));
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
        syncElderlyAccount(id);
        accepted("updateUser:" + id);
        return record("accepted", true, "id", id, "resource", "users");
    }

    @Override
    public Map<String, Object> deleteUser(Long id) {
        jdbcTemplate.update("delete from account where id in (select account_id from elderly_profile where legacy_user_id = ?) and role_type = 'elderly'", id);
        jdbcTemplate.update("delete from elderly_profile where legacy_user_id = ?", id);
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
                        "date_format(u.birthday, '%Y-%m-%d') as birthday, u.phone, u.id_card as idCard, u.address, u.bio, u.ethnicity, u.education, u.avatar_url as avatarUrl, u.height, u.weight, u.blood_type as bloodType, u.rh_negative = 1 as rhNegative, " +
                        "u.chronic_disease as chronicDisease, u.sleep_quality as sleepQuality, u.smoking_freq as smokingFreq, u.drinking_freq as drinkingFreq, u.exercise_freq as exerciseFreq, u.diet_preference as dietPreference, " +
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
                "select id, data_type as dataType, value, unit, date_format(record_date, '%Y-%m-%d') as recordDate, " +
                        "date_format(record_time, '%H:%i:%s') as recordTime, source, device_id as deviceId " +
                        "from health_data where user_id = ? order by record_date, record_time, id",
                id
        ));
        row.put("devices", jdbcTemplate.queryForList("select id, device_name as deviceName, device_type as deviceType, device_code as deviceCode, if(status = 1, '绑定', '解绑') as status from device where user_id = ? order by id", id));
        row.put("reports", jdbcTemplate.queryForList("select id, title, report_type as reportType, date_format(report_date, '%Y-%m-%d') as reportDate, file_url as fileUrl, doctor_name as doctorName, summary from report where user_id = ? order by report_date desc", id));
        row.put("orders", jdbcTemplate.queryForList("select id, order_no as orderNo, product_id as productId, product_name as productName, amount, case status when 'pending_accept' then '待接单' when 'pending_service' then '待服务' when 'completed' then '已完成' when 'closed' then '已关闭' when 'after_sale' then '售后中' else status end as status, service_type as serviceType from service_order where buyer_id = ? order by id desc", id));
        row.put("coupons", jdbcTemplate.queryForList("select id, coupon_no as couponNo, name, type, discount, status, date_format(expire_date, '%Y-%m-%d') as expireDate from coupon where user_id = ? order by id desc", id));
        row.put("points", jdbcTemplate.queryForList("select id, points, total_earned as totalEarned, total_spent as totalSpent, level, growth_value as growthValue from user_points where user_id = ?", id));
        row.put("contents", jdbcTemplate.queryForList("select id, title, type, publisher, author, if(status = 1, '已发布', '草稿') as status from operation_content where publisher = ? order by id desc", row.get("realName")));
        row.put("serviceRecords", jdbcTemplate.queryForList(
                "select w.id, w.order_no as orderNo, w.product_id as productId, w.service_item as serviceItem, w.amount, " +
                        "p.id as personnelId, p.name as personnelName, " +
                        "case w.status when 'pending' then '待服务' when 'service_in' then '服务中' when 'completed' then '已完成' when 'cancelled' then '已取消' else w.status end as status, " +
                        "date_format(w.service_time, '%Y-%m-%d %H:%i:%s') as serviceTime, date_format(w.complete_time, '%Y-%m-%d %H:%i:%s') as completeTime " +
                        "from work_order w left join service_personnel p on w.personnel_id = p.id where w.customer_id = ? order by w.id desc",
                id
        ));
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
    public PageResult<Map<String, Object>> messages() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select m.id, m.user_id as userId, u.real_name as userName, u.phone, m.content, " +
                        "case m.status when 'pending' then '待处理' when 'processing' then '处理中' when 'resolved' then '已解决' else m.status end as status, " +
                        "date_format(m.created_at, '%Y-%m-%d %H:%i') as createdAt, date_format(m.updated_at, '%Y-%m-%d %H:%i') as updatedAt " +
                        "from elderly_message m join `user` u on u.id = m.user_id order by m.created_at desc, m.id desc"
        );
        Map<String, Map<String, Object>> grouped = new LinkedHashMap<String, Map<String, Object>>();
        for (Map<String, Object> row : rows) {
            String userKey = String.valueOf(row.get("userId"));
            Map<String, Object> group = grouped.get(userKey);
            if (group == null) {
                group = record(
                        "userId", row.get("userId"),
                        "userName", row.get("userName"),
                        "phone", row.get("phone"),
                        "messageCount", 0,
                        "pendingCount", 0,
                        "lastMessageTime", row.get("createdAt"),
                        "messages", new ArrayList<Map<String, Object>>()
                );
                grouped.put(userKey, group);
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> messages = (List<Map<String, Object>>) group.get("messages");
            messages.add(record(
                    "id", row.get("id"),
                    "content", row.get("content"),
                    "status", row.get("status"),
                    "createdAt", row.get("createdAt"),
                    "updatedAt", row.get("updatedAt")
            ));
            group.put("messageCount", ((Number) group.get("messageCount")).intValue() + 1);
            if ("待处理".equals(row.get("status"))) {
                group.put("pendingCount", ((Number) group.get("pendingCount")).intValue() + 1);
            }
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>(grouped.values());
        return new PageResult<Map<String, Object>>(result.size(), result);
    }

    @Override
    public Map<String, Object> updateMessageStatus(Long id, Map<String, Object> payload) {
        String displayStatus = stringValue(payload == null ? null : payload.get("status")).trim();
        Map<String, String> statuses = new LinkedHashMap<String, String>();
        statuses.put("待处理", "pending");
        statuses.put("处理中", "processing");
        statuses.put("已解决", "resolved");
        String storedStatus = statuses.get(displayStatus);
        if (storedStatus == null) throw new IllegalArgumentException("留言状态只能是待处理、处理中或已解决");
        if (jdbcTemplate.update("update elderly_message set status = ?, updated_at = now() where id = ?", storedStatus, id) != 1) {
            throw new IllegalArgumentException("留言不存在");
        }
        return record("id", id, "status", displayStatus, "accepted", true);
    }

    @Override
    public PageResult<Map<String, Object>> resource(String name, ResourceQuery query) {
        List<Map<String, Object>> rows;
        if ("personnel".equals(name)) {
            rows = jdbcTemplate.queryForList("select id, name, phone, service_type as serviceType, area, audit_status as auditStatus, if(status = 1, '启用', '禁用') as status, date_format(created_at, '%Y-%m-%d %H:%i') as updatedAt from service_personnel order by id");
        } else if ("audits".equals(name)) {
            rows = jdbcTemplate.queryForList("select id, name, phone, service_type as serviceType, area, audit_status as auditStatus, if(status = 1, '启用', '禁用') as status, date_format(created_at, '%Y-%m-%d %H:%i') as updatedAt from service_personnel order by id");
        } else if ("workOrders".equals(name)) {
            rows = workOrders(null, null, new ResourceQuery()).getList();
        } else if ("products".equals(name)) {
            rows = jdbcTemplate.queryForList("select id, name, code, item_type as itemType, category, description, duration, price, if(status = 1, '上架', '下架') as status, date_format(updated_at, '%Y-%m-%d %H:%i') as updatedAt from product order by item_type desc, category, id");
        } else if ("orders".equals(name)) {
            rows = jdbcTemplate.queryForList("select o.id, o.order_no as orderNo, o.product_id as productId, o.product_name as productName, u.real_name as buyer, o.amount, o.service_type as serviceType, case o.status when 'pending_accept' then '待接单' when 'pending_service' then '待服务' when 'completed' then '已完成' when 'closed' then '已关闭' when 'after_sale' then '售后中' else o.status end as status, date_format(o.created_at, '%Y-%m-%d %H:%i') as createdAt from service_order o left join `user` u on o.buyer_id = u.id order by o.id");
        } else if ("afterSales".equals(name)) {
            rows = jdbcTemplate.queryForList("select a.id, o.order_no as orderNo, u.real_name as applicant, a.reason, a.status, date_format(a.created_at, '%Y-%m-%d %H:%i') as createdAt from after_sale a left join service_order o on a.order_id = o.id left join `user` u on a.applicant_id = u.id order by a.id");
        } else if ("reviews".equals(name)) {
            rows = jdbcTemplate.queryForList("select r.id, p.name as productName, r.product_id as productId, u.real_name as user, r.rating, r.content, if(r.visible = 1, '已显示', '已隐藏') as status, date_format(r.created_at, '%Y-%m-%d %H:%i') as createdAt from review r left join product p on r.product_id = p.id left join `user` u on r.user_id = u.id order by r.id");
        } else if ("staffs".equals(name)) {
            rows = jdbcTemplate.queryForList("select s.id, s.staff_no as staffNo, s.name, s.phone, s.role_id as roleId, r.name as role, s.remark, if(s.status = 1, '启用', '禁用') as status, date_format(s.updated_at, '%Y-%m-%d %H:%i') as updatedAt from staff s left join role r on s.role_id = r.id order by s.id");
        } else if ("roles".equals(name)) {
            rows = jdbcTemplate.queryForList("select id, name, description, '启用' as status, date_format(created_at, '%Y-%m-%d %H:%i') as createdAt from role order by id");
        } else if ("logs".equals(name)) {
            rows = jdbcTemplate.queryForList("select id, operator, action_type as actionType, target, date_format(created_at, '%Y-%m-%d %H:%i') as createdAt from operation_log order by id desc");
        } else if ("agreements".equals(name)) {
            rows = jdbcTemplate.queryForList("select id, title, type, if(status = 1, '启用', '禁用') as status, date_format(updated_at, '%Y-%m-%d %H:%i') as updatedAt from agreement order by id");
        } else if (phaseTableName(name) != null) {
            rows = phaseRows(name);
        } else {
            rows = operationContent(name);
        }
        rows = query.filter(rows);
        return new PageResult<Map<String, Object>>(rows.size(), rows);
    }

    @Override
    public PageResult<Map<String, Object>> workOrders(Long personnelId, Long customerId, ResourceQuery query) {
        StringBuilder sql = new StringBuilder(
                "select w.id, w.order_no as orderNo, w.order_id as orderId, o.order_no as serviceOrderNo, " +
                        "w.product_id as productId, w.service_item as serviceItem, w.amount, " +
                        "u.id as customerId, u.real_name as customer, u.phone as customerPhone, " +
                        "p.id as personnelId, p.name as personnelName, p.phone as personnelPhone, " +
                        "case w.status when 'pending' then '待服务' when 'service_in' then '服务中' " +
                        "when 'completed' then '已完成' when 'cancelled' then '已取消' else w.status end as status, " +
                        "date_format(w.dispatch_time, '%Y-%m-%d %H:%i') as dispatchTime, " +
                        "date_format(w.dispatch_time, '%Y-%m-%d %H:%i') as updatedAt, " +
                        "date_format(w.service_time, '%Y-%m-%d %H:%i:%s') as serviceTime, " +
                        "date_format(w.service_time, '%Y-%m-%d') as filterDate " +
                        "from work_order w left join `user` u on w.customer_id = u.id " +
                        "left join service_personnel p on w.personnel_id = p.id " +
                        "left join service_order o on w.order_id = o.id where 1 = 1"
        );
        List<Object> args = new ArrayList<Object>();
        if (personnelId != null) {
            sql.append(" and w.personnel_id = ?");
            args.add(personnelId);
        }
        if (customerId != null) {
            sql.append(" and w.customer_id = ?");
            args.add(customerId);
        }
        sql.append(" order by w.id desc");
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        rows = query.filter(rows);
        return new PageResult<Map<String, Object>>(rows.size(), rows);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
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
            require(payload, "name", "商品服务名称不能为空");
            String category = text(payload, "category", "家政护理");
            id = insert("product", record(
                    "name", text(payload, "name", ""),
                    "code", text(payload, "code", "P-" + System.currentTimeMillis()),
                    "item_type", catalogItemType(text(payload, "itemType", "服务")),
                    "category", category,
                    "description", text(payload, "description", ""),
                    "duration", nullableLong(payload, "duration"),
                    "price", decimal(payload, "price", BigDecimal.valueOf(99)),
                    "status", statusCode(text(payload, "status", "上架")),
                    "updater", "系统管理员"
            ));
        } else if ("orders".equals(name)) {
            Map<String, Object> product = catalogItem(payload);
            long productId = ((Number) product.get("id")).longValue();
            long buyerId = userId(payload, "buyerId", firstId("user"));
            id = insert("service_order", record(
                    "order_no", text(payload, "orderNo", "OD" + System.currentTimeMillis()),
                    "product_id", productId,
                    "product_name", product.get("name"),
                    "amount", product.get("price"),
                    "buyer_id", buyerId,
                    "status", orderStatus(text(payload, "status", "待接单")),
                    "service_type", product.get("category")
            ));
        } else if ("appointments".equals(name)) {
            id = createCatalogWorkOrder(payload);
        } else if ("workOrders".equals(name)) {
            id = createCatalogWorkOrder(payload);
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
                    "password_hash", passwordEncoder.encode(text(payload, "password", "admin123")),
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
            Map<String, Object> createValues = phaseCreateValues(name, payload);
            if ("activityEnrolls".equals(name) && activeEnrollmentStatus(createValues.get("status"))) {
                ActivityEnrollmentSql.lockAndEnsureCapacity(jdbcTemplate, ((Number) createValues.get("activity_id")).longValue());
            }
            id = insert(phaseTableName(name), createValues);
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
        if ("staffs".equals(name)) {
            syncAdminAccount(id.longValue());
        }
        if ("activityEnrolls".equals(name)) {
            ActivityEnrollmentSql.syncCount(jdbcTemplate, enrollmentActivityId(id.longValue()));
        }
        accepted("createResource:" + name + ":" + id);
        return record("accepted", true, "id", id.longValue(), "resource", name);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Map<String, Object> updateResource(String name, Long id, Map<String, Object> payload) {
        if (payload == null) {
            payload = new LinkedHashMap<String, Object>();
        }
        Map<String, Object> previousEnrollment = "activityEnrolls".equals(name)
                ? enrollmentState(id) : Collections.<String, Object>emptyMap();
        Long previousActivityId = parseLong(previousEnrollment.get("activityId"));
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        if ("personnel".equals(name) || "audits".equals(name)) {
            putIfPresent(values, "name", payload, "name");
            putIfPresent(values, "phone", payload, "phone");
            putIfPresent(values, "service_type", payload, "serviceType");
            putIfPresent(values, "area", payload, "area");
            if (payload.containsKey("status")) values.put("status", statusCode(text(payload, "status", "启用")));
            putIfPresent(values, "audit_status", payload, "auditStatus");
            updateById("service_personnel", id, values);
        } else if ("products".equals(name)) {
            putIfPresent(values, "name", payload, "name");
            putIfPresent(values, "code", payload, "code");
            if (payload.containsKey("itemType")) values.put("item_type", catalogItemType(text(payload, "itemType", "服务")));
            putIfPresent(values, "category", payload, "category");
            putIfPresent(values, "description", payload, "description");
            if (payload.containsKey("duration")) values.put("duration", nullableLong(payload, "duration"));
            if (payload.containsKey("price")) values.put("price", decimal(payload, "price", BigDecimal.valueOf(99)));
            if (payload.containsKey("status")) values.put("status", statusCode(text(payload, "status", "上架")));
            values.put("updater", "系统管理员");
            updateById("product", id, values);
        } else if ("orders".equals(name)) {
            if (payload.containsKey("productId")) {
                Map<String, Object> product = catalogItem(payload);
                values.put("product_id", product.get("id"));
                values.put("product_name", product.get("name"));
                values.put("amount", product.get("price"));
                values.put("service_type", product.get("category"));
            }
            if (hasUserRef(payload, "buyerId")) values.put("buyer_id", userId(payload, "buyerId", firstId("user")));
            if (payload.containsKey("status")) values.put("status", orderStatus(text(payload, "status", "待接单")));
            updateById("service_order", id, values);
        } else if ("workOrders".equals(name) || "appointments".equals(name)) {
            if (payload.containsKey("productId")) {
                Map<String, Object> product = catalogItem(payload);
                values.put("product_id", product.get("id"));
                values.put("service_item", product.get("name"));
                values.put("amount", product.get("price"));
            }
            if (hasUserRef(payload, "customerId")) values.put("customer_id", userId(payload, "customerId", firstId("user")));
            if (payload.containsKey("personnelId")) values.put("personnel_id", selectedPersonnelId(payload));
            if (payload.containsKey("status")) values.put("status", workOrderStatus(text(payload, "status", "待服务")));
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
            if (payload.containsKey("productId")) values.put("product_id", longValue(payload, "productId", firstId("product")));
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
            Map<String, Object> phaseValues = phaseUpdateValues(name, payload);
            if ("activityEnrolls".equals(name) && !previousEnrollment.isEmpty()) {
                Long targetActivityId = phaseValues.containsKey("activity_id")
                        ? parseLong(phaseValues.get("activity_id")) : previousActivityId;
                Long previousUserId = parseLong(previousEnrollment.get("userId"));
                Long targetUserId = phaseValues.containsKey("user_id")
                        ? parseLong(phaseValues.get("user_id")) : previousUserId;
                Object targetStatus = phaseValues.containsKey("status")
                        ? phaseValues.get("status") : previousEnrollment.get("status");
                boolean activatesEnrollment = activeEnrollmentStatus(targetStatus)
                        && (!activeEnrollmentStatus(previousEnrollment.get("status"))
                        || (targetActivityId != null && !targetActivityId.equals(previousActivityId))
                        || (targetUserId != null && !targetUserId.equals(previousUserId)));
                if (activatesEnrollment) {
                    ActivityEnrollmentSql.lockAndEnsureCapacity(jdbcTemplate, targetActivityId);
                }
            }
            updateById(phaseTableName(name), id, phaseValues);
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
        if ("staffs".equals(name)) {
            if (payload.containsKey("password")) {
                jdbcTemplate.update("update staff set password_hash = ? where id = ?", passwordEncoder.encode(text(payload, "password", "admin123")), id);
            }
            syncAdminAccount(id);
        }
        if ("roles".equals(name) && payload.containsKey("permissions")) {
            jdbcTemplate.update("update role set permissions = ? where id = ?", text(payload, "permissions", "{}"), id);
        }
        if ("activityEnrolls".equals(name)) {
            Long currentActivityId = enrollmentActivityId(id);
            ActivityEnrollmentSql.syncCount(jdbcTemplate, previousActivityId);
            if (currentActivityId != null && !currentActivityId.equals(previousActivityId)) {
                ActivityEnrollmentSql.syncCount(jdbcTemplate, currentActivityId);
            }
        }
        accepted("updateResource:" + name + ":" + id);
        return record("accepted", true, "id", id, "resource", name);
    }

    @Override
    public Map<String, Object> deleteResource(String name, Long id) {
        Long activityId = "activityEnrolls".equals(name) ? enrollmentActivityId(id) : null;
        if ("staffs".equals(name)) {
            jdbcTemplate.update("delete from admin_profile where account_id = ?", id);
            jdbcTemplate.update("delete from account where id = ? and role_type = 'staff'", id);
        }
        String table = tableName(name);
        jdbcTemplate.update("delete from `" + table + "` where id = ?", id);
        if (activityId != null) {
            ActivityEnrollmentSql.syncCount(jdbcTemplate, activityId);
        }
        accepted("deleteResource:" + name + ":" + id);
        return record("accepted", true, "id", id, "resource", name);
    }

    @Override
    public Map<String, Object> analyticsOverview(ResourceQuery query) {
        LocalDate start = query.startDateOr(LocalDate.now().minusDays(29));
        LocalDate end = query.endDateOr(LocalDate.now());
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
        BigDecimal amount = jdbcTemplate.queryForObject(
                "select coalesce(sum(amount), 0) from service_order where date(created_at) between ? and ?",
                BigDecimal.class,
                start,
                end
        );
        long reviewTotal = count("select count(*) from review where date(created_at) between ? and ?", start, end);
        long goodReviews = count("select count(*) from review where rating >= 4 and date(created_at) between ? and ?", start, end);
        String goodRate = reviewTotal == 0 ? "0%" : String.format("%.1f%%", goodReviews * 100.0 / reviewTotal);
        List<Map<String, Object>> ageDistribution = jdbcTemplate.queryForList(
                "select case when timestampdiff(year, birthday, curdate()) < 70 then '60-69岁' " +
                        "when timestampdiff(year, birthday, curdate()) < 80 then '70-79岁' else '80岁以上' end as name, " +
                        "count(*) as value from `user` where date(created_at) between ? and ? group by name order by name",
                start,
                end
        );
        List<Map<String, Object>> tradeDistribution = jdbcTemplate.queryForList(
                "select service_type as name, count(*) as value from service_order where date(created_at) between ? and ? group by service_type order by value desc",
                start,
                end
        );
        List<Map<String, Object>> serviceTrend = jdbcTemplate.queryForList(
                "select date_format(created_at, '%Y-%m-%d') as day, count(*) as orders, coalesce(sum(amount), 0) as amount " +
                        "from service_order where date(created_at) between ? and ? " +
                        "group by date_format(created_at, '%Y-%m-%d') order by day",
                start,
                end
        );
        return record(
                "metrics", list(
                        record("label", "新增用户", "value", count("select count(*) from `user` where date(created_at) between ? and ?", start, end), "delta", "所选时段"),
                        record("label", "成交金额", "value", "¥" + money(amount), "delta", "所选时段"),
                        record("label", "完成工单", "value", count("select count(*) from work_order where status = 'completed' and date(coalesce(complete_time, created_at)) between ? and ?", start, end), "delta", "所选时段"),
                        record("label", "好评率", "value", goodRate, "delta", "所选时段")
                ),
                "ageDistribution", ageDistribution,
                "tradeDistribution", tradeDistribution,
                "serviceTrend", serviceTrend
        );
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
                "select id, title, publisher, likes, location, quota, author, if(status = 1, '已发布', '草稿') as status, date_format(created_at, '%Y-%m-%d %H:%i') as createdAt from operation_content where type = ? order by id",
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

    private Long enrollmentActivityId(Long enrollmentId) {
        return parseLong(enrollmentState(enrollmentId).get("activityId"));
    }

    private Map<String, Object> enrollmentState(Long enrollmentId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select activity_id as activityId, user_id as userId, status from activity_enroll where id = ?",
                enrollmentId
        );
        return rows.isEmpty() ? Collections.<String, Object>emptyMap() : rows.get(0);
    }

    private boolean activeEnrollmentStatus(Object status) {
        return "enrolled".equals(status) || "attended".equals(status);
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
        if ("healthSettings".equals(name)) return jdbcTemplate.queryForList("select h.id, u.real_name as userName, h.heart_rate_upper as heartRateUpper, h.heart_rate_lower as heartRateLower, h.step_goal as stepGoal, h.sleep_goal as sleepGoal, if(h.medication_reminder = 1, '启用', '禁用') as status, date_format(h.updated_at, '%Y-%m-%d %H:%i') as updatedAt from health_settings h left join `user` u on h.user_id = u.id order by h.id");
        if ("healthData".equals(name)) return jdbcTemplate.queryForList("select h.id, u.real_name as userName, h.data_type as dataType, h.value, h.unit, date_format(h.record_date, '%Y-%m-%d') as recordDate, date_format(h.record_time, '%H:%i:%s') as recordTime, h.source from health_data h left join `user` u on h.user_id = u.id order by h.record_date desc, h.id desc");
        if ("medications".equals(name)) return jdbcTemplate.queryForList("select m.id, u.real_name as userName, m.period, m.drug_name as drugName, m.frequency, date_format(m.take_time, '%H:%i:%s') as takeTime, m.dosage, if(m.reminder_enabled = 1, '启用', '禁用') as status, date_format(m.created_at, '%Y-%m-%d %H:%i') as createdAt from medication_record m left join `user` u on m.user_id = u.id order by m.id");
        if ("devices".equals(name)) return jdbcTemplate.queryForList("select d.id, u.real_name as userName, d.device_name as deviceName, d.device_type as deviceType, d.device_code as deviceCode, if(d.status = 1, '绑定', '解绑') as status, date_format(d.created_at, '%Y-%m-%d %H:%i') as createdAt from device d left join `user` u on d.user_id = u.id order by d.id");
        if ("reports".equals(name)) return jdbcTemplate.queryForList("select r.id, u.real_name as userName, r.title, r.report_type as reportType, date_format(r.report_date, '%Y-%m-%d') as reportDate, r.file_url as fileUrl, r.doctor_name as doctorName, r.summary from report r left join `user` u on r.user_id = u.id order by r.id");
        if ("coupons".equals(name)) return jdbcTemplate.queryForList("select id, coupon_no as couponNo, name, type, discount, min_amount as minAmount, status, date_format(expire_date, '%Y-%m-%d') as expireDate from coupon order by id");
        if ("userPoints".equals(name)) return jdbcTemplate.queryForList("select p.id, u.real_name as userName, p.points, p.total_earned as totalEarned, p.total_spent as totalSpent, p.level, p.growth_value as growthValue, date_format(p.updated_at, '%Y-%m-%d %H:%i') as updatedAt from user_points p left join `user` u on p.user_id = u.id order by p.id");
        if ("pointsRecords".equals(name)) return jdbcTemplate.queryForList("select r.id, u.real_name as userName, r.change_value as changeValue, r.reason, date_format(r.created_at, '%Y-%m-%d %H:%i') as createdAt from points_record r left join `user` u on r.user_id = u.id order by r.id desc");
        if ("memberLevels".equals(name)) return jdbcTemplate.queryForList("select id, name, min_growth as minGrowth, max_growth as maxGrowth, benefits, if(status = 1, '启用', '禁用') as status, date_format(created_at, '%Y-%m-%d %H:%i') as createdAt from member_level where name in ('普通', '银卡', '金卡') order by field(name, '普通', '银卡', '金卡'), id");
        if ("pointsRules".equals(name)) return jdbcTemplate.queryForList("select id, case action_type when 'signin' then '签到' when 'order' then '完成订单' when 'review' then '发布评价' else action_type end as actionType, description, points, growth, if(status = 1, '启用', '禁用') as status, date_format(created_at, '%Y-%m-%d %H:%i') as createdAt from points_rule where action_type in ('signin', 'order', 'review') order by field(action_type, 'signin', 'order', 'review'), id");
        if ("productCategories".equals(name)) return jdbcTemplate.queryForList("select id, name, code, description, sort_order as sortOrder, if(status = 1, '启用', '禁用') as status, date_format(created_at, '%Y-%m-%d %H:%i') as createdAt from product_category order by sort_order, id");
        if ("serviceItems".equals(name)) return jdbcTemplate.queryForList("select s.id, s.product_id as productId, p.name as productName, s.name, s.description, s.duration, s.price, if(s.status = 1, '启用', '禁用') as status, date_format(s.created_at, '%Y-%m-%d %H:%i') as createdAt from service_item s left join product p on s.product_id = p.id order by s.id");
        if ("banners".equals(name)) return jdbcTemplate.queryForList("select id, title, image_url as imageUrl, location, sort_order as sortOrder, if(status = 1, '启用', '禁用') as status, date_format(created_at, '%Y-%m-%d %H:%i') as createdAt from banner order by sort_order, id");
        if ("activities".equals(name)) return jdbcTemplate.queryForList(
                "select a.id, a.title, a.cover_url as coverUrl, a.location, date_format(a.start_time, '%Y-%m-%d %H:%i:%s') as startTime, " +
                        "date_format(a.end_time, '%Y-%m-%d %H:%i:%s') as endTime, a.quota, coalesce(ec.enrolled, 0) as enrolled, a.content, " +
                        "case a.status when 'published' then '已发布' when 'draft' then '草稿' when 'ended' then '已结束' else a.status end as status " +
                        "from activity a left join " + ActivityEnrollmentSql.LATEST_ACTIVE_COUNTS + " on ec.activity_id = a.id order by a.id"
        );
        if ("activityEnrolls".equals(name)) return jdbcTemplate.queryForList("select e.id, e.activity_id as activityId, a.title as activityTitle, u.real_name as userName, case e.status when 'enrolled' then '已报名' when 'cancelled' then '已取消' when 'attended' then '已到场' else e.status end as status, e.remark, date_format(e.enroll_time, '%Y-%m-%d %H:%i') as enrollTime from activity_enroll e left join activity a on e.activity_id = a.id left join `user` u on e.user_id = u.id order by e.id");
        if ("topics".equals(name)) return jdbcTemplate.queryForList("select id, name, description, post_count as postCount, if(status = 1, '启用', '禁用') as status, date_format(created_at, '%Y-%m-%d %H:%i') as createdAt from topic order by id");
        if ("recipes".equals(name)) return jdbcTemplate.queryForList("select id, name as title, category, ingredients, steps, calories, suitable_for as suitableFor, if(status = 1, '启用', '禁用') as status, date_format(created_at, '%Y-%m-%d %H:%i') as createdAt from recipe order by id");
        if ("articles".equals(name)) return jdbcTemplate.queryForList("select id, title, summary, content, author, category, view_count as viewCount, if(status = 1, '已发布', '草稿') as status, date_format(created_at, '%Y-%m-%d %H:%i') as createdAt from article order by id");
        if ("diseases".equals(name)) return jdbcTemplate.queryForList("select id, name as title, category, summary, symptoms, prevention, if(status = 1, '启用', '禁用') as status, date_format(created_at, '%Y-%m-%d %H:%i') as createdAt from disease order by id");
        if ("institutions".equals(name)) return jdbcTemplate.queryForList("select id, name as title, type, address, phone, rating, capacity, if(status = 1, '启用', '禁用') as status, date_format(created_at, '%Y-%m-%d %H:%i') as createdAt from institution order by id");
        if ("videos".equals(name)) return jdbcTemplate.queryForList("select id, title, lecturer, category, video_url as videoUrl, duration, view_count as viewCount, if(status = 1, '已发布', '草稿') as status, date_format(created_at, '%Y-%m-%d %H:%i') as createdAt from video order by id");
        if ("foods".equals(name)) return jdbcTemplate.queryForList("select id, name as title, category, calories, protein, fat, carbs, if(status = 1, '启用', '禁用') as status, date_format(created_at, '%Y-%m-%d %H:%i') as createdAt from food order by id");
        if ("assessments".equals(name)) return jdbcTemplate.queryForList("select id, title, description, case type when 'sleep' then '睡眠测评' when 'fall' then '跌倒风险' when 'custom' then '综合测评' else type end as type, if(status = 1, '启用', '禁用') as status, date_format(created_at, '%Y-%m-%d %H:%i') as createdAt from assessment order by id");
        if ("assessmentResults".equals(name)) return jdbcTemplate.queryForList("select r.id, a.title as assessmentTitle, u.real_name as userName, r.score, r.result, date_format(r.created_at, '%Y-%m-%d %H:%i') as createdAt from assessment_result r left join assessment a on r.assessment_id = a.id left join `user` u on r.user_id = u.id order by r.id");
        return new ArrayList<Map<String, Object>>();
    }

    private Map<String, Object> phaseCreateValues(String name, Map<String, Object> payload) {
        if ("healthSettings".equals(name)) return record("user_id", userId(payload, "userId", firstId("user")), "heart_rate_upper", longValue(payload, "heartRateUpper", 110), "heart_rate_lower", longValue(payload, "heartRateLower", 55), "step_goal", longValue(payload, "stepGoal", 6000), "sleep_goal", decimal(payload, "sleepGoal", BigDecimal.valueOf(7)), "medication_reminder", statusCode(text(payload, "status", "启用")));
        if ("healthData".equals(name)) return record("user_id", userId(payload, "userId", firstId("user")), "data_type", text(payload, "dataType", "weight"), "value", text(payload, "value", "0"), "unit", text(payload, "unit", ""), "record_date", nullIfBlank(text(payload, "recordDate", LocalDate.now().toString())), "record_time", nullIfBlank(text(payload, "recordTime", "")), "source", text(payload, "source", "后台录入"), "device_id", nullableLong(payload, "deviceId"));
        if ("medications".equals(name)) return record("user_id", userId(payload, "userId", firstId("user")), "period", text(payload, "period", "早餐"), "drug_name", text(payload, "drugName", "药品"), "frequency", text(payload, "frequency", "每天"), "take_time", nullIfBlank(text(payload, "takeTime", "")), "dosage", text(payload, "dosage", ""), "reminder_enabled", statusCode(text(payload, "status", "启用")), "source", text(payload, "source", "后台录入"), "creator", text(payload, "creator", "系统管理员"));
        if ("devices".equals(name)) return record("user_id", userId(payload, "userId", firstId("user")), "device_name", text(payload, "deviceName", "智能设备"), "device_type", text(payload, "deviceType", "band"), "device_code", text(payload, "deviceCode", "DEV" + uniqueDigits(6)), "bind_time", null, "last_sync_time", null, "status", statusCode(text(payload, "status", "绑定")));
        if ("reports".equals(name)) return record("user_id", userId(payload, "userId", firstId("user")), "title", text(payload, "title", "健康报告"), "report_type", text(payload, "reportType", "健康评估"), "report_date", nullIfBlank(text(payload, "reportDate", LocalDate.now().toString())), "file_url", text(payload, "fileUrl", ""), "summary", text(payload, "summary", ""), "doctor_name", text(payload, "doctorName", "系统医生"));
        if ("coupons".equals(name)) return record("user_id", nullableUserId(payload, "userId"), "coupon_no", text(payload, "couponNo", "CP" + System.currentTimeMillis()), "name", text(payload, "name", "优惠券"), "type", text(payload, "type", "满减"), "discount", decimal(payload, "discount", BigDecimal.valueOf(10)), "min_amount", decimal(payload, "minAmount", BigDecimal.ZERO), "status", text(payload, "status", "未使用"), "expire_date", nullIfBlank(text(payload, "expireDate", LocalDate.now().plusDays(30).toString())));
        if ("userPoints".equals(name)) return record("user_id", userId(payload, "userId", firstId("user")), "points", longValue(payload, "points", 0), "total_earned", longValue(payload, "totalEarned", 0), "total_spent", longValue(payload, "totalSpent", 0), "level", text(payload, "level", "普通"), "growth_value", longValue(payload, "growthValue", 0));
        if ("pointsRecords".equals(name)) return record("user_id", userId(payload, "userId", firstId("user")), "change_value", longValue(payload, "changeValue", 1), "reason", text(payload, "reason", "后台调整"));
        if ("memberLevels".equals(name)) return record("name", memberLevelName(text(payload, "name", "普通")), "min_growth", longValue(payload, "minGrowth", 0), "max_growth", nullableLong(payload, "maxGrowth"), "icon", text(payload, "icon", ""), "benefits", text(payload, "benefits", ""), "status", statusCode(text(payload, "status", "启用")));
        if ("pointsRules".equals(name)) return record("action_type", pointActionType(text(payload, "actionType", "签到")), "description", text(payload, "description", "积分规则"), "points", longValue(payload, "points", 1), "growth", longValue(payload, "growth", 1), "daily_limit", nullableLong(payload, "dailyLimit"), "status", statusCode(text(payload, "status", "启用")));
        if ("productCategories".equals(name)) return record("name", text(payload, "name", "新分类"), "code", text(payload, "code", "CAT" + uniqueDigits(4)), "description", text(payload, "description", ""), "sort_order", longValue(payload, "sortOrder", 0), "status", statusCode(text(payload, "status", "启用")));
        if ("serviceItems".equals(name)) return record("product_id", longValue(payload, "productId", firstId("product")), "name", text(payload, "name", "服务项目"), "description", text(payload, "description", ""), "duration", longValue(payload, "duration", 60), "price", decimal(payload, "price", BigDecimal.valueOf(99)), "status", statusCode(text(payload, "status", "启用")));
        if ("banners".equals(name)) return record("title", text(payload, "title", "轮播图"), "image_url", text(payload, "imageUrl", "https://example.com/banner.png"), "link_url", text(payload, "linkUrl", ""), "sort_order", longValue(payload, "sortOrder", 0), "location", text(payload, "location", "home"), "status", statusCode(text(payload, "status", "启用")));
        if ("activities".equals(name)) return record("title", text(payload, "title", "活动"), "cover_url", text(payload, "coverUrl", ""), "location", text(payload, "location", ""), "start_time", text(payload, "startTime", LocalDate.now().toString() + " 09:00:00"), "end_time", nullIfBlank(text(payload, "endTime", "")), "quota", longValue(payload, "quota", 50), "enrolled", longValue(payload, "enrolled", 0), "status", activityStatus(text(payload, "status", "已发布")), "content", text(payload, "content", ""));
        if ("activityEnrolls".equals(name)) return record("activity_id", longValue(payload, "activityId", firstId("activity")), "user_id", userId(payload, "userId", firstId("user")), "status", activityEnrollStatus(text(payload, "status", "已报名")), "remark", text(payload, "remark", ""));
        if ("topics".equals(name)) return record("name", text(payload, "name", "新话题"), "description", text(payload, "description", ""), "icon", text(payload, "icon", ""), "post_count", longValue(payload, "postCount", 0), "status", statusCode(text(payload, "status", "启用")));
        if ("recipes".equals(name)) return record("name", text(payload, "title", text(payload, "name", "新菜谱")), "category", text(payload, "category", "午餐"), "ingredients", text(payload, "ingredients", "食材"), "steps", text(payload, "steps", "步骤"), "calories", longValue(payload, "calories", 300), "suitable_for", text(payload, "suitableFor", ""), "cover_url", text(payload, "coverUrl", ""), "status", statusCode(text(payload, "status", "启用")));
        if ("articles".equals(name)) return record("title", text(payload, "title", "新资讯"), "summary", text(payload, "summary", ""), "content", text(payload, "content", ""), "cover_url", text(payload, "coverUrl", ""), "author", text(payload, "author", "运营中心"), "category", text(payload, "category", "养生"), "tags", text(payload, "tags", ""), "view_count", longValue(payload, "viewCount", 0), "status", statusCode(text(payload, "status", "已发布")));
        if ("diseases".equals(name)) return record("name", text(payload, "title", text(payload, "name", "疾病")), "category", text(payload, "category", "常见病"), "summary", text(payload, "summary", ""), "symptoms", text(payload, "symptoms", ""), "prevention", text(payload, "prevention", ""), "diet_advice", text(payload, "dietAdvice", ""), "risk_factors", text(payload, "riskFactors", ""), "status", statusCode(text(payload, "status", "启用")));
        if ("institutions".equals(name)) return record("name", text(payload, "title", text(payload, "name", "机构")), "address", text(payload, "address", ""), "phone", text(payload, "phone", ""), "type", text(payload, "type", "养老院"), "rating", decimal(payload, "rating", BigDecimal.valueOf(4.5)), "capacity", longValue(payload, "capacity", 100), "price_range", text(payload, "priceRange", ""), "services", text(payload, "services", ""), "images", text(payload, "images", ""), "status", statusCode(text(payload, "status", "启用")));
        if ("videos".equals(name)) return record("title", text(payload, "title", "视频"), "description", text(payload, "description", ""), "cover_url", text(payload, "coverUrl", ""), "video_url", text(payload, "videoUrl", "https://example.com/video.mp4"), "duration", longValue(payload, "duration", 600), "lecturer", text(payload, "lecturer", ""), "category", text(payload, "category", "健康"), "view_count", longValue(payload, "viewCount", 0), "status", statusCode(text(payload, "status", "已发布")));
        if ("foods".equals(name)) return record("name", text(payload, "title", text(payload, "name", "食物")), "category", text(payload, "category", "主食"), "calories", longValue(payload, "calories", 100), "protein", decimal(payload, "protein", BigDecimal.ZERO), "fat", decimal(payload, "fat", BigDecimal.ZERO), "carbs", decimal(payload, "carbs", BigDecimal.ZERO), "fiber", decimal(payload, "fiber", BigDecimal.ZERO), "sodium", decimal(payload, "sodium", BigDecimal.ZERO), "suitable_for", text(payload, "suitableFor", ""), "status", statusCode(text(payload, "status", "启用")));
        if ("assessments".equals(name)) return record("title", text(payload, "title", "测评"), "description", text(payload, "description", ""), "type", assessmentType(text(payload, "type", "综合测评")), "questions", text(payload, "questions", "[]"), "scoring_rules", text(payload, "scoringRules", "{}"), "status", statusCode(text(payload, "status", "启用")));
        if ("assessmentResults".equals(name)) return record("assessment_id", longValue(payload, "assessmentId", firstId("assessment")), "user_id", userId(payload, "userId", firstId("user")), "score", longValue(payload, "score", 0), "result", text(payload, "result", ""), "answers", text(payload, "answers", "{}"));
        return record("title", text(payload, "title", "新增内容"));
    }

    private Map<String, Object> phaseUpdateValues(String name, Map<String, Object> payload) {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        if ("healthSettings".equals(name)) {
            if (hasUserRef(payload, "userId")) values.put("user_id", userId(payload, "userId", firstId("user")));
            if (payload.containsKey("heartRateUpper")) values.put("heart_rate_upper", longValue(payload, "heartRateUpper", 110));
            if (payload.containsKey("heartRateLower")) values.put("heart_rate_lower", longValue(payload, "heartRateLower", 55));
            if (payload.containsKey("stepGoal")) values.put("step_goal", longValue(payload, "stepGoal", 6000));
            if (payload.containsKey("sleepGoal")) values.put("sleep_goal", decimal(payload, "sleepGoal", BigDecimal.valueOf(7)));
            if (payload.containsKey("status")) values.put("medication_reminder", statusCode(text(payload, "status", "启用")));
        } else if ("devices".equals(name)) {
            if (hasUserRef(payload, "userId")) values.put("user_id", userId(payload, "userId", firstId("user")));
            putIfPresent(values, "device_name", payload, "deviceName");
            putIfPresent(values, "device_type", payload, "deviceType");
            putIfPresent(values, "device_code", payload, "deviceCode");
            if (payload.containsKey("status")) values.put("status", statusCode(text(payload, "status", "绑定")));
        } else if ("reports".equals(name)) {
            if (hasUserRef(payload, "userId")) values.put("user_id", userId(payload, "userId", firstId("user")));
            putIfPresent(values, "title", payload, "title");
            putIfPresent(values, "report_type", payload, "reportType");
            putIfPresent(values, "report_date", payload, "reportDate");
            putIfPresent(values, "file_url", payload, "fileUrl");
            putIfPresent(values, "summary", payload, "summary");
            putIfPresent(values, "doctor_name", payload, "doctorName");
        } else if ("coupons".equals(name)) {
            if (hasUserRef(payload, "userId")) values.put("user_id", nullableUserId(payload, "userId"));
            putIfPresent(values, "coupon_no", payload, "couponNo");
            putIfPresent(values, "name", payload, "name");
            putIfPresent(values, "type", payload, "type");
            if (payload.containsKey("discount")) values.put("discount", decimal(payload, "discount", BigDecimal.ZERO));
            if (payload.containsKey("minAmount")) values.put("min_amount", decimal(payload, "minAmount", BigDecimal.ZERO));
            putIfPresent(values, "status", payload, "status");
            putIfPresent(values, "expire_date", payload, "expireDate");
        } else if ("userPoints".equals(name)) {
            if (hasUserRef(payload, "userId")) values.put("user_id", userId(payload, "userId", firstId("user")));
            if (payload.containsKey("points")) values.put("points", longValue(payload, "points", 0));
            if (payload.containsKey("totalEarned")) values.put("total_earned", longValue(payload, "totalEarned", 0));
            if (payload.containsKey("totalSpent")) values.put("total_spent", longValue(payload, "totalSpent", 0));
            putIfPresent(values, "level", payload, "level");
            if (payload.containsKey("growthValue")) values.put("growth_value", longValue(payload, "growthValue", 0));
        } else if ("memberLevels".equals(name)) {
            if (payload.containsKey("name")) values.put("name", memberLevelName(text(payload, "name", "普通")));
            if (payload.containsKey("minGrowth")) values.put("min_growth", longValue(payload, "minGrowth", 0));
            if (payload.containsKey("maxGrowth")) values.put("max_growth", nullableLong(payload, "maxGrowth"));
            putIfPresent(values, "benefits", payload, "benefits");
            if (payload.containsKey("status")) values.put("status", statusCode(text(payload, "status", "启用")));
        } else if ("pointsRules".equals(name)) {
            if (payload.containsKey("actionType")) values.put("action_type", pointActionType(text(payload, "actionType", "签到")));
            putIfPresent(values, "description", payload, "description");
            if (payload.containsKey("points")) values.put("points", longValue(payload, "points", 1));
            if (payload.containsKey("growth")) values.put("growth", longValue(payload, "growth", 1));
            if (payload.containsKey("status")) values.put("status", statusCode(text(payload, "status", "启用")));
        } else if ("serviceItems".equals(name)) {
            if (payload.containsKey("productId")) values.put("product_id", longValue(payload, "productId", firstId("product")));
            putIfPresent(values, "name", payload, "name");
            putIfPresent(values, "description", payload, "description");
            if (payload.containsKey("duration")) values.put("duration", longValue(payload, "duration", 60));
            if (payload.containsKey("price")) values.put("price", decimal(payload, "price", BigDecimal.ZERO));
            if (payload.containsKey("status")) values.put("status", statusCode(text(payload, "status", "启用")));
        } else if ("activities".equals(name)) {
            putIfPresent(values, "title", payload, "title");
            putIfPresent(values, "cover_url", payload, "coverUrl");
            putIfPresent(values, "location", payload, "location");
            putIfPresent(values, "start_time", payload, "startTime");
            putIfPresent(values, "end_time", payload, "endTime");
            if (payload.containsKey("quota")) values.put("quota", longValue(payload, "quota", 50));
            if (payload.containsKey("status")) values.put("status", activityStatus(text(payload, "status", "已发布")));
            putIfPresent(values, "content", payload, "content");
        } else if ("activityEnrolls".equals(name)) {
            if (payload.containsKey("activityId")) values.put("activity_id", longValue(payload, "activityId", firstId("activity")));
            if (hasUserRef(payload, "userId")) values.put("user_id", userId(payload, "userId", firstId("user")));
            if (payload.containsKey("status")) values.put("status", activityEnrollStatus(text(payload, "status", "已报名")));
            putIfPresent(values, "remark", payload, "remark");
        } else if ("banners".equals(name)) {
            putIfPresent(values, "title", payload, "title");
            putIfPresent(values, "image_url", payload, "imageUrl");
            putIfPresent(values, "link_url", payload, "linkUrl");
            putIfPresent(values, "location", payload, "location");
            if (payload.containsKey("sortOrder")) values.put("sort_order", longValue(payload, "sortOrder", 0));
            if (payload.containsKey("status")) values.put("status", statusCode(text(payload, "status", "启用")));
        } else if ("assessments".equals(name)) {
            putIfPresent(values, "title", payload, "title");
            putIfPresent(values, "description", payload, "description");
            if (payload.containsKey("type")) values.put("type", assessmentType(text(payload, "type", "综合测评")));
            if (payload.containsKey("status")) values.put("status", statusCode(text(payload, "status", "启用")));
        } else {
            values.putAll(phaseCreateValues(name, payload));
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

    private AuthenticatedUser currentUser() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            HttpServletRequest request = attributes.getRequest();
            Object value = request.getAttribute(JwtAuthFilter.USER_ATTRIBUTE);
            return value instanceof AuthenticatedUser ? (AuthenticatedUser) value : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private Map<String, Object> loginUser(Long accountId, String roleType) {
        if ("staff".equals(roleType)) {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(staffProfileSql() + " where a.id = ? and a.status = 1 limit 1", accountId);
            return rows.isEmpty() ? new LinkedHashMap<String, Object>() : rows.get(0);
        }
        if ("elderly".equals(roleType)) {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "select a.id, a.phone, a.role_type as roleType, a.nickname, a.avatar_url as avatarUrl, " +
                            "p.legacy_user_id as legacyUserId, p.real_name as name, 'elderly' as role, " +
                            "date_format(a.updated_at, '%Y-%m-%d %H:%i:%s') as updatedAt " +
                            "from account a join elderly_profile p on p.account_id = a.id " +
                            "where a.id = ? and a.status = 1 limit 1",
                    accountId
            );
            return rows.isEmpty() ? new LinkedHashMap<String, Object>() : rows.get(0);
        }
        if ("service".equals(roleType)) {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "select a.id, a.phone, a.role_type as roleType, a.nickname, a.avatar_url as avatarUrl, " +
                            "p.legacy_personnel_id as legacyPersonnelId, p.real_name as name, 'service' as role, " +
                            "p.service_type as serviceType, p.area, p.audit_status as auditStatus, " +
                            "date_format(a.updated_at, '%Y-%m-%d %H:%i:%s') as updatedAt " +
                            "from account a join service_profile p on p.account_id = a.id " +
                            "where a.id = ? and a.status = 1 limit 1",
                    accountId
            );
            return rows.isEmpty() ? new LinkedHashMap<String, Object>() : rows.get(0);
        }
        return new LinkedHashMap<String, Object>();
    }

    private String staffProfileSql() {
        return "select a.id, a.role_type as roleType, p.staff_no as staffNo, p.real_name as name, a.phone, a.avatar_url as avatarUrl, p.remark, " +
                "p.role_id as roleId, r.name as role, r.permissions, date_format(a.updated_at, '%Y-%m-%d %H:%i:%s') as updatedAt " +
                "from account a join admin_profile p on p.account_id = a.id left join role r on p.role_id = r.id";
    }

    private Map<String, List<String>> permissionsMap(Object value) {
        String json = stringValue(value);
        if (json.trim().length() == 0) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, List<String>>>() {});
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private void syncAdminAccount(Long staffId) {
        jdbcTemplate.update(
                "insert into account(id, phone, password_hash, role_type, nickname, avatar_url, status, created_at, updated_at) " +
                        "select id, phone, password_hash, 'staff', name, avatar_url, status, created_at, updated_at from staff where id = ? " +
                        "on duplicate key update phone = values(phone), password_hash = values(password_hash), nickname = values(nickname), " +
                        "avatar_url = values(avatar_url), status = values(status), updated_at = values(updated_at)",
                staffId
        );
        jdbcTemplate.update(
                "insert into admin_profile(account_id, staff_no, real_name, role_id, remark, updated_at) " +
                        "select id, staff_no, name, role_id, remark, updated_at from staff where id = ? " +
                        "on duplicate key update staff_no = values(staff_no), real_name = values(real_name), " +
                        "role_id = values(role_id), remark = values(remark), updated_at = values(updated_at)",
                staffId
        );
    }

    private void syncElderlyAccount(Long userId) {
        List<Long> accountIds = jdbcTemplate.queryForList(
                "select account_id from elderly_profile where legacy_user_id = ? limit 1",
                Long.class,
                userId
        );
        if (accountIds.isEmpty()) {
            accountIds = jdbcTemplate.queryForList(
                    "select a.id from account a join `user` u on u.phone = a.phone where u.id = ? and a.role_type = 'elderly' limit 1",
                    Long.class,
                    userId
            );
        }
        Long accountId = accountIds.isEmpty() ? userId : accountIds.get(0);
        jdbcTemplate.update(
                "insert into account(id, phone, password_hash, role_type, nickname, avatar_url, status, last_login_time, created_at, updated_at) " +
                        "select ?, phone, ?, 'elderly', nickname, avatar_url, status, last_login_time, created_at, updated_at from `user` where id = ? " +
                        "on duplicate key update phone = values(phone), nickname = values(nickname), avatar_url = values(avatar_url), " +
                        "status = values(status), last_login_time = values(last_login_time), updated_at = values(updated_at)",
                accountId,
                passwordEncoder.encode("753951"),
                userId
        );
        jdbcTemplate.update(
                "insert into elderly_profile(account_id, legacy_user_id, real_name, gender, birthday, id_card, address, bio, height, weight, " +
                        "ethnicity, education, blood_type, rh_negative, chronic_disease, sleep_quality, smoking_freq, drinking_freq, exercise_freq, " +
                        "diet_preference, emergency_contact, emergency_phone, last_buy_time) " +
                        "select ?, id, real_name, gender, birthday, id_card, address, bio, height, weight, ethnicity, education, blood_type, " +
                        "rh_negative, chronic_disease, sleep_quality, smoking_freq, drinking_freq, exercise_freq, diet_preference, " +
                        "emergency_contact, emergency_phone, last_buy_time from `user` where id = ? " +
                        "on duplicate key update real_name = values(real_name), gender = values(gender), birthday = values(birthday), " +
                        "id_card = values(id_card), address = values(address), bio = values(bio), height = values(height), weight = values(weight), " +
                        "ethnicity = values(ethnicity), education = values(education), blood_type = values(blood_type), " +
                        "rh_negative = values(rh_negative), chronic_disease = values(chronic_disease), sleep_quality = values(sleep_quality), " +
                        "smoking_freq = values(smoking_freq), drinking_freq = values(drinking_freq), exercise_freq = values(exercise_freq), " +
                        "diet_preference = values(diet_preference), emergency_contact = values(emergency_contact), " +
                        "emergency_phone = values(emergency_phone), last_buy_time = values(last_buy_time)",
                accountId,
                userId
        );
    }

    private Number createCatalogWorkOrder(Map<String, Object> payload) {
        AuthenticatedUser creator = currentUser();
        Map<String, Object> product = catalogItem(payload);
        long productId = ((Number) product.get("id")).longValue();
        long customerId = userId(payload, "customerId", firstId("user"));
        long personnelId = selectedPersonnelId(payload);
        Number orderId = insert("service_order", record(
                "order_no", uniqueBusinessNo("OD"),
                "product_id", productId,
                "product_name", product.get("name"),
                "amount", product.get("price"),
                "buyer_id", customerId,
                "status", "pending_service",
                "service_type", product.get("category")
        ));
        return insert("work_order", record(
                "order_no", text(payload, "orderNo", uniqueBusinessNo("WO")),
                "order_id", orderId.longValue(),
                "product_id", productId,
                "service_item", product.get("name"),
                "amount", product.get("price"),
                "personnel_id", personnelId,
                "customer_id", customerId,
                "created_by_account_id", creator == null ? null : creator.getAccountId(),
                "created_by_role", creator == null ? "staff" : creator.getRoleType(),
                "status", workOrderStatus(text(payload, "status", "待服务")),
                "dispatch_time", Timestamp.valueOf(LocalDateTime.now()),
                "service_time", nullIfBlank(text(payload, "serviceTime", "")),
                "complete_time", nullIfBlank(text(payload, "completeTime", "")),
                "cancel_reason", null
        ));
    }

    private Map<String, Object> catalogItem(Map<String, Object> payload) {
        Long productId = parseLong(payload == null ? null : payload.get("productId"));
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("请选择商品服务");
        }
        Map<String, Object> product = firstRow(
                "select id, name, category, price from product where id = ? and status = 1 limit 1",
                productId
        );
        if (product.isEmpty()) {
            throw new IllegalArgumentException("所选商品服务不存在或已下架");
        }
        return product;
    }

    private Long selectedPersonnelId(Map<String, Object> payload) {
        Long requested = parseLong(payload == null ? null : payload.get("personnelId"));
        if (requested == null || requested <= 0) throw new IllegalArgumentException("请选择服务人员");
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select id from service_personnel where id = ? and status = 1 and audit_status = '已通过' limit 1",
                requested
        );
        if (rows.isEmpty()) throw new IllegalArgumentException("所选服务人员不存在或不可接单");
        return requested;
    }

    private String uniqueBusinessNo(String prefix) {
        return prefix + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
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
        return "禁用".equals(value) || "下架".equals(value) || "草稿".equals(value) || "解绑".equals(value) ? 0 : 1;
    }

    private int booleanCode(Object value) {
        if (value instanceof Boolean) return ((Boolean) value) ? 1 : 0;
        String normalized = stringValue(value).trim();
        return "1".equals(normalized) || "true".equalsIgnoreCase(normalized) || "是".equals(normalized) ? 1 : 0;
    }

    private String catalogItemType(String value) {
        if ("商品".equals(value) || "服务".equals(value)) {
            return value;
        }
        throw new IllegalArgumentException("类型只能为商品或服务");
    }

    private String orderStatus(String value) {
        if ("待接单".equals(value)) return "pending_accept";
        if ("待服务".equals(value)) return "pending_service";
        if ("已完成".equals(value)) return "completed";
        if ("已关闭".equals(value)) return "closed";
        if ("售后中".equals(value)) return "after_sale";
        return value;
    }

    private String workOrderStatus(String value) {
        if ("待服务".equals(value)) return "pending";
        if ("服务中".equals(value)) return "service_in";
        if ("已完成".equals(value)) return "completed";
        if ("已取消".equals(value)) return "cancelled";
        return value;
    }

    private String activityEnrollStatus(String value) {
        if ("已报名".equals(value)) return "enrolled";
        if ("已取消".equals(value)) return "cancelled";
        if ("已到场".equals(value)) return "attended";
        return value;
    }

    private String activityStatus(String value) {
        if ("已发布".equals(value)) return "published";
        if ("草稿".equals(value)) return "draft";
        if ("已结束".equals(value)) return "ended";
        return value;
    }

    private String pointActionType(String value) {
        if ("签到".equals(value)) return "signin";
        if ("完成订单".equals(value)) return "order";
        if ("发布评价".equals(value)) return "review";
        return value;
    }

    private String memberLevelName(String value) {
        if (MEMBER_LEVEL_NAMES.contains(value)) {
            return value;
        }
        throw new IllegalArgumentException("等级名称只能为普通、银卡、金卡");
    }

    private String assessmentType(String value) {
        if ("睡眠测评".equals(value)) return "sleep";
        if ("跌倒风险".equals(value)) return "fall";
        if ("综合测评".equals(value)) return "custom";
        return value;
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
