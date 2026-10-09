package be.url_backend.feature.admin;

import be.url_backend.common.exception.CustomException;
import be.url_backend.common.exception.ErrorCode;
import be.url_backend.common.util.UserAgentAnalyzer;
import be.url_backend.feature.admin.dto.AdminResponseDto;
import be.url_backend.feature.admin.dto.AdminSignupRequestDto;
import be.url_backend.feature.log.ClickLog;
import be.url_backend.feature.log.ClickLogResponseDto;
import be.url_backend.feature.log.repository.ClickLogRepository;
import be.url_backend.feature.stats.DailyStatsDto;
import be.url_backend.feature.stats.dto.*;
import be.url_backend.feature.stats.repository.DailyStatsRepository;
import be.url_backend.feature.url.UrlMapping;
import be.url_backend.feature.url.dto.UrlResponseDto;
import be.url_backend.feature.url.repository.UrlMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService implements UserDetailsService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClickLogRepository clickLogRepository;
    private final UrlMappingRepository urlMappingRepository;
    private final DailyStatsRepository dailyStatsRepository;

    @Transactional
    public AdminResponseDto signup(AdminSignupRequestDto requestDto) {
        if (adminRepository.findByUsername(requestDto.getUsername()).isPresent()) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        Admin admin = new Admin(requestDto.getUsername(), encodedPassword);
        Admin savedAdmin = adminRepository.save(admin);
        return AdminResponseDto.from(savedAdmin);
    }

    public void login(String username, String password) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
    }

    public Page<UrlResponseDto> getAllUrlMappings(Pageable pageable, String shortKey, String originalUrl, LocalDate startDate, LocalDate endDate, String baseUrl) {
        Page<UrlResponseDto> page = urlMappingRepository.searchUrlMappings(pageable, shortKey, originalUrl, startDate, endDate);
        page.getContent().forEach(dto -> dto.setShortenUrl(baseUrl + "/" + dto.getShortKey()));
        return page;
    }

    public Page<ClickLogResponseDto> getAllClickLogs(Pageable pageable, String ipAddress, LocalDate startDate, LocalDate endDate) {
        return getAllClickLogs(pageable, null, ipAddress, startDate, endDate);
    }

    public Page<ClickLogResponseDto> getAllClickLogs(Pageable pageable, String shortKey, String ipAddress, LocalDate startDate, LocalDate endDate) {
        return clickLogRepository.searchClickLogs(pageable, shortKey, ipAddress, startDate, endDate);
    }

    public Page<DailyStatsDto> getDailyStats(Pageable pageable, LocalDate startDate, LocalDate endDate) {
        return getDailyStats(pageable, null, startDate, endDate);
    }

    public Page<DailyStatsDto> getDailyStats(Pageable pageable, String shortKey, LocalDate startDate, LocalDate endDate) {
        return dailyStatsRepository.searchDailyStats(pageable, shortKey, startDate, endDate);
    }

    public UrlAnalyticsResponseDto getUrlAnalytics(String shortKey, LocalDate startDate, LocalDate endDate) {
        UrlMapping urlMapping = urlMappingRepository.findByShortKey(shortKey)
                .orElseThrow(() -> new CustomException(ErrorCode.URL_NOT_FOUND));

        List<ClickLog> clickLogs = clickLogRepository.findClickLogsForAnalytics(shortKey, startDate, endDate);
        long totalClicks = clickLogs.size();

        // 1. 고유 방문자 수 (Distinct IP)
        Set<String> uniqueIps = clickLogs.stream()
                .map(ClickLog::getIpAddress)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        long uniqueVisitors = uniqueIps.size();

        // 2. 일별 트래픽 트렌드 (Daily Trends)
        Map<LocalDate, List<ClickLog>> logsByDate = clickLogs.stream()
                .collect(Collectors.groupingBy(log -> log.getCreatedAt().toLocalDate(), TreeMap::new, Collectors.toList()));

        List<DailyTrafficTrendDto> dailyTrends = logsByDate.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<ClickLog> dailyLogs = entry.getValue();
                    long dailyClicks = dailyLogs.size();
                    long dailyUv = dailyLogs.stream()
                            .map(ClickLog::getIpAddress)
                            .filter(Objects::nonNull)
                            .distinct()
                            .count();
                    return DailyTrafficTrendDto.builder()
                            .date(date)
                            .clicks(dailyClicks)
                            .uniqueVisitors(dailyUv)
                            .build();
                })
                .collect(Collectors.toList());

        // 3. User-Agent 분석 (Browser, OS, Device Type)
        Map<String, Long> browserCounts = new HashMap<>();
        Map<String, Long> osCounts = new HashMap<>();
        Map<String, Long> deviceCounts = new HashMap<>();

        for (ClickLog log : clickLogs) {
            UserAgentAnalyzer.ClientInsight insight = UserAgentAnalyzer.analyze(log.getUserAgent());
            browserCounts.merge(insight.browser(), 1L, Long::sum);
            osCounts.merge(insight.os(), 1L, Long::sum);
            deviceCounts.merge(insight.deviceType(), 1L, Long::sum);
        }

        List<TrafficRatioDto> browsers = calculateRatios(browserCounts, totalClicks);
        List<TrafficRatioDto> operatingSystems = calculateRatios(osCounts, totalClicks);
        List<TrafficRatioDto> deviceTypes = calculateRatios(deviceCounts, totalClicks);

        // 4. IP 분석 (Top IP Addresses)
        Map<String, Long> ipCounts = clickLogs.stream()
                .map(ClickLog::getIpAddress)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        List<TrafficCountDto> topIpAddresses = ipCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> new TrafficCountDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        // 5. 유입 경로 분석 (Top Referrers)
        Map<String, Long> referrerCounts = clickLogs.stream()
                .map(log -> (log.getReferer() == null || log.getReferer().isBlank()) ? "Direct / None" : log.getReferer())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        List<TrafficCountDto> topReferrers = referrerCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> new TrafficCountDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return UrlAnalyticsResponseDto.builder()
                .shortKey(urlMapping.getShortKey())
                .originalUrl(urlMapping.getOriginalUrl())
                .totalClicks(totalClicks)
                .uniqueVisitors(uniqueVisitors)
                .dailyTrends(dailyTrends)
                .browsers(browsers)
                .operatingSystems(operatingSystems)
                .deviceTypes(deviceTypes)
                .topIpAddresses(topIpAddresses)
                .topReferrers(topReferrers)
                .build();
    }

    private List<TrafficRatioDto> calculateRatios(Map<String, Long> counts, long total) {
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> {
                    double percentage = total > 0 ? Math.round((entry.getValue() * 100.0 / total) * 10.0) / 10.0 : 0.0;
                    return new TrafficRatioDto(entry.getKey(), entry.getValue(), percentage);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUrl(String shortKey) {
        UrlMapping urlMapping = urlMappingRepository.findByShortKey(shortKey)
                .orElseThrow(() -> new CustomException(ErrorCode.URL_NOT_FOUND));
        urlMappingRepository.delete(urlMapping);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return adminRepository.findByUsername(username)
                .map(admin -> new User(admin.getUsername(), admin.getPassword(), Collections
                        .singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }
}
