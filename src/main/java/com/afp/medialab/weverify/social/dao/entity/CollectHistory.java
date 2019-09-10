package com.afp.medialab.weverify.social.dao.entity;

import com.afp.medialab.weverify.social.model.Status;
import com.mysql.cj.Session;

import javax.persistence.Id;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import java.util.Date;

@Entity
public class CollectHistory {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Integer id;
    @Column(name = "session")
    private String session;
    @Column(name = "query", columnDefinition = "TEXT")
    private String query;
    @Column(name = "processStart")
    private Date processStart;
    @Column(name = "processEnd")
    private Date processEnd;
    @Column(name = "Status")
    private Status status;

    public CollectHistory(String session, String query, Date processStart, Date processEnd, Status status) {
        this.session = session;
        this.query = query;
        this.processStart = processStart;
        this.processEnd = processEnd;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Date getProcessStart() {
        return processStart;
    }

    public void setProcessStart(Date processStart) {
        this.processStart = processStart;
    }

    public Date getProcessEnd() {
        return processEnd;
    }

    public void setProcessEnd(Date processEnd) {
        this.processEnd = processEnd;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
