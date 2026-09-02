# Kubernetes Administration: Zero to Architect

The orchestration course written the way you would defend it in an architecture review - what the control
plane actually does, why a Pod is the unit and not a container, what breaks at 3am, and what it costs.

> Written by **Himanshu Kumar**. Part of [Enterprise Stack: Zero to Architect](../README.md).

**All Markdown, nothing to download.** Every lesson renders right here on GitHub, with Mermaid diagrams
showing the happy path and the failure path side by side. Start at [the course overview](index.md).

---

## Why this exists

Most Kubernetes material teaches you `kubectl apply` and stops. That is the easy 20%. The other 80% is:

- Why Kubernetes exists when Docker Swarm already did orchestration
- Why the smallest thing you deploy is a Pod, not a container
- Why your Pod is `Pending` and no event obviously says why
- Why `CrashLoopBackOff` is a symptom, never a cause
- Why a missing `resources.requests` breaks the scheduler, not just the node
- Why a Service works and Ingress does not, on the same cluster
- What actually happens to your data when a Pod is rescheduled to another node
- When Kubernetes is the wrong answer, and something simpler wins

This course is written from that side of the line.

---

## Prerequisite

This assumes the container fundamentals from [Docker: Zero to Architect](../docker/README.md) - images,
layers, volumes, networking and resource limits. Kubernetes schedules containers; it does not replace
understanding them. Modules 11, 16 and 17 of that course are the ones you will lean on most.

---

## Modules

Building now. Modules land here as each one is finished.

| # | Module | What it covers |
| --- | --- | --- |
| 01 | [What is Kubernetes](lessons/01-what-is-kubernetes.md) | Containers, microservices, why one host is a liability, and what orchestration actually owns |
| 02 | [Kubernetes defined, and the architecture](lessons/02-kubernetes-defined-and-architecture.md) | The interview answer, control plane and worker nodes, why Kubernetes manages Pods and not containers, and what it does *not* do |
| 03 | [Architecture part 1: the control plane](lessons/03-control-plane-architecture.md) | kubectl, the API server hub, etcd, controllers and the reconciliation loop, the scheduler, and what breaks when each component dies |
| 04 | [Architecture part 2: the worker node](lessons/04-worker-node-architecture.md) | kubelet as the control plane's agent, CRI and the runtime, kube-proxy, the full twelve-step journey, and what a dead node really costs |
| 05 | [Choosing a lab](lessons/05-lab-setup-options.md) | kubeadm, EKS/AKS/GKE, kind, k3s, k3d and minikube - the names decoded, the tradeoffs, and the one to actually use |
| 06 | [Building the lab, step by step](lessons/06-lab-build.md) | Windows to VMware to Ubuntu to Docker to k3d, in 30 minutes - with the networking fix, the troubleshooting table, and an nginx page as proof |
| 07 | [Removing lab friction, and a 30-day plan](lessons/07-lab-friction-and-plan.md) | Bootstrap scripts, OVA appliances, the guided-practice-challenge ladder, self-validation, the CKA, and a day-by-day plan |
| 08 | [kubectl: the remote control](lessons/08-kubectl.md) | Why kubectl is *not* Kubernetes, kubeconfig and contexts, the verbs that do 90% of the work, output formats, and the wrong-cluster trap |

Every module follows the same six blocks: **mental model → mechanics → build it → what breaks →
cost & performance → interview drill**.

---

## House rules

1. **Understand the control loop first.** Every Kubernetes behaviour is a consequence of "observe desired
   state, observe actual state, act to close the gap".
2. **Read the failure path.** Every diagram shows what goes wrong next to what goes right, because that
   is what you are paid to recognise.
3. **`kubectl describe` before `kubectl logs`.** Most failures are visible in events long before they
   reach application logs.
4. **Type the commands.** Reading YAML teaches you nothing about what the API server rejects.
5. **Every lesson ends with interview questions,** answered the way an architect answers them.

---

## Attribution & ownership

Copyright (c) 2026 Himanshu Kumar. All rights reserved.

This material is my original work. Reading it here and linking to it is welcome.
Downloading, copying, mirroring, forking, redistributing, or using it to train an
AI model requires **prior written permission**.

Request permission: [github.com/89himanshu-dwivedi](https://github.com/89himanshu-dwivedi)

## License

See [LICENSE](../LICENSE) &mdash; proprietary, all rights reserved, permission required.
