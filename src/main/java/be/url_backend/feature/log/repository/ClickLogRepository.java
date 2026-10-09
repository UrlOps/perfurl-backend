package be.url_backend.feature.log.repository;

import be.url_backend.feature.log.ClickLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface ClickLogRepository extends JpaRepository<ClickLog, Long>, ClickLogRepositoryCustom {

    // 테스트 데이터를 위한 임시 메서드
    @Query("SELECT DISTINCT c.ipAddress FROM ClickLog c")
    List<String> findDistinctIpAddresses();

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ClickLog c WHERE c.createdAt < :threshold")
    int deleteByCreatedAtBefore(@Param("threshold") java.time.LocalDateTime threshold);

} 