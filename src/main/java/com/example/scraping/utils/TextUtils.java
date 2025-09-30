package com.example.scraping.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

public class TextUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b(?:\\+84|84|0)(?:[3579]\\d{8}|[12]\\d{9})\\b");
    private static final Pattern SOCIAL_MEDIA_PATTERN = Pattern.compile("(?:facebook\\.com/|fb\\.com/|instagram\\.com/|twitter\\.com/|linkedin\\.com/in/)[\\w.-]+");
    private static final Pattern ID_NUMBER_PATTERN = Pattern.compile("\\b\\d{9,12}\\b");
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("\\b\\d{13,19}\\b");
    private static final Pattern DATE_PATTERN = Pattern.compile("\\b\\d{1,2}[-/]\\d{1,2}[-/]\\d{4}\\b");

    // Patterns for removing unwanted content
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern EMOJI_PATTERN = Pattern.compile("[\\p{So}\\p{Sk}]");
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");
    private static final Pattern MULTIPLE_NEWLINES = Pattern.compile("\\n{3,}");
    private static final Pattern AD_KEYWORDS = Pattern.compile("(?i)(quảng cáo|advertisement|sponsored|ads|banner|popup)");

    public static String generateMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    public static String anonymizeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // Anonymize emails
        text = EMAIL_PATTERN.matcher(text).replaceAll("x@x.x");

        // Anonymize phone numbers
        text = PHONE_PATTERN.matcher(text).replaceAll("xxxxxxxxxx");

        // Anonymize social media profiles
        text = SOCIAL_MEDIA_PATTERN.matcher(text).replaceAll("social.x/x");

        // Anonymize ID numbers
        text = ID_NUMBER_PATTERN.matcher(text).replaceAll("xxxxxxxxxx");

        // Anonymize bank card numbers
        text = BANK_CARD_PATTERN.matcher(text).replaceAll("xxxxxxxxxxxxxxxx");

        // Anonymize dates
        text = DATE_PATTERN.matcher(text).replaceAll("xx/xx/xxxx");

        return text;
    }

    public static String cleanText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // Remove HTML tags
        text = HTML_TAG_PATTERN.matcher(text).replaceAll("");

        // Remove emojis and special symbols
        text = EMOJI_PATTERN.matcher(text).replaceAll("");

        // Remove advertisement content
        text = AD_KEYWORDS.matcher(text).replaceAll("");

        // Clean multiple spaces and newlines
        text = MULTIPLE_SPACES.matcher(text).replaceAll(" ");
        text = MULTIPLE_NEWLINES.matcher(text).replaceAll("\n\n");

        // Trim and normalize
        text = text.trim();

        return text;
    }

    public static boolean isValidContent(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        // Check minimum word count (200 words)
        String[] words = text.trim().split("\\s+");
        if (words.length < 200) {
            return false;
        }

        // Check for advertisement keywords
        if (AD_KEYWORDS.matcher(text).find()) {
            return false;
        }

        return true;
    }

    public static String processImageLinks(String text) {
        // Replace image tags with the required format
        text = text.replaceAll("<img[^>]*src=[\"']([^\"']+)[\"'][^>]*>", "[img_$1]");
        return text;
    }
}
