# Kubernetes Administration: Zero to Architect

*Course overview*

Containers solved packaging. They did not solve running hundreds of them across dozens of machines,
surviving node failure, rolling out a new version without downtime, and telling you why none of that is
working right now. That is the job Kubernetes took.

[Repo home](../README.md) / Kubernetes

---

## How to read this

Every lesson is built the same way:

| Block | What it gives you |
| --- | --- |
| **Mental model** | The one idea the rest of the lesson hangs off |
| **Mechanics** | What actually happens, component by component |
| **Build it** | Commands and manifests you type yourself |
| **What breaks** | The failure path, drawn next to the happy path |
| **Cost & performance** | The numbers you bring to a review |
| **Interview drill** | Questions answered the way an architect answers them |

Mermaid diagrams show the happy path in plain nodes and the failure path in red. Read both - the red one
is the half you get paid for.

```mermaid
flowchart LR
    S0["You declare desired state"]
    S1["API server accepts and stores it"]
    S2["Controller sees a gap"]
    S3["Scheduler places the work"]
    S4["Kubelet runs it, reports back"]
    S5["Actual state matches desired state"]
    S0 --> S1
    S1 --> S2
    S2 --> S3
    S3 --> S4
    S4 --> S5
    F0["Node dies at 3am"]
    F1["Kubelet stops reporting"]
    F2["Controller sees the gap again"]
    F3["Work is rescheduled elsewhere - no human involved"]
    S4 -.->|"fails"| F0
    F0 --> F1
    F1 --> F2
    F2 --> F3
    classDef bad fill:#fdecea,stroke:#c62828;
    class F0,F1,F2,F3 bad;
```

> **Why it matters:** That loop is the entire product. Deployments, Services, autoscalers and operators are all the same loop with a different controller in the middle. Learn it once and the rest stops feeling like memorisation.

---

## Modules

### [What is Kubernetes](lessons/01-what-is-kubernetes.md)

`MODULE 01`

The definition, and the two words hiding inside it.

- physical machine to VM to container, and why the unit kept shrinking
- monolith to microservices, and what that did to the runtime
- why a container is "lightweight" - and where the "lightweight VM" analogy breaks
- the container host, and its single point of failure
- why more hosts alone gives you islands, not availability
- the eight jobs an orchestrator actually owns
- when Kubernetes is the wrong answer

### [Kubernetes defined, and the architecture](lessons/02-kubernetes-defined-and-architecture.md)

`MODULE 02`

The first interview question, answered three ways - and the picture it refers to.

- the formal definition, and the four jobs inside it
- control plane and worker nodes in one diagram
- the surprise: Kubernetes manages **Pods**, not containers
- why the Pod wrapper exists at all
- manual estate vs orchestrated, task by task
- the eight facilities, each with the caveat nobody mentions
- what Kubernetes is **not**, and why `CrashLoopBackOff` is not its fault

### [Architecture part 1: the control plane](lessons/03-control-plane-architecture.md)

`MODULE 03`

The diagram every Kubernetes interview is built on.

- kubectl, and why it is just an HTTP client
- the API server as the hub - nothing talks around it
- authentication, authorization, admission, validation
- etcd: the only stateful thing you own
- `kubectl apply` to `3/3 Running`, traced in twelve steps
- controllers and the reconciliation loop that *is* Kubernetes
- the scheduler: filter, score, bind - and why Pods sit in `Pending`
- what breaks when each component dies, and what keeps serving

### [Architecture part 2: the worker node](lessons/04-worker-node-architecture.md)

`MODULE 04`

Where the decision becomes a running container.

- kubelet: the control plane's agent posted on every node
- the direction that surprises people - nodes **pull** work, nothing is pushed
- CRI, containerd, runc, and why dockershim was removed
- steps 8 to 12, and how the status loop closes back at the controller
- kube-proxy, and the clusters that do not have one
- the complete twelve-step journey in one diagram
- what a dead node costs: 40 seconds, then 5 minutes - and the dead-kubelet trap

### [Choosing a lab](lessons/05-lab-setup-options.md)

`MODULE 05`

The hardest part of learning Kubernetes is not Kubernetes. It is the lab.

- K8s, kubeadm, kind, k3s, k3d, minikube - every name decoded
- two distributions, two ways to host their nodes: the whole landscape in one grid
- kubeadm and the three-VM, 16 GB trap
- why a cloud cluster is the wrong place to learn `kubectl`
- k3s is genuinely certified Kubernetes, not a lookalike
- **k3d: one machine, three containers - the lab this course uses**
- what you can never test locally, and when to pay for a cloud cluster

### [Building the lab, step by step](lessons/06-lab-build.md)

`MODULE 06`

Thirty minutes, no cloud account, a real multi-node cluster.

- Windows to VMware to Ubuntu to Docker to k3d to Kubernetes
- why installing straight onto Windows breaks the networking modules later
- the Ubuntu install, and the one checkbox you must not skip
- NAT, VMnet8, and what to do when there is no IP
- Docker, k3d and kubectl, with the `docker` group trap
- one control plane, two workers - and `docker ps` proving they are containers
- the final test: an nginx page from your cluster in your browser
- a troubleshooting table, and the snapshot habit that saves hours

### [Removing lab friction, and a 30-day plan](lessons/07-lab-friction-and-plan.md)

`MODULE 07`

Why people quit Kubernetes at the lab, not at the concepts.

- friction decides how often you practise, and practice decides everything
- bootstrap and cluster scripts: one command to a clean, known-good cluster
- OVA appliances - a lab that imports in three minutes
- the guided → practice → challenge ladder, and why skipping the middle fails
- what a complete 136-lab curriculum looks like
- write your own validation scripts - challenge mode, for free
- the CKA, and a realistic day-by-day 30-day plan

### [kubectl: the remote control](lessons/08-kubectl.md)

`MODULE 08`

The client. Not Kubernetes - and everything that follows from that.

- kubectl is a remote control; the remote is not the television
- prove it is just HTTP with `-v=8`
- kubeconfig: clusters, users, contexts - and the wrong-cluster trap
- the shape of every command, and the eight verbs that do 90% of the work
- `describe` before `logs`, always
- output formats, jsonpath, and generating YAML with `--dry-run=client`
- namespaces, RBAC, `auth can-i`, autocompletion and speed

### [kubectl actions](lessons/09-kubectl-actions.md)

`MODULE 09`

The five buttons on the remote that do almost everything.

- get, run, describe, create, delete - actions, not commands
- one Pod born, inspected and destroyed, step by step
- why `run` for Pods but `create` for everything else
- namespaces as folders, and the delete that takes everything with it
- `delete` is asynchronous, and why a deleted Pod comes straight back

### [Discovering resources](lessons/10-api-resources-and-explain.md)

`MODULE 10`

Used smartly, kubectl means you memorise nothing.

- `api-resources`: every kind, short name, apiVersion and scope
- why a Pod is `v1` but a Deployment is `apps/v1`
- the `no matches for kind` error, explained once and for all
- short names, and why they matter under exam pressure
- `kubectl explain`, drilled level by level, to a working manifest
- `--recursive`, and generating skeletons with `--dry-run=client`

### [Output formats](lessons/11-output-formats.md)

`MODULE 11`

Where you stop reading Kubernetes and start querying it.

- YAML for humans, JSON for machines - and why that is the whole rule
- the escalation ladder: default → wide → yaml → json
- `spec` vs `status`, and why the gap between them is your bug
- the defaults Kubernetes filled in that you never wrote
- jsonpath, custom-columns, `--sort-by`, and `jq`
- why `-o yaml > file.yaml` is not really a backup

### [Imperative vs declarative](lessons/12-imperative-vs-declarative.md)

`MODULE 12`

Practical first, theory second - the same Pod, two roads.

- `kubectl run` versus a file and `kubectl apply -f`
- the full comparison: speed, reuse, review, version control, production
- why `apply` can update an object when `create` cannot
- `kubectl diff` before `kubectl apply`, every time
- configuration drift, and the fix that silently disappears
- GitOps as the reconciliation loop, one level up

### [What is a Pod](lessons/13-what-is-a-pod.md)

`MODULE 13`

Understand the Pod and you understand half of Kubernetes.

- the journey: code → Dockerfile → image → container
- the twist: Kubernetes never places a container, only a Pod
- reason 1: some containers must live together - the sidecar pattern
- reason 2: one standard layer over containerd, CRI-O and anything next
- what "share network and storage" really means
- the pause container you never asked for
- why Pods are ephemeral, and why that explains everything later

### [Creating and inspecting a Pod](lessons/14-creating-pods.md)

`MODULE 14`

The commands you will type thousands of times.

- `kubectl run` broken down word by word
- where the image comes from when you never downloaded it
- `0/1 ContainerCreating` → `1/1 Running`, read properly
- `-o wide`: the Pod's IP, the node, and who chose it
- `describe`: the image digest, and the twelve-step flow in the Events
- why `nginx` is really `docker.io/library/nginx:latest`
- why a bare Pod dies with its node

### [Pod status and lifecycle](lessons/15-pod-status-lifecycle.md)

`MODULE 15`

Four statuses, produced on purpose so you never have to guess.

- reading `READY` and `STATUS` as two different questions
- `Running`, `Completed`, `Error` - and exit code 0 versus everything else
- the `--restart=Never` mystery, and why `CrashLoopBackOff` is not an error
- "BackOff" is the delay, not the crash
- exit codes worth recognising, including 137 and OOMKilled
- the wider status list, with the first command for each

### [Logs, exec and port-forward](lessons/16-logs-exec-portforward.md)

`MODULE 16`

What it printed, getting inside it, and opening it in a browser.

- `logs` versus `describe` - instruction is not result
- `-f`, `--tail`, `--since`, and the `--previous` that saves you
- why an app logging to a file produces no logs at all
- `exec`, the `--` separator, and shells that do not exist
- why fixing production with `exec` is drift you cannot see
- `port-forward` tunnels through the API server, not the Pod network
- the four-command debugging loop, in order

### [Creating a Pod with YAML](lessons/17-pod-yaml.md)

`MODULE 17`

The one file in this course worth typing by hand.

- get the apiVersion from `api-resources`, never guess it
- `apiVersion`, `kind`, `metadata`, `spec` - the shape of every manifest
- why `containers` needs a dash and `name` does not
- two spaces, never a tab - and the exact errors when you get it wrong
- `--dry-run=client` and `diff` before you apply
- everything you will later add to this same file

### [Pod environment variables](lessons/18-pod-environment-variables.md)

`MODULE 18`

Why a hard-coded value costs you an image rebuild.

- the 10.10.10.10 to 11.11.11.11 problem, drawn
- variable defined in the code, value supplied in the YAML
- `printenv` from inside a running container
- `spec: Forbidden: pod updates may not change fields...` - on purpose
- recreate the **Pod**, but never the **image**
- ConfigMaps, Secrets, `envFrom` and the Downward API
- why a password must never go in `value:`

---

## Before you start

You need the container fundamentals: images and layers, volumes, networking, and resource limits. If any
of those are shaky, work through [Docker: Zero to Architect](../docker/README.md) first - especially
modules 11 (storage and networking), 16 (resource limits) and 17 (monitoring and logging). Kubernetes
assumes all of it.

---

Kubernetes Administration: Zero to Architect · Himanshu Kumar.
