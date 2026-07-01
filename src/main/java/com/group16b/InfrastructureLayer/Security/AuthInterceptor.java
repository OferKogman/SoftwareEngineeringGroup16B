package com.group16b.InfrastructureLayer.Security;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.InfrastructureLayer.RequestContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final IAuthenticationService authService;

    public AuthInterceptor(IAuthenticationService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod method))
            return true;

        if (method.hasMethodAnnotation(PublicEndpoint.class))
            return true;

        String token = extractToken(request);

        if (token == null || !authService.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        RequestContext.set(
                authService.extractSubjectFromToken(token),
                authService.extractRoleFromToken(token));

        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null)
            return null;

        if (header.startsWith("Bearer "))
            return header.substring(7);

        return header;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {

        RequestContext.clear();
    }
}