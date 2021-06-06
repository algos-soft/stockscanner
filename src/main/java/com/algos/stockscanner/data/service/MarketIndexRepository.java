package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.MarketIndex;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface MarketIndexRepository extends JpaRepository<MarketIndex, Integer> {

    List<MarketIndex> findBySymbol(String symbol);

    @Query("select m from MarketIndex m")
    List<MarketIndex> findAllAndSort(Sort sort);

//    @Query("SELECT m FROM MarketIndex m WHERE m.symbol LIKE :value%")
//    List<MarketIndex> findBySymbolStartsWith (@Param("value") String value);

    @Query("SELECT m FROM MarketIndex m ORDER BY m.symbol")
    Page<MarketIndex> findAllOrderBySymbol(Pageable pageable);


    @Query("SELECT m FROM MarketIndex m WHERE m.symbol LIKE :filterSymbol% OR m.name LIKE %:filterName% ORDER BY m.symbol")
    Page<MarketIndex> findAllWithFilterOrderBySymbol(Pageable pageable, @Param("filterSymbol") String filterSymbol, @Param("filterName") String filterName);


    static final String sWhere=
            "((:symbol is null or m.symbol LIKE :symbol%) or (:name is null or m.name LIKE %:name%))" +
                    " AND (:exchange is null or m.exchange LIKE %:exchange%)" +
                    " AND (:country is null or m.country LIKE %:country%)" +
                    " AND (:sector is null or m.sector LIKE %:sector%)" +
                    " AND (:industry is null or m.industry LIKE %:industry%)" +
                    " AND (:capmin is null or m.marketCap >= :capmin)"+
                    " AND (:capmax is null or m.marketCap <= :capmax)"+
                    " AND (:ebitdamin is null or m.ebitda >= :ebitdamin)"+
                    " AND (:ebitdamax is null or m.ebitda <= :ebitdamax)"+
                    " AND (:unitsfrommin is null or m.unitsFrom <= :unitsfrommin)";

    @Query("SELECT m FROM MarketIndex m WHERE " + sWhere+ " ORDER BY m.symbol")
    Page<MarketIndex> findAllWithFilterOrderBySymbol(Pageable pageable, @Param("symbol") String symbol,  @Param("name") String name, @Param("exchange") String exchange, @Param("country") String country,  @Param("sector") String sector, @Param("industry") String industry, @Param("capmin") Long capmin,  @Param("capmax") Long capmax, @Param("ebitdamin") Long ebitdamin,  @Param("ebitdamax") Long ebitdamax, @Param("unitsfrommin") String unitsFromMin);

    @Query("SELECT count(m) FROM MarketIndex m WHERE " +sWhere)
    long count(@Param("symbol") String symbol,  @Param("name") String name, @Param("exchange") String exchange, @Param("country") String country,  @Param("sector") String sector, @Param("industry") String industry, @Param("capmin") Long capmin,  @Param("capmax") Long capmax, @Param("ebitdamin") Long ebitdamin,  @Param("ebitdamax") Long ebitdamax, @Param("unitsfrommin") String unitsFromMin);


    @Query("SELECT m FROM MarketIndex m WHERE m IN (:indexes)")
    Page<MarketIndex> findAllInSet(Pageable pageable, @Param("indexes")Set<MarketIndex> indexes);

    @Query("SELECT m FROM MarketIndex m ORDER BY m.unitsTo")
    Page<MarketIndex> findAllOrderByUnitsTo(Pageable pageable);

}