package com.library.security;

import com.library.model.Role;
import com.library.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleAwareAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        User user = SecurityUtil.currentUser();
        boolean isStaff = user != null
                && (user.getRole() == Role.ADMIN || user.getRole() == Role.LIBRARIAN);
        String uri = request.getRequestURI();
        boolean staffPortal = uri.contains("/admin-login");
        boolean memberPortal = uri.contains("/member-login");

        if (staffPortal && !isStaff) {
            reject(request, response, "/auth/admin-login?roleError=staff");
            return;
        }
        if (memberPortal && isStaff) {
            reject(request, response, "/auth/member-login?roleError=member");
            return;
        }
        response.sendRedirect("/dashboard");
    }

    private void reject(HttpServletRequest request, HttpServletResponse response, String target)
            throws IOException {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(target);
    }
}
