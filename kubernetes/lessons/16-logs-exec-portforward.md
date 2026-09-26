# Logs, exec and port-forward

*Module 16 · Pods*

A Pod contains a container, and your application runs inside it. Three questions follow immediately:
**what did it print**, **can I get inside it**, and **can I open it in a browser**. Three commands answer
them, and between them they cover most of day-to-day Kubernetes work.

[Course home](../index.md) / Module 16

## 1. `kubectl logs` - what did it print?

Create a Pod that produces output and then finishes:

```bash
kubectl run demo-pod --image=busybox --restart=Never -- sh -c "echo 'Hello from the cluster'; echo 'Pod log line 1'; echo 'Pod log line 2'"
```

`echo` prints. So this container will print three lines and terminate.

```bash
kubectl get pods
# demo-pod   0/1   ContainerCreating   0   4s
# demo-pod   0/1   Completed           0   18s
```

`Completed` - the container ran and exited cleanly. **But what did it actually print?**

```bash
kubectl logs demo-pod
```

```text
Hello from the cluster
Pod log line 1
Pod log line 2
```

### 1.1 `logs` is not `describe`

This trips people up, so make it explicit:

```bash
kubectl describe pod demo-pod
```

`describe` tells you the container was busybox, that it is `Terminated`, `Completed`, exit code 0 - and
it even shows the **command** that was supposed to run. It does **not** show what that command produced.

| Command | Answers |
| --- | --- |
| `kubectl describe pod` | What state is the container in, and what was it asked to do? |
| `kubectl logs` | What did it actually output? |

> **Why it matters:** Seeing the command listed in `describe` makes people think they are looking at output. They are not - that is the *instruction*, not the *result*. For the result there is exactly one command, and it is `logs`.

### 1.2 Live logs with `-f`

Now a container that keeps producing output:

```bash
kubectl run live-demo --image=busybox --restart=Never -- sh -c 'i=1; while [ $i -le 100 ]; do echo "tick $i"; sleep 1; i=$((i+1)); done'
```

```bash
kubectl get pods
# live-demo   1/1   Running   0   8s
```

`Running`, because the command is still executing. Follow its output live:

```bash
kubectl logs -f live-demo
```

```text
tick 1
tick 2
...
tick 17
```

Press **Ctrl-C** to stop following. That stops *your* view - the container is still running and will
carry on until tick 100, then go `Completed`.

```bash
kubectl logs live-demo          # everything written so far
kubectl get pods                # Completed, once it reaches 100
kubectl logs live-demo          # now all 100 lines
```

| Flag | Use |
| --- | --- |
| `-f` | Follow live, like `tail -f` |
| `--previous` | The **crashed** container's logs - essential for `CrashLoopBackOff` |
| `--tail=50` | Only the last 50 lines |
| `--since=10m` | Only the last ten minutes |
| `--timestamps` | Prefix each line with a timestamp |
| `-c <container>` | Which container, in a multi-container Pod |
| `-l app=web` | All Pods matching a label |
| `deploy/web` | Follow a controller, not a Pod name |

```bash
kubectl logs -f deploy/web --tail=100 --timestamps
```

> **WARNING - Logs die with the Pod**
>
> `kubectl logs` reads the container's stdout and stderr from the node. Delete the Pod and they are gone; the node rotates them anyway. Anything you might need after an incident must be shipped somewhere central - Loki, Elasticsearch, CloudWatch. Also note that an application writing to a *file* inside the container produces **no** `kubectl logs` output at all. Containerised applications must log to stdout.

## 2. `kubectl exec` - getting inside

Sometimes you need to be in the container: inspect a file, check a config, start or stop something,
confirm what the process can actually see.

```bash
kubectl run demo-pod --image=nginx
kubectl get pods            # wait for 1/1 Running - the first pull takes a while
```

### 2.1 One command at a time

```bash
kubectl exec demo-pod -- hostname
```

```text
demo-pod
```

That is the **container's** hostname, not your machine's - proof the command ran inside.

```bash
kubectl exec demo-pod -- pwd            # /
kubectl exec demo-pod -- ls             # the container's root filesystem
kubectl exec demo-pod -- cat /etc/os-release
```

| Part | Meaning |
| --- | --- |
| `kubectl exec` | Run a command inside a container |
| `demo-pod` | Which Pod |
| `--` | Everything after this belongs to the **container**, not to kubectl |
| `hostname` | The command to run |

> **TIP - The `--` is not optional**
>
> Without it, kubectl tries to interpret your command's flags as its own. `kubectl exec demo-pod ls -l` fails confusingly; `kubectl exec demo-pod -- ls -l` works. Same separator, same reason, as `kubectl run ... -- sh -c "..."`.

### 2.2 An interactive shell

```bash
kubectl exec -it demo-pod -- sh
```

```text
# pwd
/
# ls
bin  boot  dev  etc  home  ...
# hostname
demo-pod
# exit
```

`-it` is **i**nteractive plus a **t**ty - a real terminal. `exit` returns you to your own machine.

| Situation | Use |
| --- | --- |
| One quick command | `kubectl exec pod -- cmd` |
| Poking around | `kubectl exec -it pod -- sh` |
| Multi-container Pod | add `-c <container>` |
| Debian/Ubuntu-based image | `bash` works |
| Alpine-based image | only `sh` - there is no bash |
| Distroless or scratch | **no shell at all** - see below |

> **WARNING - Do not fix production with `exec`**
>
> Anything you change inside a running container is lost the moment it restarts, and it exists in no manifest, no git history and no one else's knowledge. `exec` is for *investigating*. The fix belongs in the image or the manifest. Note also that `pods/exec` is a distinct RBAC permission precisely because it is powerful - it grants shell access to your workloads.

> **NOTE - When there is no shell**
>
> Distroless and `scratch` images contain no `sh`, so `exec` simply fails. That is a security feature, not a defect. Use `kubectl debug` to attach an ephemeral container with tooling, or - as the Docker course put it - build with `--target` and debug in the fat stage instead of putting a shell into production.

## 3. `kubectl port-forward` - opening it in a browser

The application runs in a container inside a Pod. The **container has no IP; the Pod does.** There are
several ways to reach an application from outside, and the simplest is port forwarding.

```bash
kubectl run demo-pod --image=nginx
kubectl get pods
kubectl get pods -o wide          # note the Pod IP
```

> **NOTE - Why nginx for this lab**
>
> nginx serves on **TCP port 80 out of the box** - a ready-to-use application with no configuration. Many images do not listen on any port at all, and then there is nothing to forward to. Use an image that actually serves something when you are learning this.

```bash
kubectl port-forward pod/demo-pod 8080:80 --address 0.0.0.0
```

```text
Forwarding from 0.0.0.0:8080 -> 80
```

| Part | Meaning |
| --- | --- |
| `pod/demo-pod` | Forward to this Pod |
| `8080:80` | **local** 8080 → **container** 80 |
| `--address 0.0.0.0` | Listen on all interfaces, not just localhost |

Two details worth being precise about. **`8080:80` means a user connecting to port 8080 is sent to the
Pod's port 80** - and port 80 has to actually be serving something inside the container, which with nginx
it is. And **`--address 0.0.0.0` is needed here because you are browsing from your Windows machine, not
from the VM itself** - by default port-forward listens only on localhost, which would be the VM's own
loopback.

```mermaid
flowchart LR
    S0["Browser on Windows"]
    S1["VM IP port 8080"]
    S2["kubectl port-forward process"]
    S3["API server"]
    S4["kubelet on the node"]
    S5["Pod, port 80 - nginx"]
    S0 --> S1
    S1 --> S2
    S2 -->|"over the Kubernetes API"| S3
    S3 --> S4
    S4 --> S5
    F0["Image does not listen on port 80"]
    F1["Forwarding starts anyway"]
    F2["Browser shows connection refused"]
    F3["Nothing is wrong with port-forward"]
    S5 -.->|"fails"| F0
    F0 --> F1
    F1 --> F2
    F2 --> F3
    classDef bad fill:#fdecea,stroke:#c62828;
    class F0,F1,F2,F3 bad;
```

> **Why it matters:** Port-forward tunnels through the **API server**, not through the Pod network. That is why it works without any Service, without any ingress, and without your machine having a route to the Pod's IP - and why it is authenticated and encrypted. It is also why it is a debugging tool and never a way to serve users.

Open a browser at `http://<vm-ip>:8080` and you get **Welcome to nginx**.

> **NOTE - "But I want my own page"**
>
> Everyone thinks it at this point. That comes later, once we can build and ship an image and mount configuration. Right now the goal is narrower and worth respecting: prove that traffic from your browser reached a container running inside a Pod on a worker node.

Stop it with **Ctrl-C**, then clean up:

```bash
kubectl delete pod demo-pod
kubectl get pods
```

### 3.1 Port-forward in real use

```bash
kubectl port-forward svc/my-service 8080:80
kubectl port-forward deploy/web 8080:80
kubectl port-forward pod/db 5432:5432          # reach a database with a local client
kubectl port-forward pod/web :80               # random free local port
```

| | `port-forward` | A Service |
| --- | --- | --- |
| Who can reach it | Only you, while the command runs | Anything with network access |
| Needs a Service | No | It *is* one |
| Survives Ctrl-C | No | Yes |
| Load balanced | No - one Pod | Yes, across all ready Pods |
| Use for | Debugging, admin tools, local development | **Actual traffic** |

## 4. The three commands together

```mermaid
flowchart LR
    N0["Something is wrong with my app"]
    N1["kubectl describe pod - what state is it in?"]
    N2["kubectl logs - what did it say?"]
    N3["kubectl exec - what can it see from inside?"]
    N4["kubectl port-forward - does it actually respond?"]
    N0 --> N1
    N1 --> N2
    N2 --> N3
    N3 --> N4
```

> **Why it matters:** That order is the whole debugging loop, and it is deliberate. `describe` for the platform's view, `logs` for the application's view, `exec` for the environment's view, `port-forward` to test the behaviour end to end. Skipping straight to `exec` is the most common way to waste twenty minutes.

> **PRACTICE - Practice now**
>
> **Logs**
>
> 1. A Pod that prints and exits:
>    ```bash
>    kubectl run demo-pod --image=busybox --restart=Never -- sh -c "echo 'Hello from the cluster'; echo 'Pod log line 1'; echo 'Pod log line 2'"
>    kubectl get pods
>    kubectl logs demo-pod
>    ```
> 2. **Prove `describe` does not show output:**
>    ```bash
>    kubectl describe pod demo-pod
>    ```
>    Find the command in the output. Now notice the three printed lines are nowhere.
> 3. Live logs:
>    ```bash
>    kubectl run live-demo --image=busybox --restart=Never -- sh -c 'i=1; while [ $i -le 100 ]; do echo "tick $i"; sleep 1; i=$((i+1)); done'
>    kubectl logs -f live-demo
>    ```
>    Ctrl-C, confirm with `kubectl get pods` that it is still `Running`, then follow it again.
> 4. Try the useful flags:
>    ```bash
>    kubectl logs live-demo --tail=5
>    kubectl logs live-demo --timestamps
>    kubectl logs live-demo --since=30s
>    ```
>
> **exec**
>
> 5. ```bash
>    kubectl run nginx-demo --image=nginx
>    kubectl exec nginx-demo -- hostname
>    kubectl exec nginx-demo -- pwd
>    kubectl exec nginx-demo -- ls
>    kubectl exec nginx-demo -- cat /etc/os-release
>    ```
> 6. Interactive:
>    ```bash
>    kubectl exec -it nginx-demo -- sh
>    # pwd, ls, hostname, then: exit
>    ```
> 7. **Prove the `--` matters.** Run `kubectl exec nginx-demo ls -l` and read the error, then run it
>    correctly.
> 8. **Prove changes inside are lost:**
>    ```bash
>    kubectl exec nginx-demo -- sh -c "echo hacked > /tmp/proof"
>    kubectl exec nginx-demo -- cat /tmp/proof
>    kubectl delete pod nginx-demo
>    kubectl run nginx-demo --image=nginx
>    kubectl exec nginx-demo -- cat /tmp/proof
>    ```
>
> **port-forward**
>
> 9. ```bash
>    kubectl get pods -o wide
>    kubectl port-forward pod/nginx-demo 8080:80 --address 0.0.0.0
>    ```
>    Open `http://<vm-ip>:8080` in your browser. Ctrl-C to stop.
> 10. **Prove the Pod IP is not reachable from your laptop** - try the IP from `-o wide` in the browser
>     and watch it fail. Then confirm it *is* reachable from inside the cluster:
>     ```bash
>     kubectl run curler --image=busybox --restart=Never --rm -it -- wget -qO- http://<pod-ip>
>     ```
> 11. Clean up:
>     ```bash
>     kubectl delete pods --all
>     ```

> **ASSIGNMENT - Assignment**
>
> Deploy any application image you like that serves HTTP, then answer four questions using one command each, writing them down: what has it logged since it started, what is its hostname from inside, what files are in its working directory, and what does it return over HTTP. Then delete the Pod and try to retrieve its logs. The failure of that last step is the single best argument for centralised logging you will ever get, and you should be able to explain it in an interview.

## 5. Interview drill

<details>
<summary><b>What is the difference between `kubectl logs` and `kubectl describe pod`?</b></summary>

`describe` reports the platform's view - the container's state, restart count, the command it was given,
resource settings and the Events showing scheduling, image pulls and probe failures. `logs` returns the
application's stdout and stderr. They answer different questions: `describe` tells you whether the
container started and why it did not, `logs` tells you what it said once it did. Failures before startup
appear only in `describe`; application errors appear only in `logs`.

</details>

<details>
<summary><b>A Pod is in `CrashLoopBackOff` and `kubectl logs` shows nothing. What now?</b></summary>

`kubectl logs <pod> --previous`. The current container has just been restarted and has not printed
anything yet; the output you want belongs to the instance that crashed. If that is also empty, the process
is probably dying before it writes anything - check `kubectl describe` for the exit code, since 137 means
OOMKilled and 127 usually means the command does not exist in that image.

</details>

<details>
<summary><b>My application writes logs to a file. Why does `kubectl logs` show nothing?</b></summary>

Because `kubectl logs` reads the container's stdout and stderr as captured by the container runtime, not
files inside the container. A containerised application should log to stdout so the platform can collect
it. If the application cannot be changed, the usual workaround is a sidecar that tails the file and echoes
it to stdout, or a log agent mounted into the same Pod.

</details>

<details>
<summary><b>Is `kubectl exec` an acceptable way to fix a problem in production?</b></summary>

No. Anything changed inside a running container is lost when it restarts, exists in no manifest and no
git history, and creates drift nobody can see. `exec` is for investigation - reading a config, checking
DNS, confirming an environment variable. The fix belongs in the image or the manifest. It is also a
distinct RBAC permission, `pods/exec`, because shell access to a workload is effectively access to
whatever that workload can reach.

</details>

<details>
<summary><b>How does `kubectl port-forward` work, and when should you use it?</b></summary>

It opens a tunnel through the **API server** to the kubelet and on to the Pod, so traffic to a local port
arrives at a container port. Because it goes through the API it is authenticated and encrypted, needs no
Service or ingress, and works even though the Pod IP is not routable from your machine. It is a debugging
and administration tool: it serves one Pod, no load balancing, and it stops the moment you press Ctrl-C.
Real traffic belongs behind a Service.

</details>

<details>
<summary><b>You ran port-forward successfully but the browser says connection refused. Why?</b></summary>

Almost always because nothing is listening on the target port inside the container - the image does not
serve HTTP, or it serves on a different port. Port-forward will happily start regardless, since it only
sets up the tunnel. Check the container port with `kubectl describe pod` or by exec-ing in. The other
common cause is browsing from a different machine without `--address 0.0.0.0`, since port-forward binds
only to localhost by default.

</details>

---

[← Module 15](15-pod-status-lifecycle.md) &nbsp;&nbsp;|&nbsp;&nbsp; [Module 17: Creating a Pod with YAML →](17-pod-yaml.md)

---

Kubernetes Administration: Zero to Architect · Himanshu Kumar.
