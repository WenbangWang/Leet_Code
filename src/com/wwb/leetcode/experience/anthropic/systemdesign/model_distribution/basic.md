

## 1️⃣ Functional Requirements (FRs) — Abstract

1. **Model Acquisition**

    * The system can retrieve a machine learning model from an external source.

2. **Model Delivery**

    * Ensure the model becomes available on all hosts in the datacenter.
    * Hosts may receive portions from other hosts or directly from external sources.

3. **Progress Awareness**

    * The system tracks which hosts have received portions of the model to guide efficient delivery.

4. **Reliability**

    * Delivery continues despite host or network failures, ensuring all reachable hosts eventually receive the complete model.

5. **Completion Verification**

    * Confirm that all hosts have received the full and correct model.

---

## 2️⃣ Non-Functional Requirements (NFRs) — Abstract

1. **Performance**

    * Minimize total time for all hosts to receive the model.
    * Efficiently use the external connection and internal datacenter network capacity.

2. **Scalability**

    * Support large numbers of hosts (e.g., 1,000+).
    * Performance should degrade gracefully as the number of hosts increases.

3. **Reliability**

    * Tolerant of host or network failures.
    * Delivery should eventually complete for all reachable hosts.

4. **Integrity**

    * Hosts must receive a complete and correct copy of the model.

5. **Extensibility**

    * Allow future improvements to increase speed, resilience, or flexibility.

---

## 3️⃣ Back-of-the-Envelope Calculations — Abstract

**Given:**

* Model size: `S = 50 GB`
* Hosts: `N = 1,000`
* External bandwidth: `B_ext = 10 GB/s`
* Internal aggregate bandwidth (upload + download): `B_int = 10 GB/s`

### Step 1: Time to retrieve one copy from external source


`50 / 10 = 5 seconds`

[
T_\text{external} = \frac{S}{B_\text{ext}} = \frac{50}{10} = 5~\text{seconds}
]

* External link completes quickly relative to internal distribution.

### Step 2: Time to deliver to all hosts internally

* Each byte transmitted counts toward the **aggregate 10GB/s** (upload + download).

* Effective internal throughput ≈ `B_int / 2 = 5 GB/s` for delivery purposes.

* Total internal traffic = `S × N = 50 × 1,000 = 50,000 GB`

`50000/5 = 10000 seconds = 2.78 hours`

[
T_\text{internal} = \frac{S \cdot N}{B_\text{effective}} = \frac{50{,}000}{5} = 10{,}000~\text{seconds} \approx 2.78~\text{hours}
]

> Internal network dominates total delivery time.

### Step 3: Observations

* **Bottleneck:** Internal network (sum of upload + download).
* **External link** completes much faster than internal distribution.
* **Goal:** Maximize parallelism and overlap to approach this lower bound.

---

### ✅ Summary Table — Abstract + Updated

| Metric                       | Value / Formula                    | Notes                           |
| ---------------------------- | ---------------------------------- | ------------------------------- |
| Model size                   | 50 GB                              | Example                         |
| Hosts                        | 1,000                              | Baseline scenario               |
| External bandwidth           | 10 GB/s                            | Single external source          |
| Internal bandwidth           | 10 GB/s (sum of upload + download) | Effective throughput ≈ 5 GB/s   |
| Time to bring model in       | 5 s                                | External link                   |
| Time to deliver to all hosts | 10,000 s (~2.78 hrs)               | Internal network is bottleneck  |
| Bottleneck                   | Internal network                   | Counts both upload and download |

---

This version is **fully abstract**, ready for interviews:

* **FRs:** What the system achieves.
* **NFRs:** How well it performs.
* **BoE calculations:** Approximate total delivery time, reflecting the sum-of-directions bandwidth constraint.


