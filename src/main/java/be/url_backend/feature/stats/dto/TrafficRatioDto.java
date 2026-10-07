package be.url_backend.feature.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrafficRatioDto {
    private String name;
    private long count;
    private double percentage;
}
