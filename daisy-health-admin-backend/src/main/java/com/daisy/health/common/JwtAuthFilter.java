package com.daisy.health.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {
    public static final String USER_ATTRIBUTE = "authenticatedUser";

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<String> PERMIT_URLS = Arrays.asList(
            "/api/v1/auth/login",
            "/api/v1/auth/ping",
            "/error"
    );

    private final JwtService jwtService;
    private final PermissionService permissionService;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(JwtService jwtService, PermissionService permissionService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.permissionService = permissionService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/v1/")) {
            return true;
        }
        for (String pattern : PERMIT_URLS) {
            if (PATH_MATCHER.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            write(response, HttpServletResponse.SC_UNAUTHORIZED, 1002, "缺少登录凭证");
            return;
        }
        try {
            AuthenticatedUser user = jwtService.parse(header.substring(7));
            request.setAttribute(USER_ATTRIBUTE, user);
            if (!permissionService.canAccess(user, request.getMethod(), request.getRequestURI())) {
                write(response, HttpServletResponse.SC_FORBIDDEN, 1003, "没有访问权限");
                return;
            }
            chain.doFilter(request, response);
        } catch (SecurityException ex) {
            write(response, HttpServletResponse.SC_FORBIDDEN, 1003, ex.getMessage());
        } catch (Exception ex) {
            write(response, HttpServletResponse.SC_UNAUTHORIZED, 1002, "登录凭证无效或已过期");
        }
    }

    private void write(HttpServletResponse response, int status, int code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(code, message)));
    }
}
