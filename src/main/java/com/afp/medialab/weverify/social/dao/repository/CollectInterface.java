package com.afp.medialab.weverify.social.dao.repository;

import com.afp.medialab.weverify.social.dao.entity.CollectHistory;
import com.afp.medialab.weverify.social.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;

public interface CollectInterface extends JpaRepository<CollectHistory, Integer> {

    CollectHistory findCollectHistoryById(Integer id);

    @Modifying
    @Query("update CollectHistory collect set collect.status = :status where collect.id = :id")
    void updateCollectStatus(@Param("id") Integer id, @Param("status") Status status);

    @Modifying
    @Query("update CollectHistory collect set collect.processStart = :processStart where collect.id = :id")
    void updateCollectProcessStart(@Param("id") Integer id, @Param("processStart") Date processStart);

    @Modifying
    @Query("update CollectHistory collect set collect.processEnd = :processEnd where collect.id = :id")
    void updateCollectProcessEnd(@Param("id") Integer id, @Param("processEnd") Date processEnd);

}
