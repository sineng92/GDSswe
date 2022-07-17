package com.example.demo.repository;

import com.example.demo.entity.UserInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {
    @Query("select u from UserInfo u where (:min IS NULL OR u.salary > :min) and (:max IS NULL OR u.salary < :max)")
    Page<UserInfo> search(@Param("min") double min, @Param("max") double max, Pageable pageable);
}
