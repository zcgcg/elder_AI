package com.daisy.health.service;

import com.daisy.health.common.AuthenticatedUser;
import com.daisy.health.common.JwtAuthFilter;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Profile("mysql")
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
                "select a.id as accountId, a.phone, a.nickname, a.avatar_url as avatarUrl, p.legacy_user_id as userId, " +
                        "p.real_name as realName, case p.gender when 1 then '男' when 2 then '女' else '未知' end as gender, " +
                        "date_format(p.birthday, '%Y-%m-%d') as birthday, p.address, p.bio, p.height, p.weight, " +
                        "p.chronic_disease as chronicDisease, p.emergency_contact as emergencyContact, p.emergency_phone as emergencyPhone, " +
                        "date_format(p.last_buy_time, '%Y-%m-%d') as lastBuyTime " +
                        "from account a join elderly_profile p on p.account_id = a.id where p.legacy_user_id = ?",
                userId
        );
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
        return "select w.id, w.order_no as orderNo, w.service_item as serviceItem, w.amount, w.status, " +
                "date_format(w.dispatch_time, '%Y-%m-%d %H:%i') as dispatchTime, date_format(w.service_time, '%Y-%m-%d %H:%i') as serviceTime, " +
                "date_format(w.complete_time, '%Y-%m-%d %H:%i') as completeTime, u.real_name as customerName, u.phone as customerPhone, u.address as customerAddress, " +
                "o.order_no as serviceOrderNo, o.product_name as productName, o.service_type as serviceType " +
                "from work_order w left join `user` u on w.customer_id = u.id left join service_order o on w.order_id = o.id";
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
