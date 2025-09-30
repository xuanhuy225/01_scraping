package com.example.scraping.repository;

import com.example.scraping.entity.ScrapData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScrapDataRepository extends JpaRepository<ScrapData, Long> {

    // Find by MD5 hash
    Optional<ScrapData> findByMd5(String md5);

    // Find by domain
    List<ScrapData> findByDomain(Short domain);

    // Find by title containing (case insensitive)
    List<ScrapData> findByTitleContainingIgnoreCase(String title);

    // Check if MD5 exists
    boolean existsByMd5(String md5);

    // Find by URL
    Optional<ScrapData> findByUrl(String url);

    // Custom query to find by domain and title
    @Query("SELECT s FROM ScrapData s WHERE s.domain = :domain AND s.title LIKE %:title%")
    List<ScrapData> findByDomainAndTitleContaining(@Param("domain") Short domain, @Param("title") String title);

    // Count by domain
    long countByDomain(Short domain);
}
