# [ PerfUrl ] 대용량 로그 수집 기반 단축 URL 리디렉션 서비스

> **핵심 가치**
> 
> - 인프라 제약 t3.medium 모사 환경 부하 리스크 ➔ 소프트웨어 아키텍처 튜닝 기반 물리적 임계점 계측 및 시스템 가용성 방어
> - 피크 600 TPS 트래픽 집중에 따른 응답 지연 리스크 ➔ 로컬 캐싱 및 비동기 파이프라인 구축으로 P50 응답 속도 3.47초에서 106ms 단축 및 부하 누락 95.9% 방어

> **핵심 성과 요약**
> 
> - 매핑 쿼리 지연에 따른 리디렉션 병목 리스크 ➔ Ehcache 로컬 캐시 적용으로 RDBMS 매핑 쿼리 지연 300ms에서 3ms 이하 오프로딩 및 P95 응답 지연 6.23초에서 1.72초 단축
> - 로그 적재 강결합에 따른 메인 스레드 마비 리스크 ➔ 비동기 워커 스레드 풀 격리 및 DiscardPolicy 적용으로 총 처리량 64.9% 확보 및 177.9에서 293.5 TPS 통제
> - 지연 객체 누적에 따른 ZGC STW 스파이크 리스크 ➔ 힙 메모리 톱니바퀴 패턴 제어로 단일 인스턴스 20.7만 건 트랜잭션 수용 및 메모리 포화 방어
> - 다중 조건 필터링에 따른 Full Table Scan 부하 리스크 ➔ 카디널리티 기반 복합 인덱스 적용으로 조회 시간 0.218초에서 0.003초 98% 단축

<br><br>

## 1. 프로젝트 소개

**[ PerfUrl ]** 긴 URL 단축 URL 변환 및 사용자 접속 통계 수집 웹 서비스

대규모 트래픽 집중에 따른 데이터베이스 I/O 병목 톰캣 활성 스레드 마비 JVM 메모리 포화 물리적 임계점 k6 Scouter APM 정량적 계측 및 소프트웨어 아키텍처 튜닝 기반 엔지니어링 성능 최적화 프로젝트

### 주요 도메인 기능

- 단축 URL 전환에 따른 응답 지연 리스크 ➔ Base62 인코딩 기반 단축 URL 생성 및 302 리디렉션 처리
- 핫키 조회 집중에 따른 부하 리스크 ➔ Ehcache 로컬 캐싱 도입으로 RDBMS 조회 부하 100% 오프로딩 통제
- 메인 스레드 결합에 따른 로그 지연 리스크 ➔ 접속 IP User-Agent 기반 상세 클릭 통계 비동기 워커 격리 수집 통제
- Soft Delete 방치에 따른 인덱스 비대화 리스크 ➔ Hard Delete Purge 배치 파이프라인 구축으로 디스크 버퍼 효율 확보
- 인가 탈취에 따른 관리자 권한 리스크 ➔ JWT 인증 기반 관리자 백오피스 구축 및 보안 통제
- 다중 조건 조회에 따른 통계 지연 리스크 ➔ 복합 인덱스 활용 통계 대시보드 구축으로 응답 속도 단축

### 디렉토리 구조 Feature-driven Architecture

```text
src/main/java/be/url_backend
├── common                      # 전역 공통 인프라 횡단 관심사 통제
│   ├── config                  # Cache Async Security 인프라 설정
│   ├── dto                     # 공통 응답 규격 계층 격리
│   ├── exception               # GlobalExceptionHandler 표준 에러 통제
│   ├── security                # JWT 필터 Stateless 인증 인가 방어
│   └── util                    # Base62Utils JwtUtil 유틸리티 추상화
└── feature                     # 도메인 주도 패키지 비즈니스 로직 응집
    ├── url                     # URL 단축 리디렉션 핵심 로직 분리
    ├── log                     # 비동기 클릭 로그 수집 Purge 전략 캡슐화
    ├── stats                   # 클릭 로그 기반 통계 집계 격리
    └── admin                   # 관리자 인증 대시보드 도메인 분리
```

<br><br>

## 2. 시스템 전체 아키텍처

<img width="1007" height="1562" alt="image" src="https://github.com/user-attachments/assets/6bddca49-8716-4377-9629-02559c9bea7b" />

<br><br>

## 3. 기술 스택

| Category | Technology | Reason for Selection |
| --- | --- | --- |
| **Language** | <img src="https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"> | LTS 버전 안정적 생태계 활용 및 Record 패턴 도입으로 코드 간결성 확보 |
| **Framework** | <img src="https://img.shields.io/badge/Spring_Boot_3.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white"> | 내장 서버 기반 신속한 환경 구성 및 의존성 관리 통제 |
| **Database** | <img src="https://img.shields.io/badge/MySQL_8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> <img src="https://img.shields.io/badge/Ehcache-005571?style=for-the-badge&logo=java&logoColor=white"> | 대량 로그 데이터 적재 목적 인덱싱 튜닝 및 로컬 캐싱 도입으로 I/O 병목 방어 |
| **ORM** | <img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/QueryDSL-007ACC?style=for-the-badge&logo=java&logoColor=white"> | 객체 지향적 설계 생산성 증대 및 통계 쿼리 최적화 기반 타입 안정성 통제 |
| **Infra / Test** | <img src="https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white"> <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white"> <img src="https://img.shields.io/badge/k6-7D64FF?style=for-the-badge&logo=k6&logoColor=white"> | 런타임 환경 일관성 유지 및 CI/CD 파이프라인 구축으로 배포 자동화 통제 <br> Docker 리소스 제한 기반 물리적 인프라 모사 및 k6 부하 성능 테스트 계측 확보 |

<br><br>

## 4. 핵심 엔지니어링 최적화 딥다이브

> 하드웨어 증설 없는 소프트웨어 아키텍처 튜닝 기반 물리적 병목 최적화 방어 프로세스

<br>

### [ Deep-Dive 1 ] 리디렉션 트래픽 집중 및 RDBMS I/O 병목 최적화

**Q. 트래픽 집중에 따른 DB I/O 병목 및 활성 스레드 마비 리스크 방어 전략**

* **문제 상황 AS-IS**
  * 핫키 트래픽 편중에 따른 매핑 쿼리 로그 적재 강결합 리스크 ➔ SQL Time 300ms 수직 상승 식별
  * 매핑 지연에 따른 톰캣 워커 스레드 대기 리스크 ➔ Active Service Count 200 포화 및 스레드 풀 정체 식별
  * 응답 지연 객체 누적에 따른 힙 포화 리스크 ➔ ZGC STW 4.5초 스파이크 및 43,519건 부하 누락 식별

    <br>
      <img width="872" height="1022" alt="image" src="https://github.com/user-attachments/assets/15bb8e8c-b391-496c-b360-f1940a9458a9" />
    <br>

* **해결 전략 및 아키텍처**
  * Ehcache 로컬 캐싱 적용 ➔ RDBMS 핫키 매핑 조회 부하 100% 오프로딩 및 JVM 힙 메모리 직접 조회 통제
  * 비동기 워커 스레드 풀 격리 ➔ 메인 스레드 대기 방어 및 로그 적재 I/O 블로킹 통제
  * DiscardPolicy 적용 ➔ 비동기 큐 포화에 따른 서비스 정체 리스크 시 로그 거부 기반 리디렉션 가용성 확보
  * Hard Delete Purge 배치 ➔ Soft Delete 배제로 인덱스 블록 비대화 방어 및 만료 로그 파기 기반 디스크 버퍼 효율 확보

* **정량적 실측 성과 피크 600 TPS 부하 계측**
  * 총 처리량 ➔ 단일 트랜잭션 동기 처리 병목 해제 및 비동기 워커 격리로 TPS 177.9에서 293.5 피크 550 확보
  * P50 응답 지연 ➔ 메인 스레드 I/O 차단으로 P50 Latency 3.47초에서 106ms 단축 통제
  * P95 응답 지연 ➔ 큐 적재 지연 오프로딩으로 P95 Latency 6.23초에서 1.72초 방어
  * 부하 누락 ➔ 톰캣 활성 스레드 마비 방어 및 비동기 워커 튜닝으로 누락 43,519건에서 1,764건 95.9% 방어

<br>

---

### [ Deep-Dive 2 ] 복합 인덱스 설계를 통한 통계 조회 성능 최적화

**Q. 대용량 로그 데이터 조회에 따른 Full Table Scan 지연 방어 전략**

* **문제 상황 AS-IS**
  * 50만 건 이상 데이터 적재 환경에 따른 IP 날짜 다중 조건 필터링 쿼리 지연 리스크 ➔ 응답 지연 0.218초 소요 식별
  * 복합 인덱스 부재에 따른 RDBMS 쿼리 스캔 부하 리스크 ➔ EXPLAIN ANALYZE 분석 기반 Full Table Scan 발생 식별

* **해결 전략 및 아키텍처**
  * 카디널리티 기반 복합 인덱스 설계 ➔ IP 조회 범위 지정 및 날짜 컬럼 결합으로 다중 조건 쿼리 스캔 부하 튜닝

    ```sql
    CREATE INDEX idx_ip_created ON click_log ip_address created_at;
    ```

* **정량적 실측 성과 쿼리 성능 계측**
  * SQL 처리 시간 ➔ 복합 인덱스 적용 전후 실행 계획 검증 및 0.21887초에서 0.00372초 98% 단축 확보

<br><br>

## 5. 트러블 슈팅 및 설계 회고

### 1. Redis 글로벌 캐시 대신 Ehcache 로컬 캐시 선택
* 외부 인프라 통신에 따른 네트워크 I/O 병목 리스크 ➔ 2vCPU 자원 제약 고려 Redis 네트워크 RTT 지연 배제 및 JVM 힙 메모리 직접 조회 기반 Ehcache 로컬 캐싱 도입으로 0ms 속도 통제

### 2. 비동기 워커 큐 포화 시 DiscardPolicy 선택
* 대용량 트래픽 유입에 따른 비동기 큐 포화 리스크 ➔ CallerRunsPolicy 적용 시 리디렉션 스레드 I/O 블로킹 리스크 식별 및 빠른 리디렉션 목적 기반 DiscardPolicy 적용으로 연쇄 장애 방어

### 3. Soft Delete 방치 경계 및 Hard Delete Purge 전략
* 일률적 Soft Delete 적용에 따른 인덱스 비대화 및 버퍼 풀 오염 리스크 ➔ 수명 주기 만료 로그 대상 Hard Delete Purge 배치 파이프라인 구축으로 RDBMS 인덱스 스캔 블록 통제 및 디스크 버퍼 효율 확보

### 4. 고부하 환경 OSIV 비활성화 DB 커넥션 고갈 방어
* 트래픽 급증에 따른 View 렌더링 응답 시점 커넥션 점유 리스크 ➔ OSIV 비활성화 튜닝으로 트랜잭션 종료 즉시 DB 커넥션 HikariCP 반환 처리 및 Service 계층 DTO 변환으로 커넥션 고갈 사전 방어

<br><br>

## 6. ERD 데이터베이스 모델링

<img width="422" height="430" alt="image" src="https://github.com/user-attachments/assets/94d13b20-6011-4fc7-bc04-3a1211c6c4ea" />

<br><br>

## 7. 인프라 운영 및 CI/CD 파이프라인

* 클라우드 인프라 자원 제약 리스크 ➔ AWS 프리티어 EC2 인스턴스 구축으로 물리적 서버 환경 확보
* 도메인 네임 시스템 관리 리스크 ➔ AWS Route 53 연동으로 단축 URL 접근성 및 트래픽 라우팅 통제
* 배포 환경 불일치에 따른 장애 리스크 ➔ Docker Docker Compose 도입으로 런타임 환경 일관성 및 컨테이너 격리 확보
* 휴먼 에러 리스크 ➔ GitHub Actions 기반 CI/CD 파이프라인 구축으로 빌드 배포 자동화 통제
