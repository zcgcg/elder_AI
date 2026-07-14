package com.daisy.health.controller;

import com.daisy.health.service.AdminDataService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class PasswordSettingsControllerTest {
    @Test
    void adminCanResetAUsersPasswordWithoutSupplyingAReplacement() throws Exception {
        AdminDataService dataService = mock(AdminDataService.class);
        when(dataService.resetUserPassword(10001L)).thenReturn(Collections.singletonMap("reset", true));
        MockMvc mockMvc = standaloneSetup(new PasswordSettingsController(dataService)).build();

        mockMvc.perform(put("/api/v1/settings/users/10001/password/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reset").value(true));

        verify(dataService).resetUserPassword(10001L);
    }

    @Test
    void adminCanResetAServicePersonnelsPasswordWithoutSupplyingAReplacement() throws Exception {
        AdminDataService dataService = mock(AdminDataService.class);
        when(dataService.resetPersonnelPassword(2L)).thenReturn(Collections.singletonMap("reset", true));
        MockMvc mockMvc = standaloneSetup(new PasswordSettingsController(dataService)).build();

        mockMvc.perform(put("/api/v1/settings/personnel/2/password/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reset").value(true));

        verify(dataService).resetPersonnelPassword(2L);
    }
}
