package com.afp.medialab.weverify.social.dao.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.afp.medialab.weverify.social.model.Status;

@Entity
public class CollectHistory implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;
    @Column(name = "session")
    private String session;
    @Column(name = "query", columnDefinition = "TEXT")
    private String query;
    @Column(name = "processStart", nullable = true)
    private Date processStart;
    @Column(name = "processEnd", nullable = true)
    private Date processEnd;
    @Column(name = "Status")
    private String status;
    @Column(name = "message")
    private String message;




    public CollectHistory(){}

    public CollectHistory(String session, String query, Date processStart, Date processEnd, Status status) {
        this.session = session;
        this.query = query;
        this.processStart = processStart;
        this.processEnd = processEnd;
        this.status = status.toString();
        this.message = null;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
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
        return Status.valueOf(this.status);
    }

    public void setStatus(Status status) {
        this.status = status.toString();
    }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }
}
