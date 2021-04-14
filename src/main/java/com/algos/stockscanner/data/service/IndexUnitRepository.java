package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.entity.MarketIndex;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IndexUnitRepository extends JpaRepository<IndexUnit, Integer> {

    List<IndexUnit> findByIndex (MarketIndex index);

    long deleteByIndex (MarketIndex index);

    @Query("SELECT u FROM IndexUnit u where index=:index AND u.dateTime >= :dateTime ORDER BY u.id ASC")
    List<IndexUnit> findAllByIndexWithDateTimeEqualOrAfter(@Param("index") MarketIndex index, @Param("dateTime") LocalDateTime dateTime);

    @Query("SELECT u FROM IndexUnit u where index=:index AND u.dateTime >= :dateTime ORDER BY u.id ASC")
    List<IndexUnit> findAllByIndexWithDateTimeEqualOrAfter(@Param("index") MarketIndex index, @Param("dateTime") LocalDateTime dateTime, Pageable pageable);

    @Query("SELECT u FROM IndexUnit u where index=:index AND u.id >= :id ORDER BY u.id ASC")
    List<IndexUnit> findAllByIndexFromId(@Param("index") MarketIndex index, @Param("id") int id, Pageable pageable);


}

