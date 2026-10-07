package be.url_backend.feature.log.repository;

import be.url_backend.feature.log.ClickLog;
import be.url_backend.feature.log.ClickLogResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ClickLogRepositoryCustom {
    Page<ClickLogResponseDto> searchClickLogs(Pageable pageable, String ipAddress, LocalDate startDate, LocalDate endDate);
    Page<ClickLogResponseDto> searchClickLogs(Pageable pageable, String shortKey, String ipAddress, LocalDate startDate, LocalDate endDate);
    List<ClickLog> findClickLogsForAnalytics(String shortKey, LocalDate startDate, LocalDate endDate);
}