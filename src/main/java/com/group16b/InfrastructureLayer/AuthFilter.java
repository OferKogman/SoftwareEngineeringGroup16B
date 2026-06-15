package com.group16b.InfrastructureLayer;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthFilter extends OncePerRequestFilter {
    private final IAuthenticationService authService;

    public AuthFilter(IAuthenticationService authService)
    {
        this.authService=authService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException
    {
        String token = extractToken(request);
        try
        {
            if(token == null)
            {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            if(!authService.validateToken(token))
            {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String subject = authService.extractSubjectFromToken(token);
            String role = authService.extractRoleFromToken(token);

            RequestContext.set(subject, role);

            filterChain.doFilter(request, response);
        } catch(Exception e){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } finally{
            RequestContext.clear();
        }
    }

    private String extractToken(HttpServletRequest request)
    {
        String header=request.getHeader("Authorization");
        if(header==null) return null;
        if(header.startsWith("Bearer ")){
            return header.substring(7);
        }
        return header;
    }
    
}
