package com.example.URLShortnerService;

import com.example.URLShortnerService.util.ShortCodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UrlShortnerService {

    private final ShortUrlRepository shortUrlRepository;
    private final ShortCodeGenerator shortCodeGenerator;

    public UrlShortnerService(ShortUrlRepository shortUrlRepository,
                              ShortCodeGenerator shortCodeGenerator) {
        this.shortUrlRepository = shortUrlRepository;
        this.shortCodeGenerator = shortCodeGenerator;
    }

    @Transactional
    public ShortUrl createShortUrl(String originalUrl, LocalDateTime expiresAt) {
        // Return existing mapping if URL already shortened
        return shortUrlRepository.findByOriginalUrl(originalUrl)
                .orElseGet(() -> {
                    String shortCode = generateUniqueShortCode();

                    ShortUrl shortUrl = ShortUrl.builder()
                            .shortCode(shortCode)
                            .originalUrl(originalUrl)
                            .expiresAt(expiresAt)
                            .build();

                    return shortUrlRepository.save(shortUrl);
                });
    }

    @Transactional(readOnly = true)
    public ShortUrl getOriginalUrl(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode)
                .orElse(null);
    }

    private String generateUniqueShortCode() {
        String code;
        int attempts = 0;
        do {
            if (attempts++ > 10) {
                throw new IllegalStateException("Failed to generate unique short code after multiple attempts");
            }
            code = shortCodeGenerator.generateShortCode();
        } while (shortUrlRepository.findByShortCode(code).isPresent());
        return code;
    }
}

