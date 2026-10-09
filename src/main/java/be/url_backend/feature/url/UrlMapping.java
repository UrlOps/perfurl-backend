package be.url_backend.feature.url;

import be.url_backend.common.entity.BaseTimeEntity;
import be.url_backend.feature.log.ClickLog;
import be.url_backend.feature.stats.DailyStats;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "url_mappings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UrlMapping extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_key", nullable = false, unique = true)
    private String shortKey;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @OneToMany(mappedBy = "urlMapping", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClickLog> clickLogs = new ArrayList<>();

    @OneToMany(mappedBy = "urlMapping", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyStats> dailyStats = new ArrayList<>();

    public static UrlMapping createUrlMapping(String originalUrl) {
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.originalUrl = originalUrl;
        urlMapping.expiresAt = LocalDateTime.now().plusYears(1); // 기본 만료 기간 1년
        return urlMapping;
    }

    public void updateShortKey(String shortKey) {
        this.shortKey = shortKey;
    }
}
