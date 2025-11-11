package com.ept.sn.cri.backend.rh.repository;

import com.ept.sn.cri.backend.entity.RH;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RHRepository extends JpaRepository<RH, Long> {
}