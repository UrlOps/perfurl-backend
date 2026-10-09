package be.url_backend.feature.url.repository;

import be.url_backend.feature.url.UrlMapping;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long>, UrlMappingRepositoryCustom {
    // @Cacheable(value = "url-mapping-cache", key = "#shortKey", unless = "#result == null")
    Optional<UrlMapping> findByShortKey(String shortKey);
}
