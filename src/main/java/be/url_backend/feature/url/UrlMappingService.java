package be.url_backend.feature.url;

import be.url_backend.feature.log.ClickLogService;
import be.url_backend.common.exception.CustomException;
import be.url_backend.common.exception.ErrorCode;
import be.url_backend.common.util.Base62Utils;
import be.url_backend.feature.url.dto.UrlCreateRequestDto;
import be.url_backend.feature.url.dto.UrlResponseDto;
import be.url_backend.feature.url.repository.UrlMappingRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UrlMappingService {
    private final UrlMappingRepository urlMappingRepository;

    @Transactional
    public UrlResponseDto createShortUrl(UrlCreateRequestDto requestDto, String baseUrl) {
        String originalUrl = requestDto.getOriginalUrl();

        if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
            throw new CustomException(ErrorCode.INVALID_URL_FORMAT);
        }
        
        int shortenedUrlLength = baseUrl.length() + 1 + Base62Utils.SHORT_KEY_LENGTH;

        if (originalUrl.length() <= shortenedUrlLength) {
            throw new CustomException(ErrorCode.URL_IS_ALREADY_SHORT);
        }

        UrlMapping urlMapping = UrlMapping.createUrlMapping(requestDto.getOriginalUrl());
        // 고유 DB ID(Auto-Increment) 획득을 위해 임시 키 설정 후 엔티티 저장 및 플러시
        urlMapping.updateShortKey("TEMP_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        UrlMapping savedUrlMapping = urlMappingRepository.saveAndFlush(urlMapping);

        // 발급된 고유 ID를 기반으로 Base62 진법 인코딩 수행 (O(1) 충돌 없는 고유 단축 키 보장)
        String shortKey = Base62Utils.encodeWithPadding(savedUrlMapping.getId(), Base62Utils.SHORT_KEY_LENGTH);
        savedUrlMapping.updateShortKey(shortKey);

        return UrlResponseDto.from(savedUrlMapping, baseUrl);
    }

    @Cacheable(value = "url-mapping-cache", key = "#shortKey")
    public UrlMapping getUrlMapping(String shortKey) {
        return urlMappingRepository.findByShortKey(shortKey)
                .orElseThrow(() -> new CustomException(ErrorCode.URL_NOT_FOUND));
    }
}
