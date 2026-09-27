# 🐳 DOCKER --- ZERO → ARCHITECT MASTER NOTES (Enriched Edition)

> **19 Modules • Hinglish • Print-Friendly • Source Coverage Preserved • Interview Ladder**
>
> **Format:** Mental Model → Mechanics → Build/Use → What Breaks → Cost/Performance → Interview Drill
>
> **Question ladder:** 🟢 Beginner → 🔵 Junior/Developer → 🟡 Senior → 🟠 Lead → 🔴 Architect

**What's new in this enriched edition:** every module now has a **🧪 Try It Yourself** hands-on lab (with sample commands and expected-shape output), a **💡 Extra Insight** callout with a detail that separates "knows the command" from "understands the system," and a **🩹 Common Error & Fix** box for the mistake people actually hit first. A new **Glossary**, **Common Error Messages Reference**, and **Command Reference by Task** have been added at the end.

---

# 🗺️ 19-MODULE ROADMAP

| #  | Module                  | Core Focus                                                  |
|----|--------------------------|-------------------------------------------------------------|
| 01 | Why Containers Exist    | Physical machines → VMs → containers                        |
| 02 | OS-level Virtualisation | Kernel, user space, namespaces, cgroups, union FS            |
| 03 | VM vs Container         | Honest comparison + when VM wins                             |
| 04 | Docker Architecture     | CLI, daemon, API, registry, image/container, containerd/runc |
| 05 | Installing Docker       | Ubuntu methods, post-install, `docker` group security         |
| 06 | Docker CLI              | Grammar, flags, formatting, help, completion                 |
| 07 | Images                  | Pull, tags/digests, layers, cache, inspect, prune             |
| 08 | First Containers        | `run`, ports, lifecycle, logs, exec                          |
| 09 | Dockerfiles             | Instructions, caching, multi-stage, `.dockerignore`, security |
| 10 | Registries              | Docker Hub/private/cloud registry, push, tags, scanning       |
| 11 | Storage & Networking    | Volumes, bind mounts, tmpfs, six drivers, DNS, ports           |
| 12 | Docker Compose          | Multi-container app + YAML workflow                          |
| 13 | Docker Swarm            | Managers/workers, Raft, HA, rolling updates                   |
| 14 | Swarm Networking & LB   | Overlay, VIP, routing mesh, external NGINX                    |
| 15 | Stacks, Distribution & Scaling | Compose vs stack, registry, image distribution, replicas |
| 16 | Resource Limits         | Memory, CPU, hard/soft limits, reservations                   |
| 17 | Monitoring & Logging    | `stats`, cAdvisor, log drivers, rotation, EFK                  |
| 18 | Production & Interviews | Security, health, CI/CD, limits of Docker alone                |
| 19 | Salesforce + AI + Cloud | `sf` CLI, Heroku, MuleSoft, Agentforce, BYO models, MCP        |

---

# 01 --- WHY CONTAINERS EXIST

## Mental Model

Old world:

```text
Hardware → OS → Runtime → Application
```

One physical machine traditionally meant one active OS.

Problems: one workload/server, low utilization, provisioning slow, OS patch blast radius, incompatible library/runtime versions, classic **"works on my machine."**

## Hardware Virtualisation

VMs introduced:

```text
Hardware
 ↓
Hypervisor
 ↓
VM 1 → Guest OS → App
VM 2 → Guest OS → App
VM 3 → Guest OS → App
```

Types: `Type 1` → bare metal, `Type 2` → hosted on host OS.

VM solved: utilization, provisioning, multiple OSs on one machine. But every VM still carries a complete guest OS.

## Container Insight

Docker's major contribution was packaging:

```text
Application + Runtime + Libraries + Configuration → Image → Container
```

Container shares host kernel:

```text
Container 1 ─┐
Container 2 ─┼→ Host Kernel → Hardware
Container 3 ─┘
```

## Key Trade-off

```text
More sharing → More density + speed → Thinner isolation boundary
```

Containers did **not** replace VMs. Real enterprise pattern often is:

```text
Cloud VM → Container Runtime → Many Containers
```

## Why Containers Matter

Reproducible environments, portable application packaging, fast startup, higher density, immutable infrastructure, practical microservices deployment.

## What Containers Do NOT Automatically Fix

Bad architecture, missing tests, poor ownership, broken release process, bad observability, bad security. They add new concerns: image supply chain, registry, networking, orchestration, persistent data, security.

## Practice

For one real application, draw current vs containerized state and mark what disappears, what stays, what becomes harder.

### 🧪 Try It Yourself

```bash
docker run --rm hello-world
```

Expected-shape output:

```text
Hello from Docker!
This message shows that your installation appears to be working correctly.
```

Read the rest of the message it prints — Docker explicitly walks you through what just happened (daemon pulled an image, created a container, ran it, streamed output back), which is this entire module in four sentences.

### 💡 Extra Insight

The `hello-world` container exits immediately after printing its message — that's not a bug, it's the point. A container's lifecycle is tied to its main process; when that process exits, the container stops. This single fact explains a huge fraction of "why did my container just stop?" confusion later.

### 🩹 Common Error & Fix

```text
docker: command not found
```
Docker isn't installed or isn't on PATH — see Module 05 before anything else.

### 🎤 Interview

**🟢 Beginner:** Container kyun use karte hain?
**🔵 Junior:** VM ne kaunsi problem solve ki aur container ne kya improve kiya?
**🟡 Senior:** Containers ne microservices ko practical kaise banaya?
**🟠 Lead:** Organization mein container adoption ka hidden cost kya hai?
**🔴 Architect:** VM + container combined architecture kab design karoge?

---

# 02 --- OS-LEVEL VIRTUALISATION

## Mental Model

```text
User Space → Kernel → Hardware
```

Kernel handles CPU scheduling, memory, devices, filesystem, networking. User space contains shell, libraries, applications, tools. A container primarily packages user space; **kernel shared hota hai**.

## VM vs Container

```text
VM:        App + libraries + user space + own kernel
Container: App + libraries + user space → host kernel
```

## Three Core Linux Features

### 1. Namespaces --- What process can SEE

```text
pid    → processes
net    → network
mnt    → mounts/filesystem view
uts    → hostname
ipc    → IPC
user   → UID/GID mapping
cgroup → control-group view
```

### 2. cgroups --- What process can USE

Controls/accounts: CPU, memory, block I/O, process count.

```bash
docker run --memory 512m ...
docker run --cpus 1.5 ...
```

### 3. Union/Layered Filesystem --- What process can CHANGE

```text
Read-only image layers → Thin writable container layer
```

Copy-on-write: `read → lower layer`, `write → copy-up → writable layer`. Delete container → writable layer gone. Therefore important data needs a volume/bind mount.

## Kernel Compatibility

> Container uses host kernel.

```text
Linux host   → Linux containers native
Windows host → Linux containers through Linux VM layer
macOS host   → Linux containers through Linux VM
Linux host   → Windows containers ❌
```

## Security Reality

Shared kernel = shared attack surface. Controls: non-root user, drop capabilities, avoid `--privileged`, seccomp, AppArmor/SELinux, user namespaces, patch host kernel, VM boundary for stronger tenant isolation.

### ⚠️ Gotcha

`root in container` + `--privileged` + `Docker socket` access can effectively become host-level control.

## PID 1

Container's main application may become PID 1 and must handle signals, child process reaping, graceful shutdown. `SIGTERM` handling matters for clean deployment.

## Distroless/Scratch

Advantages: smaller, less attack surface. Trade-off: no shell, no package manager, debugging is different.

### 🧪 Try It Yourself

```bash
docker run -d --name pidtest alpine sleep 1000
docker exec pidtest ps aux
```

Expected-shape output:

```text
PID   USER     TIME  COMMAND
    1 root      0:00 sleep 1000
    7 root      0:00 ps aux
```

Notice `sleep 1000` is PID 1 *inside the container* — even though it's a completely different (much higher) PID on the host. That's the `pid` namespace in action.

```bash
docker inspect --format '{{.State.Pid}}' pidtest
```

Compare that host-side PID against the container-side `1` above — same process, two different numbers depending on which namespace you're looking from.

### 💡 Extra Insight

A very common production bug: running a shell script as your container's `CMD` (e.g., `CMD ["./start.sh"]`) makes the *shell* PID 1, not your actual application. Shells often don't forward `SIGTERM` to child processes properly, so `docker stop` hangs for the full timeout and then force-kills. Fix: either exec the app directly, or use `exec "$@"` at the end of the script, or add a minimal init like `tini`.

### 🩹 Common Error & Fix

```text
docker: Error response from daemon: OCI runtime create failed: ... permission denied
```
Often caused by a base image needing capabilities the container doesn't have, or a bind-mounted path with restrictive host permissions — check `--cap-add` needs and host file ownership before reaching for `--privileged`.

### 🎤 Interview

**🟢:** Container lightweight kyun hai?
**🔵:** Namespace vs cgroup?
**🟡:** Copy-on-write kya hai?
**🟠:** Container escape risk kaise reduce karoge?
**🔴:** Shared-kernel isolation ko VM isolation se compare karke multi-tenant architecture design karo.

---

# 03 --- VM VS CONTAINER

## Honest Comparison

| Area              | VM                           | Container                      |
|-------------------|-------------------------------|----------------------------------|
| Virtualises       | Hardware                     | OS                              |
| Needs             | Hypervisor                   | Container engine/runtime        |
| OS                | Full guest OS                | User space                      |
| Kernel            | Own                          | Host/shared                     |
| Size              | Usually larger               | Usually smaller                 |
| Startup           | Slower                       | Faster                          |
| Density           | Lower                        | Higher                          |
| Isolation         | Stronger boundary            | Thinner boundary                |
| Different kernel  | Yes                          | No                               |
| Best fit          | Strong isolation / OS control | Fast app packaging/density      |

## Five Interview Benefits of Containers

1. **Portability**
2. **Consistency**
3. **Fast startup**
4. **Resource efficiency**
5. **Isolation**

## When VM Still Wins

Different kernel, kernel-level control, stronger tenant boundary, legacy OS, workloads unsuitable for containers, specific virtualization/security boundary.

## Real Architecture

Often:

```text
Cloud → VMs → Container runtime → Containers
```

This combines VM isolation with container portability/density.

## Cost

Containers can reduce infrastructure overhead, but total cost also includes registry, CI, observability, networking, orchestration, security, operations.

### 🧪 Try It Yourself

```bash
time docker run --rm alpine echo "container started"
```

Compare that timing against how long it takes to boot a VM (even a lightweight cloud VM) — the order-of-magnitude difference (usually well under a second vs tens of seconds) is the "fast startup" benefit made concrete.

### 💡 Extra Insight

"Container density" isn't just about disk size — it's mainly about **not paying the memory/CPU cost of N duplicate kernels and OS services**. Ten VMs running the same app pay for ten kernels' worth of background overhead; ten containers on one host pay for one kernel's overhead, total.

### 🩹 Common Error & Fix

Symptom: "we containerized everything and costs didn't drop." Root cause is usually that the workload was never CPU/memory-bound by OS overhead in the first place, or orchestration/observability tooling costs offset the density gains — cost analysis needs to include the *whole* platform, not just compute.

### 🎤 Interview

**🟢:** VM vs container one-line difference?
**🔵:** Container kab better?
**🟡:** VM kab better?
**🟠:** Why do cloud Kubernetes nodes often remain VMs?
**🔴:** Multi-tenant SaaS architecture mein VM/container boundary ka decision kaise loge?

---

# 04 --- DOCKER ARCHITECTURE

## Main Components

```text
Docker CLI → Docker API → Docker Daemon → Container Runtime → Containers
```

Also: `Registry ↕ Images`. Docker client and daemon can be on same or different systems.

## Image vs Container

```text
Image     = read-only template
Container = running instance of image
```

One image can back multiple containers:

```text
Image ├→ Container A ├→ Container B └→ Container C
```

## Docker Objects

Common objects: images, containers, networks, volumes, plugins.

## containerd + runc

```text
Docker → containerd → OCI runtime such as runc → container process
```

Docker provides developer-facing lifecycle tooling; lower runtime layers execute/manage containers.

## Registry

```text
docker pull:  Registry → local image
docker push:  Local image → Registry
```

Docker Hub is a public registry; private registries are common in enterprises.

### 🧪 Try It Yourself

```bash
docker version
```

Look at the output carefully — it has two sections, `Client:` and `Server:`, each with their own API version. This is the CLI-talks-to-daemon-over-an-API architecture made visible.

```bash
sudo ls /var/run/docker.sock
```

That socket file is literally the local channel the CLI uses to talk to the daemon — anyone who can write to it can control every container on the host (this is exactly why "docker group = privileged" in Module 05).

### 💡 Extra Insight

You can point the `docker` CLI at a *remote* daemon (`docker -H tcp://remote-host:2375 ps`), which is exactly how tools like Docker context and some CI runners work — the CLI is genuinely just a client, and "your" Docker commands might be executing on a completely different machine if `DOCKER_HOST` is set.

### 🩹 Common Error & Fix

```text
Cannot connect to the Docker daemon at unix:///var/run/docker.sock. Is the docker daemon running?
```
Either the daemon service isn't started (`sudo systemctl start docker`) or your user lacks permission on the socket (see the `docker` group note in Module 05).

### 🎤 Interview

**🟢:** Docker daemon kya hai?
**🔵:** Image vs container?
**🟡:** Docker CLI daemon se kaise communicate karta hai?
**🟠:** containerd/runc ka role?
**🔴:** Remote Docker daemon architecture ke security implications kya hain?

---

# 05 --- INSTALLING DOCKER

## Requirements

Supported OS, appropriate architecture, virtualization only where platform requires it, privileges for installation/configuration. Ubuntu has package/repository-based installation approaches.

```bash
docker version
docker info
docker run hello-world
```

## Post-Install

```bash
sudo systemctl enable --now docker
```

To use Docker without `sudo`, users are commonly added to the `docker` group:

```bash
sudo usermod -aG docker $USER
newgrp docker    # or log out/in for the group change to take effect
```

## ⚠️ Docker Group Trap

> Membership in the `docker` group is effectively highly privileged because Docker daemon control can lead to host-level access.

```text
docker group ≠ ordinary application-user permission
```

Treat it as privileged access.

## Verify

```bash
docker version
docker info
docker run --rm hello-world
```

### 🧪 Try It Yourself

```bash
docker info | grep -i "Server Version\|Storage Driver\|Cgroup"
```

This gives you three of the most-asked-about facts on a real machine in one command: what Docker version the daemon is running, which storage driver it uses for images/layers, and which cgroup version it's using.

### 💡 Extra Insight

To *prove* to yourself why the docker group is privileged, on a lab/test machine only: `docker run -v /:/hostfs -it alpine chroot /hostfs sh` mounts the entire host filesystem into a container and chroots into it — anyone in the `docker` group can do this in one line. This is why many hardened environments use **rootless Docker** instead, where the daemon itself runs without root privileges.

### 🩹 Common Error & Fix

```text
permission denied while trying to connect to the Docker daemon socket
```
Your user isn't in the `docker` group yet, or the group membership hasn't refreshed in your current shell session — `newgrp docker` or log out and back in.

### 🎤 Interview

**🟢:** Docker install verify kaise?
**🔵:** `docker info` kya batata hai?
**🟡:** Docker group security risk?
**🟠:** Production host par Docker access kaise govern karoge?
**🔴:** Rootless Docker vs privileged daemon access ka architecture decision kaise karoge?

---

# 06 --- THE DOCKER CLI

## Command Grammar

```bash
docker <command> <subcommand> [options] [arguments]
```

```bash
docker run nginx
docker ps
docker image ls
docker container ls
docker network ls
```

## Help

```bash
docker --help
docker run --help
docker image --help
```

Use help instead of memorizing every flag.

## Common Operations

```bash
docker ps
docker ps -a

docker image ls
docker container ls
docker network ls
docker volume ls

docker inspect <name>
docker logs <name>
docker exec -it <name> sh
```

## Formatting

```bash
docker ps --format '{{.Names}}\t{{.Status}}'
```

## Flags

Always distinguish: command, subcommand, option/flag, argument.

### 🧪 Try It Yourself

```bash
docker run -d --name web1 nginx
docker run -d --name web2 nginx
docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}'
```

Expected-shape output:

```text
NAMES   IMAGE   STATUS          PORTS
web2    nginx   Up 2 seconds    80/tcp
web1    nginx   Up 5 seconds    80/tcp
```

This is exactly the kind of output a monitoring script or a Slack-bot health check would parse — far more reliable than screen-scraping the default table.

### 💡 Extra Insight

`docker <object> <verb>` (e.g., `docker image ls`, `docker container prune`) is the modern, unambiguous form; the older shorthand (`docker images`, `docker rm`) still works for backward compatibility but is being layered under the object-oriented grammar. Learning the `<object> <verb>` pattern once lets you guess most commands correctly instead of memorizing each one.

### 🩹 Common Error & Fix

```text
Error: No such container: wb1
```
A typo'd container name/ID — `docker ps -a` first to confirm the exact name, or use `docker ps -aq -f name=web` to fuzzy-match in scripts.

### 🎤 Interview

**🟢:** `docker ps`?
**🔵:** `docker ps -a`?
**🟡:** `docker inspect` vs `docker logs`?
**🟠:** Script-friendly CLI output kaise create karoge?
**🔴:** CLI automation ko stable/portable kaise design karoge?

---

# 07 --- IMAGES

## Image Mental Model

```text
Dockerfile → (build) → Image → (run) → Container
```

## Pull

```bash
docker pull nginx
docker pull nginx:1.27
```

## Tag

```text
myapp:1.4
myapp:latest
```

Do not treat a tag as immutable identity — a tag can be re-pushed to point at different content later.

## Digest

```text
image@sha256:<digest>
```

For reproducible deployment, immutable references are valuable.

## Layers

```text
Base → Dependencies → Application → Config/metadata
```

Each Dockerfile instruction can contribute a layer/cache boundary.

## Cache

If earlier build steps remain unchanged → cached layer → faster build.

## Inspect

```bash
docker image inspect nginx
docker image history nginx
```

## Prune

```bash
docker image prune
```

⚠️ Cleanup commands can remove unused data; understand scope before using aggressive prune commands.

### 🧪 Try It Yourself

```bash
docker pull nginx:1.27
docker inspect --format '{{index .RepoDigests 0}}' nginx:1.27
docker image history nginx:1.27 --no-trunc | head -5
```

The first command shows the immutable digest behind the human-readable tag; the second shows the actual layers and the exact command that created each one — genuinely useful for auditing what's inside an image you didn't build yourself.

### 💡 Extra Insight

Two images with completely different tags/names can share the *exact same underlying layers* on disk if their content is identical (e.g., both built `FROM node:22-alpine`). This is why `docker system df` often shows far less disk usage than "sum of every image's stated size" would suggest — Docker deduplicates at the layer level automatically.

### 🩹 Common Error & Fix

```text
Error response from daemon: manifest for myapp:1.0 not found
```
Either the tag was never pushed, was deleted, or there's a typo — check `docker image ls` locally and the registry's tag list before assuming a build/push failure.

### 🎤 Interview

**🟢:** Image kya hai?
**🔵:** Tag vs digest?
**🟡:** Layers kya benefit dete hain?
**🟠:** Build cache kaise optimize karoge?
**🔴:** Production deployment mein mutable tags vs digest pinning ka trade-off?

---

# 08 --- YOUR FIRST CONTAINERS

## Run

```bash
docker run nginx
```

## Interactive

```bash
docker run -it ubuntu:22.04 bash
```

`-i` = interactive, `-t` = pseudo-terminal.

## Detached

```bash
docker run -d nginx
```

## Port Publishing

```bash
docker run -p 8080:80 nginx
```

Read as `host:container`. Safer local-only example:

```bash
docker run -p 127.0.0.1:8080:80 nginx
```

## Lifecycle

```text
create → start → running → stop → start → remove
```

```bash
docker ps
docker stop <container>
docker start <container>
docker restart <container>
docker rm <container>
```

## Logs

```bash
docker logs <container>
docker logs -f <container>
```

## Exec

```bash
docker exec -it <container> sh
```

`docker exec` is not SSH; it starts a new process inside the existing container namespaces.

## Names

```bash
docker run --name web nginx
```

### 🧪 Try It Yourself

```bash
docker run -d --name web -p 8080:80 nginx
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080
docker exec web nginx -v
docker stop web && docker ps -a --filter name=web
```

The `curl` confirms the port publish actually works end-to-end; `docker ps -a` after `stop` shows the container still exists (just not running) — proving `stop` ≠ `rm`.

### 💡 Extra Insight

`docker exec` runs *inside the container's existing namespaces* — meaning if the container's main process has already crashed and the container is stopped, `exec` won't work (there's no running namespace to attach to). This is a common point of confusion: `exec` needs a *running* container, unlike `docker cp` or `docker inspect`, which work on stopped containers too.

### 🩹 Common Error & Fix

```text
Error response from daemon: Container ... is not running
```
Trying to `exec` into a stopped/crashed container — check `docker logs <name>` first to see *why* it stopped before trying to get a shell inside it.

### 🎤 Interview

**🟢:** `docker run`?
**🔵:** `-it` vs `-d`?
**🟡:** `-p 8080:80` meaning?
**🟠:** `exec` vs SSH?
**🔴:** Production container lifecycle ko immutable deployment model mein kaise design karoge?

---

# 09 --- DOCKERFILES

## Purpose

Dockerfile defines reproducible image build steps.

```dockerfile
FROM
WORKDIR
COPY
ADD
RUN
ENV
ARG
EXPOSE
USER
ENTRYPOINT
CMD
HEALTHCHECK
```

## Basic Example

```dockerfile
FROM node:22-alpine

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

USER node

EXPOSE 3000

CMD ["node", "server.js"]
```

## Layer Caching

Bad ordering:

```dockerfile
COPY . .
RUN npm ci
```

Every source change may invalidate dependency installation. Better:

```dockerfile
COPY package*.json ./
RUN npm ci
COPY . .
```

Stable dependencies first.

## Multi-stage Build

```dockerfile
FROM node:22 AS build
WORKDIR /app
COPY . .
RUN npm ci && npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
```

Benefits: smaller runtime image, build tools excluded, lower attack surface.

## `.dockerignore`

```text
.git
node_modules
dist
logs
.env
temporary files
```

## Security

Prefer `USER nonroot`. Avoid: secrets in image, unnecessary packages, privileged runtime, huge base images.

## CMD vs ENTRYPOINT

```text
ENTRYPOINT = primary executable
CMD        = default arguments/default command
```

Exact behavior depends on shell vs exec form.

## Build

```bash
docker build -t myapp:1.0 .
```

### 🧪 Try It Yourself

Build the same app twice — once with bad instruction ordering, once with good — and compare cache behavior:

```bash
# after changing only application source, not package.json:
docker build -t myapp:bad -f Dockerfile.bad .
docker build -t myapp:good -f Dockerfile.good .
```

Watch the build output: the "good" version shows `CACHED` for the `npm ci` step; the "bad" version reruns it every time because `COPY . .` came first and invalidated everything after it.

### 💡 Extra Insight

`ADD` can fetch remote URLs and auto-extract archives, which sounds convenient but is exactly why most style guides say **prefer `COPY` unless you specifically need `ADD`'s extra behavior** — `ADD`'s implicit magic (silent extraction, remote fetch without checksum verification by default) has caused real supply-chain surprises.

### 🩹 Common Error & Fix

```text
failed to solve: process "/bin/sh -c npm ci" did not complete successfully: exit code 1
```
A build-time step failed inside the container — re-run just that step interactively to debug: `docker build --target build -t debug-stage .` then `docker run -it debug-stage sh` to poke around in the exact intermediate environment.

### 🎤 Interview

**🟢:** Dockerfile kya?
**🔵:** `COPY` vs `RUN`?
**🟡:** Layer caching kaise work karti hai?
**🟠:** Multi-stage build kyun?
**🔴:** Secure/reproducible Dockerfile architecture kaise design karoge?

---

# 10 --- REGISTRIES

## Registry Mental Model

```text
Developer → Build image → Tag image → Push → Registry → Pull → Environment
```

Examples: Docker Hub, private enterprise registries, cloud registries.

## Tagging Strategy

Avoid only `latest`. Prefer meaningful immutable release identifiers:

```text
myapp:1.8.2
myapp:git-<commit>
```

For exact deployment: `image@sha256:<digest>`.

## Login

```bash
docker login
```

Use appropriate credentials/token mechanisms.

## Push

```bash
docker tag myapp:1.0 registry.example.com/team/myapp:1.0
docker push registry.example.com/team/myapp:1.0
```

## Scanning

Registry/image security should check: OS vulnerabilities, package vulnerabilities, secrets, malware/signatures where available, policy compliance.

## Supply Chain

```text
Source → Build → Image → Scan → Sign/attest where supported → Registry → Deploy
```

### 🧪 Try It Yourself

```bash
docker tag myapp:1.0 registry.example.com/team/myapp:1.0
docker push registry.example.com/team/myapp:1.0
docker pull registry.example.com/team/myapp:1.0
```

Notice `docker tag` doesn't create a copy of the image data — it just adds another name pointing at the same image ID. Confirm with:

```bash
docker image ls --digests | grep myapp
```

Both tags will show the same `IMAGE ID`.

### 💡 Extra Insight

`docker login` credentials are stored (by default, unencrypted unless you've configured a credential helper) in `~/.docker/config.json` — a leaked laptop or a misconfigured CI cache with this file exposed is a real, common way registry credentials leak. Always configure a proper credential helper (`docker-credential-*`) in shared/CI environments.

### 🩹 Common Error & Fix

```text
denied: requested access to the resource is denied
```
Either not logged in, logged in with the wrong account, or trying to push to a repository name/namespace you don't have write access to — check `docker login` status and the exact target path (`registry/namespace/image`).

### 🎤 Interview

**🟢:** Registry kya hai?
**🔵:** Push vs pull?
**🟡:** Tag vs digest?
**🟠:** Image scanning pipeline mein kahan?
**🔴:** Trusted image supply chain/provenance ka architecture kaise design karoge?

---

# 11 --- STORAGE & NETWORKING

> Source ka key message: production incidents often come from **where data lives** and **how containers communicate**.

## Storage: Writable Layer Is NOT Persistent Storage

Container has: image layers + writable layer. `docker stop/start` can keep it. But `docker rm` removes the container writable layer. Therefore stateful data needs a **volume/bind mount**.

## Three Storage Options

| Type       | Location                    | Best Use                         |
|------------|-------------------------------|-----------------------------------|
| Volume     | Docker-managed host storage  | DB/stateful data                  |
| Bind mount | User-selected host path      | Development/config                |
| tmpfs      | RAM                          | Temporary sensitive/scratch data  |

```bash
docker volume create pgdata

docker run -d \
  -v pgdata:/var/lib/postgresql/data \
  postgres:16
```

Bind:

```bash
docker run -d \
  --mount type=bind,source=/path/src,target=/app/src \
  myapp
```

tmpfs:

```bash
docker run --tmpfs /run/secrets:rw,size=1m myapp
```

## `-v` vs `--mount`

`--mount` is more explicit and can fail if a bind source path doesn't exist — useful in scripts because silent path creation can hide mistakes.

## Backup

```text
Volume → Backup → Test restore
```

## ⚠️ Prune Trap

Aggressive prune options can delete unused volumes/data. Never run destructive cleanup casually on shared/production hosts.

---

## Networking Drivers

### 1. bridge

Default single-host networking.

### 2. user-defined bridge

Preferred for multi-container apps because of isolation and automatic DNS/container-name discovery.

```bash
docker network create appnet
docker run -d --name db --network appnet postgres
docker run -d --name api --network appnet myapi
```

API can reach `db` via Docker DNS.

### 3. host

Container uses host network namespace. Pros: less network translation, performance/host visibility. Cons: reduced isolation, port conflicts, security boundary lost.

### 4. none

No network except loopback. Useful for isolated processing, malware/untrusted analysis, confidential offline workloads, pure compute.

### 5. overlay

Multi-host networking, used with Swarm and conceptually similar to multi-node networking in orchestrators.

### 6. macvlan

Container gets its own MAC/IP on physical LAN. Useful for legacy systems, appliance-style networking. Trade-offs: network exposure, host/container communication quirks, infrastructure requirements.

### Network Plugins

Enterprise environments may integrate external networking/policy systems (e.g., Weave, Calico, Cilium, Cisco ACI).

## Port Publishing

```bash
-p host:container
```

Database used only by containers usually should **not** be publicly published. For local-only access:

```bash
-p 127.0.0.1:5432:5432
```

### ⚠️ Security

Always ask: Who needs this port? From where? Why is it public?

### 🧪 Try It Yourself

```bash
docker network create appnet
docker run -d --name db --network appnet postgres:16
docker run -it --rm --network appnet alpine ping -c 3 db
```

Expected-shape output:

```text
PING db (172.x.x.x): 56 data bytes
64 bytes from 172.x.x.x: seq=0 ttl=64 time=0.1 ms
```

That `db` resolved to an IP at all is Docker's embedded DNS server doing its job. Now try the same with the **default bridge network** (omit `--network appnet`) — the ping by name will fail, because only user-defined networks get automatic DNS resolution.

### 💡 Extra Insight

The default `bridge` network requires linking containers by IP or the legacy `--link` flag to talk to each other by name — it predates Docker's built-in DNS. This single historical fact is *why* "always use a user-defined network" is repeated so often: it's not a style preference, it's the only mode with proper service discovery.

### 🩹 Common Error & Fix

```text
Error: could not find an available, non-overlapping IPv4 address pool
```
Too many Docker networks already created and IP ranges exhausted — `docker network prune` unused ones, or specify a custom subnet with `docker network create --subnet 10.20.0.0/24 appnet`.

### 🎤 Interview

**🟢:** Volume kya?
**🔵:** Volume vs bind mount?
**🟡:** User-defined bridge default bridge se better kyun?
**🟠:** Overlay networking kaise works?
**🔴:** Multi-tier application ke frontend/backend/database networks ko least-privilege model mein design karo.

---

# 12 --- DOCKER COMPOSE

## Problem

Manual `docker network create` / `docker volume create` / multiple `docker run` calls is repetitive for multi-container apps. Compose gives one declarative file + one workflow.

## Typical Architecture

```text
Frontend → API → Database
```

## Example

```yaml
services:
  db:
    image: postgres:16
    volumes:
      - pgdata:/var/lib/postgresql/data

  api:
    build: .
    environment:
      DB_HOST: db
    depends_on:
      - db
    ports:
      - "8080:8080"

volumes:
  pgdata:
```

```bash
docker compose up -d
docker compose down
docker compose logs -f
```

## Key Concepts

services, networks, volumes, environment, build, image, ports, health checks, dependencies.

## Important

`depends_on` controls startup ordering behavior, but application readiness is a separate concern — use health checks/readiness logic where required.

## Compose vs Stack

```text
Compose = multi-container definition/workflow
Stack   = Swarm deployment model using Compose-like definitions
```

### 🧪 Try It Yourself

Add a proper readiness gate instead of relying on `depends_on` ordering alone:

```yaml
services:
  db:
    image: postgres:16
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 3s
      retries: 5

  api:
    build: .
    depends_on:
      db:
        condition: service_healthy
```

```bash
docker compose up -d
docker compose ps
```

Notice the `api` service now genuinely waits for the DB to report *healthy*, not just *started* — the gap `depends_on` alone leaves open.

### 💡 Extra Insight

`docker compose down` removes containers and the default network, but **volumes survive by default** — you need `docker compose down -v` to also remove volumes. This asymmetry is intentional (protecting data) but surprises people who expect a full reset.

### 🩹 Common Error & Fix

```text
service "api" refers to undefined network appnet: invalid compose project
```
A `networks:` block referenced a name that was never declared at the top level, or a typo — check the `networks:` top-level key matches every reference exactly.

### 🎤 Interview

**🟢:** Compose kya solve karta hai?
**🔵:** `docker compose up`?
**🟡:** `depends_on` limitation?
**🟠:** Compose networking kaise works?
**🔴:** Local development aur production orchestration ke boundary ko kaise define karoge?

---

# 13 --- DOCKER SWARM

## Orchestration

```text
Single host: Containers
Multiple hosts: Cluster → Scheduling → Replication → Self-healing → Rolling updates
```

## Swarm Roles

```text
Manager → maintains cluster state
Worker  → runs tasks
```

## Raft Quorum

Manager state uses Raft consensus. Rule: need majority quorum. Therefore odd manager counts are commonly used:

```text
3 managers → tolerate 1 manager failure
5 managers → tolerate 2
```

Don't confuse manager quorum with total worker capacity.

## Service

```bash
docker service create ...
```

Swarm manages desired replicas instead of individual containers.

## Rolling Update

```text
Version 1 → replace gradually → Version 2
```

## HA

Need: multiple managers, multiple workers, quorum, replicated services, failure-aware design.

## Current Scene

Source itself notes that in modern environments many teams encounter multi-host container networking through Kubernetes/CNI rather than Swarm. Swarm remains valuable for understanding Docker-native orchestration concepts.

### 🧪 Try It Yourself

```bash
docker swarm init
docker node ls
docker service create --name web --replicas 3 -p 8080:80 nginx
docker service ps web
```

Expected-shape output:

```text
ID       NAME     IMAGE          NODE        DESIRED STATE   CURRENT STATE
abc123   web.1    nginx:latest   node-a       Running        Running 5 seconds ago
def456   web.2    nginx:latest   node-a       Running        Running 5 seconds ago
```

Now try killing one task manually and watch Swarm reschedule it automatically:

```bash
docker service update --force web
```

### 💡 Extra Insight

A single-node "swarm" (just `docker swarm init` with no other nodes) is a completely valid and common way to get access to Swarm-only features (`docker service`, rolling updates, secrets) on a single machine — you don't need a real cluster to learn or even use the Swarm object model.

### 🩹 Common Error & Fix

```text
Error response from daemon: rpc error: code = Unavailable desc = ... raft is not initialized
```
Swarm mode isn't active on this node — `docker swarm init` (for the first manager) or `docker swarm join` (for additional nodes) is required before `docker service` commands work.

### 🎤 Interview

**🟢:** Swarm kya hai?
**🔵:** Manager vs worker?
**🟡:** Raft quorum?
**🟠:** 3 vs 5 managers?
**🔴:** Swarm vs Kubernetes decision architecture mein kaunse factors compare karoge?

---

# 14 --- SWARM NETWORKING & LOAD BALANCING

## Overlay

```text
Host A                    Host B
  c1 ───── overlay ────── c4
```

Application sees logical network; hosts handle underlying transport.

## VIP

Swarm services can use virtual IP based service routing.

```text
Client → Service VIP → Task
```

## Routing Mesh

Published service ports can be reached through any Swarm node and routed to service tasks — separating *where the client connects* from *where the task currently runs*.

## External NGINX

```text
Internet → NGINX / LB → Swarm service → replicas
```

Benefits: TLS termination, routing, security policies, centralized ingress, load balancing.

## Failure Points

Overlay control/data ports blocked, wrong published port, service discovery issue, node failure, external LB misconfiguration.

### 🧪 Try It Yourself

```bash
docker service create --name web --replicas 3 -p 8080:80 nginx
```

Now hit port 8080 on **any** node in the swarm, even ones with zero replicas actually scheduled on them:

```bash
curl -s -o /dev/null -w "%{http_code}\n" http://<any-node-ip>:8080
```

It works regardless of where the task is running — that's the routing mesh, not magic.

### 💡 Extra Insight

Overlay networks need specific ports open *between hosts* to function: UDP 4789 for VXLAN data traffic, plus TCP/UDP 7946 for the gossip protocol, and TCP 2377 for manager-to-manager cluster management. A firewall silently blocking just one of these produces confusing, partial connectivity failures — always the first thing to check when "overlay networking works sometimes."

### 🩹 Common Error & Fix

```text
curl: (7) Failed to connect ... Connection refused
```
on one node but not another usually means an inter-host overlay port is blocked at the firewall/security-group level, not a Docker configuration problem — verify the ports listed above are open between all swarm nodes.

### 🎤 Interview

**🟢:** Overlay network kya?
**🔵:** VIP kya?
**🟡:** Routing mesh kya solve karta hai?
**🟠:** External NGINX kyun use karoge?
**🔴:** Multi-layer load-balancing architecture mein LB, ingress, service VIP aur application routing responsibilities kaise divide karoge?

---

# 15 --- STACKS, IMAGE DISTRIBUTION & SCALING

## Compose vs Stack

```text
Compose → application definition/local orchestration
Stack   → Swarm deployment
```

```bash
docker stack deploy -c compose.yml myapp
```

## "No Such Image" Problem

In a multi-node cluster, the manager may know the image name but a worker cannot pull it if it's only built locally. Correct model:

```text
Build → Registry → All nodes pull → Service tasks run
```

Don't assume a local image exists on every node.

## Scaling

```text
replicas: 3
Service ├→ task 1 ├→ task 2 └→ task 3
```

```bash
docker service scale myapp=5
```

## Architect Concern

Scaling stateless application replicas is easy. Stateful systems need data placement, storage, consistency, backup, failover.

### 🧪 Try It Yourself

```bash
docker stack deploy -c compose.yml myapp
docker stack services myapp
docker service scale myapp_api=5
docker service ps myapp_api
```

Watch new tasks get distributed across whichever nodes have capacity — and if you built the image locally without pushing it anywhere, watch some tasks fail on other nodes with an image-pull error, reproducing the "no such image" problem directly.

### 💡 Extra Insight

`docker stack deploy` reads a Compose file but silently ignores some Compose-only fields (like `build:`) at deploy time in a real multi-node cluster — Stack expects a pre-built, pushed image reference, not a local Dockerfile to build on the fly. This is the single most common "it worked with `compose up` but not with `stack deploy`" gotcha.

### 🩹 Common Error & Fix

```text
No such image: myapp:1.0 (worker-node-2)
```
Push the image to a registry reachable from every node, then reference it by full registry path in the compose/stack file — a locally built image only exists on the machine it was built on.

### 🎤 Interview

**🟢:** Stack kya?
**🔵:** Registry cluster mein kyun important?
**🟡:** "No such image" multi-node problem?
**🟠:** Stateless vs stateful scaling?
**🔴:** Global image distribution + rollout + rollback architecture kaise design karoge?

---

# 16 --- RESOURCE LIMITS

## Why Limits?

Without limits: one bad container consumes CPU/memory → host contention → other services degrade.

## Memory

```bash
docker run --memory 512m myapp
```

If a container exceeds allowed memory, the kernel may reclaim/terminate processes according to memory pressure/OOM behavior.

## CPU

```bash
docker run --cpus 1.5 myapp
```

CPU shares/weight and quotas influence scheduling.

## Hard vs Soft Concepts

```text
Hard limit  = upper boundary
Reservation = expected resource requirement
```

In Swarm, `reservations` and `limits` help scheduling and protection.

## Architect Rule

Don't set limits randomly. Use load testing, metrics, SLO, capacity planning, then tune.

### 🧪 Try It Yourself

```bash
docker run -d --name memtest --memory 100m polinux/stress \
  stress --vm 1 --vm-bytes 150M --timeout 30s
docker logs memtest
docker inspect memtest --format '{{.State.OOMKilled}}'
```

Expected-shape output for the last command:

```text
true
```

You just deliberately triggered and confirmed an OOM kill — this is exactly the failure mode to recognize in production logs.

### 💡 Extra Insight

`--memory` sets a hard limit, but Docker also (by default) sets `--memory-swap` to roughly double that if unspecified, meaning the container can actually use limit + swap before being OOM-killed. If you want a truly hard ceiling with no swap headroom, set `--memory-swap` equal to `--memory` explicitly.

### 🩹 Common Error & Fix

```text
container_name exited with code 137
```
Exit code 137 = 128 + 9 (SIGKILL) — almost always an OOM kill. Check `docker inspect --format '{{.State.OOMKilled}}'` to confirm, then raise the limit or fix the memory leak, don't just retry.

### 🎤 Interview

**🟢:** Memory limit kyun?
**🔵:** CPU limit?
**🟡:** Limit vs reservation?
**🟠:** OOM issue diagnose kaise?
**🔴:** Resource governance ko SLO/capacity planning ke saath kaise design karoge?

---

# 17 --- MONITORING & LOGGING

## `docker stats`

```bash
docker stats
```

Useful for CPU, memory, network, block I/O.

## Monitoring

cAdvisor can expose container-level metrics for monitoring systems.

```text
Container → Metrics → Collector → Monitoring → Alert
```

## Logging

Docker supports log drivers. Important concerns: centralized collection, retention, rotation, searchable logs, structured logging.

## Log Rotation

Without rotation: logs → disk fills → service failure. Configure appropriate limits/retention.

## EFK

```text
Elasticsearch + Fluentd/Fluent Bit + Kibana
```

Common centralized logging pattern.

## Observability

Don't monitor only logs — use logs + metrics + traces where architecture requires.

### 🧪 Try It Yourself

```bash
docker run -d --name logtest \
  --log-driver json-file \
  --log-opt max-size=10m \
  --log-opt max-file=3 \
  nginx
docker inspect logtest --format '{{.HostConfig.LogConfig}}'
```

This confirms the rotation policy is actually attached to the container — `max-size=10m` and `max-file=3` means at most 30MB of logs kept per container, ever, regardless of how chatty the app gets.

### 💡 Extra Insight

The default `json-file` log driver has **no rotation at all** unless you explicitly set `max-size`/`max-file` — a genuinely surprising default for a lot of people, and the single most common cause of "disk full" incidents on long-running Docker hosts that were never configured with a daemon-wide default.

### 🩹 Common Error & Fix

```text
no space left on device
```
on a Docker host frequently traces back to unrotated `json-file` logs — check `du -sh /var/lib/docker/containers/*/*.log` to confirm before assuming it's image/volume bloat.

### 🎤 Interview

**🟢:** `docker stats`?
**🔵:** Container logs kaise inspect?
**🟡:** Log rotation kyun?
**🟠:** Centralized logging architecture?
**🔴:** Production observability model mein metrics/logs/traces + SLO/alerting kaise combine karoge?

---

# 18 --- PRODUCTION & INTERVIEWS

## Production Security

```text
Non-root → Least privilege → Drop capabilities → No unnecessary ports → Minimal image → Patch host → Scan images → Protect secrets → Restrict Docker socket
```

## Health

Container running ≠ application healthy. Use health checks, readiness, liveness concepts, application metrics.

## CI/CD

```text
Git → Build image → Test → Scan → Tag/digest → Push registry → Deploy → Health validation → Promote/rollback
```

## Immutable Infrastructure

Don't manually patch production containers. Prefer: old image → build new image → test → replace old.

## Docker Alone Stops Being Enough

As complexity grows, you may need an orchestrator, ingress, service discovery, secrets management, observability, policy, autoscaling, multi-node scheduling. Docker is a building block, not the complete enterprise platform.

## Production Checklist

```text
[ ] Image is minimal
[ ] Runs as non-root
[ ] No embedded secrets
[ ] Vulnerability scanning
[ ] Immutable release reference
[ ] Resource limits
[ ] Health checks
[ ] Centralized logs
[ ] Metrics
[ ] Backup strategy
[ ] Network exposure reviewed
[ ] Registry access secured
[ ] Rollback tested
[ ] Host patched
[ ] Runtime permissions minimized
```

### 🧪 Try It Yourself

Add a real `HEALTHCHECK` to a Dockerfile and watch Docker itself track it:

```dockerfile
HEALTHCHECK --interval=10s --timeout=3s --retries=3 \
  CMD curl -f http://localhost:3000/health || exit 1
```

```bash
docker build -t myapp:health .
docker run -d --name myapp myapp:health
docker ps    # STATUS column will show "(health: starting)" then "(healthy)" or "(unhealthy)"
```

### 💡 Extra Insight

"Container running ≠ application healthy" isn't theoretical — a Node process can stay alive and keep its process ID active while its event loop is completely deadlocked, or a Java app can be "up" but its DB connection pool exhausted. `docker ps` shows `Up 2 hours` regardless; only an actual `HEALTHCHECK` hitting a real endpoint would catch it.

### 🩹 Common Error & Fix

```text
STATUS: Up 3 minutes (unhealthy)
```
The container's process is running, but its `HEALTHCHECK` command is failing — check `docker inspect --format '{{json .State.Health}}' <name>` for the last few check outputs/exit codes instead of only reading logs.

### 🎤 Interview

**🟢:** Production Docker mein first security steps?
**🔵:** Health check kyun?
**🟡:** Container security vs host security?
**🟠:** Docker-only architecture kab fail to scale operationally?
**🔴:** Production container platform ke complete security/reliability architecture ko design karo.

---

# 19 --- DOCKER IN SALESFORCE, AI & CLOUD

## Salesforce CLI

```text
Developer / CI → Docker image → Salesforce CLI (`sf`) → Org / Metadata
```

Benefits: same CLI/tool versions, reproducible CI, less host pollution, portable automation.

## Salesforce CI/CD

```text
Git → Dockerized tooling → sf CLI → Validate metadata → Apex/LWC tests → Package/deploy → Environment
```

Important: **container success ≠ Salesforce deployment success**. You still need metadata validation, permissions/security checks, tests, dependency validation, environment-specific controls.

## Heroku

```text
Source → Build → Container/image → Cloud runtime
```

Exact platform capabilities and deployment model should follow current platform documentation.

## MuleSoft

Containers can package supporting integration components/tooling. Architect concern: network, secrets, certificates, connectivity, observability, scaling.

## Agentforce / AI

```text
Agent/application → API layer → Model/tool service → Containerized runtime
```

Salesforce-managed Agentforce capabilities and external model/container infrastructure are separate layers.

## BYO Models

```text
Salesforce → Secure API → External model service → Containerized inference/runtime
```

Consider: latency, data residency, authentication, PII, model governance, cost, observability, failure/retry.

## MCP

```text
Agent → MCP client → MCP server → Tool/API
```

Security focus: least privilege, tool allow-list, network boundaries, secret isolation, audit logging.

## Enterprise Salesforce + Docker Mental Model

```text
Developer → Git → Dockerized Tooling → sf CLI / tests / scanners → CI → Artifact / Package → Salesforce Environment → Validation → Production
```

### 🧪 Try It Yourself

A minimal Dockerized `sf` CLI image concept:

```dockerfile
FROM node:22-alpine
RUN npm install --global @salesforce/cli
WORKDIR /workspace
ENTRYPOINT ["sf"]
```

```bash
docker build -t sf-cli-tooling .
docker run --rm -v "$PWD":/workspace sf-cli-tooling --version
```

This guarantees every CI run and every developer laptop uses the *exact same* `sf` CLI version — no more "works with my CLI version" bugs.

### 💡 Extra Insight

The most valuable thing Docker does for Salesforce tooling isn't performance — it's **eliminating "which CLI/plugin version is this pipeline actually using"** as a debugging question. Pin the CLI version in the Dockerfile, rebuild deliberately when you want to upgrade, and every environment (dev laptop, CI runner, another dev's laptop) is provably identical.

### 🩹 Common Error & Fix

Symptom: "deployment passed in CI but fails locally" (or vice versa) for a Salesforce project. First thing to check before digging into metadata: are the local `sf` CLI version and the CI's Dockerized `sf` CLI version actually the same? Version drift between environments is a very common, very boring root cause.

### 🎤 Interview

**🟢:** Salesforce CLI ko Docker mein kyun run karoge?
**🔵:** Dockerized CI tooling ka benefit?
**🟡:** Salesforce deployment aur container deployment mein difference?
**🟠:** External AI model ko Salesforce se connect karte waqt Docker ka role kya ho sakta hai?
**🔴:** Salesforce + Docker + AI + cloud architecture mein security, latency, data residency, observability aur failure handling ka end-to-end design kaise karoge?

---

# 📖 GLOSSARY (New)

| Term | Plain-language meaning |
|---|---|
| **Namespace** | A Linux kernel feature controlling what a process can *see* (its own PIDs, network, mounts, etc.). |
| **cgroup** | A Linux kernel feature controlling what a process can *use* (CPU, memory, I/O limits). |
| **Union filesystem** | Layered filesystem where read-only image layers sit under a thin writable container layer. |
| **Copy-on-write** | Writes to a file copy it up to the writable layer first, leaving the original read-only layer untouched. |
| **OCI runtime** | The low-level program (e.g. `runc`) that actually creates and runs a container process per the OCI spec. |
| **Digest** | A content hash (`sha256:...`) that uniquely and immutably identifies an image's exact content. |
| **Tag** | A human-readable, *mutable* label pointing at an image (can be re-pushed to point elsewhere later). |
| **Routing mesh** | Swarm feature letting any node accept traffic for a published service port and route it to a task, wherever it runs. |
| **VIP (Virtual IP)** | A stable IP a Swarm service presents to clients, load-balanced across its actual task IPs. |
| **Raft quorum** | The majority-vote consensus mechanism Swarm managers use to agree on cluster state. |
| **Overlay network** | A virtual network spanning multiple Docker hosts, used for multi-node service communication. |
| **Sidecar** | A helper container running alongside a main container (e.g. a log shipper), sharing its network/lifecycle. |
| **Distroless / scratch image** | A minimal base image with no shell or package manager, reducing attack surface. |
| **Rootless Docker** | Running the Docker daemon itself without root privileges, reducing the blast radius of a compromise. |
| **Immutable infrastructure** | Never patching a running container in place; always building and deploying a new image instead. |
| **OOM kill** | The Linux kernel forcibly terminating a process (exit code 137) because it exceeded its memory limit. |

---

# 🩺 COMMON ERROR MESSAGES REFERENCE (New)

| Error message (shortened) | Likely cause | Typical fix |
|---|---|---|
| `Cannot connect to the Docker daemon` | Daemon not running, or socket permission issue | `systemctl start docker`; check `docker` group membership |
| `permission denied ... docker.sock` | User not in `docker` group yet in this shell | `newgrp docker` or re-login after `usermod -aG docker` |
| `No such image: myapp:1.0` (on a worker node) | Image only built locally, never pushed to a shared registry | Push to a registry reachable by all nodes |
| `manifest for myapp:1.0 not found` | Tag never pushed, deleted, or typo'd | Check `docker image ls` / registry tag list |
| `denied: requested access ... is denied` | Not logged in, or no write access to that repo path | `docker login`; verify exact `registry/namespace/image` path |
| `Container ... is not running` (on `exec`) | Trying to exec into a stopped/crashed container | `docker logs <name>` first to see why it stopped |
| `exited with code 137` | OOM-killed by the kernel | `docker inspect --format '{{.State.OOMKilled}}'`; raise memory limit or fix leak |
| `no space left on device` | Usually unrotated `json-file` logs, sometimes dangling images/volumes | Configure log rotation; `docker system df` / `docker system prune` |
| `manifest ... raft is not initialized` | Swarm mode not active on this node | `docker swarm init` or `docker swarm join` |
| `could not find an available, non-overlapping IPv4 address pool` | Too many Docker networks created | `docker network prune`; specify custom subnets |

---

# ⚡ COMMAND REFERENCE BY TASK (New)

```bash
# ---- Verify install ----
docker version
docker info
docker run --rm hello-world

# ---- Images ----
docker pull nginx
docker image ls
docker image inspect nginx
docker image history nginx
docker image prune

# ---- Containers ----
docker run -d --name web -p 8080:80 nginx
docker ps
docker ps -a
docker logs -f web
docker exec -it web sh
docker stop web
docker start web
docker rm web

# ---- Build ----
docker build -t myapp:1.0 .
docker build --no-cache -t myapp:1.0 .

# ---- Registry ----
docker login
docker tag myapp:1.0 registry.example.com/team/myapp:1.0
docker push registry.example.com/team/myapp:1.0

# ---- Storage ----
docker volume create pgdata
docker volume ls
docker volume inspect pgdata
docker run -d -v pgdata:/var/lib/postgresql/data postgres:16

# ---- Networking ----
docker network create appnet
docker network ls
docker network inspect appnet
docker run -d --name db --network appnet postgres

# ---- Compose ----
docker compose up -d
docker compose ps
docker compose logs -f
docker compose down
docker compose down -v

# ---- Swarm ----
docker swarm init
docker node ls
docker service create --name web --replicas 3 nginx
docker service ps web
docker service scale web=5
docker stack deploy -c compose.yml myapp
docker stack services myapp

# ---- Resource limits ----
docker run --memory 512m --cpus 1.5 myapp

# ---- Monitoring ----
docker stats
docker system df
docker inspect --format '{{.State.OOMKilled}}' <name>
docker inspect --format '{{json .State.Health}}' <name>
```

---

# 🧠 MASTER DOCKER DECISION TREE

```text
Need isolation?                              → Container
Need different kernel / stronger boundary?   → VM
Need package?                                 → Image
Need running instance?                        → Container
Need persistent data?                         → Volume
Need local source-code sharing?               → Bind mount
Need RAM-only temporary data?                 → tmpfs
Need multi-container local app?               → Compose
Need multi-host Docker-native orchestration?  → Swarm / evaluate modern orchestrator
Need cross-host network?                      → Overlay / orchestrator networking
Need exact image identity?                    → Digest
Need share image?                             → Registry
Need smaller production image?                → Multi-stage build
Need reproducible builds?                     → Pinned dependencies + controlled base + immutable references
Need resource protection?                     → Limits + reservations
Need runtime visibility?                      → Metrics + logs + health
Need enterprise orchestration?                → Evaluate Kubernetes/managed platform as appropriate
Need Salesforce tooling consistency?          → Dockerized sf CLI/CI environment
Need external AI runtime?                     → Containerized service + secure API boundary
```

---

# ⚠️ MASTER ERRORS & GOTCHAS

1. **Container ≠ VM**
2. **Image ≠ container**
3. **Writable layer ≠ persistent storage**
4. **`docker stop` ≠ `docker rm`**
5. **Tag ≠ immutable identity**
6. **`latest` ≠ guaranteed release version**
7. **`docker exec` ≠ SSH**
8. **Docker group ≈ privileged access**
9. **Root inside container ≠ automatically safe**
10. **`--privileged` destroys important isolation**
11. **Host kernel must support container workload**
12. **Default bridge ≠ preferred multi-container network**
13. **Published port ≠ private service**
14. **Compose ≠ full production orchestrator**
15. **Local image ≠ cluster-wide image**
16. **Scaling containers ≠ solving stateful data**
17. **Container running ≠ application healthy**
18. **Image scanning ≠ complete supply-chain security**
19. **Docker success ≠ Salesforce deployment success**
20. **Containerization does not fix bad architecture**
21. **`depends_on` (Compose) ≠ readiness/health verification** *(new)*
22. **Shell as `CMD` ≠ your app is PID 1 (signal-handling gotcha)** *(new)*

---

# 📊 COST & PERFORMANCE MASTER VIEW

| Area              | Benefit                     | Hidden Cost                                    |
|-------------------|-------------------------------|--------------------------------------------------|
| Containers        | High density                 | Operational complexity                            |
| Images            | Reproducibility              | Registry/storage                                  |
| Layers            | Cache/reuse                  | Bad layering can bloat                            |
| Compose           | Easy local stack              | Not enterprise orchestration                      |
| Swarm             | Docker-native orchestration   | Smaller ecosystem vs modern alternatives          |
| Volumes           | Persistence                   | Backup/restore responsibility                     |
| Overlay           | Multi-host networking         | Network overhead/MTU/control plane                |
| Resource limits   | Protection                    | Bad limits can cause throttling/OOM               |
| Central logging   | Debuggability                 | Storage/retention cost                            |
| Multi-stage build | Smaller runtime               | More complex build                                |
| Security scanning | Risk visibility               | False positives/tooling cost                      |

---

# 👨‍💻 DAILY DOCKER DEVELOPER CHECKLIST

```text
[ ] Use a small trusted base image
[ ] Add .dockerignore
[ ] Avoid secrets in Dockerfile/image
[ ] Use non-root where practical
[ ] Order Dockerfile for cache efficiency
[ ] Use multi-stage build where useful
[ ] Pin important dependencies/base references
[ ] Scan image
[ ] Test container locally
[ ] Define health behavior
[ ] Configure resource limits
[ ] Persist state outside writable layer
[ ] Use user-defined networks for multi-container apps
[ ] Publish only required ports
[ ] Use registry for shared images
[ ] Keep logs manageable
[ ] Validate production rollback
```

---

# 🏗️ ARCHITECT MASTER CHECKLIST

Before approving a container architecture, ask:

1. What is the isolation boundary?
2. Which kernel is shared?
3. What is persistent and what is disposable?
4. What is the image identity?
5. How is the image built?
6. Where is it stored?
7. How is it scanned?
8. How is it promoted?
9. How are secrets delivered?
10. What ports are exposed?
11. What network boundary exists?
12. What are CPU/memory limits?
13. What happens on OOM?
14. How is health detected?
15. How are logs/metrics/traces collected?
16. How is failure recovered?
17. How is state backed up?
18. How is rollback performed?
19. Who can control the Docker daemon?
20. What happens if the registry is unavailable?
21. What happens if a node fails?
22. Is orchestration required?
23. Is Docker alone enough?
24. What is the cloud/runtime boundary?
25. For Salesforce: how do `sf` CLI, metadata, packages and orgs fit?
26. For AI: where are model/tool/data/security boundaries?

---

# 🎯 INTERVIEW MASTER LADDER

## 🟢 BASIC

1. Docker kya hai?
2. Container kya hai?
3. Image kya hai?
4. VM vs container?
5. Dockerfile kya hai?
6. Registry kya hai?
7. `docker run` kya karta hai?
8. Volume kya hai?
9. Compose kya hai?
10. Docker Swarm kya hai?

## 🔵 JUNIOR / DEVELOPER

1. Namespace kya hai?
2. cgroup kya hai?
3. Image layers kya hain?
4. Tag vs digest?
5. `-it` vs `-d`?
6. `docker logs` vs `docker exec`?
7. Volume vs bind mount?
8. User-defined bridge kyun?
9. Dockerfile cache kaise improve karoge?
10. Multi-stage build kyun?
11. `.dockerignore` kyun?
12. Docker registry push/pull workflow?
13. Compose service discovery?
14. Resource limits ka use?
15. Health check kyun?

## 🟡 SENIOR

1. Container shared-kernel security risk?
2. Linux host par Windows container kyun nahi?
3. Docker group security issue?
4. Copy-on-write ka behavior?
5. Overlay network kaise work karta hai?
6. VIP vs routing mesh?
7. Swarm Raft quorum?
8. Multi-node image distribution?
9. OOM troubleshooting?
10. Image bloat troubleshooting?
11. Registry unavailable ho to deployment?
12. Container data loss ka root cause?
13. Container running but unhealthy --- debug?
14. Image supply-chain security?
15. Build cache invalidation?

## 🟠 LEAD

1. Compose vs Swarm vs Kubernetes?
2. Monolithic app ko containers mein kaise break karoge?
3. Stateful workloads kaise handle karoge?
4. Production network segmentation?
5. Resource limits ka standard kaise define karoge?
6. Registry strategy kaise choose karoge?
7. CI/CD image promotion model?
8. Image signing/scanning governance?
9. Central logging architecture?
10. Disaster recovery for containerized services?
11. Docker host access policy?
12. Container platform cost optimization?
13. Salesforce CI mein Docker ka role?
14. AI workload containerization risks?
15. Enterprise migration from VMs to containers?

## 🔴 ARCHITECT

1. Container vs VM isolation boundary ko enterprise tenancy model se kaise map karoge?
2. Container platform ka reference architecture design karo.
3. Image supply chain ko source → build → registry → runtime tak secure kaise karoge?
4. Immutable deployment + rollback architecture?
5. Stateful distributed application ka storage architecture?
6. Multi-host networking aur security zones ka design?
7. Container resource governance + SLO model?
8. Observability architecture for thousands of containers?
9. Registry outage / compromised image scenario?
10. Swarm/Kubernetes/managed service decision framework?
11. VM + container hybrid architecture kab best hai?
12. Salesforce DX tooling ko Docker-based CI platform mein kaise standardize karoge?
13. Salesforce metadata/package deployment aur container artifact lifecycle ko kaise separate karoge?
14. External AI/BYO model runtime ko Salesforce ke saath securely kaise integrate karoge?
15. MCP tools ko containerized enterprise environment mein least privilege ke saath kaise expose karoge?
16. Data residency + PII + AI inference + container orchestration ka architecture?
17. Multi-region container platform mein registry, networking, secrets, observability aur failover ka design?
18. "Docker solved our deployment problem" statement ko architect ke roop mein kaise challenge karoge?

---

# 🧪 REAL-WORLD SCENARIOS

## Scenario 1 --- "Works on My Machine"

```text
Different local runtime → Environment drift → Container image → Same runtime/libs → Dev/Test/Prod parity
```

## Scenario 2 --- Database Data Disappeared

```text
Data stored in writable layer → Container removed → Layer deleted → Data lost
```

Fix: Volume + Backup + Restore testing.

## Scenario 3 --- API Cannot Reach DB

```text
Same user-defined network? → DNS name? → Correct container port? → Application listening on correct interface? → Health/readiness? → Network policy/firewall?
```

## Scenario 4 --- Multi-Node "No Such Image"

```text
Service scheduled on worker → Worker cannot find image → Use accessible registry → Push image → Worker pulls image
```

## Scenario 5 --- Container Uses 100% CPU

```text
docker stats → Confirm process/resource pattern → Application profiling → Set appropriate CPU limits → Scale if demand is legitimate → Fix application if abnormal
```

## Scenario 6 --- Salesforce CI Is Inconsistent

```text
Different CLI/tool versions → Different host environments → Dockerized CI tooling → Pinned toolchain → Repeatable validation
```

## Scenario 7 --- External AI Model

```text
Salesforce → Secure API → Containerized model gateway/service → Model → Response
```

Check: Auth, PII, Latency, Timeout, Retry, Rate limit, Observability, Data residency, Cost.

## Scenario 8 --- Container Keeps Restarting in a Crash Loop *(new)*

```text
docker ps -a shows "Restarting" → docker logs <name> for the actual crash reason
 → Common causes: missing env var, DB not ready yet, port already bound, OOM kill
 → Fix root cause, not the restart policy
 → If crash is inherently transient (dependency startup race), add a proper healthcheck/retry in app code, not just --restart always
```

## Scenario 9 --- Disk Fills Up on a Long-Running Docker Host *(new)*

```text
docker system df → identify biggest consumer (images/containers/volumes/build cache)
 → check unrotated json-file logs specifically (very common root cause)
 → configure daemon-wide log rotation defaults going forward
 → prune deliberately, never on a whim on a shared host
```

---

# ⚡ 30-SECOND DOCKER CHEAT SHEET

```bash
# Images
docker pull nginx
docker image ls
docker image inspect nginx
docker image history nginx

# Containers
docker run -d --name web -p 8080:80 nginx
docker ps
docker ps -a
docker logs -f web
docker exec -it web sh
docker stop web
docker rm web

# Build
docker build -t myapp:1.0 .
docker build --no-cache -t myapp:1.0 .

# Registry
docker login
docker tag myapp:1.0 registry.example.com/team/myapp:1.0
docker push registry.example.com/team/myapp:1.0

# Volumes
docker volume create pgdata
docker volume ls
docker volume inspect pgdata

# Networks
docker network create appnet
docker network ls
docker network inspect appnet

# Compose
docker compose up -d
docker compose ps
docker compose logs -f
docker compose down

# Swarm
docker swarm init
docker node ls
docker service ls
docker service ps <service>
docker service scale <service>=5
docker stack deploy -c compose.yml myapp

# Runtime
docker stats
docker info
docker system df
```

---

# 🧠 FINAL DOCKER ARCHITECT MENTAL MODEL

```text
                 BUSINESS APPLICATION
                         ↓
                  SOURCE CODE
                         ↓
                  DOCKERFILE
                         ↓
                       IMAGE
                         ↓
                SCAN / SIGN / STORE
                         ↓
                     REGISTRY
                         ↓
              ┌──────────┴──────────┐
              ↓                     ↓
          DEV / TEST              UAT
              └──────────┬──────────┘
                         ↓
                    PRODUCTION
                         ↓
             ┌───────────┴───────────┐
             ↓                       ↓
        CONTAINERS                DATA
             ↓                       ↓
      NETWORK / LIMITS          VOLUMES
             ↓                       ↓
      HEALTH / LOGS / METRICS / TRACES
                         ↓
                    OPERATIONS
                         ↓
             SCALE / RECOVER / ROLLBACK
```

## Enterprise Layer

```text
Cloud VM / Bare Metal → Container Runtime → Orchestrator → Containers → Application
```

## Salesforce Layer

```text
Git → Dockerized sf CLI → CI → Metadata / Package validation → Salesforce Environment
```

## AI Layer

```text
Salesforce / Agent → Secure API boundary → Containerized AI/tool service → Model / MCP / External system → Governance + Observability
```

---

# 🏆 FINAL LEARNING UPGRADE

```text
Beginner   "What is Docker?"
Developer  "How do I build and run it?"
Senior     "Why did the container fail?"
Lead       "How do we operate 100s/1000s safely?"
Architect  "Where is the isolation boundary,
            state boundary, security boundary,
            network boundary, artifact boundary,
            failure boundary and business boundary?"
```

> **Architect-level Docker knowledge = container commands + Linux internals + networking + storage + security + orchestration + CI/CD + observability + cost + recovery + business trade-offs.**
