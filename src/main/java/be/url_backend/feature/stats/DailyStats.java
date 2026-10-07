package be.url_backend.feature.stats;

import be.url_backend.feature.url.UrlMapping;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "url_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id", nullable = false)
    private UrlMapping urlMapping;

    @Column(name = "stat_date", nullable = false)
    private LocalDate date;

    @Column(name = "total_clicks", nullable = false)
    private long clickCount;

    public DailyStats(UrlMapping urlMapping, LocalDate date) {
        this.urlMapping = urlMapping;
        this.date = date;
        this.clickCount = 0;
    }

    public void incrementClickCount() {
        this.clickCount++;
    }
}
