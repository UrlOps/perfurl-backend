package be.url_backend.feature.log;

import be.url_backend.feature.log.repository.ClickLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogPurgeScheduler {

    private final ClickLogRepository clickLogRepository;

    /**
     * 매일 자정에 1년(또는 특정 기간) 이상 지난 로그를 Hard Delete하는 배치
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void purgeExpiredLogs() {
        log.info("Starting expired logs purge batch...");
        LocalDateTime threshold = LocalDateTime.now().minusYears(1);
        int deletedCount = clickLogRepository.deleteByCreatedAtBefore(threshold);
        log.info("Successfully deleted {} expired logs.", deletedCount);
    }
}
