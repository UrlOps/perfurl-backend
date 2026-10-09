package be.url_backend.feature.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyTrafficTrendDto {
    private LocalDate date;
    private long clicks;
    private long uniqueVisitors;
}
