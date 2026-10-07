package be.url_backend.feature.admin;

import be.url_backend.feature.admin.dto.AdminLoginRequestDto;
import be.url_backend.feature.admin.dto.AdminResponseDto;
import be.url_backend.feature.admin.dto.AdminSignupRequestDto;
import be.url_backend.feature.log.ClickLogResponseDto;
import be.url_backend.feature.stats.DailyStatsDto;
import be.url_backend.feature.stats.dto.UrlAnalyticsResponseDto;
import be.url_backend.common.dto.JwtResponseDto;
import be.url_backend.common.dto.ApiResponse;
import be.url_backend.common.dto.ResponseText;
import be.url_backend.feature.url.dto.UrlResponseDto;
import be.url_backend.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    /**
     * 관리자 회원가입 API
     *
     * @param requestDto 관리자 회원가입 요청 데이터
     * @return 생성된 관리자 정보
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AdminResponseDto>> signup(@RequestBody AdminSignupRequestDto requestDto) {
        AdminResponseDto adminResponseDto = adminService.signup(requestDto);
        ApiResponse<AdminResponseDto> response = ApiResponse.<AdminResponseDto>builder()
                .msg(ResponseText.ADMIN_SIGNUP_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.CREATED.value()))
                .data(adminResponseDto)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * 관리자 로그인 API
     *
     * @param requestDto 관리자 로그인 요청 데이터
     * @return JWT 토큰
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponseDto>> login(@RequestBody AdminLoginRequestDto requestDto) {
        adminService.login(requestDto.getUsername(), requestDto.getPassword());
        UserDetails userDetails = adminService.loadUserByUsername(requestDto.getUsername());
        String token = jwtUtil.generateToken(userDetails);
        ApiResponse<JwtResponseDto> response = ApiResponse.<JwtResponseDto>builder()
                .msg(ResponseText.ADMIN_LOGIN_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .data(new JwtResponseDto(token))
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * URL 삭제 API (관리자)
     *
     * @param shortKey 삭제할 단축 URL 키
     * @param admin    인증된 관리자 정보
     * @return 작업 성공 여부를 담은 ApiResponse
     */
    @DeleteMapping("/urls/{shortKey}")
    public ResponseEntity<ApiResponse<Void>> deleteUrl(
            @PathVariable String shortKey,
            @AuthenticationPrincipal Admin admin) {
        adminService.deleteUrl(shortKey);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .msg(ResponseText.URL_DELETE_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.NO_CONTENT.value()))
                .build();
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    /**
     * 전체 URL 매핑 조회 API (페이징)
     *
     * @param pageable    페이징 정보
     * @param shortKey    단축 URL 키 (선택적 필터)
     * @param originalUrl 원본 URL (선택적 필터)
     * @param startDate   시작일 (선택적 필터)
     * @param endDate     종료일 (선택적 필터)
     * @param request     Base URL 생성을 위한 요청 객체
     * @return URL 매핑 데이터 페이지
     */
    @GetMapping("/url-mappings")
    public ResponseEntity<Page<UrlResponseDto>> getAllUrlMappings(
            Pageable pageable,
            @AuthenticationPrincipal Admin admin,
            @RequestParam(required = false) String shortKey,
            @RequestParam(required = false) String originalUrl,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest request) {
        String baseUrl = getBaseUrl(request);
        Page<UrlResponseDto> urlMappings = adminService.getAllUrlMappings(pageable, shortKey, originalUrl, startDate, endDate, baseUrl);
        return ResponseEntity.ok(urlMappings);
    }

    /**
     * 전체 클릭 로그 조회 API (페이징)
     *
     * @param pageable  페이징 정보
     * @param shortKey  단축 URL 키 (선택적 필터)
     * @param ipAddress IP 주소 (선택적 필터)
     * @param startDate 시작일 (선택적 필터)
     * @param endDate   종료일 (선택적 필터)
     * @return 클릭 로그 데이터 페이지
     */
    @GetMapping("/click-logs")
    public ResponseEntity<Page<ClickLogResponseDto>> getAllClickLogs(
            Pageable pageable,
            @AuthenticationPrincipal Admin admin,
            @RequestParam(required = false) String shortKey,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Page<ClickLogResponseDto> clickLogs = adminService.getAllClickLogs(pageable, shortKey, ipAddress, startDate, endDate);
        return ResponseEntity.ok(clickLogs);
    }

    /**
     * 일별 통계 조회 API (페이징)
     *
     * @param pageable  페이징 정보
     * @param shortKey  단축 URL 키 (선택적 필터)
     * @param startDate 시작일 (선택적 필터)
     * @param endDate   종료일 (선택적 필터)
     * @return 일별 통계 데이터 페이지
     */
    @GetMapping("/daily-stats")
    public ResponseEntity<Page<DailyStatsDto>> getDailyStats(
            Pageable pageable,
            @AuthenticationPrincipal Admin admin,
            @RequestParam(required = false) String shortKey,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Page<DailyStatsDto> stats = adminService.getDailyStats(pageable, shortKey, startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    /**
     * 단축 URL별 트래픽 트렌드 및 클라이언트 데이터 분석 통계 API
     *
     * @param shortKey  단축 URL 키
     * @param admin     인증된 관리자 정보
     * @param startDate 시작일 (선택적 필터)
     * @param endDate   종료일 (선택적 필터)
     * @return 클라이언트 데이터(IP, User-Agent, Referer) 분석 및 일자별 트래픽 트렌드 인사이트 DTO
     */
    @GetMapping("/urls/{shortKey}/analytics")
    public ResponseEntity<ApiResponse<UrlAnalyticsResponseDto>> getUrlAnalytics(
            @PathVariable String shortKey,
            @AuthenticationPrincipal Admin admin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UrlAnalyticsResponseDto analytics = adminService.getUrlAnalytics(shortKey, startDate, endDate);
        ApiResponse<UrlAnalyticsResponseDto> response = ApiResponse.<UrlAnalyticsResponseDto>builder()
                .msg(ResponseText.URL_ANALYTICS_FETCH_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .data(analytics)
                .build();
        return ResponseEntity.ok(response);
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        return scheme + "://" + serverName + ":" + serverPort + contextPath;
    }
} 