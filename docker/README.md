# Docker: Zero to Architect

A container course written the way you would defend it in an architecture review - what a container
actually is at the kernel level, how the pieces fit, what breaks in production, and what the cost is.

> Written by **Himanshu Kumar**. Part of [Enterprise Stack: Zero to Architect](../README.md).

**All Markdown, nothing to download.** Every lesson renders right here on GitHub, with Mermaid diagrams
showing the happy path and the failure path side by side. Start at [the course overview](index.md) or
[module 01](lessons/01-why-containers.md).

---

## Why this exists

Most Docker material teaches you five commands and stops. That is the easy 20%. The other 80% is:

- Why a container is "lightweight" - and the exact kernel feature that makes it so
- Why your image is 1.2 GB when the app is 8 MB
- What actually happens to your data when a container is deleted
- Why adding yourself to the `docker` group is the same as handing out root
- When a container is the wrong answer

This course is written from that side of the line.

---

## Modules

| # | Module | What it covers |
| --- | --- | --- |
| 01 | [Why containers exist](lessons/01-why-containers.md) | Physical machines, hardware virtualisation, and the problem containers solve |
| 02 | [OS-level virtualisation](lessons/02-os-virtualization.md) | Kernel vs user space, namespaces, cgroups, union filesystems |
| 03 | [VM vs container](lessons/03-vm-vs-container.md) | The honest comparison, host/guest OS rules, when a VM still wins |
| 04 | [Docker architecture](lessons/04-docker-architecture.md) | Host, daemon, REST API, CLI, registry, image vs container, containerd and runc |
| 05 | [Installing Docker](lessons/05-installation.md) | Requirements, both Ubuntu methods, post-install, and the `docker` group trap |
| 06 | [The Docker CLI](lessons/06-docker-cli.md) | Command grammar, flags, formatting, tab completion, `--help` |
| 07 | [Images](lessons/07-images.md) | Pull, tags vs digests, layers, caching, inspect, prune |
| 08 | [Your first containers](lessons/08-first-container.md) | `run`, `-it`, `-d`, ports, lifecycle, logs, exec |
| 09 | [Dockerfiles](lessons/09-dockerfile.md) | Instructions, layer caching, multi-stage builds, `.dockerignore`, security |
| 10 | [Registries](lessons/10-registries.md) | Docker Hub, push, private and cloud registries, tagging strategy, scanning |
| 11 | [Storage and networking](lessons/11-storage-networking.md) | Volumes, bind mounts, networks, DNS, port publishing |
| 12 | [Docker Compose](lessons/12-compose.md) | Multi-container stacks, dependencies, environments |
| 13 | [Production and interviews](lessons/13-production.md) | Security, limits, health checks, logging, CI/CD, when to move to Kubernetes |

Every module follows the same six blocks: **mental model → mechanics → build it → what breaks →
cost & performance → interview drill**.

---

## House rules

1. **Understand the kernel story first.** Everything else is a consequence of it.
2. **Every image gets a pinned base and a non-root user.** Convenience is not a security model.
3. **Containers are disposable; data is not.** If it must survive, it lives in a volume.
4. **Measure image size and start-up time** like you measure latency.
5. **The failure path is documented next to the happy path**, always.

---

## Running the examples

```bash
# any 64-bit Linux host, or a cloud VM, or Docker Desktop
docker --version
docker run --rm hello-world
```

Examples use Ubuntu 22.04 as the Docker host, because it is available on every cloud free tier.

---

## A note on versions

Docker versions, package names and cloud consoles change. This course teaches the model and the
trade-offs, which do not. Always confirm current commands against the official Docker documentation.
