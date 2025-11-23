package com.ept.sn.cri.backend.auth.repository;

import com.ept.sn.cri.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    //Trouver tous les users qui ont des roles commission_member
    @Query("SELECT u FROM User u WHERE u.role = 'COMMISSION_MEMBER'")
    List<User> findAllCommissionMembers();
}
