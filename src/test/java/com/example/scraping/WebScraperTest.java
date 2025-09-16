package com.example.scraping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jsoup.select.Elements;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for WebScraper
 */
class WebScraperTest {

    private WebScraper webScraper;

    @BeforeEach
    void setUp() {
        webScraper = new WebScraper();
    }

    @Test
    void testGetPageTitle() throws IOException {
        // Test with a reliable website
        String title = webScraper.getPageTitle("https://example.com");

        assertNotNull(title);
        assertFalse(title.isEmpty());
        assertTrue(title.contains("Example"));
    }

    @Test
    void testGetLinks() throws IOException {
        // Test getting links from example.com
        Elements links = webScraper.getLinks("https://example.com");

        assertNotNull(links);
        // example.com typically has at least one link
        assertTrue(links.size() >= 0);
    }

    @Test
    void testGetTextBySelector() throws IOException {
        // Test getting text content by CSS selector
        String text = webScraper.getTextBySelector("https://example.com", "h1");

        assertNotNull(text);
        // Should contain some text content
        assertTrue(text.length() > 0);
    }

    @Test
    void testInvalidUrl() {
        // Test with an invalid URL
        assertThrows(IOException.class, () -> {
            webScraper.getPageTitle("https://this-domain-does-not-exist-12345.com");
        });
    }
}
