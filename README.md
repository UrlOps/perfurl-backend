# [ PerfUrl ] 데이터 기반 성능 최적화: URL 단축 서비스

> **핵심 가치**
> * URL 단축 도메인을 활용해 대용량 트래픽 환경을 가정하고 **성능 튜닝(Caching, Indexing, Async) 효과를 정량적으로 검증**한 엔지니어링 프로젝트입니다.

> **핵심 성과 요약 (Local Test Env)**
> * **조회 성능:** 복합 인덱스 적용으로 통계 조회 쿼리 속도 **0.2s → 0.003s (약 65배 개선)**
> * **응답 속도:** `Ehcache` 로컬 캐시 적용으로 리디렉션 응답 시간 **7% 단축** 및 TPS **10% 향상**
> * **처리량 증대:** 로그 저장 로직 비동기(`@Async`) 전환으로 쓰기 병목 제거 및 Peak TPS **13.4% 증가**
> 
> 

<br><br>

## 1. 프로젝트 소개

**[ PerfUrl ]** 은 긴 URL을 단축 URL로 변환하고 접속 통계를 시각화하는 웹 서비스입니다. 

기능 구현을 넘어 백엔드 필수 **CS 지식(DB 인덱스, 캐시 전략, 비동기 처리)이 실제 애플리케이션 성능에 미치는 영향을 nGrinder로 직접 측정하고 분석**하는 데 집중했습니다.

### 주요 기능

* **Core:** Base62 알고리즘 기반 URL 단축 및 리디렉션
* **Analytics:** 접속 IP, 날짜, User-Agent 기반 상세 클릭 통계 제공
* **Admin:** JWT 인증 기반 관리자 대시보드 (Chart.js 시각화)

<br><br>

## 2. 기술 스택

| Category | Technology | Reason for Selection |
| :--- | :--- | :--- |
| **Language** | <img src="https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"> | LTS 버전의 안정성을 확보하고 Record 패턴 등을 활용해 코드 간결성 향상 |
| **Framework** | <img src="https://img.shields.io/badge/Spring_Boot_3.x-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/Vue.js_3-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white"> | 풍부한 레퍼런스를 통한 개발 생산성 확보 및 관리자 대시보드 UI의 효율적 구현 |
| **Database & ORM** | <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> <br> <img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/QueryDSL-007ACC?style=for-the-badge&logo=java&logoColor=white"> | 객체 지향적인 데이터 접근과 QueryDSL을 통한 컴파일 시점의 쿼리 오류 방지 |
| **Caching** | <img src="https://img.shields.io/badge/Ehcache-005571?style=for-the-badge&logo=java&logoColor=white"> | 별도 인프라 구축 없이 네트워크 비용이 없는 Local Cache로 조회 성능 최적화 |
| **DevOps & Infra** | <img src="https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white"> <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white"> | 개발/운영 환경의 불일치를 해결하고 반복적인 배포 과정을 자동화하여 개발 효율 증대 |
| **Test & Monitor** | <img src="https://img.shields.io/badge/nGrinder-FFA500?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/Scouter-00C7B7?style=for-the-badge&logo=scouter&logoColor=white"> | 실제 트래픽 환경을 모방하여 TPS, Latency 등 정량적 지표를 기반으로 병목 구간 파악 |

<br><br>

## 3. 아키텍처 및 인프라 진화

### 백엔드 패키지 구조

기능 응집도를 높이고자 계층형 대신 **도메인형 패키지 구조**를 채택했습니다. 

`feature`(비즈니스 로직)와 `common`(공통 모듈)을 분리해 유지보수 효율을 확보했습니다.

```
be/url_backend/
├── feature/          # 핵심 비즈니스 기능 (도메인)
│   ├── url/          # URL 단축 기능
│   ├── admin/        # 관리자 기능
│   ├── log/          # 클릭 로그 기능
│   └── stats/        # 통계 기능
│
└── common/           # 공통 인프라 및 유틸리티
    ├── config/       # 애플리케이션 설정
    ├── dto/          # 공통 데이터 전송 객체
    ├── entity/       # 공통 베이스 엔티티
    ├── exception/    # 전역 예외 처리
    └── util/         # 공통 유틸리티
```

<img width="100%" height="1148" alt="image" src="https://github.com/user-attachments/assets/6b54ee04-bc11-47a9-a1a6-cffbe0fac301" />

<br><br>

### 서비스 아키텍처

> 기술적 학습을 위해 **"비용 효율적인 단일 인스턴스"** 에서 **"확장 가능한 분산 환경"** 으로 아키텍처를 확장하여 설계했습니다.

#### 3-1. 인프라 아키텍처 진화

#### [Step 1] 단일 인스턴스 최적화
- **구성:** EC2(t3.micro) + Docker + **Ehcache(Local)**
- **특징:** 네트워크 I/O가 없는 로컬 캐시(Ehcache)를 활용해 **최대 성능**을 확보했습니다.
- **한계:** 서버 장애 시 단일 장애 지점 위험이 존재합니다.

#### [Step 2] 고가용성 분산 환경 설계
- **목표:** 트래픽 증가에 유연하게 대응하고 무중단 배포가 가능한 인프라 구축
- **구성:** AWS VPC, Auto Scaling Group, ALB, RDS (Multi-AZ)
- **설계 의도:**
    - **Traffic Handling:** ALB와 Auto Scaling을 통해 부하를 분산.
    - **HA:** Multi-AZ RDS 구성을 통해 데이터베이스 가용성 확보.
    - **CI/CD:** GitHub Actions와 CodeDeploy를 연동한 자동화 파이프라인 구축.
    - **(본 아키텍처 적용 시 Local Cache(Ehcache)의 데이터 불일치 문제를 해결하기 위해 Redis(Global Cache)로의 마이그레이션이 필수적임을 인지하고 설계했습니다.)**

<img width="100%" height="1054" alt="image" src="https://github.com/user-attachments/assets/b1586b83-5b48-44a8-8b6f-314e01db432a" />

<br><br>

## 4. 성능 고도화

**"가설 → 검증"** 프로세스에 따라 문제를 정의하고 부하 테스트로 검증했습니다.

* **Test Environment:** (Mac)Local, Docker Container, nGrinder
* **Target:** 임시 데이터 50만 건, 가상 사용자 접속 상황

<br><br>

### [Phase 1] 복합 인덱스로 조회 성능 65배 개선

**Q. 약 50만 건의 로그 데이터 조회 시 왜 0.2초나 소요되는가?**

* **문제 정의:** `EXPLAIN` 실행 결과 `type: ALL` (Full Table Scan) 발생. 단일 인덱스만으로는 다중 조건(`ip`, `date`) 필터링 시 데이터 추출 효율 급감 확인.
    ```sql
    EXPLAIN SELECT * FROM click_log
    WHERE ip_address = '192.168.0.50' AND created_at BETWEEN '2025-05-10' AND '2025-05-17';
    ```
    
    <img width="80%" height="100" alt="image" src="https://github.com/user-attachments/assets/234dda02-aedb-4d4c-9fec-762d125528ad" /> <br>

<br>

* **해결 전략:** 카디널리티가 높은 `ip_address`와 조회 범위가 넓은 `created_at`을 결합한 **복합 인덱스** 생성.
  ```sql
  CREATE INDEX idx_ip_created ON click_log (ip_address, created_at);
  ```
  <img width="80%" height="100" alt="image" src="https://github.com/user-attachments/assets/5ff3bed5-9a74-437d-ad70-2fe8fecfd8f8" />
  <br>
  <br>

    - MySQL Profiling 결과:
        - **0.21887s → 0.003725s {98}% 개선**
       <img width="373" height="352" alt="image" src="https://github.com/user-attachments/assets/101231e1-fde9-4a61-8014-3c56a5a024cc" />
            
* **검증 결과 및 판단 근거:**
  - **결과:** 쿼리 실행 시간 **0.218s → 0.003s (98% 단축)**, nGrinder TPS **25.4 → 1,654.8** 달성.
  - **판단 근거:** 인덱스 유지에 따른 **쓰기 비용**보다 대량의 로그 데이터를 반복 조회하는 관리자 페이지의 응답 속도 확보가 비즈니스적으로 더 높은 가치를 지닌다고 판단했습니다.

<br>

* **정량적 성과 (nGrinder 부하 테스트 결과)**

| **지표** | **최적화 이전** | **최적화 이후** | **개선 효과** |
| --- | --- | --- | --- |
| **TPS (평균 처리량)** | 25.4 | **1,654.8** | **약 65배 향상** |
| **Peak TPS (최대 처리량)** | 29.5 | **2,089.0** | **약 71배 향상** |
| **Mean Test Time (평균 응답 시간)** | 386.81ms | **5.43ms** | **약 98% 단축** |
| **Executed Tests (총 처리 건수)** | 2,949 | **192,522** | **약 65배 증가** |

<details><summary><h3> [펼치기] nGrinder 테스트 결과 그래프 확인</h3></summary> <div markdown="1">

### **인덱스 적용 전**
<img width="2048" height="620" alt="image" src="https://github.com/user-attachments/assets/8f46525c-e5d8-444e-8752-6e1660406bd9" />

<br>

### **인덱스 적용 후**
<img width="2048" height="609" alt="image" src="https://github.com/user-attachments/assets/0e04d1de-a9f0-46c9-802b-308f08b96fb7" />

</div> </details>

<br><br>

### [Phase 2] Ehcache 도입을 통한 응답 속도 향상

**Q. "Hot URL" 리디렉션 요청마다 DB I/O를 발생시켜야 하는가?**

* **문제 정의:** 특정 인기 URL(Hot Key)에 전체 트래픽의 상당 부분이 집중되는 '요청 편중 현상' 발생. 매번 DB를 조회하는 구조는 중복 쿼리 실행으로 인한 자원 낭비와 응답 속도 저하를 야기함.
* **해결 전략:** JVM 힙 메모리 기반 **Ehcache** 적용. 자주 찾는 URL은 메모리에서 즉시 반환(LRU 정책)하도록 구성.
* **검증 결과 및 판단 근거:**
* **결과:** 평균 응답 시간 **39ms → 37ms (7% 개선)** 확인.
  - **Trade-off:** 로컬 캐시는 서버 간 데이터 정합성 문제가 발생할 수 있으나 현재 단일 서버 구조에서는 네트워크 비용이 없는 최적의 선택지라 판단했습니다.
  - **분석:** DB와 애플리케이션이 동일한 localhost에서 동작하여 **네트워크 지연**이 거의 없는 환경이었습니다. 이로 인해 캐싱을 통한 I/O 비용 절감 효과보다 테스트 도구와 앱 간의 CPU/Context Switching 경합이 전체 TPS의 임계점으로 작용했습니다.
   - 서버와 DB가 분리된 AWS RDS 고가용성 환경에서는 네트워크 왕복 비용이 발생하므로 더 극적인 수치가 나올것을 인지.

* **정량적 성과 (nGrinder 부하 테스트 결과)**

| **지표** | **캐시 적용 전** | **캐시 적용 후** | **개선 효과** |
| --- | --- | --- | --- |
| **TPS (평균 처리량)** | 1,184.5 | **1,301.9** | **약 9.9% 향상** |
| **Peak TPS (최대 처리량)** | 1,481.5 | **1,718.5** | **약 16.0% 향상** |
| **Mean Test Time (평균 응답 시간)** | 39.84ms | **37.04ms** | **약 7.0% 단축** |
| **Executed Tests (총 처리 건수)** | 137,858 | **154,194** | **약 11.8% 증가** |

<details><summary><h3> [펼치기] nGrinder 테스트 결과 그래프 확인</h3></summary> <div markdown="1">

### 캐시 적용 전
<img width="2048" height="628" alt="image" src="https://github.com/user-attachments/assets/103d28bd-034c-497e-9715-cc3b53cf78ad" />

<br>

### 캐시 적용 후
<img width="2048" height="614" alt="image" src="https://github.com/user-attachments/assets/b8abf39d-b31d-4508-95c4-c90b95b4c830" />

</div> </details>

<br>

### [Phase 3] @Async 비동기 처리로 사용자 대기 시간 최소화

**Q. 통계 로그 저장이 늦어져 리디렉션 응답까지 지연되는 것이 타당한가?**

* **문제 정의:** 리디렉션(Core)과 로그 저장(Sub)이 단일 트랜잭션에 묶여 있어 로그 기록 중 발생하는 DB 지연이 사용자 경험을 직접적으로 저해함.
* **해결 전략:** Spring `@Async`를 적용하여 로그 저장을 별도 스레드로 분리.
* **검증 결과 및 판단 근거:**
  - **결과:** Peak TPS **13.4% 증가(1,585 → 1,798)** 달성.
  - **판단 근거:** 사용자는 로그 저장 성공 여부와 관계없이 즉시 원본 사이트로 이동해야 합니다. 데이터 정합성보다 응답 속도가 우선인 도메인 특성을 고려해 비동기 처리를 적용했습니다.

* **정량적 성과 (nGrinder 부하 테스트 결과)**

| **지표** | **비동기 적용 전** | **비동기 적용 후** | **개선 효과** |
| --- | --- | --- | --- |
| **TPS (평균 처리량)** | 1,124.5 | **1,275.1** | **약 13.4% 증가** |
| **Peak TPS (최대 처리량)** | 1,585.5 | **1,798.5** | **약 13.4% 증가** |
| **Mean Test Time (평균 응답 시간)** | 7.77ms | **7.10ms** | **약 8.6% 단축** |
| **Executed Tests (총 처리 건수)** | 133,013 | **148,226** | **약 11.4% 증가** |

<details><summary><h3> [펼치기] nGrinder 테스트 결과 그래프 확인</h3></summary> <div markdown="1">

### **비동기 적용 전**
<img width="2048" height="610" alt="image" src="https://github.com/user-attachments/assets/5846f48d-c987-4dde-b02e-69810bab0dfd" />

<br>

### **한 작업을 끝내는 데 필요한 전체 시간**
<img width="2048" height="1035" alt="image" src="https://github.com/user-attachments/assets/1760975d-0807-4752-af33-af8e88777726" />

<br>

### **비동기 적용 후**
<img width="2048" height="631" alt="image" src="https://github.com/user-attachments/assets/966d54c2-2dda-4312-94eb-24d3d381396a" />

<img width="2048" height="1035" alt="image" src="https://github.com/user-attachments/assets/19bab646-549e-474b-8042-71c698daa56f" />

</div> </details>

<br><br>

## 5. 설계 회고 및 한계 분석

### 1. 분산 환경에서의 캐시 정합성 (Local vs Global)

**설계 전략**: 단일 머신 환경에서 네트워크 비용이 없는 Ehcache를 통해 응답 속도를 최적화했습니다.
* **한계:** 서버 다중화 시 인스턴스 간 데이터 불일치가 발생합니다.
* **개선안:** 향후 확장성을 고려한다면 **Redis**와 같은 Global Cache를 도입해 데이터 정합성을 확보해야 함을 인지했습니다.

### 2. 비동기 처리의 데이터 유실 리스크

**구현 방식**: 사용자 경험을 최우선으로 하여 @Async 기반의 비동기 로그 저장 로직을 구축했습니다.
* **한계:** 애플리케이션 장애나 스레드 풀 고갈 시 통계 데이터가 유실될 위험이 있습니다.
* **개선안:** 데이터 유실 방지가 중요한 경우 **Kafka** 등 메시지 브로커를 도입해 메시지 영속성을 확보하는 구조로 고도화할 계획입니다.

### 3. DB 커넥션 가용성 확보 (OSIV 비활성화)
* **채택 근거**: OSIV가 활성화된 경우 API 응답 시점까지 DB 커넥션을 점유하여 고부하 상황에서 커넥션 풀 고갈을 유발할 수 있음을 인지했습니다.
* **적용 내용**: spring.jpa.open-in-view를 false로 설정해 커넥션 반환 시점을 앞당겼습니다. 지연 로딩 문제는 서비스 계층 내 DTO 변환을 통해 해결하여 시스템 전반의 자원 관리 효율을 높였습니다.

<img width="80%" height="500" alt="image" src="https://github.com/user-attachments/assets/f184a014-49ea-48eb-9737-3a3a75e1cc58" />


<br><br>

## 6. 설치 및 실행

*(※ 현재 AWS 배포는 중단 상태이며 아래 명령어로 로컬 실행 가능함)*

```bash
# Clone Repository
git clone https://github.com/UrlOps/perfurl-backend.git

```


--- 

<details><summary><h2> [펼치기] 프로젝트 주요 화면 </h2></summary>
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

<br><br>


### 관리자 로그인 화면
<img width="2820" height="1386" alt="image" src="https://github.com/user-attachments/assets/540d0dc2-e8fa-43f6-b045-ecd0c6e288e6" />
<br><br>

### 백오피스 화면
<img width="2876" height="1404" alt="image" src="https://github.com/user-attachments/assets/1e330cad-6167-4cdc-b977-e1ef59d28e77" />
</div>
</details>
