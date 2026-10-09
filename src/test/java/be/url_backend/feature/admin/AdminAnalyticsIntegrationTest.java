package be.url_backend.feature.admin;

import be.url_backend.common.util.JwtUtil;
import be.url_backend.feature.log.ClickLog;
import be.url_backend.feature.log.repository.ClickLogRepository;
import be.url_backend.feature.stats.DailyStats;
import be.url_backend.feature.stats.repository.DailyStatsRepository;
import be.url_backend.feature.url.UrlMapping;
import be.url_backend.feature.url.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminAnalyticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AdminService adminService;

    @Autowired
    private UrlMappingRepository urlMappingRepository;

    @Autowired
    private ClickLogRepository clickLogRepository;

    @Autowired
    private DailyStatsRepository dailyStatsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String adminToken;
    private UrlMapping testUrlMapping;

    @BeforeEach
    void setUp() {
        clickLogRepository.deleteAll();
        dailyStatsRepository.deleteAll();
        urlMappingRepository.deleteAll();
        adminRepository.deleteAll();

        // 1. 관리자 계정 생성 및 토큰 발급
        Admin admin = new Admin("admin", passwordEncoder.encode("password123"));
        adminRepository.save(admin);
        adminToken = "Bearer " + jwtUtil.generateToken(adminService.loadUserByUsername("admin"));

        // 2. 단축 URL 생성
        testUrlMapping = UrlMapping.createUrlMapping("https://www.example.com/long-path/page");
        testUrlMapping.updateShortKey("targetKey");
        urlMappingRepository.save(testUrlMapping);

        // 다른 URL 생성 (필터링 검증용)
        UrlMapping otherMapping = UrlMapping.createUrlMapping("https://www.other.com");
        otherMapping.updateShortKey("otherKey");
        urlMappingRepository.save(otherMapping);

        // 3. 클릭 로그 적재 (testUrlMapping 에 3건, otherMapping 에 1건)
        // Log 1: Windows Chrome, IP: 192.168.1.1, Referer: https://google.com
        ClickLog log1 = new ClickLog(
                testUrlMapping,
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                "192.168.1.1",
                "https://google.com"
        );
        // Log 2: iPhone Safari, IP: 192.168.1.1 (동일 IP - 고유 방문자 카운트 검증), Referer: https://naver.com
        ClickLog log2 = new ClickLog(
                testUrlMapping,
                "Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/604.1",
                "192.168.1.1",
                "https://naver.com"
        );
        // Log 3: Mac Edge, IP: 10.0.0.2 (다른 IP), Referer: direct
        ClickLog log3 = new ClickLog(
                testUrlMapping,
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0",
                "10.0.0.2",
                ""
        );
        clickLogRepository.save(log1);
        clickLogRepository.save(log2);
        clickLogRepository.save(log3);

        ClickLog otherLog = new ClickLog(
                otherMapping,
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
                "172.16.0.1",
                "https://other.com"
        );
        clickLogRepository.save(otherLog);

        // 4. DailyStats 생성
        DailyStats dailyStats1 = new DailyStats(testUrlMapping, LocalDate.now());
        dailyStats1.incrementClickCount();
        dailyStats1.incrementClickCount();
        dailyStatsRepository.save(dailyStats1);

        DailyStats dailyStats2 = new DailyStats(otherMapping, LocalDate.now());
        dailyStats2.incrementClickCount();
        dailyStatsRepository.save(dailyStats2);
    }

    @Test
    @DisplayName("단축 URL별 클라이언트 데이터 분석 및 트래픽 트렌드 통계 API 검증")
    void testGetUrlAnalytics() throws Exception {
        mockMvc.perform(get("/api/admin/urls/targetKey/analytics")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statuscode").value("200"))
                .andExpect(jsonPath("$.data.shortKey").value("targetKey"))
                .andExpect(jsonPath("$.data.originalUrl").value("https://www.example.com/long-path/page"))
                .andExpect(jsonPath("$.data.totalClicks").value(3))
                .andExpect(jsonPath("$.data.uniqueVisitors").value(2)) // 192.168.1.1, 10.0.0.2 -> 2명
                .andExpect(jsonPath("$.data.dailyTrends", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.browsers", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.operatingSystems", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.deviceTypes", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.topIpAddresses", hasSize(2)))
                .andExpect(jsonPath("$.data.topIpAddresses[0].key").value("192.168.1.1"))
                .andExpect(jsonPath("$.data.topIpAddresses[0].count").value(2));
    }

    @Test
    @DisplayName("일별 통계 API - shortKey 필터링 및 shortKey, originalUrl 필드 매핑 검증")
    void testGetDailyStatsWithShortKey() throws Exception {
        mockMvc.perform(get("/api/admin/daily-stats")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .param("shortKey", "targetKey")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].shortKey").value("targetKey"))
                .andExpect(jsonPath("$.content[0].originalUrl").value("https://www.example.com/long-path/page"))
                .andExpect(jsonPath("$.content[0].clickCount").value(2));
    }

    @Test
    @DisplayName("클릭 로그 API - shortKey 필터링 검증")
    void testGetClickLogsWithShortKey() throws Exception {
        mockMvc.perform(get("/api/admin/click-logs")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .param("shortKey", "targetKey")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].shortKey").value("targetKey"));
    }
}
