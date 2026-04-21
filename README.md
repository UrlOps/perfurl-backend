# [ PerfUrl ] 대용량 로그 수집 기반 단축 URL 리디렉션 서비스

> **핵심 가치**
> * URL 단축 도메인을 활용해 상시 500 TPS 대용량 트래픽 환경을 가정하고 로컬 캐싱 및 비동기 처리 성능 튜닝 효과를 데이터로 검증한 프로젝트

> **핵심 성과 요약**
> * **리디렉션 성능 최적화:** 로컬 캐시(Ehcache) 및 비동기(@Async) 로깅 도입으로 P(95) 응답 시간 1.53초에서 8.9ms로 단축 및 시스템 에러율 0% 달성
> * **인프라 자원 효율화:** t3.medium 급 제약 환경(Docker 2vCPU) 모사 테스트에서 애플리케이션 CPU 점유율 219%에서 58%로 안정화
> * **통계 조회 최적화:** 카디널리티 기반 복합 인덱스 적용으로 통계 쿼리 조회 속도 0.218초에서 0.003초로 단축

<br><br>

## 1. 프로젝트 소개

**[ PerfUrl ]** 은 긴 URL을 단축 URL로 변환하고 접속 통계를 수집하는 웹 서비스입니다 

단순 기능 구현을 넘어 백엔드 핵심 개념인 인덱스 설계, 로컬 캐시 전략, 스레드 분리가 고부하 상황에서 실제 애플리케이션 가용성과 응답 속도에 미치는 영향을 k6와 Docker Stats로 정량 측정하고 분석하는 데 집중했습니다

### 주요 기능

* **Core:** Base62 알고리즘 기반 URL 단축 및 리디렉션 처리
* **Analytics:** 접속 IP와 User-Agent 기반 상세 클릭 통계 비동기 수집
* **Admin:** JWT 인증 기반 관리자 대시보드 통계 제공

<br><br>

## 2. 기술 스택

| Category | Technology | Reason for Selection |
| --- | --- | --- |
| **Language** | <img src="https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"> | LTS 버전의 안정적인 생태계 활용 및 Record 패턴을 통한 코드 간결성 확보 |
| **Framework** | <img src="https://img.shields.io/badge/Spring_Boot_3.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white"> | 내장 서버를 통한 신속한 환경 구성 및 의존성 관리 최적화 |
| **Database** | <img src="https://img.shields.io/badge/MySQL_8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> <img src="https://img.shields.io/badge/Ehcache-005571?style=for-the-badge&logo=java&logoColor=white"> | 대량 로그 데이터 적재를 위한 인덱싱 최적화 및 로컬 캐싱을 통한 I/O 병목 제거 |
| **ORM** | <img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/QueryDSL-007ACC?style=for-the-badge&logo=java&logoColor=white"> | 객체 지향적 설계 생산성 확보 및 통계 쿼리 최적화를 위한 타입 안정성 보장 |
| **Infra / Test** | <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/k6-7D64FF?style=for-the-badge&logo=k6&logoColor=white"> | Docker 리소스 제한을 통한 물리적 인프라 모사 및 k6 부하 인가 테스트 수행 |

<br><br>

## 3. 성능 고도화 및 트러블슈팅

> 가설, 검증, 분석 프로세스에 따라 시스템의 물리적 임계점을 식별하고 정량적 지표를 바탕으로 최적화 효과를 증명했습니다

### [ Phase 1 ] 로컬 캐시와 비동기 로깅을 통한 리디렉션 병목 해소

**Q. 트래픽 집중 시 발생하는 DB I/O 병목을 제약된 자원 내에서 어떻게 해결할 것인가?**

* **문제 상황:** 특정 인기 URL에 요청이 편중될 때 원본 URL 조회와 통계 로그 적재 로직이 단일 트랜잭션에 강결합되어 디스크 I/O 지연 발생. 500 TPS 부하 인가 시 서버 대기열 누적으로 P(95) 지연 시간 1.53초 및 에러율 99.99% 도달
* **해결 과정:**
  1. **로컬 캐시 도입 (Ehcache):** 핫 키 조회를 DB가 아닌 JVM 힙 메모리에서 처리하도록 Look-aside 패턴을 적용해 DB Read I/O 차단
  2. **비동기 로깅 파이프라인 (@Async):** 통계 로그 저장을 별도 워커 스레드로 위임하여 메인 스레드가 즉시 리디렉션 응답을 반환하도록 I/O 격리
* **검증 결과 (Docker 2vCPU / 4GB 모사 환경):**
  * P(95) 응답 시간 1.53초에서 8.92ms로 단축 및 타임아웃 에러율 0%로 안정화
  * DB I/O 대기 소멸로 애플리케이션 CPU 점유율 219%에서 58%로 감소
 
    | **측정 지표** | **AS-IS (Sync + No Cache)** | **TO-BE (@Async + Ehcache)** | **개선 효과 및 의미** |
    | --- | --- | --- | --- |
    | **P(95) Latency** | 1.53s | 8.92ms | **약 99% 단축** (즉각적인 리디렉션 보장) |
    | **Error Rate** | 99.99% | 0.00% | **시스템 마비 완벽 해결** (가용성 100%) |
    | **평균 처리량 (TPS)** | 병목으로 인한 측정 불가 | 437 TPS | 500 TPS에 근접한 안정적인 트래픽 소화 |
    | **App CPU 사용량** | 219% (Overload) | 58% (Stable) | **약 73% 감소** (여유 자원 확보) |
    | **DB CPU 사용량** | 85% | 38% | **약 55% 감소** (DB 부하 경감) |

> <details>
> <summary><strong>[ 증빙 자료 ] k6 리디렉션 부하 테스트 지표 및 Docker Stats 비교</strong></summary>
> <div markdown="1">
> <br>
>
> **[ AS-IS ] Sync + No Cache: 트랜잭션 강결합 및 시스템 마비**
>
> <br>
> <img width="1276" height="862" alt="image" src="https://github.com/user-attachments/assets/2d197f32-2453-4570-ad5b-98273e4fba7f" />
>
> <br>
> <img width="730" height="88" alt="image" src="https://github.com/user-attachments/assets/5fd6a49e-b3c1-44f7-8735-afda19b005fb" />
>
> <br>
>
> **[ TO-BE ] Async + Ehcache: DB I/O 격리 및 가용성 확보**
>
> <br>
> <img width="1252" height="954" alt="image" src="https://github.com/user-attachments/assets/894faa45-74b9-4afc-b68f-9ef68f676dea" />
>
> <br>
> <img width="724" height="92" alt="image" src="https://github.com/user-attachments/assets/3393c777-998f-4f62-b027-dc01faca2bb4" />
>
> </div>
> </details>

<br>

### [ Phase 2 ] 복합 인덱스 설계를 통한 통계 조회 성능 최적화

**Q. 대용량 로그 데이터 조회 시 발생하는 Full Table Scan 지연을 어떻게 해결할 것인가?**

* **문제 상황:** 50만 건의 데이터가 적재된 환경에서 IP와 날짜 기반 다중 조건 필터링 시 쿼리 응답에 0.218초 소요
    ```sql
    EXPLAIN SELECT * FROM click_log
    WHERE ip_address = '192.168.0.50' AND created_at BETWEEN '2025-05-10' AND '2025-05-17';
    ```
    
    <img width="80%" height="100" alt="image" src="https://github.com/user-attachments/assets/234dda02-aedb-4d4c-9fec-762d125528ad" /> <br>

<br>

* **해결 과정:** 카디널리티가 높은 `ip_address`와 조회 범위 지정이 필요한 `created_at` 컬럼을 결합한 복합 인덱스 설계 및 적용
  ```sql
  CREATE INDEX idx_ip_created ON click_log (ip_address, created_at);
  ```
  <img width="80%" height="100" alt="image" src="https://github.com/user-attachments/assets/5ff3bed5-9a74-437d-ad70-2fe8fecfd8f8" />

    <br>

    - MySQL Profiling 결과:
        - **0.21887s → 0.003725s {98}% 개선**
           <img width="373" height="352" alt="image" src="https://github.com/user-attachments/assets/101231e1-fde9-4a61-8014-3c56a5a024cc" />
       
* **검증 결과:**
  * 쿼리 실행 시간 0.218초에서 0.003초로 단축
  * k6 부하 테스트 결과 통계 API 평균 처리량이 최적화 전 대비 유의미하게 향상됨을 확인

<br><br>

## 4. 설계 회고 및 인사이트

### 1. 글로벌 캐시(Redis) 대신 로컬 캐시(Ehcache)를 선택한 트레이드오프
현재 아키텍처는 단일 서버 구조입니다. 2vCPU라는 자원 제약 환경에서는 외부 저장소와 통신하는 네트워크 I/O 통신 비용조차 줄이는 것이 리디렉션 응답 속도 최적화(8.92ms)에 유리하다고 판단해 JVM 내부 메모리를 사용하는 Ehcache를 선택했습니다. 향후 서버 Scale-out 시 데이터 정합성을 확보하기 위해 Redis 마이그레이션이 필요함을 인지하고 있습니다.

### 2. 비동기 로깅(@Async)의 데이터 유실 리스크 관리
시스템 셧다운이나 큐 포화 시 로그 유실 위험이 존재합니다. 하지만 단축 URL 서비스의 핵심 가치는 사용자의 빠른 목적지 이동이므로 소수의 통계 누락보다 응답 지연으로 인한 서비스 마비가 비즈니스에 더 치명적이라 판단했습니다. 이를 보완하기 위해 독립적인 트랜잭션 격리 계층을 구축했으며 향후 고도화 시 Kafka 등 메시지 브로커를 도입하여 내결함성을 강화할 예정입니다.

### 3. 고부하 환경의 DB 커넥션 가용성 확보 (OSIV 비활성화)
API 응답 시점까지 DB 커넥션을 점유하는 OSIV 특성으로 인해 트래픽 급증 시 커넥션 풀 고갈 리스크가 있음을 확인했습니다. 이에 `spring.jpa.open-in-view`를 false로 설정하여 커넥션 반환 시점을 앞당겼으며 영속성 컨텍스트 종료로 인한 `LazyInitializationException`은 Service 계층에서 명시적인 DTO 변환을 수행하여 사전 방어했습니다.

<br><br>

## 5. 프로젝트 실행

*(※ AWS 배포는 인프라 자원 최적화를 위해 중단 상태이며 로컬 환경에서 실행 가능합니다)*

```bash
# Clone Repository
git clone [https://github.com/UrlOps/perfurl-backend.git](https://github.com/UrlOps/perfurl-backend.git)
```

<br><br>

<details><summary><h2> 7. 프로젝트 주요 화면 </h2></summary>
<div markdown="1">

<br>

### 메인 화면

<img width="2832" height="1394" alt="image" src="https://github.com/user-attachments/assets/adfc0574-c17c-43e2-a1ea-71b1c37a76d1" />
<br>
<br>
<img width="2816" height="1512" alt="image" src="https://github.com/user-attachments/assets/02efeb35-32ca-4d7d-a752-19922c71e256" />
<br>
<br>
<img width="2812" height="1464" alt="image" src="https://github.com/user-attachments/assets/c4d2eb12-7fe6-4a7e-aa9d-18769d6f8ef9" />

<br>

### 관리자 로그인 화면
<img width="2820" height="1386" alt="image" src="https://github.com/user-attachments/assets/540d0dc2-e8fa-43f6-b045-ecd0c6e288e6" />
<br><br>

### 백오피스 화면
<img width="2876" height="1404" alt="image" src="https://github.com/user-attachments/assets/1e330cad-6167-4cdc-b977-e1ef59d28e77" />
</div>
</details>
