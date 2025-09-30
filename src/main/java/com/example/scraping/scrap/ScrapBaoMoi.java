package com.example.scraping.scrap;

import com.example.scraping.dto.ScrapedArticle;
import com.example.scraping.entity.ScrapData;
import com.example.scraping.repository.ScrapDataRepository;
import com.example.scraping.utils.TextUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Component
public class ScrapBaoMoi {

    private static final Logger logger = LoggerFactory.getLogger(ScrapBaoMoi.class);
    private static final String BASE_URL = "https://baomoi.com";
    private static final int TARGET_ENTRIES = 10000;
    private static final int DELAY_BETWEEN_REQUESTS = 2000; // 2 seconds

    @Autowired
    private ScrapDataRepository scrapDataRepository;

    private final Set<String> processedUrls = new HashSet<>();
    private final List<ScrapedArticle> scrapedArticles = new ArrayList<>();

    public void startScraping() {
        logger.info("Starting BaoMoi scraping...");

        try {
            // Get main categories and article links
            List<String> articleUrls = collectArticleUrls();
            logger.info("Collected {} article URLs", articleUrls.size());

            // Scrape each article
            int successCount = 0;
            int savedCount = 0;

            for (String url : articleUrls) {
                if (savedCount >= TARGET_ENTRIES) {
                    break;
                }

                try {
                    ScrapedArticle article = scrapeArticle(url);
                    if (article != null && TextUtils.isValidContent(article.getText())) {

                        // Check if article already exists in database
                        if (!scrapDataRepository.existsByMd5(article.getId())) {
                            // Save to database
                            ScrapData scrapData = convertToEntity(article);
                            scrapDataRepository.save(scrapData);

                            scrapedArticles.add(article);
                            savedCount++;
                            logger.info("Successfully saved article {} to database: {}", savedCount, article.getTitle());
                        } else {
                            logger.info("Article already exists, skipping: {}", article.getTitle());
                        }

                        successCount++;
                    }

                    // Delay between requests to be respectful
                    Thread.sleep(DELAY_BETWEEN_REQUESTS);

                } catch (Exception e) {
                    logger.error("Error scraping article {}: {}", url, e.getMessage());
                }
            }

            saveToDatabase();

            logger.info("Scraping completed. Total articles processed: {}, Saved to database: {}", successCount, savedCount);

        } catch (Exception e) {
            logger.error("Error during scraping process: {}", e.getMessage(), e);
        }
    }

    private ScrapData convertToEntity(ScrapedArticle article) {
        ScrapData scrapData = new ScrapData();
        scrapData.setMd5(article.getId());
        scrapData.setTitle(article.getTitle());
        scrapData.setText(article.getText());
        scrapData.setDomain((short) 1); // 1 for News
        scrapData.setUrl(article.getUrl());
        return scrapData;
    }

    private List<String> collectArticleUrls() throws IOException {
        List<String> urls = new ArrayList<>();

        // Get main page
        Document mainDoc = Jsoup.connect(BASE_URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get();

        logger.info("Successfully connected to BaoMoi.com");
        logger.info("Page title: {}", mainDoc.title());
        logger.info("HTML content length: {}", mainDoc.html().length());

        // Test multiple patterns for BaoMoi articles
        String[] articlePatterns = {
            "a[href*='/c/']",           // Original pattern
            "a[href*='/tin-tuc/']",     // News articles
            "a[href*='/bai-viet/']",    // Articles
            "a[href*='.epi']",          // BaoMoi specific extension
            "a[href*='.html']",         // HTML articles
            "a[href*='/story/']",       // Story format
            ".story-title a",           // Story title links
            ".news-item a",             // News items
            ".article-title a",         // Article titles
            "a[href^='/']"              // All relative links
        };

        for (String pattern : articlePatterns) {
            Elements links = mainDoc.select(pattern);
            logger.info("Pattern '{}' found {} links", pattern, links.size());

            if (links.size() > 0) {
                // Show first few examples
                int count = 0;
                for (Element link : links) {
                    String href = link.attr("href");
                    String text = link.text().trim();
                    if (!href.isEmpty() && !text.isEmpty() && count < 5) {
                        logger.info("  Example {}: {} -> {}", count + 1, text, href);
                        count++;
                    }
                }
            }
        }

        // Extract article links from main page with improved logic
        Elements articleLinks = mainDoc.select("a[href]");
        logger.info("Total links found on main page: {}", articleLinks.size());

        for (Element link : articleLinks) {
            String href = link.attr("href");
            String linkText = link.text().trim();

            // Skip empty links or navigation links
            if (href.isEmpty() || linkText.isEmpty() || linkText.length() < 10) {
                continue;
            }

            // Skip non-article links
            if (href.contains("javascript:") || href.contains("mailto:") ||
                href.contains("tel:") || href.startsWith("#")) {
                continue;
            }

            // Convert relative URLs to absolute
            if (href.startsWith("/")) {
                href = BASE_URL + href;
            }

            // Filter for likely article URLs
            if (isLikelyArticleUrl(href) && !processedUrls.contains(href)) {
                urls.add(href);
                processedUrls.add(href);
                logger.debug("Added article URL: {} - {}", linkText, href);
            }
        }

        logger.info("Collected {} potential article URLs from main page", urls.size());

        // Try to get more articles from category/section pages
        Elements categoryLinks = mainDoc.select("a[href*='/the-loai/'], a[href*='/chuyen-muc/'], a[href*='/category/']");
        logger.info("Found {} category links", categoryLinks.size());

        for (Element categoryLink : categoryLinks) {
            if (urls.size() >= TARGET_ENTRIES * 2) break; // Get more URLs than needed

            try {
                String categoryUrl = categoryLink.attr("href");
                if (categoryUrl.startsWith("/")) {
                    categoryUrl = BASE_URL + categoryUrl;
                }

                logger.info("Processing category: {}", categoryUrl);

                Document categoryDoc = Jsoup.connect(categoryUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .timeout(10000)
                        .get();

                Elements moreArticles = categoryDoc.select("a[href]");
                int categoryArticleCount = 0;

                for (Element article : moreArticles) {
                    String href = article.attr("href");
                    String linkText = article.text().trim();

                    if (href.isEmpty() || linkText.isEmpty() || linkText.length() < 10) {
                        continue;
                    }

                    if (href.startsWith("/")) {
                        href = BASE_URL + href;
                    }

                    if (isLikelyArticleUrl(href) && !processedUrls.contains(href)) {
                        urls.add(href);
                        processedUrls.add(href);
                        categoryArticleCount++;
                    }
                }

                logger.info("Added {} articles from category: {}", categoryArticleCount, categoryUrl);
                Thread.sleep(DELAY_BETWEEN_REQUESTS);

            } catch (Exception e) {
                logger.warn("Error processing category: {}", e.getMessage());
            }
        }

        logger.info("Total collected URLs: {}", urls.size());
        return urls;
    }

    private boolean isLikelyArticleUrl(String url) {
        if (url == null || !url.contains("baomoi.com")) {
            return false;
        }

        // BaoMoi specific patterns
        return url.contains("/c/") ||
               url.contains("/tin-tuc/") ||
               url.contains("/bai-viet/") ||
               url.contains(".epi") ||
               url.contains("/story/") ||
               (url.contains(".html") && !url.contains("/static/")) ||
               // Generic article indicators
               (url.split("/").length >= 4 &&
                !url.contains("/the-loai/") &&
                !url.contains("/chuyen-muc/") &&
                !url.contains("/tag/") &&
                !url.contains("/search") &&
                !url.contains("/page/"));
    }

    private ScrapedArticle scrapeArticle(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .get();

            // Extract title
            String title = extractTitle(doc);
            if (title == null || title.trim().isEmpty()) {
                return null;
            }

            // Extract main content
            String content = extractContent(doc);
            if (content.trim().isEmpty()) {
                return null;
            }

            // Clean and process content
            content = TextUtils.cleanText(content);
            content = TextUtils.processImageLinks(content);
            content = TextUtils.anonymizeText(content);

            // Validate content
            if (!TextUtils.isValidContent(content)) {
                return null;
            }

            // Create article object
            ScrapedArticle article = new ScrapedArticle();
            article.setId(TextUtils.generateMD5(url + title));
            article.setTitle(TextUtils.cleanText(title));
            article.setText(content);
            article.setDomain("News");
            article.setUrl(url);

            return article;

        } catch (Exception e) {
            logger.error("Error scraping article {}: {}", url, e.getMessage());
            return null;
        }
    }

    private String extractTitle(Document doc) {
        // Try multiple selectors for title
        String[] titleSelectors = {
            "h1.article-title",
            "h1.title",
            ".article-header h1",
            "h1",
            ".post-title",
            ".entry-title"
        };

        for (String selector : titleSelectors) {
            Elements titleElements = doc.select(selector);
            if (!titleElements.isEmpty()) {
                return titleElements.first().text().trim();
            }
        }

        // Fallback to page title
        return doc.title();
    }

    private String extractContent(Document doc) {
        StringBuilder contentBuilder = new StringBuilder();

        // Remove unwanted elements
        doc.select("script, style, nav, header, footer, .advertisement, .ads, .popup, .banner").remove();
        doc.select(".social-share, .related-articles, .comments, .comment").remove();
        doc.select(".navigation, .menu, .sidebar").remove();

        // Try multiple selectors for content
        String[] contentSelectors = {
            ".article-content",
            ".post-content",
            ".entry-content",
            ".content",
            "article",
            ".article-body",
            ".main-content"
        };

        Elements contentElements = null;
        for (String selector : contentSelectors) {
            contentElements = doc.select(selector);
            if (!contentElements.isEmpty()) {
                break;
            }
        }

        if (!contentElements.isEmpty()) {
            for (Element contentElement : contentElements) {
                // Extract text and preserve image information
                Elements paragraphs = contentElement.select("p, div.paragraph, .content-paragraph");
                for (Element paragraph : paragraphs) {
                    // Handle images within paragraphs
                    Elements images = paragraph.select("img");
                    for (Element img : images) {
                        String src = img.attr("src");
                        if (!src.isEmpty()) {
                            if (src.startsWith("/")) {
                                src = BASE_URL + src;
                            }
                            img.replaceWith(doc.createElement("span").text("[img_" + src + "]"));
                        }
                    }

                    String paragraphText = paragraph.text().trim();
                    if (paragraphText.length() > 20) {
                        contentBuilder.append(paragraphText).append("\n\n");
                    }
                }

                // If no paragraphs found, get all text
                if (contentBuilder.isEmpty()) {
                    String allText = contentElement.text().trim();
                    if (!allText.isEmpty()) {
                        contentBuilder.append(allText);
                    }
                }
            }
        }

        return contentBuilder.toString().trim();
    }

    private void saveToDatabase() {
        List<ScrapData> entities = scrapedArticles.stream()
                .map(this::convertToEntity)
                .toList();
        scrapDataRepository.saveAll(entities);
        logger.info("Saved {} articles to database", entities.size());
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
