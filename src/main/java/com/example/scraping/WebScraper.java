package com.example.scraping;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Main class for web scraping operations
 */
public class WebScraper {
    private static final Logger logger = LoggerFactory.getLogger(WebScraper.class);

    public static void main(String[] args) {
        WebScraper scraper = new WebScraper();

        try {
            // Example: scrape a website's title
            String url = "https://example.com";
            String title = scraper.getPageTitle(url);
            logger.info("Page title: {}", title);
            System.out.println("Page title: " + title);
        } catch (IOException e) {
            logger.error("Error scraping website", e);
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Get the title of a web page
     * @param url The URL to scrape
     * @return The page title
     * @throws IOException if there's an error connecting to the URL
     */
    public String getPageTitle(String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        return document.title();
    }

    /**
     * Get all links from a web page
     * @param url The URL to scrape
     * @return Elements containing all links
     * @throws IOException if there's an error connecting to the URL
     */
    public Elements getLinks(String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        return document.select("a[href]");
    }

    /**
     * Get text content from elements matching a CSS selector
     * @param url The URL to scrape
     * @param selector CSS selector to match elements
     * @return Text content of matching elements
     * @throws IOException if there's an error connecting to the URL
     */
    public String getTextBySelector(String url, String selector) throws IOException {
        Document document = Jsoup.connect(url).get();
        Elements elements = document.select(selector);
        StringBuilder text = new StringBuilder();

        for (Element element : elements) {
            text.append(element.text()).append("\n");
        }

        return text.toString().trim();
    }
}
