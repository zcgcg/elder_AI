package com.daisy.health.ai;

import com.daisy.health.common.AuthenticatedUser;
import com.daisy.health.common.JwtAuthFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrentAccountProviderTest {
    private final CurrentAccountProvider provider = new CurrentAccountProvider();

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void returnsOnlyTheElderlyAccountStoredByTheJwtFilter() {
        requestWith(new AuthenticatedUser(88L, "elderly", "13800010001"));

        assertEquals(88L, provider.requireElderly().getAccountId());
    }

    @Test
    void rejectsStaffAndMissingAuthentication() {
        requestWith(new AuthenticatedUser(1L, "staff", "admin"));
        assertThrows(SecurityException.class, provider::requireElderly);

        requestWith(null);
        assertThrows(SecurityException.class, provider::requireElderly);
    }

    private void requestWith(AuthenticatedUser user) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (user != null) request.setAttribute(JwtAuthFilter.USER_ATTRIBUTE, user);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
