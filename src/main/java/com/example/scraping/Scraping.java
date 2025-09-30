package com.example.scraping;

import com.example.scraping.scrap.ScrapBaoMoi;
import com.example.scraping.service.ScrapDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Scraping implements CommandLineRunner {

    @Autowired
    private ScrapBaoMoi scrapBaoMoi;

    @Autowired
    private ScrapDataService scrapDataService;

    public static void main(String[] args) {
        // This will be handled by Spring Boot Application class
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting BaoMoi scraping process...");

        // Show initial database stats
        scrapDataService.showDatabaseStats();

        // Start scraping
        scrapBaoMoi.startScraping();

        // Show final database stats
        System.out.println("\n=== SCRAPING COMPLETED ===");
        scrapDataService.showDatabaseStats();
        scrapDataService.validateArticleContent();

        System.out.println("Scraping process completed!");
    }
}
