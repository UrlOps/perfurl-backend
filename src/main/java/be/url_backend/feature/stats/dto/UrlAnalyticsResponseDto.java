package be.url_backend.feature.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlAnalyticsResponseDto {
    private String shortKey;
    private String originalUrl;
    private long totalClicks;
    private long uniqueVisitors;
    private List<DailyTrafficTrendDto> dailyTrends;
    private List<TrafficRatioDto> browsers;
    private List<TrafficRatioDto> operatingSystems;
    private List<TrafficRatioDto> deviceTypes;
    private List<TrafficCountDto> topIpAddresses;
    private List<TrafficCountDto> topReferrers;
}
