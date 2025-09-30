package com.example.scraping.service;

import com.example.scraping.entity.ScrapData;
import com.example.scraping.repository.ScrapDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class ScrapDataService {

    private static final Logger logger = LoggerFactory.getLogger(ScrapDataService.class);

    @Autowired
    private ScrapDataRepository scrapDataRepository;

    public ScrapData toScrapData(String title, String text, Short domain, String url) {
        ScrapData scrapData = new ScrapData();
        scrapData.setMd5(generateMd5Hash(title));
        scrapData.setTitle(title);
        scrapData.setText(text);
        scrapData.setDomain(domain);
        scrapData.setUrl(url);
        return scrapDataRepository.save(scrapData);
    }

    public void saveScrapData(List<ScrapData> scrapData) {
        scrapDataRepository.saveAll(scrapData);
    }

    private String generateMd5Hash(String input) {
        if (input == null) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("MD5 algorithm not available", e);
            return null;
        }
    }
}
