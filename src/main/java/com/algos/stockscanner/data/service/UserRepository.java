package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.*;

import org.springframework.data.jpa.repository.*;

public interface UserRepository extends JpaRepository<User, Integer> {
//    @Query("SELECT COUNT(s) FROM Person s WHERE s.id=:generator")
//    int countByGenerator(@Param("generator") Generator generator);

}