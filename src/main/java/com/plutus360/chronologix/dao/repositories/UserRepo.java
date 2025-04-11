package com.plutus360.chronologix.dao.repositories;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.plutus360.chronologix.entities.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Repository
public class UserRepo {


    @PersistenceContext
    private EntityManager em;


    public User insert(User user) {
        if (user == null) {
            return null;
        }

        try {
            em.persist(user);
            return user;
        } catch (Exception e) {
            log.error("Error inserting user", e);
            throw e;  // Or handle accordingly
        }
    }



    public User update(User user) {
        if (user == null) {
            return null;
        }

        try {
            return em.merge(user);
        } catch (Exception e) {
            log.error("Error updating user", e);
            throw e;  // Or handle accordingly
        }
    }


     // Find a user by ID
     public User findById(Long id) {
        if (id == null) {
            return null;
        }

        return em.find(User.class, id);
    }




    // Find a user by username
    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        try {
            String jpql = "SELECT u FROM User u WHERE u.username = :username";
            TypedQuery<User> query = em.createQuery(jpql, User.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (Exception e) {
            log.error("Error finding user by username", e);
            return null;  // Return null if not found or handle accordingly
        }
    }



    // Delete a user by ID
    public boolean deleteById(Long id) {
        if (id == null) {
            return false;
        }

        try {
            User user = em.find(User.class, id);
            if (user != null) {
                em.remove(user);
                return true;
            } else {
                return false;  // User not found
            }
        } catch (Exception e) {
            log.error("Error deleting user", e);
            return false;  // Or handle accordingly
        }
    }



    // Get all users
    public List<User> findAll() {
        try {
            String jpql = "SELECT u FROM User u";
            TypedQuery<User> query = em.createQuery(jpql, User.class);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Error finding all users", e);
            return Collections.emptyList();
        }
    }

    
}
