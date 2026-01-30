package com.example.URLShortnerService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.URL;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortenUrlRequest {
    @NotBlank(message = "URL is required")
    @URL(message = "Invalid URL format")
    private String url;
}
