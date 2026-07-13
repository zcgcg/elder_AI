package com.daisy.health.service;

import com.daisy.health.common.AuthenticatedUser;
import com.daisy.health.common.JwtAuthFilter;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Profile("mysql")
@Transactional
public class JdbcPortalDataService implements PortalDataService {
    private static final List<String> SERVICE_STATUSES = Arrays.asList("pending", "service_in", "completed", "cancelled");

    private final JdbcTemplate jdbcTemplate;

    public JdbcPortalDataService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<String, Object> elderlyProfile() {
        long userId = currentLegacyUserId();
        return one(
                "select a.id as accountId, u.id as userId, u.phone, u.nickname, u.avatar_url as avatarUrl, " +
                        "u.real_name as realName, case u.gender when 1 then '男' when 2 then '女' else '未知' end as gender, " +
                        "date_format(u.birthday, '%Y-%m-%d') as birthday, u.id_card as idCard, u.address, u.bio, u.height, u.weight, " +
                        "u.ethnicity, u.education, u.blood_type as bloodType, u.rh_negative = 1 as rhNegative, " +
                        "u.chronic_disease as chronicDisease, u.sleep_quality as sleepQuality, u.smoking_freq as smokingFreq, " +
                        "u.drinking_freq as drinkingFreq, u.exercise_freq as exerciseFreq, u.diet_preference as dietPreference, " +
                        "u.emergency_contact as emergencyContact, u.emergency_phone as emergencyPhone, " +
                        "date_format(u.last_buy_time, '%Y-%m-%d') as lastBuyTime " +
                        "from elderly_profile p join account a on a.id = p.account_id join `user` u on u.id = p.legacy_user_id " +
                        "where u.id = ?",
                userId
        );
    }

    @Override
    public Map<String, Object> updateElderlyProfile(Map<String, Object> payload) {
        requireRole("elderly");
        long userId = currentLegacyUserId();
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        putProfileValue(values, payload, "nickname", "nickname", false);
        putProfileValue(values, payload, "realName", "real_name", false);
        putProfileValue(values, payload, "phone", "phone", false);
        putProfileValue(values, payload, "avatarUrl", "avatar_url", true);
        if (payload != null && payload.containsKey("gender")) values.put("gender", genderCode(stringValue(payload.get("gender"))));
        putProfileValue(values, payload, "birthday", "birthday", true);
        putProfileValue(values, payload, "idCard", "id_card", true);
        putProfileValue(values, payload, "address", "address", true);
        putProfileValue(values, payload, "bio", "bio", true);
        putProfileValue(values, payload, "height", "height", true);
        putProfileValue(values, payload, "weight", "weight", true);
        putProfileValue(values, payload, "ethnicity", "ethnicity", true);
        putProfileValue(values, payload, "education", "education", true);
        putProfileValue(values, payload, "bloodType", "blood_type", true);
        if (payload != null && payload.containsKey("rhNegative")) values.put("rh_negative", booleanCode(payload.get("rhNegative")));
        putProfileValue(values, payload, "chronicDisease", "chronic_disease", true);
        putProfileValue(values, payload, "sleepQuality", "sleep_quality", true);
        putProfileValue(values, payload, "smokingFreq", "smoking_freq", true);
        putProfileValue(values, payload, "drinkingFreq", "drinking_freq", true);
        putProfileValue(values, payload, "exerciseFreq", "exercise_freq", true);
        putProfileValue(values, payload, "dietPreference", "diet_preference", true);
        putProfileValue(values, payload, "emergencyContact", "emergency_contact", true);
        putProfileValue(values, payload, "emergencyPhone", "emergency_phone", true);
        if (values.containsKey("real_name") && stringValue(values.get("real_name")).trim().isEmpty()) {
            throw new IllegalArgumentException("真实姓名不能为空");
        }
        if (values.containsKey("phone") && stringValue(values.get("phone")).trim().isEmpty()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        updateById("user", userId, values);
        syncElderlyMirrors(userId);
        return elderlyProfile();
    }

    @Override
    public Map<String, Object> updateElderlyAvatar(Map<String, Object> payload) {
        String avatarUrl = stringValue(payload == null ? null : payload.get("avatarUrl")).trim();
        validateAvatarUrl(avatarUrl);
        AuthenticatedUser current = requireRole("elderly");
        long userId = currentLegacyUserId();
        int accountUpdated = jdbcTemplate.update(
                "update account set avatar_url = ?, updated_at = now() where id = ? and role_type = 'elderly'",
                avatarUrl,
                current.getAccountId()
        );
        int userUpdated = jdbcTemplate.update(
                "update `user` set avatar_url = ?, updated_at = now() where id = ?",
                avatarUrl,
                userId
        );
        if (accountUpdated != 1 || userUpdated != 1) {
            throw new IllegalArgumentException("用户头像更新失败");
        }
        return elderlyProfile();
    }

    @Override
    public List<Map<String, Object>> elderlyHealthData() {
        return jdbcTemplate.queryForList(
                "select id, data_type as dataType, value, unit, date_format(record_date, '%Y-%m-%d') as recordDate, " +
                        "date_format(record_time, '%H:%i') as recordTime, source from health_data where user_id = ? " +
                        "order by record_date desc, record_time desc, id desc",
                currentLegacyUserId()
        );
    }

    @Override
    public List<Map<String, Object>> elderlyMedications() {
        return jdbcTemplate.queryForList(
                "select id, period, drug_name as drugName, frequency, date_format(take_time, '%H:%i') as takeTime, " +
                        "dosage, reminder_enabled = 1 as reminderEnabled, source, creator from medication_record " +
                        "where user_id = ? order by take_time, id",
                currentLegacyUserId()
        );
    }

    @Override
    public List<Map<String, Object>> elderlyDevices() {
        return jdbcTemplate.queryForList(
                "select id, device_name as deviceName, device_type as deviceType, device_code as deviceCode, " +
                        "date_format(bind_time, '%Y-%m-%d %H:%i') as bindTime, date_format(last_sync_time, '%Y-%m-%d %H:%i') as lastSyncTime, " +
                        "if(status = 1, '绑定', '解绑') as status from device where user_id = ? order by id desc",
                currentLegacyUserId()
        );
    }

    @Override
    public Map<String, Object> updateElderlyDevice(Long id, Map<String, Object> payload) {
        long userId = currentLegacyUserId();
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        putProfileValue(values, payload, "deviceName", "device_name", false);
        putProfileValue(values, payload, "deviceType", "device_type", false);
        putProfileValue(values, payload, "deviceCode", "device_code", true);
        if (payload != null && payload.containsKey("status")) {
            String status = stringValue(payload.get("status"));
            values.put("status", ("绑定".equals(status) || "启用".equals(status) || "1".equals(status)) ? 1 : 0);
        }
        if (values.containsKey("device_name") && stringValue(values.get("device_name")).trim().isEmpty()) {
            throw new IllegalArgumentException("设备名称不能为空");
        }
        List<Object> args = new ArrayList<Object>(values.values());
        StringBuilder sql = new StringBuilder("update device set ");
        int index = 0;
        for (String column : values.keySet()) {
            if (index++ > 0) sql.append(", ");
            sql.append('`').append(column).append("` = ?");
        }
        if (values.isEmpty()) return device(id, userId);
        sql.append(" where id = ? and user_id = ?");
        args.add(id);
        args.add(userId);
        if (jdbcTemplate.update(sql.toString(), args.toArray()) != 1) {
            throw new IllegalArgumentException("设备不存在或不属于当前用户");
        }
        return device(id, userId);
    }

    @Override
    public List<Map<String, Object>> elderlyReports() {
        return jdbcTemplate.queryForList(
                "select id, title, report_type as reportType, date_format(report_date, '%Y-%m-%d') as reportDate, " +
                        "file_url as fileUrl, doctor_name as doctorName, summary from report where user_id = ? order by report_date desc, id desc",
                currentLegacyUserId()
        );
    }

    @Override
    public List<Map<String, Object>> elderlyOrders() {
        return jdbcTemplate.queryForList(
                "select o.id, o.order_no as orderNo, o.product_name as productName, o.amount, o.service_type as serviceType, " +
                        "o.status, date_format(o.created_at, '%Y-%m-%d %H:%i') as createdAt, " +
                        "w.id as workOrderId, w.status as workOrderStatus, date_format(w.service_time, '%Y-%m-%d %H:%i') as serviceTime " +
                        "from service_order o left join work_order w on w.order_id = o.id " +
                        "where o.buyer_id = ? order by o.id desc",
                currentLegacyUserId()
        );
    }

    @Override
    public List<Map<String, Object>> elderlyCoupons() {
        return jdbcTemplate.queryForList(
                "select id, coupon_no as couponNo, name, type, discount, min_amount as minAmount, status, " +
                        "date_format(expire_date, '%Y-%m-%d') as expireDate from coupon where user_id = ? order by id desc",
                currentLegacyUserId()
        );
    }

    @Override
    public Map<String, Object> elderlyPoints() {
        long userId = currentLegacyUserId();
        Map<String, Object> total = one(
                "select id, points, total_earned as totalEarned, total_spent as totalSpent, level, growth_value as growthValue " +
                        "from user_points where user_id = ?",
                userId
        );
        total.put("records", jdbcTemplate.queryForList(
                "select id, change_value as changeValue, reason, date_format(created_at, '%Y-%m-%d %H:%i') as createdAt " +
                        "from points_record where user_id = ? order by id desc",
                userId
        ));
        return total;
    }

    @Override
    public List<Map<String, Object>> elderlyCatalogItems() {
        return jdbcTemplate.queryForList(
                "select id, name, item_type as itemType, category, description, duration, price " +
                        "from product where status = 1 order by item_type desc, category, id"
        );
    }

    @Override
    public List<Map<String, Object>> elderlyPersonnel() {
        currentLegacyUserId();
        return jdbcTemplate.queryForList(
                "select p.id, p.name, p.service_type as serviceType, p.area, sp.rating, p.avatar_url as avatarUrl " +
                        "from service_personnel p left join service_profile sp on sp.legacy_personnel_id = p.id " +
                        "where p.status = 1 and p.audit_status = '已通过' order by p.name, p.id"
        );
    }

    @Override
    public List<Map<String, Object>> elderlyWorkOrders() {
        long userId = currentLegacyUserId();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                workOrderSelect() + " where w.customer_id = ? order by w.created_at desc, w.id desc",
                userId
        );
        normalizeWorkOrderStatuses(rows);
        return rows;
    }

    @Override
    public Map<String, Object> createElderlyWorkOrder(Map<String, Object> payload) {
        AuthenticatedUser current = requireRole("elderly");
        long userId = currentLegacyUserId();
        long productId = requiredLong(payload, "productId", "请选择商品服务");
        Map<String, Object> product = one(
                "select id, name, category, price from product where id = ? and status = 1 limit 1",
                productId
        );
        if (product.isEmpty()) {
            throw new IllegalArgumentException("所选商品服务不存在或已下架");
        }
        long personnelId = requiredLong(payload, "personnelId", "请选择服务人员");
        if (jdbcTemplate.queryForList(
                "select id from service_personnel where id = ? and status = 1 and audit_status = '已通过' limit 1",
                personnelId
        ).isEmpty()) {
            throw new IllegalArgumentException("所选服务人员不存在或不可接单");
        }

        String serviceOrderNo = uniqueNo("OD");
        jdbcTemplate.update(
                "insert into service_order(order_no, product_id, product_name, amount, buyer_id, status, service_type) " +
                        "values(?, ?, ?, ?, ?, 'pending_service', ?)",
                serviceOrderNo, productId, product.get("name"), product.get("price"), userId, product.get("category")
        );
        Number orderId = jdbcTemplate.queryForObject(
                "select id from service_order where order_no = ? limit 1",
                Number.class,
                serviceOrderNo
        );

        String workOrderNo = uniqueNo("WO");
        String serviceTime = stringValue(payload == null ? null : payload.get("serviceTime")).trim();
        jdbcTemplate.update(
                "insert into work_order(order_no, order_id, product_id, service_item, amount, personnel_id, customer_id, created_by_account_id, created_by_role, status, dispatch_time, service_time, complete_time, cancel_reason) " +
                        "values(?, ?, ?, ?, ?, ?, ?, ?, 'elderly', 'pending', now(), nullif(?, ''), null, null)",
                workOrderNo, orderId, productId, product.get("name"), product.get("price"), personnelId, userId, current.getAccountId(), serviceTime
        );

        Map<String, Object> created = one(
                workOrderSelect() + " where w.order_no = ? and w.customer_id = ? limit 1",
                workOrderNo,
                userId
        );
        normalizeWorkOrderStatus(created);
        return created;
    }

    @Override
    public List<Map<String, Object>> elderlyActivities() {
        long userId = currentLegacyUserId();
        return jdbcTemplate.queryForList(
                "select a.id, a.title, a.cover_url as coverUrl, a.location, " +
                        "date_format(a.start_time, '%Y-%m-%d %H:%i') as startTime, date_format(a.end_time, '%Y-%m-%d %H:%i') as endTime, " +
                        "a.quota, a.enrolled, a.content, case a.status when 'published' then '已发布' when 'ended' then '已结束' else a.status end as status, " +
                        "exists(select 1 from activity_enroll e where e.activity_id = a.id and e.user_id = ? and e.status in ('enrolled', 'attended')) as joined, " +
                        "(a.status = 'published' and a.enrolled < a.quota and (a.end_time is null or a.end_time >= now())) as canJoin " +
                        "from activity a where a.status in ('published', 'ended') order by a.start_time desc, a.id desc",
                userId
        );
    }

    @Override
    public Map<String, Object> enrollElderlyActivity(Long activityId) {
        long userId = currentLegacyUserId();
        Map<String, Object> activity = one(
                "select id, title from activity where id = ? and status = 'published' " +
                        "and (end_time is null or end_time >= now()) for update",
                activityId
        );
        if (activity.isEmpty()) throw new IllegalArgumentException("活动不存在或已结束");

        List<Map<String, Object>> enrollments = jdbcTemplate.queryForList(
                "select id, status from activity_enroll where activity_id = ? and user_id = ? order by id desc limit 1",
                activityId,
                userId
        );
        if (!enrollments.isEmpty()) {
            String currentStatus = stringValue(enrollments.get(0).get("status"));
            if ("enrolled".equals(currentStatus) || "attended".equals(currentStatus)) {
                return enrollmentResult(activityId, activity.get("title"));
            }
        }

        int reserved = jdbcTemplate.update(
                "update activity set enrolled = enrolled + 1 where id = ? and status = 'published' and enrolled < quota",
                activityId
        );
        if (reserved != 1) throw new IllegalArgumentException("活动名额已满");
        if (enrollments.isEmpty()) {
            jdbcTemplate.update(
                    "insert into activity_enroll(activity_id, user_id, enroll_time, status, remark) values(?, ?, now(), 'enrolled', '')",
                    activityId,
                    userId
            );
        } else {
            jdbcTemplate.update(
                    "update activity_enroll set status = 'enrolled', enroll_time = now() where id = ? and user_id = ?",
                    enrollments.get(0).get("id"),
                    userId
            );
        }
        return enrollmentResult(activityId, activity.get("title"));
    }

    @Override
    public List<Map<String, Object>> elderlyHealthArticles() {
        currentLegacyUserId();
        return jdbcTemplate.queryForList(
                "select id, title, summary, content, cover_url as coverUrl, author, category, tags, view_count as viewCount, " +
                        "date_format(created_at, '%Y-%m-%d') as createdAt from article where status = 1 order by created_at desc, id desc"
        );
    }

    @Override
    public List<Map<String, Object>> elderlyHealthVideos() {
        currentLegacyUserId();
        return jdbcTemplate.queryForList(
                "select id, title, description, cover_url as coverUrl, video_url as videoUrl, duration, lecturer, category, " +
                        "view_count as viewCount, date_format(created_at, '%Y-%m-%d') as createdAt " +
                        "from video where status = 1 order by created_at desc, id desc"
        );
    }

    @Override
    public Map<String, Object> serviceProfile() {
        long personnelId = currentLegacyPersonnelId();
        return one(
                "select a.id as accountId, a.phone, a.nickname, a.avatar_url as avatarUrl, p.legacy_personnel_id as personnelId, " +
                        "p.real_name as realName, p.service_type as serviceType, p.area, date_format(p.join_time, '%Y-%m-%d') as joinTime, " +
                        "p.audit_status as auditStatus, p.rating from account a join service_profile p on p.account_id = a.id " +
                        "where p.legacy_personnel_id = ?",
                personnelId
        );
    }

    @Override
    public List<Map<String, Object>> serviceWorkOrders() {
        return jdbcTemplate.queryForList(workOrderSelect() + " where w.personnel_id = ? order by w.service_time desc, w.id desc", currentLegacyPersonnelId());
    }

    @Override
    public Map<String, Object> serviceWorkOrder(Long id) {
        return ownedWorkOrder(id, currentLegacyPersonnelId());
    }

    @Override
    public Map<String, Object> updateServiceWorkOrderStatus(Long id, Map<String, Object> payload) {
        long personnelId = currentLegacyPersonnelId();
        ownedWorkOrder(id, personnelId);
        String status = stringValue(payload == null ? null : payload.get("status")).trim();
        if (!SERVICE_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Unsupported work order status");
        }
        if ("completed".equals(status)) {
            jdbcTemplate.update("update work_order set status = ?, complete_time = coalesce(complete_time, now()) where id = ? and personnel_id = ?", status, id, personnelId);
        } else {
            jdbcTemplate.update("update work_order set status = ? where id = ? and personnel_id = ?", status, id, personnelId);
        }
        return serviceWorkOrder(id);
    }

    private Map<String, Object> ownedWorkOrder(Long id, long personnelId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(workOrderSelect() + " where w.id = ? and w.personnel_id = ? limit 1", id, personnelId);
        if (rows.isEmpty()) {
            throw new SecurityException("Work order is not assigned to current service account");
        }
        return rows.get(0);
    }

    private String workOrderSelect() {
        return "select w.id, w.order_no as orderNo, w.product_id as productId, w.service_item as serviceItem, w.amount, w.status, " +
                "date_format(w.dispatch_time, '%Y-%m-%d %H:%i') as dispatchTime, date_format(w.service_time, '%Y-%m-%d %H:%i') as serviceTime, " +
                "date_format(w.complete_time, '%Y-%m-%d %H:%i') as completeTime, u.real_name as customerName, u.phone as customerPhone, u.address as customerAddress, " +
                "p.id as personnelId, p.name as personnelName, p.phone as personnelPhone, " +
                "o.order_no as serviceOrderNo, o.product_name as productName, o.service_type as serviceType " +
                "from work_order w left join `user` u on w.customer_id = u.id left join service_personnel p on w.personnel_id = p.id " +
                "left join service_order o on w.order_id = o.id";
    }

    private void normalizeWorkOrderStatuses(List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            normalizeWorkOrderStatus(row);
        }
    }

    private void normalizeWorkOrderStatus(Map<String, Object> row) {
        String status = stringValue(row.get("status"));
        if ("pending".equals(status)) row.put("status", "待服务");
        if ("service_in".equals(status)) row.put("status", "服务中");
        if ("completed".equals(status)) row.put("status", "已完成");
        if ("cancelled".equals(status)) row.put("status", "已取消");
    }

    private void putProfileValue(Map<String, Object> values, Map<String, Object> payload, String key, String column, boolean emptyAsNull) {
        if (payload == null || !payload.containsKey(key)) return;
        Object raw = payload.get(key);
        Object value = raw;
        if (raw instanceof String) {
            String text = ((String) raw).trim();
            value = emptyAsNull && text.isEmpty() ? null : text;
        }
        values.put(column, value);
    }

    private void updateById(String table, long id, Map<String, Object> values) {
        if (values.isEmpty()) return;
        StringBuilder sql = new StringBuilder("update `").append(table).append("` set ");
        List<Object> args = new ArrayList<Object>();
        int index = 0;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (index++ > 0) sql.append(", ");
            sql.append('`').append(entry.getKey()).append("` = ?");
            args.add(entry.getValue());
        }
        sql.append(" where id = ?");
        args.add(id);
        if (jdbcTemplate.update(sql.toString(), args.toArray()) != 1) {
            throw new IllegalArgumentException("用户资料更新失败");
        }
    }

    private void syncElderlyMirrors(long userId) {
        jdbcTemplate.update(
                "update account a join elderly_profile p on p.account_id = a.id join `user` u on u.id = p.legacy_user_id " +
                        "set a.phone = u.phone, a.nickname = u.nickname, a.avatar_url = u.avatar_url, a.updated_at = now() " +
                        "where u.id = ? and a.role_type = 'elderly'",
                userId
        );
        jdbcTemplate.update(
                "update elderly_profile p join `user` u on u.id = p.legacy_user_id set " +
                        "p.real_name = u.real_name, p.gender = u.gender, p.birthday = u.birthday, p.id_card = u.id_card, " +
                        "p.address = u.address, p.bio = u.bio, p.height = u.height, p.weight = u.weight, p.ethnicity = u.ethnicity, " +
                        "p.education = u.education, p.blood_type = u.blood_type, p.rh_negative = u.rh_negative, " +
                        "p.chronic_disease = u.chronic_disease, p.sleep_quality = u.sleep_quality, p.smoking_freq = u.smoking_freq, " +
                        "p.drinking_freq = u.drinking_freq, p.exercise_freq = u.exercise_freq, p.diet_preference = u.diet_preference, " +
                        "p.emergency_contact = u.emergency_contact, p.emergency_phone = u.emergency_phone, p.last_buy_time = u.last_buy_time " +
                        "where u.id = ?",
                userId
        );
    }

    private Map<String, Object> device(Long id, long userId) {
        Map<String, Object> result = one(
                "select id, device_name as deviceName, device_type as deviceType, device_code as deviceCode, " +
                        "date_format(bind_time, '%Y-%m-%d %H:%i') as bindTime, date_format(last_sync_time, '%Y-%m-%d %H:%i') as lastSyncTime, " +
                        "if(status = 1, '绑定', '解绑') as status from device where id = ? and user_id = ?",
                id,
                userId
        );
        if (result.isEmpty()) throw new IllegalArgumentException("设备不存在或不属于当前用户");
        return result;
    }

    private Map<String, Object> enrollmentResult(Long activityId, Object title) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("activityId", activityId);
        result.put("activityTitle", title);
        result.put("joined", true);
        result.put("status", "已报名");
        return result;
    }

    private int genderCode(String value) {
        if ("男".equals(value) || "1".equals(value)) return 1;
        if ("女".equals(value) || "2".equals(value)) return 2;
        return 0;
    }

    private int booleanCode(Object value) {
        if (value instanceof Boolean) return ((Boolean) value) ? 1 : 0;
        String text = stringValue(value).trim();
        return ("1".equals(text) || "true".equalsIgnoreCase(text) || "是".equals(text)) ? 1 : 0;
    }

    private long requiredLong(Map<String, Object> payload, String key, String message) {
        Object value = payload == null ? null : payload.get(key);
        try {
            long parsed = value instanceof Number ? ((Number) value).longValue() : Long.parseLong(stringValue(value).trim());
            if (parsed > 0) {
                return parsed;
            }
        } catch (Exception ignored) {
        }
        throw new IllegalArgumentException(message);
    }

    private String uniqueNo(String prefix) {
        return prefix + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private void validateAvatarUrl(String avatarUrl) {
        boolean preset = avatarUrl.matches("/default-avatars/avatar-0[1-6]\\.svg");
        boolean uploaded = avatarUrl.startsWith("/uploads/avatar/")
                && !avatarUrl.contains("..")
                && !avatarUrl.contains("\\\\")
                && !avatarUrl.contains("?")
                && !avatarUrl.contains("#");
        if (!preset && !uploaded) {
            throw new IllegalArgumentException("头像地址无效");
        }
    }

    private long currentLegacyUserId() {
        AuthenticatedUser user = requireRole("elderly");
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("select legacy_user_id from elderly_profile where account_id = ? limit 1", user.getAccountId());
        if (rows.isEmpty()) {
            throw new SecurityException("Current account has no elderly profile");
        }
        return ((Number) rows.get(0).get("legacy_user_id")).longValue();
    }

    private long currentLegacyPersonnelId() {
        AuthenticatedUser user = requireRole("service");
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("select legacy_personnel_id from service_profile where account_id = ? limit 1", user.getAccountId());
        if (rows.isEmpty()) {
            throw new SecurityException("Current account has no service profile");
        }
        return ((Number) rows.get(0).get("legacy_personnel_id")).longValue();
    }

    private AuthenticatedUser requireRole(String roleType) {
        AuthenticatedUser user = currentUser();
        if (user == null || !roleType.equals(user.getRoleType())) {
            throw new SecurityException("Current account cannot access this portal");
        }
        return user;
    }

    private AuthenticatedUser currentUser() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        Object value = request.getAttribute(JwtAuthFilter.USER_ATTRIBUTE);
        return value instanceof AuthenticatedUser ? (AuthenticatedUser) value : null;
    }

    private Map<String, Object> one(String sql, Object... args) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, args);
        return rows.isEmpty() ? new LinkedHashMap<String, Object>() : rows.get(0);
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
