package com.twiiiins.flows;
import com.twiiiins.service.AuthService;
import com.twiiiins.entity.User;
import com.twiiiins.dto.LoginRequest;
import com.twiiiins.repository.UserRepository;
import com.twiiiins.security.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.*;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class AuthClockTest {
    final Clock clock=Clock.fixed(Instant.parse("2026-10-31T00:00:00Z"),ZoneOffset.UTC);
    @AfterEach void clear(){SecurityContextHolder.clearContext();}
    @Test void sessionExpirationIsExactlyEightHoursAndRawTokenIsNeverStored(){
        var users=mock(UserRepository.class);var passwords=mock(PasswordEncoder.class);var sessions=mock(AdminSessionRepository.class);
        var account=new User();account.setUsername("admin");account.setPassword("hashed");
        when(users.findByUsername("admin")).thenReturn(Optional.of(account));when(passwords.matches("password","hashed")).thenReturn(true);
        var request=new LoginRequest();request.setUsername("admin");request.setPassword("password");
        var response=new AuthService(clock,users,passwords,sessions).login(request);
        var captor=ArgumentCaptor.forClass(AdminSession.class);verify(sessions).save(captor.capture());
        assertEquals(clock.instant().plusSeconds(28800),captor.getValue().getExpiresAt());
        assertEquals(Tokens.hash(response.getToken()),captor.getValue().getTokenHash());
        assertNotEquals(response.getToken(),captor.getValue().getTokenHash());
    }
    @Test void exactExpiryBoundaryRejectsButFutureSessionAuthenticates() throws Exception {
        var sessions=mock(AdminSessionRepository.class); var session=new AdminSession();session.setUsername("admin");session.setExpiresAt(clock.instant());
        when(sessions.findById(Tokens.hash("token"))).thenReturn(Optional.of(session));
        var request=new MockHttpServletRequest();request.addHeader("Authorization","Bearer token");
        var filter=new AdminTokenFilter(sessions,clock);
        filter.doFilter(request,new MockHttpServletResponse(),new MockFilterChain());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        session.setExpiresAt(clock.instant().plusNanos(1));
        filter.doFilter(new MockHttpServletRequest(){ {addHeader("Authorization","Bearer token");} },new MockHttpServletResponse(),new MockFilterChain());
        assertEquals("admin",SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
