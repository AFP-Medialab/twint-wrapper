package com.afp.medialab.weverify.social.dao.entity;

import com.afp.medialab.weverify.social.model.Status;
import javafx.scene.text.Text;
import org.springframework.data.annotation.Id;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import java.util.Date;

@Entity
public class CollectHistory {

    @Id
    @Column(name = "id")
    private Integer id;
    @Column(name = "query")
    private Text query;
    @Column(name = "processStart")
    private Date processStart;
    @Column(name = "processEnd")
    private Date processEnd;
    @Column(name = "Status")
    private Status status;

    public CollectHistory(Integer id, Text query, Date processStart, Date processEnd, Status status) {
        this.id = id;
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

    public Text getQuery() {
        return query;
    }

    public void setQuery(Text query) {
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
