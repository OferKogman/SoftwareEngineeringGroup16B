package com.group16b.infrastructureLayer.Security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.InfrastructureLayer.RequestContext;
import com.group16b.InfrastructureLayer.Security.AuthInterceptor;
import com.group16b.InfrastructureLayer.Security.PublicEndpoint;
import com.group16b.InfrastructureLayer.Security.Role;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class AuthInterceptorTests {

    @Mock
    private IAuthenticationService authService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HandlerMethod handler;

    private AuthInterceptor interceptor;

    private final String GOOD_TOKEN="I Do Not Recognize The Bodies In The Water";
    private final String BAD_TOKEN="I PLAY POKMON GO EVERYDAY";

    private final String GOOD_SUBJECT="Gordon ramzy";
    private final String AVARAGE_ROLE=Role.SIGNED;
    private final String OLD="your mama";

    @BeforeEach
    void setup() {
        interceptor = new AuthInterceptor(authService);

        lenient().when(request.getHeader("Authorization")).thenReturn(GOOD_TOKEN);

        lenient().when(authService.validateToken(BAD_TOKEN)).thenReturn(false);
        lenient().when(authService.validateToken(GOOD_TOKEN)).thenReturn(true);

        lenient().when(authService.extractSubjectFromToken(GOOD_TOKEN)).thenReturn(GOOD_SUBJECT);

        lenient().when(authService.extractRoleFromToken(GOOD_TOKEN)).thenReturn(AVARAGE_ROLE);
        RequestContext.set(OLD, OLD);
    }

    @AfterEach
    void cleanup()
    {
        RequestContext.clear();
    }

    @Test
    void validToken_allowsRequest() throws Exception {

        when(request.getHeader("Authorization")).thenReturn(GOOD_TOKEN);
        HandlerMethod handler = mock(HandlerMethod.class);
        boolean result =interceptor.preHandle(request, response, handler);
        assertTrue(result);
        assertEquals(GOOD_SUBJECT, RequestContext.getUserId());
        assertEquals(AVARAGE_ROLE, RequestContext.getRole());
    }

    @Test
    void invalidToken_returns401() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(BAD_TOKEN);
        boolean result =interceptor.preHandle(request, response, handler);
        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertEquals(OLD, RequestContext.getUserId());
        assertEquals(OLD, RequestContext.getRole());
    }

    @Test
    void missingToken_returns401() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        boolean result =interceptor.preHandle(request, response, handler);
        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertEquals(OLD, RequestContext.getUserId());
        assertEquals(OLD, RequestContext.getRole());
    }

    @Test
    void publicEndpoint_skipsAuthentication() throws Exception
    {
        Method method =DummyController.class.getMethod("publicMethod");
        HandlerMethod handler =new HandlerMethod(new DummyController(), method);

        boolean result =interceptor.preHandle(request, response, handler);

        assertTrue(result);

        verifyNoInteractions(authService);
        assertEquals(OLD, RequestContext.getUserId());
        assertEquals(OLD, RequestContext.getRole());
    }


    static class DummyController {
        @PublicEndpoint
        public void publicMethod() {
        }
    }
}
