package com.example.URLShortnerService.controller;

import com.example.URLShortnerService.ShortUrl;
import com.example.URLShortnerService.UrlShortnerService;
import com.example.URLShortnerService.dto.ShortenUrlRequest;
import com.example.URLShortnerService.dto.ShortenUrlResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class HomeController {

    private final UrlShortnerService urlShortnerService;

    public HomeController(UrlShortnerService urlShortnerService) {
        this.urlShortnerService = urlShortnerService;
    }

    @GetMapping("/")
    public String showForm(Model model) {
        model.addAttribute("request", new ShortenUrlRequest());
        return "index";
    }

    @PostMapping("/shorten")
    public String shortenUrl(@Valid ShortenUrlRequest request, 
                            BindingResult bindingResult, 
                            Model model,
                            HttpServletRequest httpRequest) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("request", request);
            return "index";
        }

        ShortUrl shortUrl = urlShortnerService.createShortUrl(request.getUrl(), null);
        
        String baseUrl = httpRequest.getRequestURL().toString()
                .replace(httpRequest.getRequestURI(), "");
        String shortUrlString = baseUrl + "/" + shortUrl.getShortCode();

        ShortenUrlResponse response = ShortenUrlResponse.builder()
                .shortCode(shortUrl.getShortCode())
                .shortUrl(shortUrlString)
                .originalUrl(shortUrl.getOriginalUrl())
                .createdAt(shortUrl.getCreatedAt())
                .expiresAt(shortUrl.getExpiresAt())
                .build();

        model.addAttribute("request", new ShortenUrlRequest());
        model.addAttribute("response", response);
        return "index";
    }

    @GetMapping("/{shortCode}")
    public String redirectToOriginalUrl(@PathVariable String shortCode) {
        ShortUrl shortUrl = urlShortnerService.getOriginalUrl(shortCode);
        
        if (shortUrl == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Short URL not found");
        }

        // Check if expired
        if (shortUrl.getExpiresAt() != null && 
            shortUrl.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Short URL has expired");
        }

        return "redirect:" + shortUrl.getOriginalUrl();
    }
}
