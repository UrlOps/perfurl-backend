package be.url_backend.feature.log;

import be.url_backend.common.entity.BaseTimeEntity;
import be.url_backend.feature.url.UrlMapping;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "click_logs", indexes = {
        @Index(name = "idx_ip_created", columnList = "ipAddress, createdAt"),
})
public class ClickLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id", nullable = false)
    private UrlMapping urlMapping;

    @Column(nullable = false)
    private String userAgent;

    @Column(nullable = false)
    private String ipAddress;
    @Column
    private String referer;


    public ClickLog(UrlMapping urlMapping, String userAgent, String ipAddress, String referer) {
        this.urlMapping = urlMapping;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
        this.referer = referer;

    }
}
