# UCC-Bridge

**Federated Data Profiling and Unique Column Combination Discovery**  
**Decentralized Execution with Central Coordination**

---

## Overview

**UCC-Bridge** is a research-oriented implementation for **federated Unique Column Combination (UCC) discovery**. The system investigates whether minimal UCC enumeration can be executed efficiently in a **distributed setting** where raw tabular data remains local to client nodes and only derived results are transmitted to a coordinating server. The project is motivated by the need to support **privacy-preserving data profiling** in environments where data centralization is undesirable or impossible.

The system follows a **client–server architecture**:
- each **client** performs local UCC discovery over its own partition,
- the **server** collects local UCC outputs,
- a final **aggregation stage** derives global UCC candidates without requiring access to raw tuples.

This repository operationalizes the federated workflow described in the accompanying project report, which evaluates both the **distributed method** and a **centralized baseline** for comparison. According to the report, the project studies a federated execution model for minimal UCC discovery and shows that local discovery plus server-side aggregation can reduce runtime on complex datasets compared with a centralized baseline. fileciteturn2file0L1-L20

---

## Research Motivation

A **Unique Column Combination (UCC)** is a set of columns that uniquely identifies each row in a relational table. UCC discovery is a core task in data profiling, schema analysis, and data quality assessment. In this project, UCC discovery is framed as a **minimal hitting set enumeration problem**, where:
- **vertices** correspond to table attributes,
- **hyperedges** correspond to difference sets between rows,
- **minimal hitting sets** correspond to minimal UCCs.

The project builds on the hitting-set enumeration perspective and the decision-tree enumeration method described by Bläsius, Friedrich, Lischeid, Meeks, and Schirneck. The report explains the mapping from difference sets to hypergraphs and positions minimal UCC discovery as a minimal hitting-set problem solved via recursive enumeration with pruning. fileciteturn2file0L21-L49 fileciteturn2file0L50-L79

The central research question is straightforward:

> Can minimal UCC discovery be distributed across multiple federated nodes while preserving local data confidentiality and still remain computationally efficient?

---

## Objectives

This repository is designed to support the following goals:

1. **Execute UCC discovery locally** on multiple distributed nodes.
2. **Prevent raw data centralization** by keeping original tuples on each client.
3. **Transmit only derived artifacts** such as local UCC sets to the coordinator.
4. **Aggregate local UCC outputs** into a global result.
5. **Compare federated execution** against a centralized baseline.
6. **Evaluate aggregation strategies** with respect to correctness, scalability, and runtime cost.

The report explicitly states that the system keeps raw tuples local, shares only UCC results as column-index combinations, and compares the federated pipeline with a centralized baseline implemented using the same generate–enumerate workflow. fileciteturn2file0L80-L103

---

## Core Contributions

This repository contributes a practical bridge between **theoretical hitting-set enumeration** and **federated execution**:

- a **containerized local pipeline** for reproducible UCC discovery,
- a **TCP-based client–server communication model**,
- a **server-side aggregation layer** for deriving global UCCs,
- an **experimental comparison** between centralized and distributed execution,
- an analysis of **intersection-based** and **union-based** aggregation strategies.

The methodology section of the report confirms that the implementation uses an existing C++ hitting-set enumeration toolchain, executes the generate and enumerate stages inside Docker, and extends it with a server–client architecture for federated UCC discovery. fileciteturn2file0L104-L126

---

## System Architecture

The overall system consists of three major layers.

### 1. Local Discovery Layer (Clients)
Each client holds one dataset partition and independently runs the local UCC discovery pipeline. The local workflow:
1. reads the local CSV partition,
2. generates a graph representation required by the enumeration tool,
3. enumerates minimal UCCs,
4. sends the resulting column-index combinations to the coordinator.

The report describes that each client processes one partition file, executes the same two-stage containerized workflow, and sends only derived UCC lines to the server. fileciteturn2file0L127-L149

### 2. Coordination Layer (Server)
The server listens for incoming client connections, stores each client’s uploaded UCC result stream, and orchestrates the global aggregation stage. Client outputs are persisted separately to preserve traceability and enable concurrent handling of multiple senders. fileciteturn2file0L150-L167

### 3. Aggregation Layer
Once local UCC outputs have been collected, the server computes global UCC candidates using one of two strategies:
- **Intersection-based aggregation**: keeps only UCCs common to all client outputs.
- **Union-based minimalization (MinUnion)**: forms unions across local outputs and filters to subset-minimal results.

The report presents both strategies and notes that intersection was the practical choice in the evaluated setup because it was stable and inexpensive, whereas MinUnion could become substantially more expensive. fileciteturn2file0L168-L188 fileciteturn2file0L189-L238

---

## Algorithmic Foundation

The project is grounded in **decision-tree-based minimal hitting set enumeration** with an **extension oracle** for pruning. The enumeration recursively explores candidate sets while discarding branches that cannot yield minimal solutions. The report outlines the recursive decision-tree procedure, including initialization, oracle-based pruning, candidate expansion, minimality checks, and recursive continuation until all minimal hitting sets are found. fileciteturn2file0L50-L79

In the context of databases:
- local hypergraphs encode local non-uniqueness evidence,
- local minimal hitting sets become local UCCs,
- the coordinator aggregates these local UCC outputs into global candidates.

This formulation makes the project relevant for **privacy-aware schema inference**, **distributed profiling**, and **federated metadata discovery**.

---

## Methodology

The implementation is based on a pre-existing **C++ enumeration pipeline** and operationalizes it inside Docker for reproducibility. The methodological workflow is:

1. split a dataset into multiple partitions,
2. assign each partition to a client,
3. run local graph generation and enumeration on each client,
4. send local UCC results to the server,
5. persist per-client outputs on the server,
6. aggregate local outputs into a global UCC list,
7. compare the result and runtime with a centralized baseline.

The report explicitly states that the generate–enumerate pipeline is executed inside an `enumhyp-ubuntu` Docker environment, local results are transmitted over TCP, and the server stores them in per-client files before aggregation. fileciteturn2file0L104-L126 fileciteturn2file0L127-L167

---

## Aggregation Strategies

### Intersection-Based Aggregation
This strategy computes:

\[
U_{global} = U_1 \cap U_2 \cap \cdots \cap U_n
\]

It is simple, fast, and effective when local outputs overlap substantially. The report emphasizes that this method is very inexpensive and served as the **primary aggregation method** in the experiments. fileciteturn2file0L189-L214

**Advantages**
- very low runtime,
- easy to implement,
- stable under normalized local output formatting,
- practical for identical-schema partitions of the same source dataset.

**Limitations**
- strict consensus requirement,
- may miss valid global UCCs that do not appear identically in every partition,
- may return few or even zero UCCs in highly fragmented settings.

### Union-Based Minimalization (MinUnion)
This strategy computes unions across one selected local UCC from each node and then keeps only subset-minimal candidates:

\[
U_{global} = \min\{T_1 \cup T_2 \cup \cdots \cup T_n \mid T_i \in U_i\}
\]

The report notes that this method reflects an exact union-of-constraints perspective but can become computationally expensive because the number of candidate unions grows rapidly. fileciteturn2file0L215-L238 fileciteturn2file0L239-L260

**Advantages**
- more expressive than plain intersection,
- can recover combined candidates not present verbatim in every local output.

**Limitations**
- combinatorial growth,
- expensive subset-minimal filtering,
- poor scalability for larger client counts or larger local UCC lists.

---

## Experimental Findings

The report evaluates the federated pipeline on three datasets and compares distributed execution against a centralized baseline. The main observations are:

- for **small datasets**, federated execution can be slightly slower due to fixed overheads,
- for **larger datasets**, federated execution can substantially reduce total runtime,
- **intersection aggregation** remains negligible in runtime cost,
- **union aggregation** can become a major bottleneck.

The report specifically states that the distributed runtime was slightly worse for a small `abalone` instance, but notably better for more demanding datasets such as `fdReduced` and `flight`. It also reports that MinUnion produced an outlier runtime of about **16.99 minutes** on `flight` with `n = 20` clients, while intersection completed in milliseconds. fileciteturn2file0L261-L300 fileciteturn2file0L301-L335

The conclusion of the report is clear: a federated UCC discovery pipeline can produce substantial runtime improvements on larger datasets when the aggregation step remains lightweight. fileciteturn2file0L336-L359

---

## Repository Scope

This repository is intended for:
- research prototypes in **federated data profiling**,
- experimental evaluation of **distributed UCC discovery**,
- systems research in **privacy-aware metadata extraction**,
- educational and reproducible implementations of **hitting-set-based profiling algorithms**.

It is not merely an engineering repository; it should be read as a **research artifact** that combines algorithmic foundations, systems design, and empirical analysis.

---

## Expected Repository Structure

The exact file layout may vary across revisions, but a professional organization for this repository should follow a structure similar to the following:

```text
ucc-bridge/
├── README.md
├── LICENSE
├── docker/
│   └── enumhyp-ubuntu/
├── data/
│   ├── raw/
│   ├── partitions/
│   └── generated/
├── server/
│   ├── coordinator/
│   ├── aggregation/
│   └── outputs/
├── client/
│   ├── local_runner/
│   ├── transport/
│   └── outputs/
├── scripts/
│   ├── partition_data.*
│   ├── run_client.*
│   ├── run_server.*
│   ├── run_centralized_baseline.*
│   └── evaluate_results.*
├── results/
│   ├── centralized/
│   ├── distributed/
│   └── plots/
├── docs/
│   ├── architecture/
│   ├── methodology/
│   └── report/
└── references/
```

If your actual repository structure differs, this README should be adjusted to match the concrete file and directory names.

---

## Prerequisites

The implementation described in the report assumes the following environment:

- **Docker** for isolated execution of the enumeration pipeline,
- a **C++-based enumeration backend**,
- a **TCP/IP-capable client–server environment**,
- partitioned CSV datasets with a **shared schema** across clients,
- sufficient local permissions to mount input/output directories into containers.

A typical software stack may include:
- Docker Engine
- GNU/Linux or WSL-compatible environment
- C++ toolchain if components are compiled locally
- Python or shell scripting for orchestration

---

## Data Assumptions

This project assumes that:

1. all client datasets are **partitions of one logical dataset**,
2. all partitions share the **same schema**,
3. clients are allowed to share **derived UCC outputs**,
4. clients are **not allowed to share raw tuples**.

These assumptions are essential for the validity of the aggregation strategies, especially the union-based formulation, which is meaningful only under a shared-schema setting. The report states this assumption explicitly in its discussion of aggregation. fileciteturn2file0L215-L238

---

## Running the System

Because the exact command-line interface depends on the current implementation in this repository, the workflow is best understood as a sequence of reproducible stages.

### 1. Prepare the Data
- place the original dataset in the appropriate input directory,
- partition the dataset into `n` client-specific CSV files,
- ensure all partitions preserve the same attribute order and schema.

### 2. Start the Server
- launch the coordinator,
- bind to the configured host and port,
- prepare output directories for per-client result collection.

### 3. Run the Clients
Each client should:
- read its local partition,
- execute the containerized generate step,
- execute the containerized enumerate step,
- open a TCP connection to the server,
- transmit the local UCC output.

### 4. Trigger Aggregation
After all clients finish transmission, the server should:
- finalize the collected local UCC files,
- run the selected aggregation method,
- write the resulting global UCC list,
- optionally generate logs and timing summaries.

### 5. Run the Centralized Baseline
To compare the federated system with a non-federated reference:
- execute the same generate–enumerate workflow on the full dataset,
- store the centralized UCC output,
- compare both runtime and discovered UCCs.

---

## Reproducibility Guidance

For a repository intended to support research claims, reproducibility is non-negotiable. The following practices are recommended:

- pin container and dependency versions,
- document all dataset preprocessing steps,
- preserve raw logs and final outputs,
- separate distributed and centralized experiment outputs,
- keep aggregation scripts deterministic,
- include timing scripts and plotting scripts used in the report,
- clearly document normalization of UCC output formatting.

These practices are particularly important because the report highlights traceability of per-client outputs and the sensitivity of intersection-based aggregation to formatting differences unless local UCCs are normalized. fileciteturn2file0L189-L214

---

## Limitations

This repository should be interpreted with the same limitations discussed in the report:

- fixed orchestration overhead can dominate on small datasets,
- intersection aggregation is efficient but not universally complete,
- union-based aggregation can become prohibitively expensive,
- scalability depends not only on the number of clients but also on the size and overlap of local UCC lists,
- correctness depends on consistent schema alignment and normalized local output representation.

The report explicitly concludes that small datasets may suffer from container and communication overhead, while union-based aggregation may become unsuitable for larger federated deployments. fileciteturn2file0L336-L359

---

## Potential Extensions

Future revisions of this repository could be strengthened through:

- stronger privacy guarantees beyond “derived-output-only” sharing,
- secure communication channels between clients and server,
- formal validation of aggregation correctness under broader partitioning models,
- asynchronous or fault-tolerant client coordination,
- benchmark automation across more datasets,
- richer result visualizations and experiment metadata tracking,
- integration with federated orchestration frameworks.

---

## Citation

If you use this repository in academic work, cite both the underlying hitting-set enumeration literature and the accompanying project report.

### Project Report
**Muhammad Raza Ali.** *Federated Data Profiling and Unique Column Combination Discovery: Decentralized Execution with Central Coordination.* University of Vienna, Praktikum Informatik II, 2 February 2026. fileciteturn2file0L1-L10

### Foundational Reference
The report cites the following core work as the algorithmic basis:

**T. Bläsius, T. Friedrich, J. Lischeid, K. Meeks, and M. Schirneck.**  
*Efficiently Enumerating Hitting Sets of Hypergraphs Arising in Data Profiling.* Journal of Computer and System Sciences, 2022. fileciteturn2file0L360-L368

---

## Acknowledgements

This project acknowledges the academic supervision and technical foundations described in the report, including the guidance of **Dr. Atakan Aral** and the supporting work of **Dr. Martin Schirneck** and **Julius Lischeid**. fileciteturn2file0L350-L372

---

## Final Note

This README is intentionally written at a **research-artifact standard** rather than a minimal GitHub-project standard. It is meant to communicate not only *how to run the system*, but also *why the system exists*, *what assumptions it makes*, and *how its claims should be interpreted*.

For a final production version, the next step should be to align this README precisely with the **actual repository structure, command-line entry points, script names, environment variables, and output paths** of `ucc-bridge`.
