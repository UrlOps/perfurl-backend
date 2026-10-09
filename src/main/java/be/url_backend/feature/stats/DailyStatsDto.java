package be.url_backend.feature.stats;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
public class DailyStatsDto {
    private Long id;
    private String shortKey;
    private String originalUrl;
    private LocalDate date;
    private Long clickCount;

    public DailyStatsDto(Long id, String date, Long clickCount) {
        this.id = id;
        this.date = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.clickCount = clickCount;
    }

    public DailyStatsDto(Long id, String shortKey, String originalUrl, String date, Long clickCount) {
        this.id = id;
        this.shortKey = shortKey;
        this.originalUrl = originalUrl;
        this.date = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.clickCount = clickCount;
    }

    public DailyStatsDto(Long id, String shortKey, String originalUrl, LocalDate date, Long clickCount) {
        this.id = id;
        this.shortKey = shortKey;
        this.originalUrl = originalUrl;
        this.date = date;
        this.clickCount = clickCount;
    }
}
