package be.url_backend.feature.log;

import be.url_backend.feature.url.UrlMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClickLogService {

    private final ClickLogWriter clickLogWriter;

    @Async("asyncExecutor")
    public void logClickAndupdateDailyStats(UrlMapping urlMapping, String userAgent, String ipAddress, String referer) {
        clickLogWriter.saveLogAndStats(urlMapping, userAgent, ipAddress, referer);
    }
    }