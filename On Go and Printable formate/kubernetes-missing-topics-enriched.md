# Kubernetes — Missing Topics Add-On (Enriched Edition)
## Zero → Architect | Source Roadmap + Important Gaps

> This add-on is based on the uploaded Kubernetes roadmap. The supplied roadmap currently covers foundations, architecture, lab/setup, kubectl, API resources/output/declarative concepts, and Pods through environment variables.
>
> The goal here is **not to replace those modules**. These are the important topics needed so the track reaches Developer → Senior → Lead → Architect depth.

**What's new in this enriched edition:** every module (19–61) now has a **🧪 Try It Yourself** hands-on lab or design exercise, a **💡 Extra Insight** that goes one layer deeper than the standard explanation, and a **🩹 Common Error & Fix** for the mistake people hit first. A new **Glossary**, **Common Error Messages Reference**, and **Command Reference by Task** sit at the end, along with two additional real-world scenarios.

---

# 🔴 BIGGEST MISSING AREAS

The current source list is heavily focused on: Kubernetes fundamentals, control plane / worker node, kubectl, Pods, basic lab.

The major next layers should be:

```text
PODS → WORKLOADS → SERVICES → NETWORKING → STORAGE → CONFIG / SECRETS
 → SCHEDULING → HEALTH / RESILIENCE → SECURITY → AUTOSCALING
 → OBSERVABILITY → PACKAGING / GITOPS → OPERATORS / CRDs
 → PRODUCTION / DR → ARCHITECTURE
```

Kubernetes itself organizes the platform around workloads, services/networking, storage, configuration, security, policies, scheduling/resource management, administration and extensibility.

---

# 19 — WORKLOAD CONTROLLERS

## 🟢 Simple

A Pod is usually not what you manage directly in production. You normally use a controller:

```text
Deployment → ReplicaSet → Pods
```

## Must know

Deployment, ReplicaSet, StatefulSet, DaemonSet, Job, CronJob.

## Deployment

Best fit: stateless application. Know: replicas, rolling update, rollout history, rollback, `maxSurge`, `maxUnavailable`, revision.

## StatefulSet

For workloads needing stable identity/storage:

```text
pod-0
pod-1
pod-2
```

Know: stable network identity, stable ordinal identity, persistent storage, ordered startup/shutdown behavior, headless Service.

## DaemonSet

One Pod per eligible node. Use cases: log agents, node monitoring, security agents, networking components.

## Job

Run-to-completion workload.

## CronJob

Scheduled Job.

## Architect gotcha

Do not say: "StatefulSet automatically makes your database highly available." Stateful identity/storage is not the same thing as database replication/HA.

Kubernetes documents Deployment/ReplicaSet as the common stateless workload path and separately supports stateful and batch workload controllers.

### 🧪 Try It Yourself

```bash
kubectl create deployment demo --image=nginx --replicas=3
kubectl get rs
kubectl get pods -o wide
```

Now delete the Deployment's ReplicaSet directly (not the Deployment):

```bash
kubectl delete rs -l app=demo
```

Watch `kubectl get rs,pods` immediately after — the Deployment controller notices the missing ReplicaSet and recreates it within seconds. This is reconciliation (Module 51) made visible one level early.

### 💡 Extra Insight

A StatefulSet's Pods keep their ordinal identity (`pod-0`, `pod-1`, `pod-2`) even across restarts and rescheduling — but that identity is just a *name and stable network endpoint*, not automatic data replication between them. Whether `pod-0` and `pod-1` actually have consistent, replicated data is entirely up to the application running inside them (e.g., a database's own replication protocol). This is the exact gap the architect gotcha above is warning about.

### 🩹 Common Error & Fix

Symptom: "I scaled my StatefulSet down and back up, and `pod-1` came back with no data." Expected behavior if the underlying PVC was deleted or not retained — StatefulSets by default keep PVCs around per ordinal, but check your `persistentVolumeClaimRetentionPolicy` and reclaim policy before assuming data survives scale-down automatically.

### 🎤 Interview

**🟢:** Deployment kya karta hai?
**🔵:** StatefulSet kab use karoge?
**🟡:** StatefulSet HA guarantee deta hai kya?
**🔴:** Ek stateful database workload ko Kubernetes par design karo — kya StatefulSet akela sufficient hai?

---

# 20 — DEPLOYMENT STRATEGIES

```text
Rolling:    Old → Old → New → New
Recreate:   Old → stop → New
Canary:     95% old, 5% new
Blue/Green: Blue = current, Green = new, switch traffic
A/B:        Traffic split based on defined routing/experiment rules
```

## Interview

**Q: Kubernetes Deployment gives canary automatically?** Not as a universal built-in traffic-management solution. Kubernetes provides primitives; advanced canary/traffic splitting often uses Services, Gateway/Ingress implementations or progressive-delivery tooling.

### 🧪 Try It Yourself

Simulate a manual canary using two Deployments behind one Service:

```bash
kubectl create deployment app-stable --image=myapp:v1 --replicas=9
kubectl create deployment app-canary --image=myapp:v2 --replicas=1
kubectl label deployment app-stable app-canary track=app --overwrite
```

Both Deployments' Pods carry the same `app: myapp` label (set via their Pod template) so a single Service selecting `app: myapp` load-balances roughly 90/10 across them — a real, if crude, canary using nothing but Kubernetes primitives, before reaching for any progressive-delivery tooling.

### 💡 Extra Insight

The "10% canary" achieved by replica-count ratio above is only a **statistical approximation** — Kubernetes' Service load-balancing doesn't guarantee an exact percentage split per request, especially with few replicas and connection reuse. For real percentage-accurate traffic splitting (and automated rollback on error-rate metrics), you need a Gateway API implementation or a progressive-delivery tool (Flagger, Argo Rollouts), not raw replica ratios.

### 🩹 Common Error & Fix

Symptom: "our canary got 40% of traffic instead of the intended 10%." Check replica counts and Service selector labels — a mismatched or overlapping selector, or simply too few total replicas for the ratio to hold statistically, is the usual cause with this manual approach.

### 🎤 Interview

**🟢:** Rolling vs Recreate?
**🔵:** Canary kaise achieve karoge basic Kubernetes primitives se?
**🟡:** Manual replica-ratio canary ki limitation kya hai?
**🔴:** Automated metric-based rollback ke saath progressive delivery design karo.

---

# 21 — SERVICE & SERVICE DISCOVERY

A Pod IP is ephemeral. Service gives a stable abstraction:

```text
Client → Service → Pod Pod Pod
```

Know: ClusterIP, NodePort, LoadBalancer, ExternalName, headless Service, selectors, EndpointSlice, service DNS, ports vs targetPort.

Kubernetes Services provide stable network access to changing backend Pods, and EndpointSlices track the current endpoints.

## Critical gotcha

```text
Service selector must match Pod labels
```

If labels don't match: Service exists but Endpoints = empty.

### 🧪 Try It Yourself

```bash
kubectl expose deployment demo --port=80 --target-port=80
kubectl get endpointslices -l kubernetes.io/service-name=demo
```

Now deliberately break it:

```bash
kubectl patch service demo -p '{"spec":{"selector":{"app":"wrong-label"}}}'
kubectl get endpointslices -l kubernetes.io/service-name=demo
```

Watch the EndpointSlice go empty immediately — this is the exact failure mode the gotcha describes, reproduced on purpose so you recognize it instantly in a real incident.

### 💡 Extra Insight

EndpointSlices replaced the older single `Endpoints` object specifically to scale better for Services with very large numbers of backend Pods (an old single `Endpoints` object listing thousands of IPs became a genuine performance/size problem) — if you're troubleshooting on an older cluster or older docs, you may still see references to plain `Endpoints`; the underlying "selector must match Pod labels" gotcha is identical either way.

### 🩹 Common Error & Fix

```text
Service "demo" has no endpoints
```
(seen as connection refused/timeout from a client) — almost always a selector/label mismatch; `kubectl get endpointslices` and `kubectl get pods --show-labels` side by side is the fastest diagnosis.

### 🎤 Interview

**🟢:** Service kya solve karta hai?
**🔵:** ClusterIP vs NodePort vs LoadBalancer?
**🟡:** EndpointSlice kyun introduce hua Endpoints ke upar?
**🔴:** Zero-endpoint incident ka full diagnosis flow design karo.

---

# 22 — KUBERNETES NETWORKING

A huge missing block if you want Architect level.

```text
Pod → Pod IP → CNI → Cluster network
```

Kubernetes expects each Pod to have its own cluster-wide IP and the network implementation to provide Pod-to-Pod connectivity.

## Must know

CNI, Pod network, Service network, kube-proxy, CoreDNS, ClusterIP, NodePort, LoadBalancer, NetworkPolicy, DNS, EndpointSlice, dual-stack, egress, ingress.

## CNI

```text
Pod created → CNI setup → network namespace → Pod IP → routes/interfaces
```

Recognize: Cilium, Calico, Flannel, cloud-provider CNI implementations. Don't memorize commands before understanding the network model.

### 🧪 Try It Yourself

```bash
kubectl run test1 --image=busybox --command -- sleep 3600
kubectl run test2 --image=busybox --command -- sleep 3600
kubectl get pods -o wide   # note each Pod's IP

kubectl exec test1 -- ping -c 3 <test2-pod-ip>
```

Confirm two Pods on potentially *different nodes* can reach each other directly by IP — this flat, no-NAT-required Pod-to-Pod connectivity is the core Kubernetes networking contract every CNI must satisfy, made concrete.

### 💡 Extra Insight

"Every Pod gets its own IP and can reach every other Pod's IP without NAT" is deceptively simple to state but genuinely hard to implement well at scale — it's *why* CNI exists as a pluggable layer instead of one hardcoded Kubernetes networking implementation, since different environments (bare metal, various clouds, different performance/security needs) implement this contract very differently under the hood (overlay networks, BGP routing, eBPF datapaths).

### 🩹 Common Error & Fix

Symptom: Pods on the same node can reach each other, but Pods on different nodes cannot. This points at a CNI cross-node routing problem (not a Kubernetes API issue) — check the CNI's own logs/status (e.g., `calicoctl`/Cilium status commands) and node-to-node network connectivity at the infrastructure level.

### 🎤 Interview

**🟢:** CNI kya karta hai?
**🔵:** Pod IP node ke across kaise reachable hota hai?
**🟡:** Different CNIs (overlay vs BGP vs eBPF) mein conceptual difference kya hai?
**🔴:** Cross-node Pod connectivity failure ka full diagnosis design karo.

---

# 23 — NETWORKPOLICY

Essential for production security. Without segmentation, any Pod can reach any other Pod/DB. With policy:

```text
Frontend → Backend
Backend → DB
Frontend -X→ DB
```

Know: ingress, egress, `podSelector`, `namespaceSelector`, `ipBlock`, default-deny, DNS allowance, CNI support.

## 🔥 Gotcha

A NetworkPolicy object existing in the API does not guarantee enforcement if the cluster networking implementation doesn't implement NetworkPolicy.

### 🧪 Try It Yourself

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: default-deny
spec:
  podSelector: {}
  policyTypes: ["Ingress"]
```

Apply this default-deny policy in a namespace, then try to `curl` between two Pods that worked fine before — confirm it now fails. Then add a targeted allow policy for just frontend→backend and confirm frontend→backend works while frontend→DB still doesn't.

### 💡 Extra Insight

A default-deny-all NetworkPolicy also silently blocks DNS resolution (queries to CoreDNS) unless you explicitly allow egress to the `kube-system` namespace on port 53 — a very common "why did everything break, not just the traffic I meant to restrict" gotcha the very first time someone applies default-deny in a real cluster.

### 🩹 Common Error & Fix

Symptom: applied a NetworkPolicy, nothing changed at all — traffic that should be blocked still flows. Check whether the cluster's CNI actually implements NetworkPolicy (some minimal/default CNIs historically didn't) — the policy object being accepted by the API server does not by itself prove enforcement.

### 🎤 Interview

**🟢:** NetworkPolicy kya solve karta hai?
**🔵:** Default-deny apply karne ke baad DNS kyun break ho sakta hai?
**🟡:** NetworkPolicy object accept hone ka matlab enforcement guarantee hai kya?
**🔴:** Multi-tier app (frontend/backend/DB) ke liye least-privilege NetworkPolicy set design karo.

---

# 24 — INGRESS + GATEWAY API

## Ingress

Know: host routing, path routing, TLS, IngressClass, controller, load balancing.

## Current scene

Kubernetes documentation says the Ingress API is frozen and recommends **Gateway API** for new development.

## Gateway API

```text
GatewayClass → Gateway → HTTPRoute → Backend
```

```text
Internet → Gateway → HTTPRoute → Service → Pods
```

Gateway API is designed for more expressive, role-oriented traffic management.

## Architect question

**Ingress vs Gateway API?** Ingress is stable but frozen; Gateway API is the newer extensible traffic-routing model.

### 🧪 Try It Yourself

Write the same routing rule two ways (conceptually, since exact controller availability varies by cluster) — an Ingress resource routing `/api` to `backend-service`, and the Gateway API equivalent (`Gateway` + `HTTPRoute`). Compare how much more explicit the Gateway API version is about *who* owns which piece (infra team owns the `Gateway`, app team owns the `HTTPRoute`) — that role separation is the core design difference, not just syntax.

### 💡 Extra Insight

Gateway API's role-oriented split (infra-team-owned `Gateway`/`GatewayClass` vs. app-team-owned `HTTPRoute`) directly addresses a real organizational pain point with classic Ingress: a single Ingress resource often mixed infrastructure concerns (TLS certs, load balancer config) with application routing concerns (which path goes where) in one object, owned by whoever happened to write the YAML — creating unclear ownership boundaries at scale.

### 🩹 Common Error & Fix

Symptom: "our Ingress annotations work on one cloud provider's Ingress controller but not another's." Ingress relies heavily on **controller-specific annotations** for anything beyond basic host/path routing — this portability gap across controllers is one of the concrete reasons Gateway API (with a more standardized, extensible model) was introduced.

### 🎤 Interview

**🟢:** Ingress kya karta hai?
**🔵:** Gateway API ka role split Ingress se kaise alag hai?
**🟡:** Ingress annotations portability problem kya hai?
**🔴:** New platform ke liye Ingress vs Gateway API decision framework banao.

---

# 25 — DNS / COREDNS

```text
frontend.default.svc.cluster.local
```

Know: Service DNS, Pod DNS, namespace resolution, search domains, CoreDNS, DNS debugging, headless Service DNS.

```text
frontend → DNS → backend.default.svc → ClusterIP / endpoints
```

## Gotcha

`backend` may resolve differently than `backend.production.svc` depending on which namespace the calling Pod runs in.

### 🧪 Try It Yourself

```bash
kubectl run dnsutils --image=tutum/dnsutils --command -- sleep 3600
kubectl exec dnsutils -- nslookup demo.default.svc.cluster.local
kubectl exec dnsutils -- cat /etc/resolv.conf
```

Look at `/etc/resolv.conf`'s `search` entries — this is exactly what lets a Pod resolve a bare `demo` (no namespace/suffix) to the right fully-qualified name, and exactly why that shortcut resolves *differently* depending on which namespace the calling Pod is in.

### 💡 Extra Insight

CoreDNS runs as a regular Kubernetes Deployment (usually in `kube-system`) — meaning DNS itself is subject to the same possible failure modes as any other workload (CrashLoopBackOff, resource limits, node scheduling issues). "Everything in the cluster suddenly can't resolve anything" is a real incident category, and the fix is treating CoreDNS's own Pods as a first-class thing to check (`kubectl get pods -n kube-system -l k8s-app=kube-dns`), not assuming DNS is an invisible platform layer that can't break.

### 🩹 Common Error & Fix

```text
nslookup: can't resolve 'backend.svc.cluster.local' — NXDOMAIN
```
Usually a wrong/missing namespace in the name (should be `backend.<namespace>.svc.cluster.local`), or the target Service genuinely doesn't exist yet — verify with `kubectl get svc -A | grep backend` before assuming DNS itself is broken.

### 🎤 Interview

**🟢:** Service DNS format kya hai?
**🔵:** Bare service name `backend` kaise resolve hota hai?
**🟡:** CoreDNS khud crash ho sakta hai kya?
**🔴:** Cluster-wide DNS outage ka incident-response runbook design karo.

---

# 26 — STORAGE

Major production gap.

```text
Volume → PersistentVolume → PersistentVolumeClaim → StorageClass → CSI → Storage backend
```

## Must know

`emptyDir`, `hostPath`, PV, PVC, StorageClass, dynamic provisioning, CSI, access modes, reclaim policy, volume expansion, snapshots, ephemeral volumes.

## Access modes

ReadWriteOnce, ReadOnlyMany, ReadWriteMany, newer access semantics/features where supported.

## Critical gotcha

A PVC is a **request/claim**, not the storage itself.

### 🧪 Try It Yourself

```bash
kubectl get storageclass
```

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: demo-pvc
spec:
  accessModes: ["ReadWriteOnce"]
  resources:
    requests:
      storage: 1Gi
```

```bash
kubectl apply -f demo-pvc.yaml
kubectl get pvc demo-pvc
kubectl get pv
```

Watch the PVC go from `Pending` to `Bound`, and notice a *new* PV object gets automatically created (dynamic provisioning) — the PVC was purely a request; the PV is the actual storage-backed object that satisfies it.

### 💡 Extra Insight

`ReadWriteOnce` is commonly (mis)understood as "only one Pod can use it" — more precisely (and depending on Kubernetes version/feature availability) it traditionally meant "mountable read-write by a single **node**," which multiple Pods *on that same node* could actually share. Access mode semantics have evolved across Kubernetes versions, so always check current documentation for your cluster's version rather than relying on an old mental model.

### 🩹 Common Error & Fix

```text
PersistentVolumeClaim is not bound: "demo-pvc"
```
Either no StorageClass supports dynamic provisioning for the requested access mode/size, or no matching pre-provisioned PV exists — check `kubectl get storageclass` and `kubectl describe pvc demo-pvc` for the exact reason in the events section.

### 🎤 Interview

**🟢:** PV vs PVC?
**🔵:** Dynamic provisioning kya hai?
**🟡:** ReadWriteOnce ka exact meaning kya hai?
**🔴:** Stateful workload ke liye storage architecture design karo including reclaim policy aur snapshots.

---

# 27 — CSI & STATEFUL ARCHITECTURE

CSI = Container Storage Interface.

```text
Kubernetes → CSI driver → Cloud / SAN / NAS / storage system
```

Know: attach, mount, provision, resize, snapshot, topology awareness.

## Architect question

**Why CSI?** It allows storage functionality to be integrated through a standard plugin interface rather than baking every storage vendor into Kubernetes core.

### 🧪 Try It Yourself

```bash
kubectl get csidrivers
kubectl get csinodes
```

Inspect what CSI driver(s) your cluster actually has registered, and which nodes each one is available on — `csinodes` reveals **topology constraints** in practice (e.g., an EBS-backed CSI driver only being usable by Pods scheduled in the same availability zone as the volume).

### 💡 Extra Insight

CSI's topology-awareness feature is *why* a Pod using a cloud-block-storage-backed PVC can get stuck `Pending` if the scheduler tries to place it on a node in a different AZ than where the volume was provisioned — this looks like a scheduling bug at first glance but is actually the storage topology constraint working as designed; the fix is usually aligning node affinity/zone constraints with the PVC's actual zone.

### 🩹 Common Error & Fix

```text
0/5 nodes are available: 3 node(s) had volume node affinity conflict
```
The PV was provisioned in one zone, but the scheduler is trying nodes in other zones — check the PV's node affinity and the target zone versus where your eligible nodes actually sit.

### 🎤 Interview

**🟢:** CSI kya hai?
**🔵:** CSI driver ka role kya hai?
**🟡:** Volume topology constraint scheduling ko kaise affect karta hai?
**🔴:** Multi-AZ cluster mein storage-aware scheduling design karo.

---

# 28 — CONFIGMAP & SECRETS

**ConfigMap** — non-sensitive configuration. **Secret** — sensitive configuration material.

Know: environment variables, mounted files, immutable configuration, secret rotation, external secret managers, encryption at rest.

## 🔥 Gotcha

Kubernetes Secret is not automatically equivalent to a fully secure enterprise secret vault. Security architecture may require KMS, cloud secret manager, Vault, external-secrets style integration, encryption at rest.

Kubernetes documents Secrets, encryption at rest, Pod security and admission control as separate security concerns.

### 🧪 Try It Yourself

```bash
kubectl create secret generic demo-secret --from-literal=password=hunter2
kubectl get secret demo-secret -o jsonpath='{.data.password}' | base64 -d
```

Notice the "encoding" is just base64 — reversible in one command, with no encryption involved at this layer by default. This single exercise is the fastest way to internalize "Secret ≠ automatically encrypted."

### 💡 Extra Insight

Whether Secrets are encrypted **at rest in etcd** is a separate, cluster-level configuration (`EncryptionConfiguration` for the API server) that many clusters don't enable by default — meaning on an unconfigured cluster, anyone with direct etcd access could read Secret values in plaintext, even though `kubectl get secret -o yaml` shows base64 rather than plaintext. Base64 in the API response and actual at-rest encryption in etcd are two completely different things.

### 🩹 Common Error & Fix

Symptom: "we store real secrets in Kubernetes Secrets and consider that sufficient security." This conflates two different things (API-level base64 encoding, and actual etcd-level encryption-at-rest) — verify your cluster's `EncryptionConfiguration` is actually enabled, and consider whether an external secrets manager is warranted for your compliance requirements before relying on Secrets alone.

### 🎤 Interview

**🟢:** ConfigMap vs Secret?
**🔵:** Secret base64 encode hai ya encrypt?
**🟡:** etcd encryption at rest kya solve karta hai?
**🔴:** External secrets manager integration architecture design karo (rotation, short-lived credentials sahit).

---

# 29 — RESOURCE MANAGEMENT

Very important for interviews.

**Requests** — what the Pod asks the scheduler to reserve/consider. **Limits** — maximum resource boundary for the container.

```yaml
resources:
  requests:
    cpu: ...
    memory: ...
  limits:
    cpu: ...
    memory: ...
```

Know: CPU requests, memory requests, CPU limits, memory limits, QoS classes, OOMKilled, eviction, node allocatable, overcommit.

## QoS

Guaranteed, Burstable, BestEffort.

## Gotcha

CPU throttling and memory OOM behavior are not identical.

### 🧪 Try It Yourself

```yaml
resources:
  requests: { cpu: "250m", memory: "128Mi" }
  limits:   { cpu: "500m", memory: "128Mi" }
```

```bash
kubectl apply -f pod-with-limits.yaml
kubectl describe pod <name> | grep -A3 "QoS Class"
```

Try three variations — (requests == limits for both CPU and memory → `Guaranteed`), (requests set, limits higher or unset → `Burstable`), (neither set → `BestEffort`) — and confirm the QoS class Kubernetes assigns in each case.

### 💡 Extra Insight

Hitting a **CPU limit** just throttles the container (it keeps running, just slower — no crash, no restart) — hitting a **memory limit** gets the container **OOMKilled** (a hard kill, because unlike CPU time, memory can't be "throttled," only reclaimed by killing something). This asymmetry is why memory limits deserve much more conservative headroom than CPU limits in most real workloads.

### 🩹 Common Error & Fix

```text
Pod status: OOMKilled
```
The container exceeded its memory *limit* — check `kubectl describe pod` for the exact limit and consider whether the limit is too tight for genuine peak usage, or whether there's an actual memory leak to fix, before just raising the limit blindly.

### 🎤 Interview

**🟢:** Requests vs limits?
**🔵:** QoS classes kya hain?
**🟡:** CPU throttle vs memory OOM behavior mein farak?
**🔴:** Multi-tenant cluster ke liye resource governance model design karo.

---

# 30 — LIMITRANGE & RESOURCEQUOTA

Namespace governance:

```text
Namespace ├── ResourceQuota └── LimitRange
```

ResourceQuota limits aggregate resource consumption. LimitRange can constrain defaults/min/max values for resources. Kubernetes documents both as policy mechanisms.

### 🧪 Try It Yourself

```yaml
apiVersion: v1
kind: ResourceQuota
metadata: { name: team-quota }
spec:
  hard:
    requests.cpu: "4"
    requests.memory: 8Gi
    pods: "20"
```

Apply it to a namespace, then try creating a 21st Pod — watch it get rejected at admission time with a clear quota-exceeded message, before it ever reaches the scheduler.

### 💡 Extra Insight

If a namespace has a ResourceQuota defined for CPU/memory, **every Pod created in that namespace must explicitly specify requests/limits** for those resources — a Pod with no resource specification will be rejected outright, not silently given some default. This surprises teams the first time they add a ResourceQuota to an existing namespace full of Pods that never bothered setting requests/limits.

### 🩹 Common Error & Fix

```text
Error from server (Forbidden): pods "demo" is forbidden: failed quota: team-quota: must specify limits.cpu,limits.memory
```
Add explicit `resources.requests`/`resources.limits` to the Pod spec, or use a LimitRange to supply cluster-wide namespace defaults so existing manifests don't need editing individually.

### 🎤 Interview

**🟢:** ResourceQuota kya karta hai?
**🔵:** LimitRange ka role kya hai?
**🟡:** Quota namespace mein Pod creation ko kaise affect karta hai without defaults set?
**🔴:** Multi-team namespace governance design karo quota + LimitRange ke saath.

---

# 31 — SCHEDULING DEEP DIVE

Architect-level scheduling needs much more than "scheduler assigns Pods."

Know: `nodeSelector`, node affinity, pod affinity, pod anti-affinity, taints, tolerations, topology spread constraints, priority classes, preemption, scheduling gates, topology awareness.

```text
Pending Pod → Filter → Score → Bind
```

## Taint/Toleration

```text
Node: "No general workloads" → Taint
Pod:  "I am allowed there"   → Toleration
```

## Anti-affinity

Useful to avoid all replicas landing on the same node.

### 🧪 Try It Yourself

```yaml
affinity:
  podAntiAffinity:
    requiredDuringSchedulingIgnoredDuringExecution:
      - labelSelector:
          matchExpressions:
            - key: app
              operator: In
              values: ["demo"]
        topologyKey: "kubernetes.io/hostname"
```

Apply this to a 3-replica Deployment on a cluster with at least 3 nodes, then `kubectl get pods -o wide` — confirm no two replicas share a node. Try scaling to more replicas than you have nodes and watch the excess Pods stay `Pending` (with `required` anti-affinity, this is a hard constraint, not a soft preference).

### 💡 Extra Insight

`requiredDuringSchedulingIgnoredDuringExecution` is a genuinely confusing name until you parse it literally: **required** at scheduling time (a hard constraint the scheduler must satisfy), but **ignored during execution** (if the topology changes later — say, a node is removed and Pods get consolidated in a way that violates the original constraint — Kubernetes won't proactively evict/reschedule already-running Pods just to re-satisfy it). Preferred variants exist for soft constraints that don't block scheduling if unsatisfiable.

### 🩹 Common Error & Fix

```text
0/5 nodes are available: 5 node(s) didn't match pod anti-affinity rules
```
More replicas than available distinct nodes/topology domains under a `required` anti-affinity rule — either add nodes, reduce replica count relative to node count, or switch to a `preferred` (soft) rule if some co-location is acceptable.

### 🎤 Interview

**🟢:** Taint/toleration kya solve karte hain?
**🔵:** Node affinity vs pod affinity?
**🟡:** Required vs preferred anti-affinity?
**🔴:** High-availability replica spread across nodes/zones design karo topology spread constraints ke saath.

---

# 32 — HEALTH CHECKS

Three probes: **Startup** ("has my app finished starting?"), **Readiness** ("can I receive traffic?"), **Liveness** ("am I stuck/broken enough to restart?").

```text
Startup → Readiness → Traffic
Liveness → restart when necessary
```

## Critical gotcha

```text
healthy app → probe fails → restart → probe fails → CrashLoop
```

### 🧪 Try It Yourself

```yaml
livenessProbe:
  httpGet: { path: /healthz, port: 8080 }
  initialDelaySeconds: 2
  periodSeconds: 5
  timeoutSeconds: 1
```

Deliberately set `initialDelaySeconds` too low for an app that genuinely takes 30 seconds to start, and watch `kubectl get pods` show repeated restarts — you've just reproduced the exact gotcha above on purpose, which is the fastest way to recognize it in a real incident later.

### 💡 Extra Insight

The **startup probe** exists specifically to solve the tension between "liveness needs a short timeout to catch real hangs quickly" and "some apps genuinely take a long time to start" — before startup probes existed, people had to set an overly generous `initialDelaySeconds` on the liveness probe itself (delaying real hang detection for the app's *entire* lifetime, not just startup) as a workaround. A startup probe lets liveness stay aggressive/fast once the app is actually confirmed running.

### 🩹 Common Error & Fix

Symptom: app is fine but keeps restarting under load (not just at startup). Check whether the liveness probe endpoint itself does expensive work (e.g., a `/healthz` that queries the database) — under load, that endpoint can time out even though the app is otherwise healthy, causing self-inflicted restarts; liveness checks should be cheap and fast, not full dependency health checks (that's what readiness is often better suited for).

### 🎤 Interview

**🟢:** Teeno probes ka role kya hai?
**🔵:** Startup probe kyun add hui?
**🟡:** Liveness probe expensive hone se kya problem hoti hai?
**🔴:** CrashLoop-causing probe misconfiguration ko production mein diagnose/fix karne ka playbook design karo.

---

# 33 — POD DISRUPTION & HIGH AVAILABILITY

Know: PodDisruptionBudget, voluntary vs involuntary disruption, node drain, rolling updates, replica spread, multi-AZ placement, anti-affinity, topology spread.

## PDB

Protects availability during **voluntary disruptions**.

## Gotcha

PDB is not a magical guarantee against hardware failure, kernel crash, sudden node loss, all disaster scenarios.

### 🧪 Try It Yourself

```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata: { name: demo-pdb }
spec:
  minAvailable: 2
  selector:
    matchLabels: { app: demo }
```

With a 3-replica Deployment and this PDB, try to `kubectl drain` a node hosting one of its Pods — the drain succeeds (1 Pod evicted, 2 remain, satisfying `minAvailable: 2`). Now imagine (or actually test in a throwaway cluster) draining a *second* node simultaneously — the drain would be blocked/delayed because evicting that Pod too would violate the PDB.

### 💡 Extra Insight

A PDB only ever gets consulted during **voluntary** disruptions initiated through the eviction API (like `kubectl drain`, or a cluster-autoscaler node scale-down) — it has zero effect on a node that simply crashes or gets forcibly terminated (an involuntary disruption). This is exactly why the gotcha above matters: teams sometimes treat "we have a PDB" as equivalent to "we're protected from downtime," when it only covers the *planned maintenance* half of the disruption story.

### 🩹 Common Error & Fix

```text
error when evicting pod "demo-xyz": Cannot evict pod as it would violate the pod's disruption budget.
```
Expected, protective behavior — either wait for capacity to free up elsewhere, temporarily adjust the PDB if truly necessary (understanding the availability trade-off), or scale up before draining.

### 🎤 Interview

**🟢:** PDB kya protect karta hai?
**🔵:** Voluntary vs involuntary disruption?
**🟡:** PDB node crash se protect karta hai kya?
**🔴:** Multi-AZ HA architecture design karo PDB + topology spread ke saath.

---

# 34 — SECURITY

## Identity

Authentication, authorization, RBAC, ServiceAccounts.

## Pod security

Pod Security Standards, privileged/baseline/restricted, `securityContext`, `runAsNonRoot`, capabilities, seccomp, AppArmor/SELinux where applicable, `readOnlyRootFilesystem`.

Kubernetes defines Privileged, Baseline and Restricted Pod Security Standards profiles.

## API security

```text
kubectl → API server → Authentication → Authorization → Admission → Validation
```

## Admission

Validating admission, mutating admission, admission webhooks, policy engines, image/security policy. Kubernetes admission controllers can validate or mutate API requests.

### 🧪 Try It Yourself

```yaml
securityContext:
  runAsNonRoot: true
  readOnlyRootFilesystem: true
  allowPrivilegeEscalation: false
  capabilities:
    drop: ["ALL"]
```

Apply this to a Pod running an image that expects to write to its filesystem or run as root, and watch it fail — then fix the underlying image/app behavior (writable volume mount for the specific path it needs, or a non-root-compatible base image) rather than loosening the security context, to feel the real trade-off teams face when hardening Pods.

### 💡 Extra Insight

The **Restricted** Pod Security Standard profile is deliberately strict enough that many popular off-the-shelf container images (built assuming root, writable root filesystem) fail it out of the box — this isn't a bug in the profile; it's exposing that a lot of widely-used images were never built with least-privilege runtime assumptions in mind, and hardening a cluster often means auditing/patching images, not just flipping a policy switch.

### 🩹 Common Error & Fix

```text
Error: container has runAsNonRoot and image will run as root
```
The image's default user is root and no `runAsUser`/non-root user is set — either use an image built for non-root operation, or explicitly set `runAsUser` to a known non-root UID the image supports.

### 🎤 Interview

**🟢:** AuthN vs AuthZ vs Admission — order kya hai?
**🔵:** securityContext ke important fields kaunse hain?
**🟡:** Restricted profile itna strict kyun hai?
**🔴:** Cluster-wide Pod Security enforcement rollout strategy design karo (existing workloads ko break kiye bina).

---

# 35 — RBAC DEEP DIVE

Know: Role, ClusterRole, RoleBinding, ClusterRoleBinding, ServiceAccount, least privilege, namespace scope.

```text
Role → namespace-scoped permissions
ClusterRole → cluster-scoped or reusable permissions
```

## Gotcha

RBAC controls Kubernetes API permissions. It does not automatically control Pod → database access — that's a networking/application/security concern.

### 🧪 Try It Yourself

```bash
kubectl create serviceaccount demo-reader
kubectl create role pod-reader --verb=get,list,watch --resource=pods -n default
kubectl create rolebinding demo-reader-binding --role=pod-reader --serviceaccount=default:demo-reader -n default
kubectl auth can-i list pods --as=system:serviceaccount:default:demo-reader
kubectl auth can-i delete pods --as=system:serviceaccount:default:demo-reader
```

The first `can-i` returns `yes`, the second `no` — `kubectl auth can-i --as=` is the fastest way to actually verify an RBAC grant does exactly what you intended, rather than assuming from the YAML alone.

### 💡 Extra Insight

A `ClusterRoleBinding` referencing a `ClusterRole` grants that permission **cluster-wide across all namespaces** — but a `RoleBinding` can also reference a `ClusterRole` (not just a `Role`), which grants that same set of permissions scoped to only *one* namespace. This is a genuinely useful pattern (reuse one well-defined `ClusterRole` like "view" across many namespace-scoped `RoleBindings`) that's easy to miss if you assume ClusterRole always means cluster-wide effect.

### 🩹 Common Error & Fix

```text
Error from server (Forbidden): pods is forbidden: User "..." cannot list resource "pods" in API group "" in the namespace "production"
```
Exactly the RBAC system working as designed — check `kubectl auth can-i list pods --as=<user/serviceaccount> -n production` to confirm the gap, then grant the specific missing permission via a Role/RoleBinding rather than over-granting a broad ClusterRole.

### 🎤 Interview

**🟢:** Role vs ClusterRole?
**🔵:** RoleBinding ClusterRole ko reference kar sakta hai kya?
**🟡:** RBAC application-level DB access control karta hai kya?
**🔴:** Least-privilege RBAC model design karo 10+ teams ke multi-tenant cluster ke liye.

---

# 36 — AUTOSCALING

**HPA** (Horizontal Pod Autoscaler): `Metrics → HPA → replicas`. **VPA** (Vertical Pod Autoscaler): adjusts resource sizing. **Cluster autoscaler**: `Pending Pods → Node autoscaler → More nodes`. **KEDA**: event-driven scaling (Kafka lag, queue length, scheduled events).

Kubernetes documents HPA/VPA and also identifies KEDA for event-driven scaling.

## Architect distinction

```text
HPA → Pods
VPA → Pod resources
Node autoscaler → Nodes
KEDA → event-driven workload scaling
```

### 🧪 Try It Yourself

```bash
kubectl autoscale deployment demo --cpu-percent=50 --min=2 --max=10
kubectl get hpa
```

Generate CPU load against the Deployment (e.g., a simple load-testing tool) and watch `kubectl get hpa -w` show the `TARGETS` column climb and `REPLICAS` scale up in response, then scale back down after load stops (subject to the default scale-down stabilization window).

### 💡 Extra Insight

HPA and VPA **should generally not be configured to manage CPU/memory for the same workload simultaneously** — they can fight each other (HPA scaling out because average CPU per Pod is high, VPA simultaneously trying to resize each Pod's CPU request, changing what "high" even means for HPA's calculation). Some setups combine them carefully (e.g., VPA in recommendation-only mode alongside HPA), but naive simultaneous "both actively managing the same metric" configurations are a known anti-pattern.

### 🩹 Common Error & Fix

```text
unable to get metrics for resource cpu: no metrics returned from resource metrics API
```
`metrics-server` isn't installed or isn't reporting yet — HPA depends on it (or a custom/external metrics adapter for non-CPU/memory metrics) being healthy; verify with `kubectl top pods` working independently before troubleshooting the HPA itself.

### 🎤 Interview

**🟢:** HPA kya karta hai?
**🔵:** HPA vs cluster autoscaler?
**🟡:** HPA aur VPA same metric par kyun conflict kar sakte hain?
**🔴:** Kafka-lag-driven event-based autoscaling design karo KEDA ke saath.

---

# 37 — ROLLOUT / ROLLBACK / PROGRESSIVE DELIVERY

Know: rollout status, rollout history, rollback, `maxSurge`, `maxUnavailable`, readiness interaction, deployment strategy. Then advanced: canary, blue/green, progressive delivery, traffic splitting, automated rollback based on metrics.

### 🧪 Try It Yourself

```bash
kubectl set image deployment/demo demo=myapp:v2
kubectl rollout status deployment/demo
kubectl rollout history deployment/demo
kubectl rollout undo deployment/demo
```

Deliberately deploy a broken image (one that fails its readiness probe) and watch `rollout status` hang instead of completing — then run `rollout undo` and watch it roll back to the last good revision. This is the single most useful sequence to have as muscle memory for any real production incident during a bad deploy.

### 💡 Extra Insight

`maxUnavailable` and `maxSurge` interact with readiness probes in a way that's easy to misjudge: a rolling update only proceeds to replace *more* old Pods once the *new* Pods it already created have passed their readiness probe — if new Pods never become ready (a bad image, a missing config), the rollout will hang indefinitely at whatever partial state `maxUnavailable`/`maxSurge` allowed, rather than either fully completing or fully failing on its own. This is exactly why `rollout status` (and knowing how to `rollout undo`) matters operationally, not just as a nice-to-have.

### 🩹 Common Error & Fix

Symptom: `kubectl rollout status` never returns/times out. Check `kubectl get pods` for new-revision Pods stuck in `CrashLoopBackOff` or failing readiness — the rollout is correctly *waiting* for them to become healthy, which they never will without a fix or a rollback.

### 🎤 Interview

**🟢:** `rollout status` kya batata hai?
**🔵:** `maxSurge` vs `maxUnavailable`?
**🟡:** Bad image deploy hone par rollout hang kyun hota hai instead of failing?
**🔴:** Automated metric-based rollback pipeline design karo (bina manual intervention ke).

---

# 38 — OBSERVABILITY

Three pillars: Logs, Metrics, Traces.

**Logs** — container logs, stdout/stderr, log rotation, centralized collection. **Metrics** — kube-state-metrics, metrics-server, Prometheus, node metrics, application metrics. **Tracing** — OpenTelemetry, trace context, service-to-service tracing.

```text
User request → Gateway → Service A → Service B → Database
```

You should be able to trace one request across the system.

### 🧪 Try It Yourself

```bash
kubectl logs -f deployment/demo
kubectl logs -f deployment/demo --previous   # logs from the last crashed instance
kubectl top pods
kubectl top nodes
```

Then check `kube-state-metrics` if installed (`kubectl get pods -n kube-system | grep kube-state-metrics`) — this exposes *object state* (how many replicas desired vs. available, Pod phase counts) as Prometheus metrics, distinct from `metrics-server`'s resource-usage numbers (`kubectl top`). Confirming you can tell these two apart is the actual skill being tested here.

### 💡 Extra Insight

`--previous` on `kubectl logs` is one of the most underused debugging commands — after a CrashLoopBackOff restart, the *current* container's logs may show almost nothing (it just started), while `--previous` shows the logs from the crashed instance that actually explain what went wrong. Many people waste time staring at empty current-container logs before remembering this flag exists.

### 🩹 Common Error & Fix

```text
Error from server (BadRequest): previous terminated container "demo" in pod "demo-xyz" not found
```
The Pod hasn't actually restarted yet (only one container instance has ever existed), or the previous container's logs have already been garbage-collected — check `kubectl describe pod` restart count first to confirm there's actually a "previous" instance to look at.

### 🎤 Interview

**🟢:** Three observability pillars kya hain?
**🔵:** `kube-state-metrics` vs `metrics-server`?
**🟡:** `--previous` logs kab useful hote hain?
**🔴:** End-to-end distributed tracing architecture design karo multi-service request ke liye.

---

# 39 — DEBUGGING / TROUBLESHOOTING

Master this sequence:

```text
Pod status → Events → Describe → Logs → Previous logs → Exec
 → Network/DNS → Service endpoints → Node → Cluster components
```

## Common states

**Pending** — insufficient resources, affinity, taint, topology constraints, PVC, scheduler issue. **ImagePullBackOff** — image name, registry auth, network, tag, image architecture. **CrashLoopBackOff** — application crash, bad configuration, dependency failure, probe failure, permission problem. **Terminating forever** — finalizer, stuck volume, API dependency, controller issue.

### 🧪 Try It Yourself

Deliberately create each failure mode once, on a throwaway cluster/namespace, and practice the diagnosis sequence for real:

```bash
kubectl run bad-image --image=this-image-does-not-exist:latest    # → ImagePullBackOff
kubectl describe pod bad-image                                     # events show the exact pull error

kubectl run bad-crash --image=busybox --command -- sh -c "exit 1"  # → CrashLoopBackOff
kubectl logs bad-crash --previous                                  # (once it's crashed at least once)
```

Doing this once deliberately, calmly, without production pressure, is worth far more than reading the state descriptions passively.

### 💡 Extra Insight

`kubectl describe pod` and its **Events** section is almost always the fastest path to a root cause for `Pending`/`ImagePullBackOff` — it directly states things like "0/5 nodes are available: 3 Insufficient cpu, 2 node(s) had taint..." in plain language, whereas `kubectl get pods` alone only shows you the *symptom* (the state name), not the *reason*. Many people jump straight to logs and skip the much faster `describe` step.

### 🩹 Common Error & Fix

Symptom: object stuck in `Terminating` for a very long time. Check `kubectl get pod <name> -o yaml | grep -A5 finalizers` — a controller that was supposed to remove a finalizer (after doing cleanup work) may have crashed or been uninstalled, leaving the object permanently stuck until the finalizer is manually removed (with an understanding of what cleanup you might be skipping by doing so).

### 🎤 Interview

**🟢:** Pod Pending ka matlab kya hai?
**🔵:** ImagePullBackOff ke common causes?
**🟡:** `describe` events logs se pehle kyun check karni chahiye?
**🔴:** Stuck-Terminating incident ka full root-cause + safe-remediation runbook design karo.

---

# 40 — NAMESPACES & MULTI-TENANCY

Know: namespace isolation, RBAC, quotas, LimitRange, NetworkPolicy, naming, resource governance.

> Namespace is logical isolation, not a complete security boundary by itself.

For stronger multi-tenancy, combine: RBAC + NetworkPolicy + Pod Security + Quotas + Node isolation + Admission policy.

### 🧪 Try It Yourself

Design exercise: for a shared cluster hosting Team A and Team B, list out — namespace-per-team, RBAC scoping each team to only their namespace, a default-deny NetworkPolicy per namespace with explicit allows, a ResourceQuota per namespace, and Pod Security Standard enforcement per namespace. Then identify what's still *shared* and therefore still a potential blast-radius concern (the node's kernel, the CNI, the API server, etcd) even after all of the above.

### 💡 Extra Insight

Even with every namespace-level control listed above configured perfectly, Pods from different tenants can still end up scheduled on the **same physical node**, sharing that node's kernel — meaning a container-escape vulnerability in one tenant's workload could theoretically affect another tenant's Pods on the same node. True hard multi-tenancy (competing/untrusted tenants) often requires node-level isolation (dedicated node pools per tenant, or a sandboxed runtime like gVisor/Kata Containers) on top of everything else, not just namespace-level policy.

### 🩹 Common Error & Fix

Symptom: "we have namespace isolation, but Team A's misbehaving workload still degraded Team B's performance." This points at *missing* ResourceQuota/LimitRange (a noisy neighbor consuming unbounded node resources) rather than a namespace-isolation failure per se — namespace isolation was never meant to cover resource fairness on its own.

### 🎤 Interview

**🟢:** Namespace kya isolate karta hai?
**🔵:** Namespace akela security boundary hai kya?
**🟡:** Shared node par different tenants ka risk kya hai?
**🔴:** Untrusted multi-tenant workloads ke liye hard-isolation architecture design karo.

---

# 41 — CRD & OPERATORS

Major Architect topic.

## CRD

Custom Resource Definition extends Kubernetes API.

```text
Kubernetes API + Custom Resource
kind: MyDatabase
```

## Operator

```text
Custom Resource → Operator → Real infrastructure/workload
```

Understand: CRD, custom resource, controller, reconciliation loop, finalizers, ownerReferences, status conditions, idempotent reconciliation.

### 🧪 Try It Yourself

Install a real, well-known operator on a throwaway cluster (e.g., a Postgres or Redis operator) and study its custom resource:

```bash
kubectl get crd | grep -i postgres
kubectl explain postgresql.spec
```

Create a minimal custom resource instance, then watch the operator create the underlying StatefulSet/Service/Secret objects on your behalf — this is the entire "domain knowledge encoded as a controller" concept made tangible instead of abstract.

### 💡 Extra Insight

A well-written operator's reconciliation loop must be **idempotent** — safe to run repeatedly against the same desired state without side effects, because Kubernetes controllers get re-triggered constantly (on any relevant object change, periodic resync, or restart), not just once per user action. An operator that isn't idempotent (e.g., one that blindly re-creates a resource every reconcile instead of checking if it already exists) will misbehave in ways that are hard to debug, especially under normal Kubernetes operational churn.

### 🩹 Common Error & Fix

Symptom: a custom resource's `status` field never updates, even though the operator seems to be doing work. Check the operator's own logs and RBAC permissions — a common cause is the operator's ServiceAccount lacking permission to update the custom resource's `/status` subresource specifically (a separate RBAC verb/subresource from updating the main spec).

### 🎤 Interview

**🟢:** CRD kya extend karta hai?
**🔵:** Operator ka core idea kya hai?
**🟡:** Reconciliation idempotent hona kyun zaroori hai?
**🔴:** Ek custom domain workload (e.g., ML model serving) ke liye operator design karo.

---

# 42 — FINALIZERS & OWNER REFERENCES

Advanced debugging topic.

## OwnerReference

Defines ownership relationships: `Deployment → ReplicaSet → Pod`.

## Finalizer

Prevents deletion until cleanup work completes.

## Gotcha

Object stuck in `Terminating` may have a problematic finalizer.

### 🧪 Try It Yourself

```bash
kubectl get pod <pod-name> -o jsonpath='{.metadata.ownerReferences}'
```

Confirm a Pod created by a Deployment shows the owning ReplicaSet's UID here — then delete that ReplicaSet directly and watch the owned Pods get garbage-collected automatically (cascading deletion via owner references), without anyone explicitly deleting the Pods themselves.

### 💡 Extra Insight

`kubectl delete <resource> --cascade=orphan` deliberately **breaks** the owner-reference cascade — useful in a real scenario like "I want to delete this Deployment but keep its currently-running Pods alive temporarily" (e.g., during a careful migration), but dangerous if used by accident, since it silently leaves orphaned objects behind that no longer have a controller managing them.

### 🩹 Common Error & Fix

Symptom: an object is permanently stuck in `Terminating`, `kubectl delete --force --grace-period=0` "works" but the underlying resource (e.g., an external cloud volume) never actually got cleaned up. Force-deleting the Kubernetes object doesn't mean the finalizer's cleanup work actually happened — it just removes the object from the API despite the finalizer, potentially leaving real orphaned infrastructure behind; understand what the finalizer was supposed to clean up before force-deleting.

### 🎤 Interview

**🟢:** OwnerReference kya define karta hai?
**🔵:** Finalizer ka purpose kya hai?
**🟡:** `--cascade=orphan` kab use karoge?
**🔴:** Force-delete se orphaned cloud resources ka incident diagnose aur cleanup karo.

---

# 43 — HELM / KUSTOMIZE / PACKAGING

Production Kubernetes is rarely raw YAML everywhere.

## Helm

Know: chart, values, templates, release, hooks, dependency, upgrade, rollback.

## Kustomize

Know: base, overlays, patches, environment-specific configuration.

## Decision

```text
Reusable templating/package? → Helm
Environment overlays / YAML transformation? → Kustomize
```

### 🧪 Try It Yourself

```bash
helm create demo-chart
helm install demo ./demo-chart
helm upgrade demo ./demo-chart --set replicaCount=5
helm rollback demo 1
```

Separately, sketch a Kustomize base + two overlays (`dev`, `prod`) that patch just the replica count and resource limits differently per environment — feel the difference between "Helm: one parameterized template with values" and "Kustomize: one base manifest with declarative patches per environment," since the decision line above is easy to state but only really clicks after doing both once.

### 💡 Extra Insight

Helm and Kustomize aren't mutually exclusive in practice — a common real-world pattern is using Helm to template/package a reusable chart from a vendor or internal platform team, then using Kustomize on top to apply environment-specific overlay patches to the *rendered* output, getting reusable packaging (Helm's strength) plus clean environment diffing (Kustomize's strength) simultaneously.

### 🩹 Common Error & Fix

```text
Error: UPGRADE FAILED: another operation (install/upgrade/rollback) is in progress
```
A previous Helm operation didn't complete cleanly (often due to a timeout or a crashed CI job) and left the release in a pending state — `helm history <release>` to inspect, and `helm rollback` to a known-good revision, rather than forcing a new upgrade over a stuck state.

### 🎤 Interview

**🟢:** Helm chart kya hai?
**🔵:** Kustomize overlay kya karta hai?
**🟡:** Helm aur Kustomize saath mein use kar sakte ho kya?
**🔴:** Multi-environment (dev/staging/prod) packaging strategy design karo.

---

# 44 — GITOPS

Architect-level deployment flow:

```text
Developer → Git → CI → Image Registry → Manifest/Helm change → GitOps controller → Kubernetes
```

Know: desired state, reconciliation, drift, Argo CD, Flux, promotion, rollback.

### 🧪 Try It Yourself

Design exercise (or hands-on if you have a cluster + Argo CD/Flux available): point a GitOps controller at a Git repo containing your manifests, then make a **manual** `kubectl edit` change directly against the live cluster (bypassing Git entirely) — watch the GitOps controller detect the drift and revert your manual change back to match Git within its next reconciliation cycle. This is the single most convincing demonstration of "Git is the source of truth, not the live cluster" available.

### 💡 Extra Insight

The drift-reversal behavior above is a feature, not a bug, but it surprises people used to imperative `kubectl` workflows — "I fixed it live to unblock an incident" is exactly the kind of change a properly configured GitOps controller will silently undo, unless the incident fix is *also* committed to Git (or the GitOps controller is deliberately paused/put in a manual-override mode during the incident). Incident runbooks for GitOps-managed clusters need to explicitly address this.

### 🩹 Common Error & Fix

Symptom: "we fixed a production incident with `kubectl edit`, and 5 minutes later the exact same problem came back." Classic GitOps drift-reversal — the GitOps controller reverted the manual fix back to the (broken) state still declared in Git. Fix the *Git* source and let the controller reconcile, or explicitly pause reconciliation during the incident if a live manual fix is truly necessary first.

### 🎤 Interview

**🟢:** GitOps ka core idea kya hai?
**🔵:** Drift kya hai?
**🟡:** Manual `kubectl` fix GitOps-managed cluster mein kyun revert ho sakta hai?
**🔴:** Incident-response process design karo GitOps-managed production cluster ke liye.

---

# 45 — IMAGE / SUPPLY-CHAIN SECURITY

Kubernetes security starts before the Pod.

```text
Source → Build → Dependency scan → Image scan → Sign / attest → Registry → Admission policy → Cluster
```

Know: image provenance, SBOM, vulnerability scanning, image signing, admission policies, least privilege, registry security, runtime security.

### 🧪 Try It Yourself

```bash
docker sbom myapp:1.0        # or a dedicated SBOM tool, depending on what's available
```

Generate an SBOM (Software Bill of Materials) for an image you actually use, and scan it against a vulnerability database — then design (on paper) an admission policy that would reject deployment of any image with a critical, unpatched CVE, tying together "scanning happened" and "the cluster actually enforces the result" rather than treating scanning as a report nobody acts on.

### 💡 Extra Insight

Image scanning that only happens in CI (before push) but isn't **also enforced at admission time** in the cluster has a real gap: nothing stops someone from deploying an older, unscanned, or manually-pushed image directly with `kubectl apply` or `kubectl set image`, bypassing CI entirely. Admission-time policy enforcement (checking signatures/scan results as the image is actually being deployed, not just when it was built) closes that gap — "scan in CI" and "enforce at admission" are two different, both-necessary controls.

### 🩹 Common Error & Fix

Symptom: a known-vulnerable image made it to production despite a CI scanning step existing. Check whether admission control actually blocks unsigned/unscanned images from being deployed, or whether CI scanning was purely advisory (a report, not a gate) — a scan result nobody enforces is not a security control.

### 🎤 Interview

**🟢:** SBOM kya hai?
**🔵:** Image scanning kahan-kahan honi chahiye pipeline mein?
**🟡:** CI-only scanning ka gap kya hai?
**🔴:** End-to-end supply-chain security architecture design karo source se cluster tak.

---

# 46 — SECRETS MANAGEMENT

Enterprise pattern:

```text
Application → Kubernetes → External secret integration → Vault / Cloud Secret Manager / KMS
```

Know: rotation, short-lived credentials, workload identity, encryption at rest, avoiding secrets in Git.

### 🧪 Try It Yourself

Design exercise: sketch how a Pod would obtain a database credential using workload identity (e.g., a cloud IAM role bound to a Kubernetes ServiceAccount) instead of a static Kubernetes Secret — trace the flow: Pod → ServiceAccount → cloud IAM federation → short-lived token → Vault/Secrets Manager → actual credential, never touching a long-lived static secret stored in etcd at all.

### 💡 Extra Insight

Workload identity (federating a Kubernetes ServiceAccount to a cloud IAM identity) is increasingly preferred over static long-lived Secrets specifically because it eliminates an entire class of risk: **there's no long-lived credential sitting in etcd or a Secret object to leak in the first place** — the Pod gets a short-lived, automatically-rotated token instead. This is a meaningfully different security posture than "we store the secret in Kubernetes and rotate it periodically," which still has a real credential sitting somewhere at rest.

### 🩹 Common Error & Fix

Symptom: "we rotated our database password in Vault, but Pods are still using the old one and failing auth." Check whether your integration actually re-injects the new secret into running Pods (some patterns require a Pod restart to pick up a rotated Secret's mounted-file update, since environment variables in particular are only read once at container start) — rotation without a re-delivery/restart mechanism doesn't reach already-running workloads.

### 🎤 Interview

**🟢:** Secret rotation kyun important hai?
**🔵:** Workload identity kya solve karta hai?
**🟡:** Rotated secret running Pods tak kaise pahunchti hai?
**🔴:** Zero-static-credential secrets architecture design karo cloud-native workload ke liye.

---

# 47 — NODE LIFECYCLE & MAINTENANCE

```bash
kubectl cordon
kubectl drain
kubectl uncordon
```

Understand: scheduling disable, graceful eviction, PDB interaction, daemonsets, local storage, node replacement, node upgrades.

## Gotcha

`drain` is not simply "delete all Pods." It follows eviction/ownership and disruption semantics.

### 🧪 Try It Yourself

```bash
kubectl cordon node-1
kubectl get nodes   # node-1 shows SchedulingDisabled
kubectl drain node-1 --ignore-daemonsets --delete-emptydir-data
```

Watch which Pods get evicted (respecting PDBs, as covered in Module 33) versus which stay (DaemonSet Pods, unless `--ignore-daemonsets` is passed) — then `kubectl uncordon node-1` to bring it back into the scheduling pool once maintenance is done.

### 💡 Extra Insight

`cordon` and `drain` are two genuinely separate steps for a reason: `cordon` alone only stops **new** Pods from being scheduled there — it does nothing to Pods already running on that node. `drain` (which internally cordons first, then evicts) is the actual "move existing workloads off this node" step. Running only `cordon` before, say, physical hardware maintenance would leave existing workloads sitting on a node about to go down — a real and avoidable mistake.

### 🩹 Common Error & Fix

```text
error: unable to drain node "node-1", aborting command... error: DaemonSet-managed Pods (use --ignore-daemonsets to ignore)
```
DaemonSet Pods are designed to run on every eligible node and `drain` won't evict them by default (they'll just come back when the node is uncordoned, or run for the node's whole life) — pass `--ignore-daemonsets` to proceed with draining the rest.

### 🎤 Interview

**🟢:** Cordon vs drain?
**🔵:** Drain kya "delete all Pods" jaisa hi hai kya?
**🟡:** DaemonSet Pods drain se kyun exempt hote hain by default?
**🔴:** Zero-downtime node upgrade runbook design karo PDB/cordon/drain ke saath.

---

# 48 — CLUSTER UPGRADE

```text
Control plane → Workers → CNI → CSI → Ingress/Gateway → Operators → Add-ons
```

Plan: version compatibility, deprecated APIs, backup, PDB, capacity, rollback, canary node upgrades, workload validation.

Kubernetes maintains a deprecation/migration guide because APIs can stop being served across versions.

### 🧪 Try It Yourself

```bash
kubectl api-resources --verbs=list --api-group=extensions
kubectl get --raw /metrics | grep apiserver_requested_deprecated_apis
```

Before any real upgrade, check which deprecated/removed APIs your cluster's workloads are *actually still using* — this second command (availability/exact metric name varies by version) is exactly the kind of proactive check that turns "surprise breakage on upgrade day" into "known, planned migration work beforehand."

### 💡 Extra Insight

A Kubernetes API being marked "deprecated" and a Kubernetes API being **removed** (stops being served entirely) are different events on different timelines — a manifest referencing a fully-removed API version won't just show a warning on the next upgrade, it will fail outright (`kubectl apply` rejecting it, or a controller failing to reconcile). Auditing for deprecated API usage well *before* the version that actually removes them is the entire point of doing this check proactively rather than reactively.

### 🩹 Common Error & Fix

```text
error: resource mapping not found for name: "demo" namespace: "" from "manifest.yaml": no matches for kind "Ingress" in version "extensions/v1beta1"
```
A manifest still references a fully-removed API version after a cluster upgrade — update the manifest to the current stable API version (e.g., `networking.k8s.io/v1` for Ingress) before re-applying.

### 🎤 Interview

**🟢:** Deprecated vs removed API — farak kya hai?
**🔵:** Upgrade se pehle kya check karna chahiye?
**🟡:** Removed API reference hone par kya hota hai?
**🔴:** Multi-version, multi-add-on cluster upgrade strategy design karo including rollback plan.

---

# 49 — ETCD

Know: source of cluster state, consistency, quorum, backup, restore, encryption, latency sensitivity, disk performance, disaster recovery.

```text
API Server → etcd → Cluster state
```

## Critical

Do not casually treat etcd as application data storage. It is control-plane state.

### 🧪 Try It Yourself

```bash
ETCDCTL_API=3 etcdctl snapshot save /tmp/etcd-backup.db \
  --endpoints=https://127.0.0.1:2379 \
  --cacert=/etc/kubernetes/pki/etcd/ca.crt \
  --cert=/etc/kubernetes/pki/etcd/server.crt \
  --key=/etc/kubernetes/pki/etcd/server.key
```

(On a real kubeadm-style cluster with access to etcd's certs.) Taking one real etcd snapshot and understanding the exact restore procedure (`etcdctl snapshot restore`, which involves stopping the API server, restoring to a new data directory, and reconfiguring) *before* an actual disaster is the entire value of this exercise — most teams who've never rehearsed etcd restore discover the process is harder than expected exactly when they can least afford that discovery.

### 💡 Extra Insight

etcd is extremely sensitive to **disk write latency** because every write requires a quorum-committed, fsync'd disk write across a majority of etcd members before it's acknowledged — running etcd on slow or contended disk (common in under-provisioned or noisy-neighbor cloud environments) manifests as generalized cluster slowness (slow `kubectl` commands, slow controller reconciliation) that can look like almost any other kind of problem until you specifically check etcd's own latency metrics.

### 🩹 Common Error & Fix

```text
etcdserver: request timed out
```
or generalized "everything in the cluster feels slow" — check etcd's disk I/O latency and network latency between etcd members before assuming the issue is in application workloads or the API server itself.

### 🎤 Interview

**🟢:** etcd kya store karta hai?
**🔵:** etcd application data store hai kya?
**🟡:** etcd disk latency itni sensitive kyun hai?
**🔴:** etcd backup/restore disaster-recovery runbook design karo aur actually rehearse karne ka plan banao.

---

# 50 — API SERVER / ADMISSION DEEP DIVE

Request flow:

```text
kubectl → API Server → Authentication → Authorization → Admission → Validation → Persistence → etcd
```

Then: `Controllers → Scheduler → Kubelet → Actual state`.

This flow is one of the most important Architect interview diagrams.

### 🧪 Try It Yourself

```bash
kubectl apply -f pod.yaml -v=8
```

The very verbose output shows the actual HTTP request/response exchange with the API server — you can literally see the request go out and the response (including any admission-webhook-driven mutation of your object) come back, connecting the abstract diagram above to a real, visible network exchange.

### 💡 Extra Insight

**Mutating** admission webhooks run *before* **validating** admission webhooks, always, regardless of how they're registered — this ordering matters because a mutating webhook might add a default value or inject a sidecar container that a validating webhook then needs to check against. Getting this order backwards in your mental model (or in a custom admission chain design) leads to confusing "why did validation reject something that should have been mutated first" bugs.

### 🩹 Common Error & Fix

```text
Error from server: admission webhook "mywebhook.example.com" denied the request
```
A validating (or a mutating webhook that also validates) admission webhook rejected the object — check the webhook's own logs, and remember mutating webhooks ran first, so inspect the *mutated* version of your object (`kubectl get --dry-run` chains can help simulate this) rather than only your original YAML.

### 🎤 Interview

**🟢:** API server request flow ke steps kya hain?
**🔵:** Authentication vs authorization vs admission?
**🟡:** Mutating aur validating webhooks ka order kya hai?
**🔴:** Custom admission policy chain design karo (mutation + validation dono ke saath) enterprise governance ke liye.

---

# 51 — CONTROLLER / RECONCILIATION MODEL

Kubernetes is fundamentally reconciliation-driven.

```text
Desired state → Controller → Observe actual state → Calculate difference → Act → Observe again
```

Kubernetes does not simply execute a script once; controllers continuously work toward desired state.

### 🧪 Try It Yourself

This is the same exercise as Module 19's Try It Yourself, worth revisiting explicitly here: delete a ReplicaSet managed by a Deployment and watch it get recreated. Then go one step further — manually edit a running Pod's image directly (`kubectl edit pod`, bypassing the Deployment) and watch what happens (usually nothing changes automatically for that specific already-running Pod, since it's not itself being continuously reconciled against the Deployment's template the same way the *count/existence* of Pods is) — this distinction between "reconciling Pod count/existence" and "reconciling every field of an existing Pod" is a subtle but important nuance in the model.

### 💡 Extra Insight

The reconciliation loop is *why* Kubernetes self-heals from so many failure classes without any human writing failure-specific handling code — a node dying, a Pod being manually deleted, a ReplicaSet's object being corrupted, all get corrected by the same generic "observe actual, compare to desired, act" loop, rather than needing bespoke recovery logic per failure type. This generality is the actual architectural insight behind "Kubernetes is a reconciliation engine," not just a scheduling/orchestration tool.

### 🩹 Common Error & Fix

Symptom: "I edited a running Pod directly and my change keeps disappearing / doesn't actually take effect as I expected." Depending on what you edited, either the Deployment's reconciliation loop is correcting it back (if it affects something the controller manages), or Kubernetes simply doesn't allow editing that particular field on a running Pod at all (many Pod spec fields are immutable after creation) — understand which of the two you're hitting before assuming it's a bug.

### 🎤 Interview

**🟢:** Reconciliation loop kya hai?
**🔵:** Controller "script run once" jaisa hai kya?
**🟡:** Reconciliation self-healing ko kaise enable karta hai generically?
**🔴:** Custom controller design karo jo apna khud ka reconciliation loop implement kare.

---

# 52 — CLOUD KUBERNETES

Know the difference between EKS, AKS, GKE, self-managed kubeadm, OpenShift, Rancher ecosystem, local Kubernetes.

Architect questions: Who manages control plane? Who manages upgrades? Who owns nodes? Who provides CNI? Who provides CSI? How does load balancing integrate? What is the cost model? What is the security boundary?

### 🧪 Try It Yourself

For whichever managed Kubernetes offering you have access to (or documentation for), fill in a table answering all eight architect questions above concretely — e.g., "control plane managed by provider, worker nodes are customer-managed EC2/VM instances, CNI is provider-default-but-swappable, load balancer integration is via an in-cluster controller that provisions cloud LBs automatically." Doing this once per platform you actually use turns abstract "managed vs self-managed" into a concrete shared-responsibility map you can reference during an incident.

### 💡 Extra Insight

The shared-responsibility boundary is exactly where a surprising number of production incidents get stuck in "whose problem is this" limbo — a control-plane API server slowness issue might be entirely the cloud provider's responsibility to fix, while a node running out of disk is entirely yours, and a CNI bug could be either depending on whether you're using the provider's default CNI or a self-managed one you installed. Knowing this boundary *before* an incident, not during one, materially speeds up resolution.

### 🩹 Common Error & Fix

Symptom: "we opened a support ticket about slow API server responses and were told it's a node-level issue, not their responsibility" (or vice versa). This is a shared-responsibility boundary confusion — having the table from the exercise above ready ahead of time avoids wasted back-and-forth during an actual incident.

### 🎤 Interview

**🟢:** Managed vs self-managed Kubernetes?
**🔵:** EKS/AKS/GKE mein control plane kaun manage karta hai?
**🟡:** Shared responsibility boundary kya hoti hai?
**🔴:** Apni organization ke liye managed vs self-managed decision framework banao.

---

# 53 — SERVERLESS / KUBERNETES BOUNDARY

Know when Kubernetes is the platform and when managed/serverless services may be better. Do not put every workload on Kubernetes simply because Kubernetes exists.

Decision factors: operational overhead, scale, latency, statefulness, portability, team expertise, compliance, cost.

### 🧪 Try It Yourself

Take three real workloads from your organization (or hypothetical ones — a nightly batch job, a customer-facing API, an infrequently-invoked internal tool) and score each against the decision factors above. Notice how the "infrequently-invoked internal tool" often scores poorly for Kubernetes (paying for always-on Pods for something invoked twice a day) and well for a serverless/managed alternative, while the "customer-facing API with steady traffic" often inverts that.

### 💡 Extra Insight

A very real, underdiscussed cost of "Kubernetes for everything" is **team cognitive load**, not just infrastructure spend — every workload on Kubernetes means every engineer touching it needs at least baseline Kubernetes literacy (Pods, probes, resource limits, debugging). For a small, infrequent, low-risk internal tool, that literacy tax can genuinely outweigh the platform-consistency benefit of "everything lives in the same place."

### 🩹 Common Error & Fix

Symptom: "we put a rarely-invoked batch job on Kubernetes as a CronJob, and it's now one of our most confusing pieces of infrastructure to debug relative to how often it actually runs." This is a sign the decision factors above were skipped — not every workload benefits from Kubernetes' operational model, and matching workload shape to platform is itself an architecture decision, not a default.

### 🎤 Interview

**🟢:** Kubernetes har workload ke liye zaroori hai kya?
**🔵:** Serverless kab better fit hota hai?
**🟡:** "Everything on Kubernetes" ka hidden cost kya hai?
**🔴:** Workload-to-platform fit decision framework design karo apni organization ke liye.

---

# 54 — MULTI-CLUSTER

Enterprise environments may have Cluster A (Region 1), Cluster B (Region 2), Cluster C (Dev), Cluster D (DR).

Know: fleet management, cluster lifecycle, GitOps, workload placement, service discovery, cross-cluster networking, DR, policy propagation.

### 🧪 Try It Yourself

Design exercise: sketch how a single GitOps repo structure would manage manifests for 4 clusters (dev, staging, prod-region-1, prod-region-2) — where do cluster-specific overrides live (Kustomize overlays? separate Argo CD Applications per cluster?), and how would a policy (e.g., a mandatory NetworkPolicy) get propagated consistently to all 4 without being manually duplicated and drifting out of sync over time.

### 💡 Extra Insight

Cross-cluster service discovery (a service in Cluster A calling a service in Cluster B) is genuinely one of the hardest unsolved-feeling problems in multi-cluster Kubernetes — unlike single-cluster DNS (Module 25), there's no single built-in mechanism; real solutions involve either a service mesh with multi-cluster support, a dedicated multi-cluster gateway/API layer, or purely external DNS/load-balancer-based routing between clusters, each with real trade-offs in latency, complexity, and failure modes.

### 🩹 Common Error & Fix

Symptom: "we manually copy-pasted a NetworkPolicy/RBAC change across 4 cluster repos and one got missed, causing a security gap." This is exactly the policy-propagation problem the design exercise above surfaces — a genuinely multi-cluster-aware GitOps/policy tool (rather than manual copy-paste across repos) is the structural fix, not more careful copy-pasting.

### 🎤 Interview

**🟢:** Multi-cluster kyun zaroori ho sakta hai?
**🔵:** Cross-cluster service discovery mushkil kyun hai?
**🟡:** Policy propagation consistency kaise ensure karoge?
**🔴:** Multi-region, multi-cluster fleet management architecture design karo including DR.

---

# 55 — SERVICE MESH

Know conceptually:

```text
App → Sidecar / node proxy / ambient dataplane → mTLS → Service
```

Topics: mTLS, service identity, retries, traffic policy, telemetry, traffic splitting. Recognize: Istio, Linkerd, ambient mesh concepts.

Don't assume a service mesh is required for every Kubernetes cluster.

### 🧪 Try It Yourself

Design exercise: list 3 specific problems a service mesh solves (mTLS between services without each app implementing TLS itself, consistent retry/timeout policy without each app implementing it, uniform golden-signal telemetry without each app instrumenting itself identically) — then honestly assess whether your own workloads actually have these problems today, or whether they're solved well enough already by simpler means (e.g., a shared HTTP client library with built-in retries). This exercise is meant to counter reflexive "we should add a service mesh" thinking.

### 💡 Extra Insight

The sidecar-per-Pod pattern (classic Istio/Linkerd) has a real resource-overhead cost at scale — every single Pod gets an extra proxy container consuming CPU/memory — which is exactly the problem "ambient mesh" architectures (a newer approach removing the per-Pod sidecar in favor of node-level or shared proxying) are trying to solve. Knowing that this trade-off exists, and that the mesh landscape is still evolving specifically around it, is a genuinely current architect-level talking point.

### 🩹 Common Error & Fix

Symptom: "we added a service mesh and now every Pod uses noticeably more memory/CPU, and debugging network issues got harder (is it the app or the sidecar?)." This is the classic sidecar-overhead-and-complexity trade-off — worth weighing explicitly against the specific problems (from the exercise above) the mesh was meant to solve, rather than adopting one by default.

### 🎤 Interview

**🟢:** Service mesh kya solve karta hai conceptually?
**🔵:** Sidecar pattern ka cost kya hai?
**🟡:** Ambient mesh sidecar problem ko kaise address karta hai?
**🔴:** Apni platform ke liye service-mesh adoption decision (yes/no/which) ka justified case banao.

---

# 56 — NETWORKING ARCHITECTURE DEEP DIVE

Architect should be able to explain:

```text
Pod IP → CNI → Node networking → Service VIP → kube-proxy / replacement dataplane
 → Gateway / LoadBalancer → External client
```

Also: SNAT, DNAT, routing, iptables, IPVS, eBPF, CNI dataplane, service proxying, `externalTrafficPolicy`, `internalTrafficPolicy`.

### 🧪 Try It Yourself

```bash
kubectl get svc demo -o yaml | grep -A2 externalTrafficPolicy
```

Compare a Service with `externalTrafficPolicy: Cluster` (traffic can be forwarded to a Pod on any node, potentially adding an extra network hop, but evenly load-balanced) versus `Local` (traffic only goes to Pods on the node that received it, preserving the original client source IP, but potentially unevenly loaded if Pods aren't spread evenly). Actually testing source-IP visibility with each setting (checking application logs for the client's real IP vs. a node's internal IP) makes the trade-off concrete rather than theoretical.

### 💡 Extra Insight

The historical shift from `iptables` to `IPVS` to `eBPF`-based dataplanes for kube-proxy's job (translating Service VIPs to actual Pod IPs) is fundamentally about **scaling behavior** — iptables rule evaluation is roughly linear in the number of Services/rules, meaning a cluster with thousands of Services can see real latency from iptables rule-chain traversal on every packet; IPVS and eBPF-based approaches use more efficient data structures (hash tables, direct kernel hooks) that don't degrade the same way as Service count grows. This is a genuinely measurable, not just theoretical, scaling concern in very large clusters.

### 🩹 Common Error & Fix

Symptom: "our application logs show the load balancer's/node's IP as the client, not the real external client IP." Check `externalTrafficPolicy` — it's very likely set to `Cluster` (the default), which can SNAT the original source IP away during the extra hop; switching to `Local` (with awareness of its own even-load trade-off) preserves the real client IP.

### 🎤 Interview

**🟢:** Service VIP kya hai physically?
**🔵:** `externalTrafficPolicy: Cluster` vs `Local`?
**🟡:** iptables se IPVS/eBPF shift ka reason kya hai?
**🔴:** Very large cluster (1000s of Services) ke liye dataplane choice justify karo.

---

# 57 — eBPF CURRENT SCENE

```text
Traditional networking → iptables / kernel paths
Modern dataplane options → eBPF
```

Use cases: networking, observability, security, load balancing. Important ecosystem example: Cilium.

## Architect question

**Why is eBPF relevant?** It can move networking/security/observability logic closer to the kernel datapath with programmable hooks, potentially reducing dependence on some traditional packet-processing paths.

### 🧪 Try It Yourself

If you have access to a Cilium-based cluster (or a local kind/minikube cluster where you install Cilium as the CNI):

```bash
cilium status
cilium connectivity test
```

Even without deep eBPF expertise, running these and reading the connectivity test's output gives a concrete sense of what an eBPF-based CNI validates about itself (Pod-to-Pod, Pod-to-Service, cross-node connectivity, policy enforcement) compared to a traditional CNI's more opaque iptables rule chains.

### 💡 Extra Insight

eBPF's real architectural significance isn't "networking is faster" in isolation — it's that the **same underlying technology** (programmable, safely-sandboxed kernel hooks) can power networking, security enforcement (NetworkPolicy at the kernel level instead of iptables rule matching), and deep observability (per-packet, per-syscall visibility) as one coherent platform, rather than three separate bolted-together subsystems each with their own performance characteristics and failure modes.

### 🩹 Common Error & Fix

Symptom: "our eBPF-based CNI works great, but a specific older kernel version in one environment doesn't support the features we rely on." eBPF's capabilities are tied to kernel version/feature support — verify minimum kernel version requirements for your chosen CNI *before* standardizing on it across heterogeneous infrastructure (a common gap when moving between cloud providers or from cloud to on-prem/edge).

### 🎤 Interview

**🟢:** eBPF kya hai conceptually?
**🔵:** Cilium ka role kya hai ecosystem mein?
**🟡:** eBPF networking, security, aur observability ko ek platform kaise bana deta hai?
**🔴:** Heterogeneous infrastructure (cloud + on-prem) ke liye eBPF adoption ka kernel-compatibility-aware plan banao.

---

# 58 — COST / CAPACITY / FINOPS

```text
Node cost + Storage + Load balancers + Network egress + Observability + Control-plane/add-on costs
```

Optimize with: right-sizing, requests accuracy, autoscaling, bin packing, spot/preemptible nodes where appropriate, storage lifecycle, log retention, workload scheduling.

### 🧪 Try It Yourself

```bash
kubectl top pods -A --sort-by=cpu
kubectl top pods -A --sort-by=memory
```

Compare actual usage against configured `requests` for your top consumers (`kubectl get pods -o jsonpath` or a tool like `kubectl-resource-capacity` / VPA's recommendation mode) — a very common finding on a first pass is requests set far higher than actual usage "just to be safe," directly inflating the cluster's minimum required node capacity (and therefore cost) without any real benefit.

### 💡 Extra Insight

Over-provisioned **requests** (not limits) are one of the most underestimated cost drivers in Kubernetes FinOps — the scheduler reserves capacity based on requests, not actual usage, so a fleet of Pods requesting 2 CPU each but actually using 200m collectively forces the cluster to provision (and pay for) capacity for the requested amount, not the real amount. Right-sizing requests based on actual observed usage (ideally informed by VPA's recommendation-only mode) is often a bigger cost lever than switching to spot instances or other more visible optimizations.

### 🩹 Common Error & Fix

Symptom: "our cluster autoscaler keeps adding nodes even though `kubectl top nodes` shows low actual CPU/memory usage cluster-wide." The autoscaler reacts to unschedulable Pods based on **requests**, not real usage — if requests are set far above real usage, the autoscaler will add nodes to satisfy those inflated requests long before actual utilization justifies it; fix the requests, not the autoscaler.

### 🎤 Interview

**🟢:** Cluster cost drivers kya hain?
**🔵:** Requests over-provisioning cost ko kaise inflate karta hai?
**🟡:** Cluster autoscaler requests par react karta hai ya actual usage par?
**🔴:** FinOps-aware capacity planning process design karo (right-sizing + autoscaling combined).

---

# 59 — SLO / SLA / SLI

```text
SLI = successful request percentage
SLO = 99.9%
SLA = contractual commitment
```

```text
SLO → availability → replicas → PDB → multi-AZ → autoscaling → observability
```

### 🧪 Try It Yourself

Pick one real (or hypothetical) service and work backwards from a target SLO of 99.9% availability: how many replicas minimum, spread across how many AZs, with what PDB `minAvailable`, do you need so that losing one AZ (or draining one node at a time) never drops below the replica count required to serve traffic within your latency/error budget? Writing out this chain concretely is the actual skill — most people can recite "SLO informs architecture" without ever having done the arithmetic once.

### 💡 Extra Insight

99.9% availability allows roughly 43 minutes of downtime **per month** — a number that sounds generous until you realize a single un-budgeted node drain, a slow rolling deployment, or one missed PDB configuration can burn through a meaningful fraction of that budget in a single incident. Translating the abstract percentage into "minutes per month" (or "minutes per week" for tighter SLOs) makes the budget's real scarcity concrete for a team that hasn't internalized it yet.

### 🩹 Common Error & Fix

Symptom: "we have a 99.9% SLO on paper, but our actual architecture (single AZ, no PDB, 2 replicas) can't realistically sustain it." This is a sign the SLO was set aspirationally without working backward through the chain above — an honest architecture review against the stated SLO (not just accepting the number) is the actual fix, either by improving the architecture or renegotiating the SLO to match reality.

### 🎤 Interview

**🟢:** SLI/SLO/SLA mein farak?
**🔵:** 99.9% SLO ka downtime budget per month kitna hai?
**🟡:** SLO se replica/PDB/multi-AZ decisions kaise derive karoge?
**🔴:** Ek real service ke liye SLO se architecture tak poora chain design karo.

---

# 60 — KUBERNETES ARCHITECT INTERVIEW SCENARIOS

## Scenario 1 — Pod Pending

```text
describe pod → events → resources → taints → affinity → topology → PVC → scheduler
```

## Scenario 2 — Service has no traffic

```text
Service selector → EndpointSlice → Pod readiness → Pod IP → DNS → NetworkPolicy
```

## Scenario 3 — App keeps restarting

```text
logs → previous logs → exit code → events → probes → resources/OOM → config/secrets
```

## Scenario 4 — Deployment rollout stuck

```text
rollout status → ReplicaSet → new Pods → readiness → image → resources → events
```

## Scenario 5 — Traffic works internally but not externally

```text
Service → Ingress/Gateway → LoadBalancer → DNS → TLS → cloud firewall/security group → NetworkPolicy
```

### 🧪 Try It Yourself

Pick whichever of these 5 scenarios you're personally least confident diagnosing under pressure, and actually reproduce it end-to-end on a throwaway cluster this week (each earlier module in this add-on has the specific commands you'd need). Reading the diagnosis chain is recognition; having walked it once yourself is recall — the difference matters enormously during a real incident or interview.

### 💡 Extra Insight

All 5 scenarios above share a structural pattern worth naming explicitly: **each is a chain of dependent layers, and the fix is always finding the first broken link, not guessing at the last one.** Interviewers (and real incidents) reward people who work systematically down a chain like this rather than jumping to the most "interesting" or most recently-learned-about possible cause first.

### 🩹 Common Error & Fix

A meta-error worth naming: jumping straight to `kubectl logs` or restarting things before running `kubectl describe` / checking events, for *any* of these 5 scenarios. Events and `describe` output are almost always the fastest, cheapest first step and are skipped surprisingly often under pressure.

### 🎤 Interview

**🟢:** In paanch scenarios mein se koi ek explain karo.
**🔵:** Har scenario ka "first check" kya hona chahiye?
**🟡:** Chain-based diagnosis approach ka value kya hai vs guessing?
**🔴:** Ek unified troubleshooting runbook design karo jo in saari scenario-chains ko cover kare.

---

# 61 — MASTER INTERVIEW LADDER

## 🟢 BEGINNER

1. What is Kubernetes?
2. What is a Pod?
3. What is a Node?
4. What is a Cluster?
5. What is kubectl?
6. What is a Deployment?
7. What is a Service?
8. What is a Namespace?
9. What is ConfigMap?
10. What is Secret?

## 🔵 DEVELOPER

1. Deployment vs StatefulSet?
2. Service types?
3. ConfigMap vs Secret?
4. Readiness vs liveness?
5. Requests vs limits?
6. What causes CrashLoopBackOff?
7. What causes ImagePullBackOff?
8. How does Service discovery work?
9. What is Ingress?
10. What is PVC?

## 🟡 SENIOR

1. How does the scheduler choose a node?
2. Taints vs affinity?
3. HPA vs VPA?
4. How does kube-proxy work?
5. How does CNI work?
6. How do NetworkPolicies work?
7. How do rolling updates work?
8. How do you debug a Pending Pod?
9. How do you debug a Service with zero endpoints?
10. How do you perform a node drain safely?
11. Why can't HPA and VPA safely manage the same metric together? *(new)*
12. What's the difference between mutating and validating admission webhooks, and their order? *(new)*

## 🟠 LEAD

1. Design a multi-AZ cluster.
2. Design zero-downtime deployment.
3. Design secure namespace multi-tenancy.
4. Design autoscaling for burst traffic.
5. Design centralized observability.
6. Design secrets management.
7. Design cluster upgrade strategy.
8. Design DR.
9. Choose Ingress vs Gateway API.
10. Choose CNI and CSI architecture.
11. Design a FinOps process around requests right-sizing. *(new)*
12. Design an etcd backup/restore rehearsal program. *(new)*

## 🔴 ARCHITECT

1. Design Kubernetes for 100+ teams.
2. Design multi-cluster multi-region architecture.
3. Design platform engineering model.
4. Design GitOps operating model.
5. Design supply-chain security.
6. Design tenant isolation.
7. Design stateful workloads.
8. Design cost governance.
9. Design disaster recovery.
10. Decide when Kubernetes should NOT be used.
11. Design an SLO-to-architecture derivation process end to end. *(new)*
12. Design node-level hard isolation for untrusted multi-tenant workloads. *(new)*

---

# 📖 GLOSSARY (New)

| Term | Plain-language meaning |
|---|---|
| **ReplicaSet** | Ensures a specified number of Pod replicas are running; usually managed by a Deployment, not directly. |
| **EndpointSlice** | Tracks the current set of ready backend addresses for a Service, replacing the older single `Endpoints` object at scale. |
| **CNI** | Container Network Interface — the pluggable layer implementing Pod networking. |
| **CSI** | Container Storage Interface — the pluggable layer implementing storage provisioning/attachment. |
| **QoS class** | Guaranteed/Burstable/BestEffort — derived automatically from a Pod's requests/limits configuration. |
| **PodDisruptionBudget (PDB)** | A policy limiting how many Pods of a set can be voluntarily disrupted at once. |
| **Taint / Toleration** | A node-side repellent and a Pod-side "I can tolerate this repellent" pairing controlling scheduling eligibility. |
| **Admission controller** | API-server-side logic that validates or mutates requests after authN/authZ, before persistence. |
| **CRD (Custom Resource Definition)** | Extends the Kubernetes API with a new, user-defined resource kind. |
| **Operator** | A controller encoding domain-specific operational knowledge, reconciling custom resources into real infrastructure/state. |
| **Finalizer** | A marker preventing an object's actual deletion until associated cleanup work completes. |
| **OwnerReference** | A metadata link establishing parent/child ownership for cascading garbage collection. |
| **GitOps** | Using Git as the source of truth for desired cluster state, reconciled continuously by a controller. |
| **Drift** | When the live cluster state diverges from what's declared in Git (or another desired-state source). |
| **Gateway API** | The newer, role-oriented, extensible traffic-routing model succeeding Ingress for new development. |
| **Service mesh** | An infrastructure layer (often sidecar- or ambient-based) providing mTLS, retries, traffic policy, and telemetry uniformly across services. |
| **eBPF** | A kernel technology allowing safe, programmable hooks used for modern networking, security, and observability datapaths. |
| **SLI / SLO / SLA** | The measured indicator, the target for that indicator, and the contractual commitment built on top of it. |
| **Workload identity** | Federating a Kubernetes ServiceAccount to a cloud IAM identity to avoid static long-lived credentials. |

---

# 🩺 COMMON ERROR MESSAGES REFERENCE (New)

| Error message (shortened) | Likely cause | Typical fix |
|---|---|---|
| `Service has no endpoints` | Selector doesn't match Pod labels | Compare `kubectl get endpointslices` and `kubectl get pods --show-labels` |
| `0/N nodes are available: Insufficient cpu/memory` | Requests exceed available node capacity | Right-size requests, or add node capacity |
| `0/N nodes are available: node(s) had volume node affinity conflict` | PV provisioned in a different zone than eligible nodes | Align node affinity/zone with the PV's actual zone |
| `0/N nodes are available: node(s) didn't match pod anti-affinity rules` | More replicas than distinct topology domains under `required` anti-affinity | Add nodes/zones, reduce replicas, or switch to `preferred` |
| `pods "x" is forbidden: failed quota` | ResourceQuota requires requests/limits not specified on the Pod | Add explicit resources, or supply namespace defaults via LimitRange |
| `OOMKilled` | Container exceeded its memory limit | Confirm real usage vs. limit; raise limit or fix a leak |
| `Cannot evict pod as it would violate the pod's disruption budget` | PDB `minAvailable` would be breached by this eviction | Wait for capacity, adjust PDB deliberately, or scale up first |
| `Error from server: admission webhook denied the request` | A validating/mutating webhook rejected the object | Check webhook logs; remember mutating webhooks run first |
| `resource mapping not found ... no matches for kind` | Manifest references a removed API version | Update to the current stable API version |
| `unable to get metrics for resource cpu` | `metrics-server` missing/unhealthy | Verify `kubectl top pods` works before troubleshooting HPA |
| `UPGRADE FAILED: another operation ... in progress` (Helm) | A previous Helm operation didn't complete cleanly | `helm history`, then `helm rollback` to a known-good revision |
| `etcdserver: request timed out` | etcd disk/network latency issue | Check etcd disk I/O and inter-member network latency |

---

# ⚡ COMMAND REFERENCE BY TASK (New)

```bash
# ---- Workloads ----
kubectl create deployment demo --image=nginx --replicas=3
kubectl get rs,pods -o wide
kubectl set image deployment/demo demo=myapp:v2
kubectl rollout status deployment/demo
kubectl rollout history deployment/demo
kubectl rollout undo deployment/demo

# ---- Services & networking ----
kubectl expose deployment demo --port=80 --target-port=80
kubectl get endpointslices -l kubernetes.io/service-name=demo
kubectl get svc demo -o yaml | grep -A2 externalTrafficPolicy

# ---- Storage ----
kubectl get storageclass
kubectl get pvc
kubectl get pv
kubectl get csidrivers
kubectl get csinodes

# ---- Config & secrets ----
kubectl create configmap demo-config --from-literal=key=value
kubectl create secret generic demo-secret --from-literal=password=hunter2
kubectl get secret demo-secret -o jsonpath='{.data.password}' | base64 -d

# ---- Resources & QoS ----
kubectl describe pod <name> | grep -A3 "QoS Class"
kubectl top pods -A --sort-by=cpu
kubectl top nodes

# ---- Scheduling ----
kubectl get pods -o wide
kubectl describe pod <name>   # events show scheduling failures in plain language

# ---- Autoscaling ----
kubectl autoscale deployment demo --cpu-percent=50 --min=2 --max=10
kubectl get hpa -w

# ---- Disruption / node maintenance ----
kubectl cordon <node>
kubectl drain <node> --ignore-daemonsets --delete-emptydir-data
kubectl uncordon <node>

# ---- RBAC ----
kubectl create role pod-reader --verb=get,list,watch --resource=pods -n default
kubectl create rolebinding demo-binding --role=pod-reader --serviceaccount=default:demo-sa -n default
kubectl auth can-i list pods --as=system:serviceaccount:default:demo-sa

# ---- Debugging ----
kubectl describe pod <name>
kubectl logs -f <pod>
kubectl logs -f <pod> --previous
kubectl exec -it <pod> -- sh
kubectl get events --sort-by='.lastTimestamp'

# ---- Helm / Kustomize ----
helm install demo ./chart
helm upgrade demo ./chart --set replicaCount=5
helm rollback demo 1
kubectl apply -k overlays/prod

# ---- etcd ----
ETCDCTL_API=3 etcdctl snapshot save /tmp/etcd-backup.db --endpoints=https://127.0.0.1:2379 \
  --cacert=/etc/kubernetes/pki/etcd/ca.crt --cert=/etc/kubernetes/pki/etcd/server.crt --key=/etc/kubernetes/pki/etcd/server.key

# ---- API deprecation audit ----
kubectl api-resources --verbs=list --api-group=extensions
```

---

# 🧠 FINAL KUBERNETES ARCHITECT MAP

```text
KUBERNETES
│
├── FOUNDATION:      Pod, Node, Cluster, kubectl
├── CONTROL PLANE:   API Server, etcd, Scheduler, Controllers, Admission
├── WORKLOADS:       Deployment, StatefulSet, DaemonSet, Job, CronJob
├── NETWORKING:      CNI, Service, DNS, NetworkPolicy, Ingress, Gateway API, eBPF
├── STORAGE:         PV, PVC, StorageClass, CSI
├── CONFIG:          ConfigMap, Secret, External Secrets
├── SCHEDULING:      Requests/Limits, Affinity, Taints/Tolerations, Topology, Priority/Preemption
├── RESILIENCE:      Probes, PDB, Rollout/Rollback, Multi-AZ, DR
├── SECURITY:        RBAC, ServiceAccounts, Pod Security, NetworkPolicy, Admission, Supply Chain
├── SCALE:           HPA, VPA, Node Autoscaling, KEDA
├── EXTENSIBILITY:   CRD, Operators, Controllers, Webhooks
├── PLATFORM:        Helm, Kustomize, GitOps, Argo CD / Flux, CI/CD
└── ARCHITECTURE:    Multi-cluster, Multi-region, Service Mesh, FinOps, SLO/SLA, Platform Engineering,
                      When NOT to use Kubernetes
```

---

# 🎯 CURRENT-SCENE PRIORITY

If time is limited, prioritize these after your existing Pod/kubectl modules:

```text
1. Deployment / StatefulSet / DaemonSet
2. Service + EndpointSlice + DNS
3. CNI + Networking
4. NetworkPolicy
5. Ingress → Gateway API
6. PV / PVC / StorageClass / CSI
7. Requests / Limits / QoS
8. Scheduling / Affinity / Taints
9. Probes + PDB
10. RBAC + Pod Security
11. HPA / VPA / Node Autoscaling / KEDA
12. ConfigMap / Secrets
13. Helm / Kustomize
14. CRD / Operators
15. Observability
16. GitOps
17. Supply-chain security
18. etcd / upgrades
19. Multi-AZ / DR / multi-cluster
20. Architect scenarios
```

## Most important current-scene change

For networking, don't learn only traditional Ingress. Kubernetes currently describes Ingress as frozen and recommends Gateway API for new development; Gateway API is the newer extensible traffic-routing model.

Also don't learn Kubernetes as only "Pods + Deployments". Production architecture requires workload controllers, Services/EndpointSlices, CNI, storage/CSI, security/policy, scheduling, autoscaling, observability, and extensibility.

---

# 🧪 TWO ADDITIONAL REAL-WORLD SCENARIOS (New)

## Scenario 6 — HPA and VPA Fighting Each Other

```text
HPA scales replicas out based on average CPU per Pod
 → VPA simultaneously resizes each Pod's CPU request
 → the definition of "high average CPU" keeps shifting under HPA's feet
 → replica count oscillates unpredictably
 → Fix: run VPA in recommendation-only mode alongside HPA, or scope each to different resources/workloads entirely
```

## Scenario 7 — GitOps Drift Reversal During an Incident

```text
Production incident → engineer runs kubectl edit to fix it live, unblocking users
 → GitOps controller's next reconciliation cycle detects drift from Git
 → reverts the manual fix back to the (still-broken) declared state
 → same incident recurs minutes later, confusing the responder
 → Fix: commit the fix to Git immediately, or explicitly pause the GitOps controller's reconciliation during active incident response
```

---

# 🏆 FINAL LEARNING UPGRADE

```text
Beginner   "What is a Pod/Service/Deployment?"
Developer  "How do I deploy and expose my app?"
Senior     "Why did this fail, and how do I diagnose it systematically?"
Lead       "How do we run this safely and reliably at team/organization scale?"
Architect  "Where are the isolation boundaries, failure domains, cost drivers,
            security boundaries, and — critically — where does Kubernetes
            stop being the right tool at all?"
```

> **Architect-level Kubernetes knowledge = workloads + networking + storage + security + scheduling + autoscaling + extensibility + GitOps + observability + cost + multi-cluster/DR + knowing when NOT to use Kubernetes.**
