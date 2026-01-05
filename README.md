# URL Shortener Service

## 프로젝트 소개

이 프로젝트는 긴 URL을 짧고 관리하기 쉬운 URL로 변환해주는 웹 애플리케이션입니다.

또한, 관리자 대시보드를 통해 URL 생성 통계 및 클릭 로그 분석 기능을 제공하여 URL을 관리할 수 있습니다.

<br>

## 주요 기능

- **URL 단축**: 긴 원본 URL을 고유하고 짧은 키를 가진 URL로 변환합니다.
- **빠른 리디렉션**: 생성된 단축 URL로 접속 시, 즉시 원본 URL로 리디렉션됩니다.
- **클릭 통계**: 단축 URL이 클릭될 때마다 관련 로그를 기록하여 통계를 추적합니다.
- **관리자 대시보드**:
    - 관리자 인증 (JWT 기반)
    - 전체 URL 목록 조회
    - URL별 클릭 통계 및 상세 로그 확인 (시간, IP 주소, User-Agent, 클릭 횟수 등)
 
<br>

## 주요 화면


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

<br><br>

## 기술 스택

| Category | Technology |
| :--- | :--- |
| **Language** | <img src="https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"> <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black"> |
| **Framework** | <img src="https://img.shields.io/badge/Spring_Boot_3.5-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/Vue.js_3-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white"> <img src="https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white"> |
| **Database & Cache** | <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> <img src="https://img.shields.io/badge/H2_Database-003B57?style=for-the-badge&logo=h2&logoColor=white"> <img src="https://img.shields.io/badge/Ehcache-005571?style=for-the-badge&logo=java&logoColor=white"> |
| **Backend Tech** | <img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/QueryDSL-007ACC?style=for-the-badge&logo=java&logoColor=white"> <br> <img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white"> <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white"> |
| **Frontend UI** | <img src="https://img.shields.io/badge/Vuetify-1867C0?style=for-the-badge&logo=vuetify&logoColor=white"> <img src="https://img.shields.io/badge/Tailwind_CSS-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white"> <br> <img src="https://img.shields.io/badge/Axios-5A29E4?style=for-the-badge&logo=axios&logoColor=white"> <img src="https://img.shields.io/badge/Chart.js-FF6384?style=for-the-badge&logo=chartdotjs&logoColor=white"> |
| **DevOps & Infra** | <img src="https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white"> <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white"> |
| **Test & Monitor** | <img src="https://img.shields.io/badge/nGrinder-FFA500?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/Scouter-00C7B7?style=for-the-badge&logo=scouter&logoColor=white"> <img src="https://img.shields.io/badge/Lombok-BC0230?style=for-the-badge&logo=lombok&logoColor=white"> |

 <br>

## 백엔드 아키텍처

백엔드는 **도메인 중심의 패키지 구조**로 기능별 응집도를 높이고 다른 기능과의 결합도를 낮췄습니다.

프로젝트는 크게 두 개의 최상위 패키지, **`feature`** 와 **`common`** 으로 나뉩니다.

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


<img width="1866" height="1148" alt="image" src="https://github.com/user-attachments/assets/6b54ee04-bc11-47a9-a1a6-cffbe0fac301" />


<br><br>

## 서비스 아키텍처

<img width="1859" height="1054" alt="image" src="https://github.com/user-attachments/assets/b1586b83-5b48-44a8-8b6f-314e01db432a" />

<br><br>

## 성능 최적화

대용량 트래픽 환경에서도 안정적인 서비스를 제공하기 위해 다음과 같은 성능 최적화를 진행했습니다.

### 1. 캐싱 (Ehcache)

> **URL 리디렉션 API – Ehcache 적용 성능 최적화**

#### **1. 배경**

- URL 단축 서비스의 핵심 기능인 **리디렉션 API**는 특정 단축 URL을 원본 URL로 매핑하여 리디렉션을 수행.
- 트래픽 특성상 일부 “핫 키(Hot Key(자주 이용되는 URL))“에 요청이 집중되는 현상이 발생.
- 초기 구현에서는 모든 요청이 DB를 조회해야 하므로 **DB 부하 증가와 응답 속도 저하** 문제가 발생.
- 대량의 트래픽이 몰리는 부하 테스트 환경에서 TPS가 제한적으로 상승하는 한계가 관찰.

---

#### **2. 원인 분석**

- URL 리디렉션 API는 **조회 성격**이 강해 동일한 키가 반복적으로 요청됨.
- 그러나 DB를 매번 조회하는 구조로 인해:
    - **DB Connection Pool 소진 위험**
    - **네트워크 및 디스크 I/O 오버헤드 증가**
    - **사용자 응답 지연**
- 자주 호출되는 데이터조차 캐싱하지 않아 **중복 쿼리 실행**이 성능 저하의 직접적 원인임을 확인.

---

#### **3. 해결 과정 및 기술적 결정**

- **1단계: Ehcache 적용**
    - 자주 사용되는 단축 URL → 원본 URL 매핑 데이터를 **Ehcache(Local Cache)** 에 저장.
    - API 호출 시 DB를 조회하기 전에 캐시를 우선 확인하여 **Cache Hit 시 DB 접근 차단**.
- **2단계: 캐시 전략 설계**
    - **LRU** 정책 기반 캐시 만료 전략 적용.
    - 핫 키 특성을 고려하여 캐시 용량을 확보.
- **3단계: 부하 테스트 (nGrinder + Scouter)**
    - 실제 트래픽 패턴을 반영: 전체 키 중 10%를 “핫 키”로 지정, 이들에 전체 트래픽의 80% 집중.

---

#### **4. 정량적 성과 (nGrinder 부하 테스트 결과)**

| **지표** | **캐시 적용 전** | **캐시 적용 후** | **개선 효과** |
| --- | --- | --- | --- |
| **TPS (평균 처리량)** | 1,184.5 | **1,301.9** | **약 9.9% 향상** |
| **Peak TPS (최대 처리량)** | 1,481.5 | **1,718.5** | **약 16.0% 향상** |
| **Mean Test Time (평균 응답 시간)** | 39.84ms | **37.04ms** | **약 7.0% 단축** |
| **Executed Tests (총 처리 건수)** | 137,858 | **154,194** | **약 11.8% 증가** |

<br>

#### 캐시 적용 전
<img width="2048" height="628" alt="image" src="https://github.com/user-attachments/assets/103d28bd-034c-497e-9715-cc3b53cf78ad" />

<br>

#### 캐시 적용 후
<img width="2048" height="614" alt="image" src="https://github.com/user-attachments/assets/b8abf39d-b31d-4508-95c4-c90b95b4c830" />

<br><br>

### 2. 복합 인덱스

> **특정 IP 클릭 통계 조회 – 복합 인덱스 적용 성능 최적화**

#### **1. 배경**

- 대용량 데이터 환경을 가정하여 **임시 데이터 500,000건**을 삽입하고 테스트 진행.
- **특정 IP**와 **기간 조건**으로 조회 시 **쿼리 응답 속도가 매우 느려짐**을 확인.
- EXPLAIN 실행 결과, 해당 쿼리가 **풀 테이블 스캔**으로 동작함을 확인.
    
    ```sql
    EXPLAIN SELECT * FROM click_log
    WHERE ip_address = '192.168.0.50' AND created_at BETWEEN '2025-05-10' AND '2025-05-17';
    ```
    
    <img width="1266" height="134" alt="image" src="https://github.com/user-attachments/assets/234dda02-aedb-4d4c-9fec-762d125528ad" />

---

#### **2. 원인 분석**

- 두 조건이 함께 자주 사용되지만, 인덱스 부재로 인해 전체 데이터(50만 건)를 스캔.
- 결과적으로 **쿼리 응답 시간 지연 및 대량 요청 시 TPS 저하**가 발생.

---

#### **3. 해결 과정 및 기술적 결정**

- **1단계: 복합 인덱스 생성**
    - **카디널리티**가 높은 ip_address와 created_at을 조합하여 복합 인덱스를 생성.
        
        ```sql
        CREATE INDEX idx_ip_created ON click_log (ip_address, created_at);
        ```
        
- **2단계: 인덱스 활용 검증**
    - EXPLAIN 실행 결과, 풀 테이블 스캔 → **인덱스 범위 스캔**으로 전환됨을 확인.
        
        <img width="1434" height="134" alt="image" src="https://github.com/user-attachments/assets/5ff3bed5-9a74-437d-ad70-2fe8fecfd8f8" />

        
    - MySQL Profiling 결과:
        - **0.21887s → 0.003725s {98}% 개선**
            
            <img width="772" height="752" alt="image" src="https://github.com/user-attachments/assets/101231e1-fde9-4a61-8014-3c56a5a024cc" />


- **3단계: 대량 요청 환경 부하 테스트 (nGrinder + Scouter)**
    - 단일 쿼리 실행만으로는 체감이 크지 않았으나, 대량 동시 요청 시 TPS 및 응답 시간 개선 효과가 뚜렷하게 나타남.
    - 인덱스 적용 후, 동일 자원으로 훨씬 많은 요청을 처리 가능함을 검증.

---

#### **4. 정량적 성과 (nGrinder 부하 테스트 결과)**

| **지표** | **최적화 이전** | **최적화 이후** | **개선 효과** |
| --- | --- | --- | --- |
| **TPS (평균 처리량)** | 25.4 | **1,654.8** | **약 65배 향상** |
| **Peak TPS (최대 처리량)** | 29.5 | **2,089.0** | **약 71배 향상** |
| **Mean Test Time (평균 응답 시간)** | 386.81ms | **5.43ms** | **약 98% 단축** |
| **Executed Tests (총 처리 건수)** | 2,949 | **192,522** | **약 65배 증가** |

<br>

#### **인덱스 적용 전**
<img width="2048" height="620" alt="image" src="https://github.com/user-attachments/assets/8f46525c-e5d8-444e-8752-6e1660406bd9" />

<br>

#### **인덱스 적용 후**
<img width="2048" height="609" alt="image" src="https://github.com/user-attachments/assets/0e04d1de-a9f0-46c9-802b-308f08b96fb7" />

<br><br>

### 3. 비동기 처리 (@Async)

> **URL 리디렉션 API – 비동기 처리(@Async) 적용 성능 최적화**

#### **1. 배경**

- URL 단축 서비스의 리디렉션 API는 **사용자가 단축 URL을 호출할 때, 원본 URL로 리디렉션 처리**를 담당하는 핵심 기능.
- 기존 구현에서는 리디렉션 시점에 **클릭 로그 저장 및 통계 업데이트를 동기적으로 처리**하여, 불필요한 응답 지연이 발생.
- 특히 대량 요청 환경에서 로그 처리 로직이 **API 병목**으로 작용하여 응답 속도와 TPS 모두 한계에 부딪힘.

---

#### **2. 원인 분석**

- **동기 처리 구조 문제**:
    - 리디렉션 처리와 동시에 클릭 로그 저장(DB I/O)을 수행.
    - DB I/O는 네트워크/디스크 지연이 발생하기 때문에 사용자 요청 응답 시간을 불필요하게 증가시킴.

---

#### **3. 해결 과정 및 기술적 결정**

- **1단계: 비동기 처리 도입 (@Async)**
    - Spring의 @Async를 활용하여 **클릭 로그 저장 및 통계 업데이트 로직을 비동기 처리**.
    - API 응답은 즉시 반환하고, 부수적인 작업은 별도 스레드 풀에서 처리 → **사용자 대기 시간 최소화**.
- **2단계: 부하 테스트 (nGrinder + Scouter)**
    - 실제 트래픽 환경을 가정한 부하 테스트 진행.
    - TPS, 평균 응답 시간, 실행된 테스트 건수에서 개선 효과를 정량적으로 검증.

---

#### **4. 정량적 성과 (nGrinder 부하 테스트 결과)**

| **지표** | **비동기 적용 전** | **비동기 적용 후** | **개선 효과** |
| --- | --- | --- | --- |
| **TPS (평균 처리량)** | 1,124.5 | **1,275.1** | **약 13.4% 증가** |
| **Peak TPS (최대 처리량)** | 1,585.5 | **1,798.5** | **약 13.4% 증가** |
| **Mean Test Time (평균 응답 시간)** | 7.77ms | **7.10ms** | **약 8.6% 단축** |
| **Executed Tests (총 처리 건수)** | 133,013 | **148,226** | **약 11.4% 증가** |

<br>

#### **비동기 적용 전**
<img width="2048" height="610" alt="image" src="https://github.com/user-attachments/assets/5846f48d-c987-4dde-b02e-69810bab0dfd" />

<br>

**한 작업을 끝내는 데 필요한 전체 시간**
<img width="2048" height="1035" alt="image" src="https://github.com/user-attachments/assets/1760975d-0807-4752-af33-af8e88777726" />

<br>

#### **비동기 적용 후**
<img width="2048" height="631" alt="image" src="https://github.com/user-attachments/assets/966d54c2-2dda-4312-94eb-24d3d381396a" />


<img width="2048" height="1035" alt="image" src="https://github.com/user-attachments/assets/19bab646-549e-474b-8042-71c698daa56f" />

<br><br>

### 4. OSIV (Open Session In View)

Spring Boot의 기본 설정인 OSIV(open-in-view: true)가 불필요하게 데이터베이스 커넥션을 요청 시작부터 응답 완료 시점까지 점유하여, 트래픽 증가 시 잠재적인 성능 저하 및 장애를 유발할 수 있는 위험을 인지.

이에 `spring.jpa.open-in-view` 옵션을 `false`로 명시적으로 비활성화하여, 트랜잭션이 실행되는 서비스 계층 내에서만 DB 커넥션을 사용하도록 정책을 변경.
이 과정에서 발생할 수 있는 `LazyInitializationException`은 DTO 변환 로직을 철저히 적용하여 해결, 이를 통해 컨트롤러 계층과 영속성 계층을 명확히 분리.

--- 
