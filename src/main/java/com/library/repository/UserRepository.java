package com.library.repository;

import com.library.model.MembershipStatus;
import com.library.model.Role;
import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    long countByRole(Role role);

    long countByRoleAndMembershipStatus(Role role, MembershipStatus status);

    List<User> findByRole(Role role);

    Optional<User> findByResetToken(String token);

    List<User> findByRoleAndMembershipStatus(Role role, MembershipStatus status);
}
