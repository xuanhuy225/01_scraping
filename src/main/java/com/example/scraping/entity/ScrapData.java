package com.example.scraping.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "scrap_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScrapData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "md5", length = 32)
    private String md5;

    @Column(name = "title")
    private String title;

    @Column(name = "text")
    private String text;

    @Column(name = "domain")
    private Short domain;

    @Column(name = "url")
    private String url;
}
