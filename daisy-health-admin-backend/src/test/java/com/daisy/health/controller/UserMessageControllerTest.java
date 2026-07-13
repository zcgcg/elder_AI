package com.daisy.health.controller;

import com.daisy.health.common.PageResult;
import com.daisy.health.service.AdminDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class UserMessageControllerTest {
    private AdminDataService dataService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        dataService = mock(AdminDataService.class);
        mockMvc = standaloneSetup(new UserController(dataService)).build();
    }

    @Test
    void adminListsMessagesGroupedByUser() throws Exception {
        Map<String, Object> group = record(
                "userId", 10001L,
                "userName", "王秀兰",
                "messageCount", 2,
                "messages", Collections.singletonList(record("id", 1L, "content", "请联系我", "status", "待处理"))
        );
        when(dataService.messages()).thenReturn(new PageResult<Map<String, Object>>(1, Collections.singletonList(group)));

        mockMvc.perform(get("/api/v1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].userName").value("王秀兰"))
                .andExpect(jsonPath("$.data.list[0].messages[0].content").value("请联系我"));
    }

    @Test
    void adminUpdatesMessageStatus() throws Exception {
        when(dataService.updateMessageStatus(eq(9L), any())).thenReturn(record("id", 9L, "status", "处理中"));

        mockMvc.perform(put("/api/v1/messages/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"处理中\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("处理中"));

        verify(dataService).updateMessageStatus(eq(9L), any());
    }

    private Map<String, Object> record(Object... values) {
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        for (int i = 0; i < values.length; i += 2) row.put(String.valueOf(values[i]), values[i + 1]);
        return row;
    }
}
