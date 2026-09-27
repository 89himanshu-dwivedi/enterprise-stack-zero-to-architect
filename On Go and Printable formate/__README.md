# Enterprise Architecture Thinking: Zero to Architect

Hi, I'm **Himanshu Kumar** — Technical Lead & Solution Architect with **8 years** of experience building enterprise systems.

I work at **CRISIL Ltd, an S&P Global company**, on S&P Global projects.

This is my catch-all learning repo for everything that sits *around the application* — messaging, caching, databases, integrations, infrastructure, cloud, AI/GenAI, and the architecture thinking needed to connect them into production-ready enterprise systems.

---
# 🚀 Engineering Master README — Docker + Git + Kafka + Kubernetes

> **One-Go Master Index:** This README gives the complete learning map for the four core repos:
>
> **Git → Docker → Kafka → Kubernetes**
>
> Keep this page for the **printable big picture**.  
> If you want deep explanations, commands, examples, errors/gotchas, interview questions, and real-world implementation, **go folder-by-folder inside the respective repo**.

---

# 🧭 The Complete Journey

```text
GIT
 ↓
Source Control
 ↓
DOCKER
 ↓
Application Packaging & Containers
 ↓
KAFKA
 ↓
Event Streaming & Distributed Messaging
 ↓
KUBERNETES
 ↓
Container Orchestration & Platform
 ↓
ARCHITECT
 ↓
Security + Scale + Reliability + Observability + GitOps + DR + Cost
```

The four technologies solve different problems but work together in modern engineering.

```text
Developer
   ↓
Git
   ↓
Code
   ↓
Docker
   ↓
Container Image
   ↓
Kafka
   ↓
Events / Integration
   ↓
Kubernetes
   ↓
Run + Scale + Secure + Observe
```

---

# 1️⃣ GIT — Source Control & Engineering Workflow

## Core Purpose

Git manages **source code history, collaboration, branching, merging and release workflows**.

### Learning Areas

- Why Git exists
- Git installation and configuration
- Repository basics
- Working tree
- Staging area
- Commit history
- Three-tree model
- `add`, `commit`, `status`, `diff`, `log`
- Undoing changes
- Branching
- Merging
- Remote repositories
- Push / Pull / Fetch
- Merge conflicts
- Rebase
- Interactive rebase
- Reset / Revert / Restore
- Reflog
- Git rescue / recovery
- Pull Requests
- Branching strategies
- Commit hygiene
- Tags and releases
- History rewriting
- Hooks
- Large repositories
- Git internals
- CI/CD
- GitHub Actions
- Pipelines
- Deployment
- GitOps
- Supply-chain security
- Salesforce development workflow

### Git Mental Model

```text
Working Directory
      ↓
Staging Area
      ↓
Commit
      ↓
Local Repository
      ↓
Remote Repository
      ↓
PR / Review
      ↓
CI/CD
      ↓
Release
```

### Git Interview Progression

```text
🟢 Beginner
Git / Repository / Commit / Branch / Remote

🔵 Developer
Merge / Rebase / Conflict / Reset / Revert

🟡 Senior
Reflog / Interactive Rebase / Branch Strategy / History Recovery

🟠 Lead
PR Strategy / Release / CI/CD / Large Repositories

🔴 Architect
GitOps / Supply Chain / Enterprise Workflow / Governance
```

📁 **Deep study:** Go folder-by-folder through the **Git repo**.

---

# 2️⃣ DOCKER — Containers & Application Packaging

## Core Purpose

Docker packages an application and its dependencies into a **portable container image** and runs it consistently across environments.

### Learning Areas

- Containers vs VMs
- Docker architecture
- Docker Engine
- Docker CLI
- Images
- Containers
- Dockerfile
- Build context
- Image layers
- Container lifecycle
- Ports
- Volumes
- Bind mounts
- Environment variables
- Networks
- Docker Compose
- Multi-container applications
- Registries
- Image tagging
- Image push / pull
- Docker Hub / private registries
- Multi-stage builds
- Image optimization
- Build cache
- `.dockerignore`
- Container security
- Non-root containers
- Resource limits
- Health checks
- Logging
- Container debugging
- Docker networking
- Storage
- Compose environments
- CI/CD image builds
- Image scanning
- Image signing / provenance
- Production container practices

### Docker Mental Model

```text
Application Code
      ↓
Dockerfile
      ↓
docker build
      ↓
Image
      ↓
Registry
      ↓
docker pull
      ↓
Container
      ↓
Network + Volume + Config
```

### Docker Interview Progression

```text
🟢 Beginner
Image / Container / Dockerfile / Port

🔵 Developer
Volumes / Networks / Compose / Environment Variables

🟡 Senior
Layers / Cache / Multi-stage Build / Security / Debugging

🟠 Lead
Production Images / Registry / CI/CD / Resource Management

🔴 Architect
Container Platform / Supply Chain / Runtime Security / Scaling
```

📁 **Deep study:** Go folder-by-folder through the **Docker repo**.

---

# 3️⃣ KAFKA — Event Streaming Platform

## Core Purpose

Kafka is used for **durable, scalable, distributed event streaming**.

### Foundation

- Why Kafka exists
- Event-driven architecture
- Producers
- Consumers
- Brokers
- Topics
- Partitions
- Records
- Offsets
- Consumer Groups
- Replication

### Kafka Architecture

- Broker
- Controller
- KRaft
- Metadata
- Leaders
- Followers
- Replicas
- ISR
- Partition assignment

### Producer

- Producer API
- Batching
- Compression
- Acknowledgements
- Retries
- Idempotence
- Ordering
- Partitioning
- Delivery semantics

### Consumer

- Consumer API
- Consumer Groups
- Rebalancing
- Offsets
- Commits
- Lag
- Replay
- Parallelism
- Assignment strategies

### Reliability

- Replication
- `acks`
- Min ISR
- Idempotent producer
- Transactions
- Exactly-once semantics
- At-least-once
- At-most-once
- Failure recovery

### Kafka Ecosystem

- Kafka Connect
- Debezium
- CDC
- Schema Registry
- Schema evolution
- Outbox Pattern
- Kafka Streams
- Stream processing
- Event time
- Windows
- State stores

### Advanced Kafka

- Log segments
- Indexes
- Retention
- Log compaction
- Tombstones
- Tiered storage
- Quotas
- Throttling
- Security
- TLS
- SASL
- ACLs
- AdminClient
- Observability
- Lag monitoring
- Capacity planning

### Enterprise / Architect

- Multi-AZ
- Rack awareness
- Disaster Recovery
- Multi-cluster
- MirrorMaker 2
- Cross-region architecture
- Event governance
- Schema governance
- Retry / DLT / DLQ patterns
- Backpressure
- Scaling partitions
- Cost optimization
- Production failure scenarios
- Modern consumer-group / share-consumer concepts

### Kafka Mental Model

```text
Producer
   ↓
Topic
   ↓
Partition
   ↓
Broker
   ↓
Replication
   ↓
Consumer Group
   ↓
Consumer
   ↓
Offset
```

### Kafka Interview Progression

```text
🟢 Beginner
Kafka / Topic / Partition / Producer / Consumer

🔵 Developer
Consumer Group / Offset / Rebalance / Lag

🟡 Senior
Replication / ISR / Idempotency / Transactions / Ordering

🟠 Lead
Kafka Connect / CDC / Schema / Streams / DR

🔴 Architect
Multi-Cluster / Multi-Region / Governance / Capacity / Cost / Reliability
```

📁 **Deep study:** Go folder-by-folder through the **Kafka repo**.

---

# 4️⃣ KUBERNETES — Container Orchestration & Platform

## Core Purpose

Kubernetes manages **containerized workloads, networking, storage, scaling, security and reconciliation at cluster scale**.

---

## Kubernetes Foundation

- What Kubernetes is
- Why orchestration is required
- Cluster
- Control Plane
- Worker Node
- Pod
- kubectl
- API Server
- etcd
- Scheduler
- Controllers
- kubelet
- Container Runtime
- kube-proxy

### Core Flow

```text
kubectl
   ↓
API Server
   ↓
Authentication
   ↓
Authorization
   ↓
Admission
   ↓
Validation
   ↓
etcd
   ↓
Controllers / Scheduler
   ↓
Worker Node
   ↓
Pod
```

---

## kubectl & API

- kubectl actions
- kubeconfig
- Contexts
- Namespaces
- API resources
- `kubectl explain`
- YAML / JSON
- Output formats
- Imperative vs declarative
- Dry run

---

## Pods & Workloads

- Pod creation
- Pod YAML
- Pod lifecycle
- Logs
- Exec
- Port-forward
- Environment variables
- Deployment
- ReplicaSet
- StatefulSet
- DaemonSet
- Job
- CronJob
- Rollout
- Rollback

---

## Networking

- CNI
- Pod networking
- Services
- ClusterIP
- NodePort
- LoadBalancer
- Headless Services
- EndpointSlice
- DNS
- CoreDNS
- NetworkPolicy
- Ingress
- Gateway API
- Egress
- kube-proxy
- eBPF

---

## Storage

- Volumes
- PV
- PVC
- StorageClass
- Dynamic Provisioning
- CSI
- Access Modes
- Reclaim Policy
- Volume Expansion
- Snapshots
- Stateful Storage

---

## Configuration

- ConfigMap
- Secret
- Environment Variables
- Mounted configuration
- Secret rotation
- External Secret Managers
- Encryption at rest
- Workload Identity

---

## Scheduling & Resources

- Requests
- Limits
- QoS
- OOMKilled
- Eviction
- nodeSelector
- Node Affinity
- Pod Affinity
- Anti-Affinity
- Taints
- Tolerations
- Topology Spread
- PriorityClass
- Preemption

---

## Health & Reliability

- Startup Probe
- Readiness Probe
- Liveness Probe
- PodDisruptionBudget
- Rolling Updates
- Multi-AZ
- Node Drain
- Graceful Eviction
- Disaster Recovery

---

## Security

- Authentication
- Authorization
- RBAC
- ServiceAccounts
- Pod Security Standards
- securityContext
- runAsNonRoot
- Capabilities
- seccomp
- AppArmor / SELinux
- NetworkPolicy
- Admission Control
- Policy Engines
- Supply-chain security
- Image scanning
- Image signing
- SBOM
- Provenance

---

## Scaling

```text
HPA
 ↓
Scale Pods

VPA
 ↓
Adjust Pod Resources

Node Autoscaler
 ↓
Scale Nodes

KEDA
 ↓
Event-driven Scaling
```

---

## Observability & Troubleshooting

- Logs
- Metrics
- Traces
- Events
- Prometheus
- Metrics Server
- kube-state-metrics
- OpenTelemetry
- Centralized Logging
- DNS troubleshooting
- Service troubleshooting
- Node troubleshooting
- Cluster troubleshooting

Common failures:

```text
Pending
ImagePullBackOff
CrashLoopBackOff
OOMKilled
Node NotReady
DNS failure
Service has no endpoints
Readiness failure
Rollout stuck
Terminating stuck
```

---

## Extensibility & Platform

- CRD
- Custom Resources
- Controllers
- Operators
- Reconciliation
- Finalizers
- OwnerReferences
- Admission Webhooks
- Helm
- Kustomize
- GitOps
- Argo CD
- Flux
- CI/CD
- Platform Engineering

---

## Enterprise Kubernetes

- Managed Kubernetes
- EKS / AKS / GKE
- Self-managed Kubernetes
- OpenShift / Rancher ecosystem
- Multi-cluster
- Multi-region
- Service Mesh
- eBPF
- FinOps
- SLI / SLO / SLA
- Cluster upgrades
- etcd backup / restore
- DR
- Governance
- Tenant isolation
- Capacity planning
- Cost optimization
- When NOT to use Kubernetes

### Kubernetes Mental Model

```text
Desired State
      ↓
API Server
      ↓
Control Plane
      ↓
Scheduler + Controllers
      ↓
Nodes
      ↓
Pods
      ↓
Networking + Storage + Security
      ↓
Observe
      ↓
Reconcile
      ↓
Desired State
```

📁 **Deep study:** Go folder-by-folder through the **Kubernetes repo**.

---

# 🔗 How The Four Fit Together

A realistic application platform can look like:

```text
                 GIT
                  │
                  │ source code
                  ▼
             CI / Build
                  │
                  ▼
               DOCKER
                  │
                  │ container image
                  ▼
             IMAGE REGISTRY
                  │
                  ▼
           KUBERNETES CLUSTER
                  │
       ┌──────────┼──────────┐
       │          │          │
      App        API        Worker
       │          │          │
       └──────────┼──────────┘
                  │
                  ▼
                KAFKA
                  │
        ┌─────────┼─────────┐
        ▼         ▼         ▼
     Service   Consumer   CDC
```

### Example production flow

```text
Developer
   ↓
Git Commit
   ↓
Pull Request
   ↓
CI
   ↓
Docker Build
   ↓
Image Scan
   ↓
Registry
   ↓
GitOps / Deployment
   ↓
Kubernetes
   ↓
Application Pods
   ↓
Kafka Events
   ↓
Consumers / Services
   ↓
Database / External Systems
```

---

# 🧠 The Complete Skill Stack

```text
                    ARCHITECT
                       │
        ┌──────────────┼──────────────┐
        │              │              │
     Reliability     Security       Scale
        │              │              │
        └──────────────┼──────────────┘
                       │
              Kubernetes Platform
                       │
             ┌─────────┴─────────┐
             │                   │
           Kafka              Docker
             │                   │
             └─────────┬─────────┘
                       │
                      Git
```

---

# 🎯 Recommended Learning Order

```text
01. Git
      ↓
02. Docker
      ↓
03. Kubernetes Foundation
      ↓
04. Kubernetes Networking / Storage / Security
      ↓
05. Kafka Foundation
      ↓
06. Kafka Reliability / Streams / Connect
      ↓
07. Git + Docker + Kafka + Kubernetes Integration
      ↓
08. CI/CD + GitOps
      ↓
09. Observability + Security
      ↓
10. Multi-Cluster / DR / Cost / Platform Engineering
      ↓
11. Architect-Level Design
```

---

# 🏆 Final Interview Ladder

## 🟢 Beginner

```text
Git
Docker
Container
Image
Kubernetes
Pod
Node
Service
Kafka
Topic
Producer
Consumer
```

## 🔵 Developer

```text
Git Branching / Merge / Rebase
Dockerfile / Compose / Volumes
Deployment / ConfigMap / Secret
Kafka Consumer Groups / Offsets / Lag
Kubernetes Services / Probes / PVC
```

## 🟡 Senior

```text
Git Recovery / CI/CD
Docker Layers / Security / Optimization
Kafka Replication / ISR / Transactions
Kubernetes Scheduling / CNI / RBAC / HPA
Production Troubleshooting
```

## 🟠 Lead

```text
Enterprise Git Workflow
Container Platform
Kafka CDC / Connect / Streams / DR
Kubernetes HA / Multi-AZ / Observability
Security / Upgrades / Governance
```

## 🔴 Architect

```text
GitOps
Supply-Chain Security
Container Platform Architecture
Kafka Multi-Cluster / Multi-Region
Kubernetes Multi-Cluster / Multi-Region
Platform Engineering
FinOps
SLO / SLA
DR / RTO / RPO
Governance
Enterprise Scale
When NOT to use a technology
```

---

# 📚 Repo Usage Rule

## One-Go Reading

Use:

```text
README.md
```

to understand:

- What the technology does
- Why it exists
- Major concepts
- How it connects with the other technologies
- Complete learning progression
- Interview progression

## Deep Learning

When a topic needs detail:

```text
Git Repo
  → Open required folder/module

Docker Repo
  → Open required folder/module

Kafka Repo
  → Open required folder/module

Kubernetes Repo
  → Open required folder/module
```

The detailed folders are the **source of truth for deep study**.

---

# 🧩 Final Mental Model

```text
                 ┌─────────────┐
                 │     GIT     │
                 │ Source Code │
                 └──────┬──────┘
                        │
                        ▼
                 ┌─────────────┐
                 │   DOCKER    │
                 │ Containers  │
                 └──────┬──────┘
                        │
             ┌──────────┴──────────┐
             ▼                     ▼
      ┌─────────────┐       ┌─────────────┐
      │   KAFKA     │       │ KUBERNETES  │
      │ Event Stream │       │ Orchestration│
      └─────────────┘       └──────┬──────┘
                                   │
                                   ▼
                         ┌──────────────────┐
                         │ Production       │
                         │ Platform         │
                         └────────┬─────────┘
                                  │
             ┌────────────────────┼────────────────────┐
             ▼                    ▼                    ▼
          Security            Reliability            Scale
             │                    │                    │
             └────────────────────┼────────────────────┘
                                  ▼
                              ARCHITECT
```

> **Git controls the code. Docker packages the application. Kafka moves events. Kubernetes runs and orchestrates the platform.**
>
> For the complete detail, **go folder-by-folder in each repo**.
