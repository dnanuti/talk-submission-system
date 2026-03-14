package com.gdg.muddy.model;

import jakarta.persistence.*;

@Entity
public class Talk {

    @Id
    @GeneratedValue
    private Long id;

    private String title;

    @Column(length = 2000)
    private String abstractText;

    private String speaker;

    private String status;

    public Talk() {}

    public Talk(String title, String abstractText, String speaker) {
        this.title = title;
        this.abstractText = abstractText;
        this.speaker = speaker;
        this.status = "DRAFT";
    }

    public Long getId() { return id; }

    public String getTitle() { return title; }

    public String getAbstractText() { return abstractText; }

    public String getSpeaker() { return speaker; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}
