# Storage and networking

*Module 11 · Core*

The two subjects that cause the most production incidents: where data actually lives, and how containers
reach each other and the outside world.

[Course home](../index.md) / Module 11

## 1. The writable layer is not storage

Every container gets a thin writable layer on top of the image (module 02). It is fast, it is convenient,
and it is **destroyed with the container**.

```mermaid
flowchart LR
    S0["Container writes /var/lib/mysql"]
    S1["Data lands in the writable layer"]
    S2["Container restarted with docker start"]
    S3["Data still there"]
    S0 --> S1
    S1 --> S2
    S2 --> S3
    F0["Container removed or replaced with a new image"]
    F1["Writable layer deleted"]
    F2["Database gone"]
    F3["Use a volume - always, for anything stateful"]
    S1 -. fails .-> F0
    F0 --> F1
    F1 --> F2
    F2 --> F3
    classDef bad fill:#fdecea,stroke:#c62828;
    class F0,F1,F2,F3 bad;
```

> **Why it matters:** `docker stop` and `docker start` keep the writable layer. `docker rm` - and every deployment that replaces a container with a new image - destroys it. If your data is not in a volume, upgrading your app deletes your database.

## 2. Three ways to persist

| | Volume | Bind mount | tmpfs |
| --- | --- | --- | --- |
| Stored | Docker-managed area on the host | A path you choose on the host | Host RAM |
| Created by | `docker volume create` or automatically | You, by path | `--tmpfs` |
| Portable | Yes | No - depends on host layout | n/a |
| Permissions | Docker handles them | Host UID/GID must match | n/a |
| Backup | `docker volume` tooling, drivers | Ordinary filesystem tools | Not persisted |
| Use for | **Databases, uploads, anything stateful** | Source code in development, config files | Secrets and scratch space |

```bash
# volume (preferred)
docker volume create pgdata
docker run -d --name db -v pgdata:/var/lib/postgresql/data postgres:16

# bind mount (development)
docker run -d --name web -v "$(pwd)/src:/app/src" -p 3000:3000 myapp:dev

# tmpfs (never written to disk)
docker run --tmpfs /run/secrets:rw,size=1m myapp:1.0

docker volume ls
docker volume inspect pgdata
docker volume rm pgdata          # destroys the data
```

> **WARNING - `docker system prune --volumes` deletes data**
>
> It removes volumes not currently attached to a container. A stopped database whose container you removed has an unattached volume - and it will go. Back up before you prune, and never run it casually on a shared host.

Backing up a volume:

```bash
docker run --rm -v pgdata:/data -v "$(pwd)":/backup alpine \
  tar czf /backup/pgdata-$(date +%F).tar.gz -C /data .
```

> **TIP - Bind mounts and permissions on Linux**
>
> The container writes as its own UID. If that UID does not match the host directory's owner, you get permission errors - or files owned by a strange UID on your host. Match with `--user "$(id -u):$(id -g)"`, or set ownership deliberately in the Dockerfile.

## 3. Networking: the default and why you should not use it

| Network driver | What it does | Use for |
| --- | --- | --- |
| `bridge` (default) | Private network on the host, NAT to the outside | The default when you specify nothing |
| **user-defined bridge** | Same, plus **automatic DNS between containers** | **Everything multi-container** |
| `host` | No isolation - container uses the host's network stack directly | Rare: extreme performance needs, some monitoring agents |
| `none` | No networking at all | Batch jobs that must not reach the network |
| `overlay` | Spans multiple hosts | Swarm / multi-host clusters |
| `macvlan` | Container gets a real MAC and IP on your LAN | Legacy integrations expecting a physical host |

```bash
docker network create appnet

docker run -d --name db  --network appnet -v pgdata:/var/lib/postgresql/data postgres:16
docker run -d --name api --network appnet -e DB_HOST=db -p 8080:8080 myapi:1.0
```

The API reaches the database at hostname **`db`** - Docker's embedded DNS resolves container names on a
user-defined network. No IP addresses, no `--link`, no configuration.

> **WARNING - The default bridge has no DNS**
>
> On the default `bridge` network, containers can reach each other by IP but **not by name**. IPs change on restart. Always create a user-defined network - it is one command and removes an entire class of "worked yesterday" failures.

## 4. Publishing ports

```bash
docker run -p 8080:80 nginx              # host 8080 -> container 80, on ALL host interfaces
docker run -p 127.0.0.1:5432:5432 postgres   # localhost only - not reachable from outside
docker run -P nginx                      # publish EXPOSEd ports to random high ports
```

Read `-p` left to right: **host:container**.

| Situation | What to publish |
| --- | --- |
| Public web app | `-p 80:8080` on the host, behind a reverse proxy or load balancer |
| Database used only by other containers | **Nothing.** Put it on the shared network and do not publish |
| Database you need locally for tooling | `-p 127.0.0.1:5432:5432` |
| Admin interface | Never `0.0.0.0` on an internet-facing host |

> **WARNING - Published ports can bypass your firewall**
>
> On Linux, Docker writes its own iptables rules. A published port can therefore be reachable from the internet even though your host firewall appears to block it. Bind to `127.0.0.1` for anything that should not be public, and verify from outside the host rather than trusting the firewall config.

## 5. Putting it together

```bash
docker network create appnet
docker volume create pgdata

docker run -d --name db --network appnet \
  -v pgdata:/var/lib/postgresql/data \
  -e POSTGRES_PASSWORD_FILE=/run/secrets/pw \
  --restart unless-stopped postgres:16

docker run -d --name api --network appnet \
  -e DB_HOST=db -e DB_PORT=5432 \
  -p 127.0.0.1:8080:8080 \
  -m 512m --cpus 1 \
  --restart unless-stopped myapi:1.4.2
```

Note what this gets right: named volume for state, user-defined network for DNS, database not published,
API bound to localhost, resource limits set, restart policy set. That is the shape of a correct
single-host deployment - and module 12 turns it into one file.

## 6. Diagnosing

```bash
docker network ls
docker network inspect appnet             # which containers, which IPs
docker inspect -f '{{json .NetworkSettings.Networks}}' api

docker exec -it api sh -c "getent hosts db"        # does DNS resolve?
docker exec -it api sh -c "nc -zv db 5432"         # is the port reachable?
docker port api                                    # what is actually published
docker exec -it api sh -c "df -h"                  # is the volume mounted where you think?
```

| Symptom | Usual cause |
| --- | --- |
| `connection refused` between containers | Not on the same user-defined network, or the service is not listening yet |
| Name does not resolve | Using the default bridge, or a typo in `--name` |
| Works from host, not from another container | You used `localhost` inside a container - that means *that container*, not the host |
| Data disappeared after deploy | It was in the writable layer, not a volume |
| `port is already allocated` | Another process or container holds the host port |
| Permission denied on a bind mount | UID mismatch between container user and host directory |

> **TIP - `localhost` inside a container means the container**
>
> Each container has its own network namespace. To reach the host from inside a container, use `host.docker.internal` on Docker Desktop, or the gateway IP / `--add-host` on Linux. To reach another container, use its name on a shared network.

## 7. Extra points

- **Volumes are the only supported way to run stateful workloads on a single host.** Anything
  multi-host needs a networked filesystem or a managed database.
- **Named volumes beat anonymous ones.** Anonymous volumes accumulate as untraceable IDs and are exactly
  what `prune` removes by surprise.
- **Read-only root filesystem** is a strong hardening step: `--read-only` plus a `tmpfs` for scratch.
  If an attacker cannot write, most exploits become far harder.
- **Do not put secrets in `-e`.** They are visible in `docker inspect`, in `ps` output and in shell
  history. Use file-based secrets, `tmpfs`, or the platform's secret manager.
- **Network segmentation works here too.** Frontend and backend networks with the database only on the
  backend is the same defence-in-depth you would apply anywhere.
- **In Kubernetes these concepts map directly** - volumes to PersistentVolumeClaims, user-defined
  networks to Services and DNS. Learning them properly here pays off twice.

> **PRACTICE - Practice now**
>
> 1. Run Postgres **without** a volume, create a table, `docker rm -f` it, run it again - the table is gone.
> 2. Repeat with `-v pgdata:/var/lib/postgresql/data` and confirm the table survives.
> 3. Run two containers on the default bridge and try to `ping` by name. It fails.
> 4. Create a user-defined network, attach both, and try again. It works.
> 5. Publish a database with `-p 5432:5432`, then check from another machine whether you can reach it. Then rebind to `127.0.0.1` and re-check.
> 6. Bind mount a source directory, edit a file on the host, and see the change inside the container.
> 7. Back up a volume with the tar one-liner and restore it into a fresh volume.

> **ASSIGNMENT - Assignment**
>
> Deploy a three-container stack imperatively - database, API, reverse proxy - with a named volume, two networks (frontend and backend), the database published to nothing, resource limits and restart policies. Then write the runbook: how to back up the volume, how to restore it, and exactly which command would destroy the data. Test the restore. An untested backup is not a backup.

## 8. Interview drill

<details>
<summary><b>Volume or bind mount?</b></summary>

Volumes for anything stateful in any environment you care about: Docker manages the location and
permissions, they are portable across hosts and backed by tooling. Bind mounts for development, where you
want live host files inside the container, or for injecting a specific host config file - at the cost of
depending on host paths and UID matching.

</details>

<details>
<summary><b>Where does a container's data live if you do not mount anything?</b></summary>

In the container's writable layer, which is part of the container and is deleted when the container is
removed or replaced. Stop and start preserve it; `rm` and any deployment that replaces the container do
not.

</details>

<details>
<summary><b>Two containers cannot talk to each other by name. Why?</b></summary>

They are on the default bridge network, which has no embedded DNS - only IPs work there, and IPs change.
Create a user-defined bridge network and attach both containers; Docker then resolves container names
automatically.

</details>

<details>
<summary><b>Should a database container publish its port?</b></summary>

Not if only other containers use it - put it on a shared user-defined network and publish nothing. If you
need local access for tooling, bind to `127.0.0.1` explicitly. Publishing `0.0.0.0:5432` on an
internet-facing host exposes the database to the internet, and Docker's iptables rules can bypass what
your host firewall appears to allow.

</details>

<details>
<summary><b>An app inside a container cannot reach a service on `localhost`. Why?</b></summary>

Each container has its own network namespace, so `localhost` is the container itself. Use the other
container's name on a shared network, or `host.docker.internal` (Desktop) / the gateway address on Linux
to reach the host.

</details>

---

[← Module 10](10-registries.md) &nbsp;&nbsp;|&nbsp;&nbsp; [Module 12: Docker Compose →](12-compose.md)

---

Docker: Zero to Architect · Himanshu Kumar.
