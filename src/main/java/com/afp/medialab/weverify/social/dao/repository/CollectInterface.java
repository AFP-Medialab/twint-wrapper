package com.afp.medialab.weverify.social.dao.repository;

import com.afp.medialab.weverify.social.dao.entity.CollectHistory;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CollectInterface extends JpaRepository<CollectHistory, Integer> {

    CollectHistory findCollectHistoryBySession(String session);

    CollectHistory findCollectHistoryByQuery(String query);

    Set<CollectHistory> findCollectHistoryByQueryContains(String str);

    List<CollectHistory> findCollectHistoryByStatus(String status);

    List<CollectHistory> findCollectHistoryByStatusAndProcessStartGreaterThan(String status, Date processStart);

    List<CollectHistory> findCollectHistoryByStatusAndProcessEndLessThan(String status, Date processEnd);

    List<CollectHistory> findCollectHistoryByProcessEndLessThanEqualOrProcessEndIsNullAndProcessStartGreaterThanEqualAndStatus(Date processEnd, Date processStart, String status);

    List<CollectHistory> findCollectHistoryByProcessStartGreaterThan(Date processStart);

    List<CollectHistory> findCollectHistoryByProcessEndLessThan(Date processEnd);

    List<CollectHistory> findAll();


    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update CollectHistory collect set collect.status = :status where collect.session = :session")
    void updateCollectStatus(@Param("session") String session, @Param("status") String status);

    @Modifying
    @Transactional
    @Query("update CollectHistory collect set collect.processStart = :processStart where collect.session = :session")
    void updateCollectProcessStart(@Param("session") String session, @Param("processStart") Date processStart);

    @Modifying
    @Transactional
    @Query("update CollectHistory collect set collect.processEnd = :processEnd where collect.session = :session")
    void updateCollectProcessEnd(@Param("session") String session, @Param("processEnd") Date processEnd);

    @Modifying
    @Transactional
    @Query("update CollectHistory collect set collect.query = :query where collect.session = :session")
    void updateCollectQuery(@Param("session") String session, @Param("query") String query);

    @Modifying
    @Transactional
    @Query("update CollectHistory collect set collect.message = :message where collect.session = :session")
    void updateCollectMessage(@Param("session") String session, @Param("message") String message);

    @Modifying
    @Transactional
    @Query("update CollectHistory collect set collect.count = :int where collect.session = :session")
    void updateCollectCount(@Param("session") String session, @Param("int") Integer count);

}
