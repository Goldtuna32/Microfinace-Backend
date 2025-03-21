package com.sme.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class EmailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String to;
    @Column(name = "subject", length = 10000)
    private String subject;
    @Column(name = "body", length = 10000)
    private String body;
    private long timestamp;
}