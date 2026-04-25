package com.example.demo.services;

import com.example.demo.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SecurityService {

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated.");
    }

    /**
     * Checks if the current staff user has access to a specific cinema.
     * An ADMIN user always has access.
     * A STAFF user must be assigned to the given cinemaId.
     * A CUSTOMER user never has access.
     *
     * @param cinemaId The ID of the cinema to check access for.
     */
    public void checkStaffCinemaAccess(Integer cinemaId) {
        User currentUser = getCurrentUser();
        User.Role role = currentUser.getRole();

        if (role == User.Role.admin) {
            return; // Admin has access to all cinemas
        }

        if (role == User.Role.staff) {
            if (currentUser.getCinema() == null || !currentUser.getCinema().getId().equals(cinemaId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this cinema's resources.");
            }
            // If they are staff and the cinema ID matches, access is granted.
            return;
        }

        // Any other role (like CUSTOMER) is forbidden.
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to perform this action.");
    }
}
