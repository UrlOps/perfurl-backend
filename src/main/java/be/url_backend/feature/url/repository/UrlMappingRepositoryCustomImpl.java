package be.url_backend.feature.url.repository;

import be.url_backend.feature.url.dto.UrlResponseDto;
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
import java.time.LocalTime;
import java.util.List;

import static be.url_backend.feature.url.QUrlMapping.urlMapping;

@Repository
@RequiredArgsConstructor
public class UrlMappingRepositoryCustomImpl implements UrlMappingRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<UrlResponseDto> searchUrlMappings(Pageable pageable, String shortKey, String originalUrl, LocalDate startDate, LocalDate endDate) {
        List<UrlResponseDto> content = queryFactory
                .select(Projections.constructor(UrlResponseDto.class,
                        urlMapping.id,
                        urlMapping.shortKey,
                        urlMapping.originalUrl,
                        urlMapping.createdAt,
                        urlMapping.expiresAt
                ))
                .from(urlMapping)
                .where(
                        shortKeyEq(shortKey),
                        originalUrlContains(originalUrl),
                        createdAtGoe(startDate),
                        createdAtLoe(endDate)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(urlMapping.createdAt.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(urlMapping.count())
                .from(urlMapping)
                .where(
                        shortKeyEq(shortKey),
                        originalUrlContains(originalUrl),
                        createdAtGoe(startDate),
                        createdAtLoe(endDate)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression shortKeyEq(String shortKey) {
        return (shortKey != null && !shortKey.trim().isEmpty()) ? urlMapping.shortKey.eq(shortKey) : null;
    }

    private BooleanExpression originalUrlContains(String originalUrl) {
        return (originalUrl != null && !originalUrl.trim().isEmpty()) ? urlMapping.originalUrl.contains(originalUrl) : null;
    }

    private BooleanExpression createdAtGoe(LocalDate startDate) {
        return startDate != null ? urlMapping.createdAt.goe(startDate.atStartOfDay()) : null;
    }

    private BooleanExpression createdAtLoe(LocalDate endDate) {
        return endDate != null ? urlMapping.createdAt.loe(endDate.atTime(LocalTime.MAX)) : null;
    }
} 