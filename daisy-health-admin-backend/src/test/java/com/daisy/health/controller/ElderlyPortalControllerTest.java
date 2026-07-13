package com.daisy.health.controller;

import com.daisy.health.common.JwtAuthFilter;
import com.daisy.health.common.JwtService;
import com.daisy.health.common.PermissionService;
import com.daisy.health.service.PortalDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ElderlyPortalControllerTest {
    private PortalDataService portalDataService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        portalDataService = mock(PortalDataService.class);
        mockMvc = standaloneSetup(new ElderlyPortalController(portalDataService)).build();
    }

    @Test
    void userCanListAvailableCatalogItems() throws Exception {
        when(portalDataService.elderlyCatalogItems()).thenReturn(Arrays.asList(
                record("id", 7L, "name", "助浴护理", "price", 199)
        ));

        mockMvc.perform(get("/api/v1/elderly/catalog-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("助浴护理"))
                .andExpect(jsonPath("$.data[0].price").value(199));
    }

    @Test
    void userCanListOnlyTheWorkOrdersReturnedForCurrentAccount() throws Exception {
        when(portalDataService.elderlyWorkOrders()).thenReturn(Collections.singletonList(
                record("id", 12L, "serviceItem", "助浴护理")
        ));

        mockMvc.perform(get("/api/v1/elderly/work-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(12));
    }

    @Test
    void userCanCreateWorkOrderFromCatalogItem() throws Exception {
        when(portalDataService.createElderlyWorkOrder(any())).thenReturn(
                record("id", 13L, "productId", 7L, "amount", 199, "dispatchTime", "2026-07-10 10:00")
        );

        mockMvc.perform(post("/api/v1/elderly/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":7,\"serviceTime\":\"2026-07-11 09:00:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value(7))
                .andExpect(jsonPath("$.data.amount").value(199))
                .andExpect(jsonPath("$.data.dispatchTime").isNotEmpty());

        verify(portalDataService).createElderlyWorkOrder(any());
    }

    @Test
    void userCanChooseFromEligibleServicePersonnelWhenCreatingWorkOrder() throws Exception {
        when(portalDataService.elderlyPersonnel()).thenReturn(Collections.singletonList(
                record("id", 2L, "name", "李华", "serviceType", "康复理疗")
        ));
        when(portalDataService.createElderlyWorkOrder(any())).thenReturn(
                record("id", 14L, "personnelId", 2L, "personnelName", "李华")
        );

        mockMvc.perform(get("/api/v1/elderly/personnel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(2))
                .andExpect(jsonPath("$.data[0].name").value("李华"));
        mockMvc.perform(post("/api/v1/elderly/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":7,\"personnelId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.personnelId").value(2));
    }

    @Test
    void userCanUpdateOwnAvatar() throws Exception {
        when(portalDataService.updateElderlyAvatar(any())).thenReturn(
                record("avatarUrl", "/default-avatars/avatar-02.svg")
        );

        mockMvc.perform(put("/api/v1/elderly/profile/avatar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"avatarUrl\":\"/default-avatars/avatar-02.svg\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.avatarUrl").value("/default-avatars/avatar-02.svg"));

        verify(portalDataService).updateElderlyAvatar(any());
    }

    @Test
    void userCanUpdateOwnCompleteProfile() throws Exception {
        when(portalDataService.updateElderlyProfile(any())).thenReturn(
                record("realName", "张阿姨", "idCard", "310101195001010000", "dietPreference", "清淡")
        );

        mockMvc.perform(put("/api/v1/elderly/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"realName\":\"张阿姨\",\"idCard\":\"310101195001010000\",\"dietPreference\":\"清淡\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.realName").value("张阿姨"))
                .andExpect(jsonPath("$.data.dietPreference").value("清淡"));

        verify(portalDataService).updateElderlyProfile(any());
    }

    @Test
    void userCanUpdateAnOwnedDevice() throws Exception {
        when(portalDataService.updateElderlyDevice(org.mockito.ArgumentMatchers.eq(8L), any())).thenReturn(
                record("id", 8L, "deviceName", "床头监测仪", "status", "绑定")
        );

        mockMvc.perform(put("/api/v1/elderly/devices/8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deviceName\":\"床头监测仪\",\"status\":\"绑定\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(8))
                .andExpect(jsonPath("$.data.deviceName").value("床头监测仪"));

        verify(portalDataService).updateElderlyDevice(org.mockito.ArgumentMatchers.eq(8L), any());
    }

    @Test
    void userCanBrowseActivitiesAndEnrollAnExistingActivity() throws Exception {
        when(portalDataService.elderlyActivities()).thenReturn(Collections.singletonList(
                record("id", 21L, "title", "社区健康义诊", "location", "社区活动中心",
                        "startTime", "2026-07-20 09:00", "endTime", "2026-07-20 11:30",
                        "content", "提供健康检查", "joined", true, "enrollmentStatus", "已报名",
                        "enrollTime", "2026-07-13 10:00", "canJoin", false)
        ));
        when(portalDataService.enrollElderlyActivity(21L)).thenReturn(
                record("activityId", 21L, "joined", true, "status", "已报名")
        );

        mockMvc.perform(get("/api/v1/elderly/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("社区健康义诊"))
                .andExpect(jsonPath("$.data[0].joined").value(true))
                .andExpect(jsonPath("$.data[0].enrollmentStatus").value("已报名"))
                .andExpect(jsonPath("$.data[0].content").value("提供健康检查"));
        mockMvc.perform(post("/api/v1/elderly/activities/21/enroll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.joined").value(true));

        verify(portalDataService).enrollElderlyActivity(21L);
    }

    @Test
    void userCanBrowsePublishedHealthContent() throws Exception {
        when(portalDataService.elderlyHealthArticles()).thenReturn(Collections.singletonList(
                record("id", 31L, "title", "夏季健康饮食")
        ));
        when(portalDataService.elderlyHealthVideos()).thenReturn(Collections.singletonList(
                record("id", 41L, "title", "居家防跌倒")
        ));

        mockMvc.perform(get("/api/v1/elderly/health-articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("夏季健康饮食"));
        mockMvc.perform(get("/api/v1/elderly/health-videos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("居家防跌倒"));
    }

    @Test
    void staffAccountCannotUpdateElderlyAvatar() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JwtService jwtService = new JwtService("daisy-health-local-dev-secret-please-change-32", 60_000L);
        JwtAuthFilter authFilter = new JwtAuthFilter(
                jwtService,
                new PermissionService(null, objectMapper),
                objectMapper
        );
        MockMvc securedMockMvc = standaloneSetup(new ElderlyPortalController(portalDataService))
                .addFilters(authFilter)
                .build();
        String token = jwtService.createToken(1L, "staff", "13800000000");

        securedMockMvc.perform(put("/api/v1/elderly/profile/avatar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"avatarUrl\":\"/default-avatars/avatar-02.svg\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(1003));

        verifyNoInteractions(portalDataService);
    }

    private Map<String, Object> record(Object... values) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (int i = 0; i < values.length; i += 2) {
            map.put(String.valueOf(values[i]), values[i + 1]);
        }
        return map;
    }
}
