package com.plutus360.chronologix.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plutus360.chronologix.dao.repositories.UserRepo;
import com.plutus360.chronologix.entities.User;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;

    @Transactional
    public User createUser(User user) {
        try {
            return userRepo.insert(user);
        } catch (Exception e) {
            log.error("Failed to create user", e);
            throw e;  // Or use a custom exception
        }
    }

    @Transactional
    public User updateUser(User user) {
        try {
            return userRepo.update(user);
        } catch (Exception e) {
            log.error("Failed to update user", e);
            throw e;  // Or use a custom exception
        }
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        try {
            return userRepo.findById(id);
        } catch (Exception e) {
            log.error("Failed to find user by ID", e);
            return null;  // Or handle differently
        }
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        try {
            return userRepo.findByUsername(username);
        } catch (Exception e) {
            log.error("Failed to find user by username", e);
            return null;  // Or handle differently
        }
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        try {
            return userRepo.findByEmail(email)
                    .orElse(null);  // Return null if the user is not found
        } catch (Exception e) {
            log.error("Failed to find user by email", e);
            return null;
        }
    }


    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        try {
            return userRepo.findAll();
        } catch (Exception e) {
            log.error("Failed to find all users", e);
            return List.of();  // Return empty list if error
        }
    }

    @Transactional
    public boolean deleteUserById(Long id) {
        try {
            return userRepo.deleteById(id);
        } catch (Exception e) {
            log.error("Failed to delete user", e);
            return false;  // Or handle differently
        }
    }
}
