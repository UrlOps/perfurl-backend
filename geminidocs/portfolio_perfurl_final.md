## **4. 단축 URL 리디렉션 트래픽 집중 시, DB I/O 병목으로 인한 시스템 마비 현상 발생 및 로컬 캐시·비동기 로깅 도입으로 P(95) 응답 속도 6.23s → 1.72s 단축**

***(Project: PerfUrl)***

- **전체적인 아키텍처**
    
    ```mermaid
    %%{init: {'theme': 'base', 'themeVariables': { 'fontSize': '13px', 'fontFamily': 'Pretendard, sans-serif'}}}%%
    graph TD
        %% AS-IS 섹션
        subgraph AS_IS_4 ["<b>[AS-IS] 트랜잭션 강결합 및 시스템 마비</b>"]
            A_User((User Traffic)) --> A_Main["<b>Main Thread</b><br/>리디렉션 + 로그 저장<br/>강결합"]
            A_Main --> A_DB[(MySQL DB)]
            A_DB -.-> |"<b>6.23s 지연 (P95)</b><br/>(활성 스레드 마비 및 GC 지연)"| A_User
        end
    
        %% TO-BE 섹션
        subgraph TO_BE_4 ["<b>[TO-BE] 고가용성 리디렉션 파이프라인</b>"]
            subgraph Phase1 ["<b>Phase 1: 실시간 리디렉션 (Read)</b>"]
                B1((User Traffic)) -- "GET /short-url" --> B2{"<b>1 | Local Cache</b><br/>Ehcache Lookup"}
                B2 -- "<b>Hit</b>" --> B3["<b>1.72s 응답 (P95)</b><br/>302 Redirect"]
                B2 -- "Miss" --> B_DB_R[(MySQL DB)]
                B_DB_R --> B3
            end
    
            subgraph Phase2 ["<b>Phase 2: 비동기 로그 격리 (Write)</b>"]
                B3 -.-> |"<b>2 | Non-Blocking</b><br/>Async Event"| B4["<b>3 | Worker Thread</b><br/>@Transactional"]
                B4 -- "<b>Discard Policy</b><br/>(Resilience)" --> B_DB_W[(MySQL DB)]
            end
        end
    
        %% 스타일링
        style AS_IS_4 fill:#fdf2f2,stroke:#f87171,stroke-width:2px,stroke-dasharray: 8 4
        style TO_BE_4 fill:#f8fafc,stroke:#334155,stroke-width:2px
    
        %% DB 스타일
        style A_DB fill:#E0F2FE,stroke:#ef4444,stroke-width:2.5px
        style B_DB_R fill:#E0F2FE,stroke:#3B82F6,stroke-width:2px
        style B_DB_W fill:#E0F2FE,stroke:#3B82F6,stroke-width:2px
    
        %% 기술 요소 스타일
        style B2 fill:#ffffff,stroke:#d97706,stroke-width:1.5px
        style B4 fill:#ffffff,stroke:#339af0,stroke-width:1.5px
        style B3 fill:#ecfdf5,stroke:#059669,stroke-width:2px
        style A_User fill:#f3f4f6,stroke:#374151
        style B1 fill:#f3f4f6,stroke:#374151
    ```

- **문제 원인**
    - 단축 URL 서비스 특성상 인기 링크(Hot Key)에 대규모 트래픽 집중 시, 원본 URL 조회와 클릭 통계 적재 로직이 단일 트랜잭션으로 강결합되어 디스크 I/O 지연 및 대규모 요청 누락 리스크 식별
    - 제한된 서버 자원(1.0 vCPU) 내에서 300ms 이상의 SQL 쿼리 지연이 발생하며, 톰캣 워커 스레드가 응답을 대기하느라 활성 요청 수(Active Service 200)가 포화되는 스레드 마비 현상 발생
    - 무한 대기 상태의 밀려드는 요청 객체들로 인해 메모리가 팽창하여 최대 4.5초의 극심한 GC 쓰레싱을 유발하며 애플리케이션의 물리적 마비 상태 확인
- **해결 과정**
    - 네트워크 I/O 비용이 발생하지 않는 JVM 힙 메모리 기반의 로컬 캐시(Ehcache)를 도입하여, 핫 키 조회 시 발생하는 DB Read 부하를 원천 차단하고 SQL 실행 시간을 3ms 이하로 단축
    - 통계 데이터의 100% 무결성보다 리디렉션 응답 속도가 우선이라는 비즈니스 트레이드오프를 수용하여, 무거운 로깅 쓰기 작업을 `@Async` 워커 스레드로 분리해 메인 스레드의 응답성 확보
    - 극한의 부하 상황을 대비해 스레드 풀에 `DiscardPolicy`를 적용하고, 로깅 계층에 독립적인 `@Transactional`을 부여하여 쓰기 병목이 메인 리디렉션 흐름을 차단하지 않도록 장애 격리망 구축
    - **테스트**
        - AWS t3.medium 단일 노드(WAS 1.0 vCPU, DB 1.0 vCPU) 제약 환경을 Docker로 정밀 모사하여, 하드웨어 확장이 아닌 소프트웨어 아키텍처의 한계 돌파 측정 환경 구축
        - k6를 활용해 피크 타임 목표인 600 TPS 도달을 가정한 ramping-arrival-rate 시나리오 스크립트를 작성하여 부하 인가 및 Scouter APM을 통한 개선 지표 실시간 검증
- **결과**
    - 메인 스레드에서 I/O 블로킹 구간을 완전히 걷어내어 평균 응답 속도(P50)를 3.47s에서 106ms로 96.9% 단축하고, 스레드 풀 마비를 해소하여 평균 처리량을 177.9 TPS에서 293.5 TPS로 향상
    - 부하 누락(Dropped) 건수를 43,519건에서 1,764건으로 95.9% 감소시키고, 동일 시간 내 2배 이상인 207,186건의 트랜잭션을 소화하여 극단적인 자원 제약 내 비즈니스 가용성 확보
    - 로깅 책임을 온전히 분리하여 메인 스레드의 가용성을 방어하고, 처리량 급증에도 불구하고 안정적인 톱니바퀴(Sawtooth) 패턴의 메모리 관리를 통해 서버 다운 리스크 제거