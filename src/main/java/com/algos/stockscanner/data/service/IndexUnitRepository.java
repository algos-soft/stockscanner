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
    List<IndexUnit> findAllByIndexWithDateTimeEqualOrAfter(@Param("index") MarketIndex index, @Param("dateTime") String dateTime);

    @Query("SELECT u FROM IndexUnit u where index=:index AND u.dateTime >= :dateTime ORDER BY u.id ASC")
    List<IndexUnit> findAllByIndexWithDateTimeEqualOrAfter(@Param("index") MarketIndex index, @Param("dateTime") String dateTime, Pageable pageable);

    @Query("SELECT u FROM IndexUnit u where index=:index AND u.id >= :id ORDER BY u.id ASC")
    List<IndexUnit> findAllByIndexFromId(@Param("index") MarketIndex index, @Param("id") int id, Pageable pageable);

    @Query("SELECT u FROM IndexUnit u where index=:index AND u.dateTime >= :t1 AND u.dateTime <= :t2 ORDER BY u.id ASC")
    List<IndexUnit> findAllByPeriod(@Param("index") MarketIndex index, @Param("t1") String t1, @Param("t2") String t2, Pageable pageable);


}

