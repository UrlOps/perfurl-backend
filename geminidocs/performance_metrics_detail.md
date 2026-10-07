# 📊 Detailed Performance Metrics: PerfUrl Benchmark (AS-IS vs. TO-BE)

This document provides a granular consolidation of raw metrics derived from k6 stress tests, Scouter APM analysis, and Docker resource monitoring under strict t3.medium (2.0 vCPU) emulation.

---

## 1. k6 Load Test Metrics (0 → 600 TPS Ramping)

| Metric | AS-IS Baseline (`perfurl-as-is-01`) | TO-BE Optimized (`perfurl-to-be-01`) | Improvement (%) |
| :--- | :--- | :--- | :--- |
| **Total Success Requests** | 64,480 | **106,234** | **+64.75%** |
| **Average Throughput (TPS)** | 177.97 TPS | **293.48 TPS** | **+64.90%** |
| **P50 Latency (Median)** | 3,470 ms | **106.48 ms** | **-96.93%** |
| **P90 Latency** | 5,500 ms | **1,330 ms** | **-75.82%** |
| **P95 Latency** | 6,230 ms | **1,720 ms** | **-72.39%** |
| **Maximum Latency** | 13,430 ms | **4,800 ms** | **-64.26%** |
| **Minimum Latency** | 2.98 ms | **0.90 ms** | **-69.80%** |
| **Dropped Iterations** | 43,519 | **1,764** | **-95.95%** |
| **Max Concurrent VUs** | 1,000 | **1,000** | - |

---

## 2. Scouter APM Granular Analysis

### 🅰️ AS-IS State (`perfurl-as-is-01`)
- **Active Service Count:** Peaked at **200** (Full capacity of Tomcat worker threads).
- **SQL Execution Time:** 
  - Range: **100ms ~ 300ms** per request.
  - Pattern: Sharp spikes correlating with traffic increases.
- **JVM Heap Pattern:** 
  - Range: **300MB ~ 900MB**.
  - Frequency: Large, irregular sawtooth patterns.
- **GC Performance:** 
  - Peak Pause Time: **4,500ms (4.5s)**.
  - Occurrence: Regular 2-4 times per sampling period under load.
- **XLog Distribution:** Dense pillar formation between 1.0s and 3.0s, with outliers hitting **7.5s+**.

### 🅱️ TO-BE State (`perfurl-to-be-01`)
- **Active Service Count:** Maintained stability below 50 during normal redirection flows.
- **SQL Execution Time:** 
  - Range: **0.1ms ~ 3ms**.
  - Trend: Near-flatline at the bottom of the graph (Ehcache Hit).
- **New Thread Identification:**
  - `async-worker-N` (Core: 30 / Max: 50) observed in `RUNNABLE` state during logging.
- **JVM Heap Pattern:** 
  - Range: **300MB ~ 1,000MB**.
  - Pattern: Tight, high-frequency sawtooth (Healthy object recycling).
- **GC Performance:** 
  - Peak Pause Time: Generally controlled within **1s ~ 2s** range (excluding rare spikes).
- **XLog Distribution:** High-density clustering at the **0.0s ~ 0.5s** baseline.

---

## 3. Docker Stats (Resource Emulation)

- **CPU Limit:**
  - `app`: 1.0 vCPU (Hard limit)
  - `db`: 1.0 vCPU (Hard limit)
- **Memory Limit:** 
  - 2GB per container.
- **Observed Peak Usage:**
  - **AS-IS:** `app` container hit 100% CPU saturation early, causing thread starvation.
  - **TO-BE:** `app` container CPU usage became more efficient (less time spent on I/O wait), allowing for nearly double the request processing before hitting the 1.0 vCPU ceiling.

---

## 🔍 Audit & Verification Result

### 1. Wiki Report Consistency Check
- **Status:** **Matched.** 
- **Refinement:** The wiki report already correctly highlights the 65% TPS jump and 97% latency reduction. 
- **Insight:** No additional updates required, as the core facts align with the raw logs.

### 2. Portfolio Final Review
- **Status:** **Matched.**
- **Refinement:** The portfolio accurately reflects the P95 reduction from 6.23s to 1.72s. 
- **Recommendation:** Ensure the "1.0 vCPU limitation" is interpreted as the reason for the remaining 1.72s latency to strengthen the Scale-Up justification. (Already included in the final version).

### 3. UML Diagram Accuracy
- **Status:** **Matched.**
- **Refinement:** The UML correctly shows the Phase 1 (Local Cache) and Phase 2 (Async Logging) separation.
- **Addition:** Adding a "Discard Policy" note to the UML could further emphasize the "Resilience" aspect of the architecture.

---
*Created on: 2026-06-02*
