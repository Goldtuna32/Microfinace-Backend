package com.sme.repository;

import com.sme.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @EntityGraph(attributePaths = {"branch", "role"})
    Optional<User> findById(Long id);

    @EntityGraph(attributePaths = {"branch", "role"})
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = {"branch", "role"})  // Load both branch and role
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findWithBranchAndRoleByEmail(@Param("email") String email);

}
