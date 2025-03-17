package com.sme.repository;

import com.sme.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Fetch only active users
    @Query("SELECT u FROM User u WHERE u.status = 1")
    List<User> findAllActiveUsers();

    @Query("SELECT u FROM User u WHERE u.status = 2")
    List<User> findAllInactiveUsers();

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
}
