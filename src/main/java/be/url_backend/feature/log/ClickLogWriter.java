package be.url_backend.feature.log;

import be.url_backend.feature.log.repository.ClickLogRepository;
import be.url_backend.feature.stats.DailyStats;
import be.url_backend.feature.stats.repository.DailyStatsRepository;
import be.url_backend.feature.url.UrlMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ClickLogWriter {

    private final ClickLogRepository clickLogRepository;
    private final DailyStatsRepository dailyStatsRepository;

    @Transactional
    public void saveLogAndStats(UrlMapping urlMapping, String userAgent, String ipAddress, String referer) {
        // JPA hibernate.jdbc.batch_size 설정과 save() 호출로 
        // 트랜잭션 내에서 모아서 벌크 인서트(Batch Insert)가 수행되도록 유도됨 (단건 Async 처리라 실질적으론 단건)
        // README의 "비동기 Multi-Row Bulk Insert" 문구를 맞추려면 Queue를 이용한 일괄처리가 필요하나 
        // 현재 아키텍처에서는 Async 워커당 1건씩 처리되므로 DB 레벨의 Batch를 활용하거나 문서 워딩 수정 필요.
        ClickLog clickLog = new ClickLog(urlMapping, userAgent, ipAddress, referer);
        clickLogRepository.save(clickLog);

        updateDailyStats(urlMapping);
    }

    private void updateDailyStats(UrlMapping urlMapping) {
        LocalDate today = LocalDate.now();
        DailyStats dailyStats = dailyStatsRepository.findByUrlMappingAndDate(urlMapping, today)
                .orElse(new DailyStats(urlMapping, today));

        dailyStats.incrementClickCount();
        dailyStatsRepository.save(dailyStats);
    }
}
