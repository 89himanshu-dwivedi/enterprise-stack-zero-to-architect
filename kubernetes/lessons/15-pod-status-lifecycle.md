# Pod status and lifecycle

*Module 15 · Pods*

As a Kubernetes administrator, the command you run immediately after creating anything is
`kubectl get pods`. It answers with a status - `Running`, `Completed`, `Error`, `CrashLoopBackOff` - and
those words are your primary troubleshooting instrument. This module produces each one deliberately, so
you never have to guess what it means.

[Course home](../index.md) / Module 15

## 1. Reading the line properly

```text
NAME       READY   STATUS      RESTARTS   AGE
demo-pod   1/1     Running     0          2m
```

| Column | Meaning |
| --- | --- |
| `NAME` | The Pod name |
| `READY` | **ready containers / total containers** in this Pod |
| `STATUS` | What is happening - the subject of this module |
| `RESTARTS` | How many times a container has been restarted, and when |
| `AGE` | How long the Pod object has existed |

`READY` and `STATUS` are different questions. `0/1 Completed` is perfectly healthy for a job that
finished; `0/1 Running` would be a problem.

Start from a clean cluster:

```bash
kubectl get nodes         # one control plane, two workers
kubectl get pods          # No resources found
```

We will create a **new Pod for each status**, leaving the previous ones running, so at the end you can
see them all side by side.

## 2. `Running`

```bash
kubectl run lifecycle-running --image=nginx
```

```bash
kubectl get pods
# lifecycle-running   0/1   ContainerCreating   0   6s
```

`ContainerCreating` because the nginx image is not on the node yet - it is being downloaded from Docker
Hub, which takes a moment. Then:

```text
NAME                READY   STATUS    RESTARTS   AGE
lifecycle-running   1/1     Running   0          48s
```

Read it as: a Pod named `lifecycle-running`, containing 1 container, and that container is in the
`Running` state.

> **NOTE - Pod `Running` means at least one container is running**
>
> The Pod's status is `Running` if **any** container in it is running. That is why a two-container Pod can show `1/2 Running` - one container is up, one is not. `Running` broadly means "nothing has gone wrong", but always read the `READY` column with it.

```bash
kubectl get pods -o wide                    # IP and node
kubectl describe pod lifecycle-running      # Containers: State: Running, Image: nginx
```

In `describe`, the `Containers` section shows one container, state `Running`, image nginx, started by
containerd - the node's runtime. Everything is fine.

## 3. `Completed`

Leave the first Pod alone. Create a second one, of a deliberately different shape:

```bash
kubectl run lifecycle-success --image=busybox --restart=Never -- sh -c "echo done"
```

Every part of that matters:

| Part | Why |
| --- | --- |
| `--image=busybox` | A very popular, tiny Linux image - convenient for running Linux commands |
| `--restart=Never` | Park this for five minutes. Section 5 explains it |
| `--` | Everything **after** this runs **inside the container** |
| `sh -c` | Starts a shell in the container, because you need a shell to run a command |
| `"echo done"` | The command it runs |

This container has a different life from nginx: it is **created, runs one command, and terminates**. That
is a completely normal shape - think of a script you only need to run once.

```bash
kubectl get pods
```

```text
NAME                READY   STATUS      RESTARTS   AGE
lifecycle-running   1/1     Running     0          3m
lifecycle-success   0/1     Completed   0          12s
```

`0/1` - the container is no longer running. `Completed` - it finished.

```bash
kubectl describe pod lifecycle-success
```

```text
    State:          Terminated
      Reason:       Completed
      Exit Code:    0
```

> **Why it matters:** **Exit code 0 means mission accomplished.** The container was created for one job, it did it, and it stopped. Any non-zero exit code means the command did not succeed - and the status changes accordingly. The container is gone; the Pod object remains, which is how you can still read its result.

## 4. `Error`

Same shape, one difference - a command that fails:

```bash
kubectl run lifecycle-fail --image=busybox --restart=Never -- sh -c "exit 1"
```

```bash
kubectl get pods
```

```text
lifecycle-running   1/1   Running             0   5m
lifecycle-success   0/1   Completed           0   2m
lifecycle-fail      0/1   ContainerCreating   0   4s
```

Wait a moment, then:

```text
lifecycle-fail      0/1   Error               0   20s
```

```bash
kubectl describe pod lifecycle-fail
```

```text
    State:          Terminated
      Reason:       Error
      Exit Code:    1
```

We **simulated** the failure with `exit 1`. In real life you never write that - your command fails on its
own and the exit code becomes non-zero automatically.

| | `Completed` | `Error` |
| --- | --- | --- |
| Container terminated | Yes | Yes |
| Reason | `Completed` | `Error` |
| Exit code | `0` | non-zero |

The container terminates in both cases - **100% of the time**. The only difference is whether it
succeeded.

### 4.1 Exit codes worth recognising

| Code | Meaning |
| --- | --- |
| `0` | Success |
| `1` | General application error |
| `126` | Command found but not executable |
| `127` | Command not found - usually a typo, or a missing binary in a slim image |
| `137` | **SIGKILL** - almost always **OOMKilled**, the container exceeded its memory limit |
| `139` | Segmentation fault |
| `143` | **SIGTERM** - terminated gracefully, normal during a rolling update |

> **TIP - 137 is a memory problem, not a code problem**
>
> Exit code 137 with `Reason: OOMKilled` means the kernel killed your container for exceeding its memory limit. No amount of reading application logs will explain it, because the process was killed instantly and never got to complain. Check `kubectl describe` for `OOMKilled` before you go anywhere near the code.

## 5. `CrashLoopBackOff` - and the `--restart=Never` mystery

Now the question we parked.

In Kubernetes you do not start and stop Pods, and you do not start and stop the containers inside them.
A container can be restarted - but not by a kubectl command. It happens through the **restart policy**.

When you create a Pod with `kubectl run`, a restart policy is applied by default. It watches whether the
container is running, and **if it is not, it restarts it.**

Now think about our `Completed` container. Its mission was: run one command, then terminate. So without
`--restart=Never`:

```mermaid
flowchart LR
    S0["Pod created"]
    S1["Container starts"]
    S2["Runs the command"]
    S3["Terminates - mission complete"]
    S0 --> S1
    S1 --> S2
    S2 --> S3
    F0["restartPolicy is Always - the default"]
    F1["Kubernetes sees the container is not running"]
    F2["Restarts it"]
    F3["It runs the command and terminates again"]
    F4["Restart, terminate, restart, terminate..."]
    F5["CrashLoopBackOff"]
    S3 -.->|"fails"| F0
    F0 --> F1
    F1 --> F2
    F2 --> F3
    F3 --> F4
    F4 --> F5
    classDef bad fill:#fdecea,stroke:#c62828;
    class F0,F1,F2,F3,F4,F5 bad;
```

> **Why it matters:** `CrashLoopBackOff` is **not an error message**. It is Kubernetes reporting that it is doing exactly what you told it to: keep this container running. The container refuses to stay running, so the loop repeats with a growing delay. The fault is never in Kubernetes - it is in the container, the command, or the restart policy you chose.

Prove it. Take the `Completed` command and simply **remove `--restart=Never`**:

```bash
kubectl run lifecycle-success-1 --image=busybox -- sh -c "echo done"
kubectl get pods
```

```text
lifecycle-success-1   0/1   CrashLoopBackOff   3   (30s ago)   90s
```

Same image, same command, one flag different. Note the `RESTARTS` column climbing.

> **NOTE - "BackOff" is the delay, not the crash**
>
> Kubernetes does not restart instantly forever - it backs off: roughly 10s, 20s, 40s, doubling up to a five-minute cap. So `CrashLoopBackOff` literally means "it keeps crashing, and I am now waiting before trying again". A Pod that has been crashing for an hour retries only every five minutes.

### 5.1 The three restart policies

| Policy | Behaviour | Use for |
| --- | --- | --- |
| `Always` | Restart the container whenever it stops, success or failure | **Default.** Long-running servers - web apps, APIs |
| `OnFailure` | Restart only on a non-zero exit | Jobs that should retry until they succeed |
| `Never` | Never restart | One-shot tasks - exactly our `Completed` demo |

It applies to **both** cases from sections 3 and 4: a container that completes successfully and one that
errors will both be restarted under `Always`.

## 6. The wider status list

You have produced four. These are the others you will meet, with the first thing to check.

| Status | Meaning | First command |
| --- | --- | --- |
| `Pending` | Not scheduled to a node yet | `kubectl describe pod` → `FailedScheduling` |
| `ContainerCreating` | Scheduled; image pulling or container starting | Wait, then `describe` if it is stuck |
| `Running` | At least one container is up | Check `READY` too |
| `Completed` | All containers finished, exit code 0 | Nothing - this is success |
| `Error` | Container terminated with a non-zero exit | `kubectl logs`, and check the exit code |
| `CrashLoopBackOff` | Restarting repeatedly | `kubectl logs --previous` |
| `ImagePullBackOff` / `ErrImagePull` | Cannot fetch the image | Check the tag, and the pull secret |
| `CreateContainerConfigError` | A ConfigMap or Secret it references does not exist | `describe` names the missing object |
| `Init:0/1` | An init container has not finished | `kubectl logs pod -c <init-container>` |
| `Terminating` | Being deleted, in its grace period | Wait; investigate finalizers if it hangs |
| `Evicted` | Node ran out of memory or disk and removed it | `describe` node; check resource requests |
| `Unknown` / `NodeLost` | The node stopped reporting | `kubectl get nodes` |

> **TIP - `--previous` is the command for CrashLoopBackOff**
>
> `kubectl logs <pod>` shows the *current* container, which has usually just started and printed nothing yet. `kubectl logs <pod> --previous` shows the container that **crashed** - which is the output you actually want. This one flag turns a frustrating status into a five-second diagnosis.

## 7. Clean up

```bash
kubectl get pods
kubectl delete pods --all
kubectl get pods
```

> **WARNING - `--all` respects your current namespace, and nothing else**
>
> It deletes every Pod in the namespace you are currently pointed at, with no confirmation. Harmless in a lab; catastrophic if your context is production. `kubectl config current-context` first - the same habit as always.

> **PRACTICE - Practice now**
>
> Produce all four statuses yourself, deliberately, and keep them side by side.
>
> 1. `Running`:
>    ```bash
>    kubectl run lifecycle-running --image=nginx
>    kubectl get pods
>    kubectl describe pod lifecycle-running
>    ```
> 2. `Completed`:
>    ```bash
>    kubectl run lifecycle-success --image=busybox --restart=Never -- sh -c "echo done"
>    kubectl get pods
>    kubectl describe pod lifecycle-success
>    ```
>    Find `State: Terminated`, `Reason: Completed`, `Exit Code: 0`.
> 3. `Error`:
>    ```bash
>    kubectl run lifecycle-fail --image=busybox --restart=Never -- sh -c "exit 1"
>    kubectl get pods
>    kubectl describe pod lifecycle-fail
>    ```
>    Find `Reason: Error`, `Exit Code: 1`.
> 4. `CrashLoopBackOff` - the same command as step 2, with the flag removed:
>    ```bash
>    kubectl run lifecycle-success-1 --image=busybox -- sh -c "echo done"
>    kubectl get pods -w
>    ```
>    Watch `RESTARTS` climb and the status settle on `CrashLoopBackOff`.
> 5. **Read the crashed container's output**, not the new one:
>    ```bash
>    kubectl logs lifecycle-success-1
>    kubectl logs lifecycle-success-1 --previous
>    ```
> 6. **See the backoff growing.** Watch the `RESTARTS` timestamp over a few minutes.
> 7. **Produce an exit code 137** and confirm it is memory, not code:
>    ```bash
>    kubectl run oom --image=busybox --restart=Never --overrides='{"spec":{"containers":[{"name":"oom","image":"busybox","command":["sh","-c","dd if=/dev/zero of=/dev/null bs=1M count=99999"],"resources":{"limits":{"memory":"16Mi"}}}]}}'
>    kubectl describe pod oom
>    ```
> 8. Look at them all together, then clean up:
>    ```bash
>    kubectl get pods
>    kubectl delete pods --all
>    ```

> **ASSIGNMENT - Assignment**
>
> Build a one-page triage table of your own: every status in section 6, the single command you would run first, and the three most likely causes. Then have someone create a broken Pod in your cluster without telling you what they did, and diagnose it using only your table. If your table gets you there, it is finished. If you had to improvise, add what you learned - and that is a document you will still be using in three years.

## 8. Interview drill

<details>
<summary><b>What does `CrashLoopBackOff` mean?</b></summary>

That a container keeps exiting and Kubernetes keeps restarting it, with an increasing delay between
attempts - roughly 10s, 20s, 40s, capped at five minutes. It is not an error in Kubernetes; it is
Kubernetes correctly honouring the restart policy while the container refuses to stay up. The cause is in
the container: a bad command, a missing config value, a failed dependency, or a task that was never meant
to run continuously. Diagnose it with `kubectl logs <pod> --previous`, because the current container has
usually just started.

</details>

<details>
<summary><b>What is the difference between `Completed` and `Error`?</b></summary>

Both mean the container terminated - that part is identical. `Completed` means it exited with code 0, so
it did the job it was created for. `Error` means a non-zero exit code, so the command failed. You see the
distinction in `kubectl describe` under `State: Terminated` as `Reason` and `Exit Code`. The Pod object
survives in both cases, which is how you can still read its logs and exit status.

</details>

<details>
<summary><b>Why would a Pod that runs one command end up in `CrashLoopBackOff`?</b></summary>

Because the default restart policy is `Always`. A container built to run a command and exit terminates
successfully, Kubernetes sees a container that is not running, restarts it, and the cycle repeats until
it backs off. The fix is to declare the intent: `restartPolicy: Never` or `OnFailure`, which is exactly
what `--restart=Never` sets. For real one-shot work you would use a Job, which manages this properly.

</details>

<details>
<summary><b>A Pod shows exit code 137. What happened?</b></summary>

The container received SIGKILL - 128 plus signal 9. In Kubernetes that almost always means `OOMKilled`:
the container exceeded its memory limit and the kernel killed it instantly. `kubectl describe pod` will
show `Reason: OOMKilled`. Application logs will show nothing useful because the process had no chance to
report anything. The fix is either a higher memory limit or an application that uses less - and in the
JVM's case, making sure the heap is sized from the container limit.

</details>

<details>
<summary><b>What is the difference between the `READY` and `STATUS` columns?</b></summary>

`STATUS` describes what is happening to the Pod overall; `READY` is the count of containers passing their
readiness check out of the total in the Pod. They answer different questions, so combinations matter:
`0/1 Completed` is a healthy finished job, `1/2 Running` means one container in a two-container Pod is
not up, and `0/1 Running` means something is wrong. Traffic is only routed to a Pod whose containers are
ready, which is why `READY` matters more than `STATUS` for a service.

</details>

<details>
<summary><b>What are the three restart policies?</b></summary>

`Always` - restart the container whenever it stops, regardless of exit code. This is the default and is
correct for long-running servers. `OnFailure` - restart only on a non-zero exit, used for work that
should retry until it succeeds. `Never` - do not restart, used for one-shot tasks. The policy is set on
the Pod and applies to all its containers, and choosing the wrong one is the most common cause of an
unexpected `CrashLoopBackOff`.

</details>

---

[← Module 14](14-creating-pods.md) &nbsp;&nbsp;|&nbsp;&nbsp; [Module 16: Logs, exec and port-forward →](16-logs-exec-portforward.md)

---

Kubernetes Administration: Zero to Architect · Himanshu Kumar.
