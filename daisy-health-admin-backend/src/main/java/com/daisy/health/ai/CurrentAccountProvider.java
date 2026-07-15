package com.daisy.health.ai;

import com.daisy.health.common.AuthenticatedUser;
import com.daisy.health.common.JwtAuthFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Component
public class CurrentAccountProvider {
    public AuthenticatedUser requireElderly() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();
        Object value = request == null ? null : request.getAttribute(JwtAuthFilter.USER_ATTRIBUTE);
        if (!(value instanceof AuthenticatedUser) || !"elderly".equals(((AuthenticatedUser) value).getRoleType())) {
            throw new SecurityException("当前账号无权访问智能客服");
        }
        return (AuthenticatedUser) value;
    }
}
