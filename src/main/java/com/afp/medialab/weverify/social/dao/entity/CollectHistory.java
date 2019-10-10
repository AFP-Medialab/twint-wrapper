package com.afp.medialab.weverify.social.dao.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.SortedSet;

import javax.persistence.*;

import com.afp.medialab.weverify.social.model.Status;

@Entity
public class CollectHistory implements Serializable {

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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "request_id", referencedColumnName = "id")
    Request request;
    @Column(name = "processStart", nullable = true)
    private Date processStart;
    @Column(name = "processEnd", nullable = true)
    private Date processEnd;
    @Column(name = "Status")
    private String status;
    @Column(name = "message")
    private String message;
    @Column(name = "count")
    private Integer count;
    @Column(name = "finished_threads")
    private Integer finished_threads;
    @Column(name = "total_threads")
    private Integer total_threads;
    @Column(name = "successful_threads")
    private Integer successful_threads;


    public CollectHistory() {
    }

    public CollectHistory(String session, Request request, Date processStart, Date processEnd, Status status, String message, Integer count, Integer finished_threads, Integer total_threads, Integer successful_threads) {
        this.session = session;
        this.request = request;
        this.processStart = processStart;
        this.processEnd = processEnd;
        this.status = status.toString();
        this.message = message;
        this.count = count;
        this.finished_threads = finished_threads;
        this.total_threads = total_threads;
        this.successful_threads = successful_threads;
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

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getFinished_threads() {
        return finished_threads;
    }

    public void setFinished_threads(Integer finished_threads) {
        this.finished_threads = finished_threads;
    }

    public Integer getTotal_threads() {
        return total_threads;
    }

    public void setTotal_threads(Integer total_threads) {
        this.total_threads = total_threads;
    }

    public Integer getSuccessful_threads() {
        return successful_threads;
    }

    public void setSuccessful_threads(Integer successful_threads) {
        this.successful_threads = successful_threads;
    }
}
