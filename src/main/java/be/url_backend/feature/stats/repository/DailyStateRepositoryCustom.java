package be.url_backend.feature.stats.repository;

import be.url_backend.feature.stats.DailyStatsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface DailyStateRepositoryCustom {
    Page<DailyStatsDto> searchDailyStats(Pageable pageable, LocalDate startDate, LocalDate endDate);
    Page<DailyStatsDto> searchDailyStats(Pageable pageable, String shortKey, LocalDate startDate, LocalDate endDate);
}
