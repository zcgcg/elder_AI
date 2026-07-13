package com.daisy.health.controller;

import com.daisy.health.common.PageResult;
import com.daisy.health.service.AdminDataService;
import com.daisy.health.service.ResourceQuery;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ServiceControllerTest {
    @Test
    void adminCanFilterWorkOrdersByServicePersonnelAndCustomer() throws Exception {
        AdminDataService dataService = mock(AdminDataService.class);
        when(dataService.workOrders(eq(2L), eq(10001L), any(ResourceQuery.class))).thenReturn(new PageResult<Map<String, Object>>(
                1,
                Collections.singletonList(record("id", 9L, "personnelName", "李华", "customer", "王秀兰"))
        ));
        MockMvc mockMvc = standaloneSetup(new ServiceController(dataService)).build();

        mockMvc.perform(get("/api/v1/work-orders")
                        .param("personnelId", "2")
                        .param("customerId", "10001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].personnelName").value("李华"))
                .andExpect(jsonPath("$.data.list[0].customer").value("王秀兰"));
    }

    private Map<String, Object> record(Object... values) {
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        for (int i = 0; i < values.length; i += 2) row.put(String.valueOf(values[i]), values[i + 1]);
        return row;
    }
}
