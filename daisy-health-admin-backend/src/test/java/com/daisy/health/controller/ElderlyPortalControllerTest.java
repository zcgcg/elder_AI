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
