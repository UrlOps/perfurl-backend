package be.url_backend.feature.stats.repository;

import be.url_backend.feature.stats.DailyStatsDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static be.url_backend.feature.stats.QDailyStats.dailyStats;
import static be.url_backend.feature.url.QUrlMapping.urlMapping;

@Repository
@RequiredArgsConstructor
public class DailyStateRepositoryCustomImpl implements DailyStateRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<DailyStatsDto> searchDailyStats(Pageable pageable, LocalDate startDate, LocalDate endDate) {
        return searchDailyStats(pageable, null, startDate, endDate);
    }

    @Override
    public Page<DailyStatsDto> searchDailyStats(Pageable pageable, String shortKey, LocalDate startDate, LocalDate endDate) {
        List<DailyStatsDto> content = queryFactory
                .select(Projections.constructor(DailyStatsDto.class,
                        dailyStats.id,
                        urlMapping.shortKey,
                        urlMapping.originalUrl,
                        dailyStats.date,
                        dailyStats.clickCount
                ))
                .from(dailyStats)
                .join(dailyStats.urlMapping, urlMapping)
                .where(
                        shortKeyEq(shortKey),
                        dateGoe(startDate),
                        dateLoe(endDate)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(dailyStats.date.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(dailyStats.count())
                .from(dailyStats)
                .join(dailyStats.urlMapping, urlMapping)
                .where(
                        shortKeyEq(shortKey),
                        dateGoe(startDate),
                        dateLoe(endDate)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression shortKeyEq(String shortKey) {
        return (shortKey != null && !shortKey.isBlank()) ? urlMapping.shortKey.eq(shortKey) : null;
    }

    private BooleanExpression dateGoe(LocalDate startDate) {
        return startDate != null ? dailyStats.date.goe(startDate) : null;
    }

    private BooleanExpression dateLoe(LocalDate endDate) {
        return endDate != null ? dailyStats.date.loe(endDate) : null;
    }
}
