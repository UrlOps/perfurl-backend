# [ PerfUrl ] 대용량 로그 수집 기반 단축 URL 리디렉션 서비스

> **핵심 가치**
> * AWS t3.medium 단일 Pod 제약 환경 모사 ➔ 소프트웨어 아키텍처 튜닝 기반 물리적 임계점 계측 및 시스템 가용성 방어
> * 상시 500 TPS 대규모 마케팅 유입 부하 ➔ 로컬 캐싱과 비동기 파이프라인 구축으로 P50 응답 속도 3.47초에서 106ms로 단축 및 부하 누락 95.9% 방어

> **핵심 성과 요약**
> * 단축 URL 리디렉션 및 RDBMS I/O 병목 ➔ JVM 힙 기반 Ehcache 로컬 캐싱 및 비동기 파이프라인 구축으로 P50 응답 속도 3.47초에서 106ms로 96.9% 단축 및 P95 지연 6.23초에서 1.72초로 통제
> * 로그 적재 쓰기 작업 강결합 리스크 ➔ 비동기 워커 스레드 풀 분리 및 DiscardPolicy 격리로 평균 처리량 177.9에서 293.5 TPS 확보 및 부하 누락 43,519건에서 1,764건으로 95.9% 방어
> * 50만 건 이상 다중 조건 필터링 부하 ➔ 카디널리티 기반 복합 인덱스 설계로 쿼리 실행 속도 218ms에서 3ms로 98% 단축
> * 시계열 로그 누적에 따른 인덱스 비대화 ➔ 만료 로그 대상 벌크 Hard Delete 정기 Purge 배치 구축으로 디스크 I/O 최적화 및 버퍼 풀 효율 확보

<br><br>

## 1. 프로젝트 소개

**[ PerfUrl ]** 긴 URL의 단축 URL 변환 및 사용자 접속 통계 수집 웹 서비스.
대규모 트래픽 집중에 따른 물리적 임계점 및 성능 병목(DB I/O, 스레드 마비, 메모리 포화) 극복 중심 아키텍처 튜닝 및 성능 최적화 프로젝트.

### 주요 도메인 기능

* 단축 URL 리디렉션 피크 트래픽 집중과 RDBMS I/O 병목 ➔ JVM 힙 기반 Ehcache 로컬 캐싱 구축으로 응답 지연 방어
* 로그 적재 쓰기 작업 강결합에 따른 활성 스레드 풀 고갈 리스크 ➔ 비동기 워커 스레드 분리 및 DiscardPolicy 기반 장애 격리 통제
* 다중 조건 필터링 시 Full Table Scan 병목 ➔ 카디널리티 기반 복합 인덱스 설계로 쿼리 실행 속도 단축
* 시계열 로그 누적에 따른 인덱스 비대화 리스크 ➔ 벌크 Hard Delete 정기 Purge 배치 구축으로 버퍼 풀 효율 확보
* 고유 시퀀스 ID 기반 인코딩 리스크 ➔ Base62 진법 알고리즘 적용 O(1) 단축 키 생성 및 원본 URL 리디렉션 기능 구축
* 아키텍처 의사결정 지연 리스크 ➔ k6 부하 테스트 및 Scouter APM 계측 통합 리포트 구축으로 가시성 확보

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

<img width="1007" height="1562" alt="image" src="https://github.com/user-attachments/assets/b8c079c4-431f-4a68-998a-c9ccd62f2f43" />

<br><br>

## 3. 기술 스택

| Category | Technology | Reason for Selection |
| --- | --- | --- |
| **Language** | <img src="https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"> | Virtual Threads 및 ZGC 환경 고부하 I/O 블로킹 최소화 및 힙 메모리 통제 |
| **Framework** | <img src="https://img.shields.io/badge/Spring_Boot_3.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white"> | 내장 서버 기반 신속한 환경 구성 및 의존성 관리 통제 |
| **Database** | <img src="https://img.shields.io/badge/MySQL_8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> | InnoDB 무결성 보장과 통계 분석 복합 인덱스 스캔 성능 확보 |
| **Cache & Async** | <img src="https://img.shields.io/badge/Ehcache_3-005571?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/Spring_Async-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> | JVM 힙 기반 초고속 로컬 캐싱 및 DiscardPolicy 기반 비동기 격리 통제 |
| **Infra & CI/CD** | <img src="https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white"> <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white"> | 단일 노드 격리 배포 자원 제약 모사 기반 아키텍처 임계점 계측 및 무중단 배포 확보 |
| **Test & Monitor** | <img src="https://img.shields.io/badge/k6-7D64FF?style=for-the-badge&logo=k6&logoColor=white"> <img src="https://img.shields.io/badge/Scouter_APM-FF9900?style=for-the-badge&logo=java&logoColor=white"> | 피크 600 TPS 부하 인가 및 APM 스레드 메트릭 힙 메모리 삼각 계측 통제 |

<br><br>

## 4. 핵심 엔지니어링 최적화 딥다이브

> 하드웨어 증설 없는 소프트웨어 아키텍처 튜닝 기반 물리적 병목 최적화 방어 프로세스

---

### [ Deep-Dive 1 ] 대규모 리디렉션 트래픽 및 RDBMS I/O 병목 방어

<img width="1112" height="1062" alt="image" src="https://github.com/user-attachments/assets/ccfab0eb-5eb3-4847-b2db-6d34de0bfa59" />

* **문제 원인**
    * 단축 URL 리디렉션 피크 트래픽 집중과 RDBMS I/O 병목 ➔ 단일 쿼리 매핑 지연 및 P95 지연 6.23초 병목 현상 식별
    * 로그 적재 쓰기 작업 강결합 ➔ Tomcat 활성 워커 스레드 200 수준 대기 풀 포화 및 메인 스레드 블로킹 식별
    * 요청 정체에 따른 힙 메모리 1,000MB 포화 ➔ 가비지 컬렉터 최장 4.5초 STW 지연 및 43,519건 부하 누락 오류 임계점 식별
* **해결 과정**
    * 핫키 매핑 쿼리 실행 지연 리스크 ➔ JVM 힙 기반 Ehcache 로컬 캐싱 적용으로 매핑 쿼리 시간 300ms에서 3ms 이하로 오프로딩 방어
    * 로그 적재 정체 메인 스레드 전이 리스크 ➔ 클릭 로그 쓰기 작업 비동기 워커 스레드 분리 및 이벤트 위임 아키텍처로 스레드 블로킹 방어
    * 워커 큐 포화에 따른 연쇄 마비 리스크 ➔ 큐 포화 시 CallerRuns 강제 실행 배제 및 DiscardPolicy 기반 의도적 로그 유실(Drop) 채택으로 메인 리디렉션 서비스 가용성 확보
* **정량적 실측 성과**
    * 메인 스레드 I/O 블로킹 차단으로 P50 응답 속도 3.47초에서 106ms로 96.9% 단축 및 P95 지연 6.23초에서 1.72초로 통제
    * 비동기 워커 격리 기반 트랜잭션 동기 처리 병목 해소로 평균 처리량 177.9에서 293.5 TPS 확보 및 부하 누락 43,519건에서 1,764건으로 95.9% 방어
    * 메모리 사용량 톱니바퀴 패턴 제어로 단일 인스턴스 기준 동일 시간 내 106,234건 트랜잭션 정상 수용 및 인프라 가동률 확보

<br>

### [ Deep-Dive 2 ] 복합 인덱스 설계를 통한 통계 조회 병목 최적화

* **문제 원인**
    * 50만 건 이상 접속 로그 다중 조건(날짜 및 IP) 필터링 시 Full Table Scan 병목 ➔ 대시보드 쿼리 실행 속도 218ms 지연 현상 식별
    * 인덱스 부재에 따른 RDBMS 쿼리 스캔 부하 ➔ EXPLAIN 분석 기반 대량의 디스크 I/O 탐색 오버헤드 식별
* **해결 과정**
    * 다중 조건 필터링 부하 리스크 ➔ 카디널리티 기반 복합 인덱스 설계 및 조건 순서 최적화 적용으로 인덱스 스캔 구조 튜닝
    * 디스크 레벨 데이터 패치 오버헤드 ➔ 인덱스 컬럼 내 조회 조건 전면 포함 구성으로 커버링 스캔 오프로딩 통제
* **정량적 실측 성과**
    * 카디널리티 기반 복합 인덱스 스캔 전환으로 다중 조건 쿼리 실행 속도 218ms에서 3ms로 98% 단축 확보

<br>

### [ Deep-Dive 3 ] Hard Delete 기반 시계열 로그 인덱스 최적화

* **문제 원인**
    * 단순 삭제 플래그(is_deleted) 갱신 기반 Soft Delete 정책 적용 ➔ 시계열 로그 누적에 따른 인덱스 트리 비대화 현상 식별
    * 만료 데이터 잔존에 따른 InnoDB 버퍼 풀 오염 ➔ 핫데이터 캐시 히트율 하락 및 단순 리디렉션 조회 쿼리 I/O 지연 전이 식별
* **해결 과정**
    * 인덱스 비대화 및 버퍼 풀 오염 리스크 ➔ 보존 가치가 소멸된 통계용 로그 대상 Soft Delete 배제 및 영구 삭제 정책(Hard Delete) 전환
    * 실시간 삭제 트랜잭션 부하 리스크 ➔ 애플리케이션 부하 최저 시간대 만료 로그 대상 벌크 Hard Delete 정기 Purge 배치 파이프라인 구축
* **정량적 실측 성과**
    * 정기 Purge 배치 구축으로 불필요한 인덱스 블록 팽창 방어 및 버퍼 풀 히트율 확보 기반 RDBMS I/O 병목 통제

<br><br>

## 5. 트러블 슈팅 및 설계 회고

### 1. Redis 글로벌 캐시 대신 Ehcache 로컬 캐시 선택
분산 캐시 외부 통신에 따른 TCP 네트워크 RTT 지연 리스크 ➔ 단일 Pod 인프라 제약 고려 네트워크 비용 배제 및 JVM 힙 메모리 직접 조회 기반 Ehcache 로컬 캐싱 도입으로 나노초 레벨 오프로딩 통제

### 2. 비동기 워커 큐 포화 시 DiscardPolicy 선택
대용량 트래픽 유입에 따른 비동기 큐 포화 리스크 ➔ CallerRunsPolicy 적용 시 리디렉션 메인 스레드 I/O 블로킹 마비 식별 및 리디렉션 성공 최우선 목적 기반 DiscardPolicy 의도적 유실 수용으로 연쇄 장애 방어

### 3. 고부하 환경 OSIV 비활성화 기반 DB 커넥션 고갈 방어
트래픽 급증 시 View 렌더링 응답 시점 커넥션 장기 점유 리스크 ➔ OSIV 비활성화 튜닝으로 트랜잭션 종료 즉시 DB 커넥션 HikariCP 반환 처리 및 Service 계층 DTO 변환으로 커넥션 고갈 락 경합 방어

<br><br>

## 6. ERD 데이터베이스 모델링

<img width="422" height="430" alt="image" src="https://github.com/user-attachments/assets/94d13b20-6011-4fc7-bc04-3a1211c6c4ea" />

<br><br>

## 7. 인프라 운영 및 CI/CD 파이프라인

* 클라우드 인프라 자원 제약 리스크 ➔ AWS EC2 인스턴스 기반 물리적 제약 서버 모사 환경 및 프로덕션 부하 계측 환경 확보
* 도메인 네임 시스템 관리 리스크 ➔ AWS Route 53 연동 기반 단축 URL 접근성 및 트래픽 라우팅 통제
* 배포 환경 불일치에 따른 장애 리스크 ➔ Docker 엔진 기반 런타임 환경 일관성 및 컨테이너 물리 자원 격리 확보
* 수동 배포 휴먼 에러 리스크 ➔ GitHub Actions CI/CD 파이프라인 구축으로 자동 빌드 및 무중단 배포 리드타임 단축
