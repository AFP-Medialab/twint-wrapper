package com.afp.medialab.weverify.social.dao.repository;

import com.afp.medialab.weverify.social.dao.entity.CollectHistory;
import com.afp.medialab.weverify.social.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;

public interface CollectInterface extends JpaRepository<CollectHistory, Integer> {

    CollectHistory findCollectHistoryBySession(String session);

    @Modifying
    @Query("update CollectHistory collect set collect.status = :status where collect.session = :session")
    void updateCollectStatus(@Param("session") String session, @Param("status") Status status);

    @Modifying
    @Query("update CollectHistory collect set collect.processStart = :processStart where collect.session = :session")
    void updateCollectProcessStart(@Param("session") String session, @Param("processStart") Date processStart);

    @Modifying
    @Query("update CollectHistory collect set collect.processEnd = :processEnd where collect.session = :session")
    void updateCollectProcessEnd(@Param("session") String session, @Param("processEnd") Date processEnd);
}
