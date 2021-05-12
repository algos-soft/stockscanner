package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.*;
import org.jetbrains.annotations.*;
import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface UserRepository extends JpaRepository<User, Integer> {

    //    @Query("SELECT COUNT(s) FROM Person s WHERE s.id=:generator")
    //    int countByGenerator(@Param("generator") Generator generator);
    @NotNull
    Optional<User> findById(Integer id);

}