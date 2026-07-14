package com.daisy.health.controller;

import com.daisy.health.common.JwtAuthFilter;
import com.daisy.health.common.JwtService;
import com.daisy.health.common.PermissionService;
import com.daisy.health.service.AdminDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AuthControllerTest {
    @Test
    void appCanPingWithoutJwtWhileProfileRemainsProtected() throws Exception {
        AdminDataService dataService = mock(AdminDataService.class);
        JwtAuthFilter authFilter = new JwtAuthFilter(
                mock(JwtService.class), mock(PermissionService.class), new ObjectMapper());
        MockMvc mockMvc = standaloneSetup(new AuthController(dataService)).addFilters(authFilter).build();

        mockMvc.perform(get("/api/v1/auth/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.service").value("daisy-health"))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.apiVersion").value("v1"));

        mockMvc.perform(get("/api/v1/auth/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedAccountCanSubmitItsCurrentAndNewPassword() throws Exception {
        AdminDataService dataService = mock(AdminDataService.class);
        when(dataService.updatePassword(argThat(payload -> "753951".equals(payload.get("currentPassword"))
                && "654321".equals(payload.get("newPassword")))))
                .thenReturn(Collections.singletonMap("changed", true));
        MockMvc mockMvc = standaloneSetup(new AuthController(dataService)).build();

        mockMvc.perform(put("/api/v1/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"753951\",\"newPassword\":\"654321\",\"confirmPassword\":\"654321\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.changed").value(true));

        verify(dataService).updatePassword(argThat((Map<String, Object> payload) ->
                "654321".equals(payload.get("confirmPassword"))));
    }
}
