package com.library.service;

import com.library.model.MembershipStatus;
import com.library.model.Role;
import com.library.model.User;
import com.library.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityService activityService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       ActivityService activityService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.activityService = activityService;
    }

    @Transactional
    public User registerMember(String username, String email, String fullName, String phone, String rawPassword) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(Role.MEMBER);
        user.setMembershipStatus(MembershipStatus.ACTIVE);
        user.setMembershipNo("M" + System.currentTimeMillis() % 100000);
        user = userRepository.save(user);
        activityService.log("New member registered: " + fullName, username);
        return user;
    }

    public User createUser(String username, String email, String fullName, String phone,
                           Role role, String rawPassword) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setMembershipStatus(MembershipStatus.ACTIVE);
        user.setMembershipNo(role == Role.MEMBER ? "M" + System.currentTimeMillis() % 100000 : null);
        return userRepository.save(user);
    }

    public User updateProfile(Long id, String fullName, String phone, String email, String address) {
        User user = userRepository.findById(id).orElseThrow();
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setAddress(address);
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    public boolean passwordMatches(User user, String raw) {
        return passwordEncoder.matches(raw, user.getPassword());
    }

    public String createResetToken(String email) {
        return userRepository.findByEmail(email).map(u -> {
            String token = UUID.randomUUID().toString().replace("-", "");
            u.setResetToken(token);
            u.setResetTokenExpiry(LocalDateTime.now().plusHours(24));
            userRepository.save(u);
            return token;
        }).orElse(null);
    }

    public User findByResetToken(String token) {
        return userRepository.findByResetToken(token)
                .filter(u -> u.getResetTokenExpiry() != null
                        && u.getResetTokenExpiry().isAfter(LocalDateTime.now()))
                .orElse(null);
    }

    public List<User> allUsers() {
        return userRepository.findAll();
    }

    public List<User> staffMembers() {
        List<User> staff = new ArrayList<>();
        staff.addAll(userRepository.findByRole(Role.ADMIN));
        staff.addAll(userRepository.findByRole(Role.LIBRARIAN));
        return staff;
    }

    public List<User> allMembers() {
        return userRepository.findByRole(Role.MEMBER);
    }

    public List<User> searchMembers(String q) {
        String query = q == null ? "" : q.toLowerCase();
        return allMembers().stream()
                .filter(m -> query.isEmpty()
                        || m.getFullName().toLowerCase().contains(query)
                        || m.getEmail().toLowerCase().contains(query)
                        || m.getPhone().toLowerCase().contains(query)
                        || (m.getMembershipNo() != null && m.getMembershipNo().toLowerCase().contains(query)))
                .toList();
    }

    public User get(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public void toggleEnabled(Long id) {
        userRepository.findById(id).ifPresent(u -> {
            u.setEnabled(!u.isEnabled());
            userRepository.save(u);
        });
    }

    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }
}
