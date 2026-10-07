package be.url_backend.feature.url.dto;

import be.url_backend.feature.url.UrlMapping;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UrlResponseDto {
    private final Long id;
    private final String shortKey;
    private final String originalUrl;
    private String shortenUrl;
    private final LocalDateTime createdAt;
    private final LocalDateTime expireAt;

    public UrlResponseDto(Long id, String shortKey, String originalUrl, LocalDateTime createdAt, LocalDateTime expireAt) {
        this.id = id;
        this.shortKey = shortKey;
        this.originalUrl = originalUrl;
        this.createdAt = createdAt;
        this.expireAt = expireAt;
    }

    public static UrlResponseDto from(UrlMapping urlMapping, String baseUrl) {
        return UrlResponseDto.builder()
                .id(urlMapping.getId())
                .shortKey(urlMapping.getShortKey())
                .originalUrl(urlMapping.getOriginalUrl())
                .shortenUrl(baseUrl + "/" + urlMapping.getShortKey())
                .createdAt(urlMapping.getCreatedAt())
                .expireAt(urlMapping.getExpiresAt())
                .build();
    }

    public void setShortenUrl(String shortenUrl) {
        this.shortenUrl = shortenUrl;
    }
}