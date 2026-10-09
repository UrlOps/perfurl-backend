package be.url_backend.feature.url;

import be.url_backend.common.dto.ResponseText;
import be.url_backend.common.dto.ApiResponse;
import be.url_backend.feature.log.ClickLogService;
import be.url_backend.feature.url.dto.UrlCreateRequestDto;
import be.url_backend.feature.url.dto.UrlResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class UrlMappingController {

    private final UrlMappingService urlMappingService;
    private final ClickLogService clickLogService;

    @PostMapping("/api/urls")
    public ResponseEntity<ApiResponse<UrlResponseDto>> createShortUrl(
            @RequestBody UrlCreateRequestDto request, HttpServletRequest httpServletRequest) {
        UrlResponseDto urlResponseDto = urlMappingService.createShortUrl(request, getBaseUrl(httpServletRequest) + "/r");
        ApiResponse<UrlResponseDto> response = ApiResponse.<UrlResponseDto>builder()
                .msg(ResponseText.URL_CREATE_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.CREATED.value()))
                .data(urlResponseDto)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/r/{shortKey}")
    public ResponseEntity<Void> redirectToOriginalUrl(
            @PathVariable String shortKey, HttpServletRequest request) {
        UrlMapping urlMapping = urlMappingService.getUrlMapping(shortKey);
        clickLogService.logClickAndupdateDailyStats(urlMapping, request.getHeader("User-Agent"), request.getRemoteAddr(), request.getHeader("Referer"));
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(urlMapping.getOriginalUrl()))
                .build();
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        return scheme + "://" + serverName + ":" + serverPort + contextPath;
    }
}
