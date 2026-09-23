# Securing the repo and supply chain

*Module 24 · Pipelines*

Your repository is the input to everything: the pipeline trusts it, the artefact comes from it,
production runs whatever it produces. That makes it the highest-value target in your delivery
system - and it is usually the least protected. This module covers what protects the repository
itself, what proves a commit and an artefact are genuine, and what to do when a secret gets
committed.

[Course home](../index.md) / Module 24

## 1. The threat model

Ask the uncomfortable question: what would an attacker actually do?

```mermaid
flowchart TB
    T0["Attacker's goal:<br/>run their code in production"]
    T1["Push directly to main"]
    T2["Approve their own PR"]
    T3["Compromise a dependency"]
    T4["Compromise a CI action"]
    T5["Steal a CI secret"]
    T6["Impersonate a maintainer"]
    T0 --> T1
    T0 --> T2
    T0 --> T3
    T0 --> T4
    T0 --> T5
    T0 --> T6
```

| Attack | Control that stops it |
| --- | --- |
| Direct push to `main` | Branch protection / rulesets |
| Self-approved change | Required reviewers, CODEOWNERS |
| Malicious dependency | Pinning, lockfiles, review of updates |
| Malicious GitHub Action | Pin actions to a commit SHA |
| Stolen long-lived key | OIDC (module 22) |
| Forged authorship | Signed commits |
| Secret in history | Push protection, then rotation |

> **Why it matters:** Almost every real-world supply chain incident is one of these six, and each has a specific control that is available to you today for free. The failure is rarely a missing capability - it is that nobody turned it on.

## 2. Branch protection and rulesets

Everything else assumes `main` cannot be modified arbitrarily. Configure this first.

**Manual step:** *Settings → Rules → Rulesets → New branch ruleset*, targeting `main`.

| Rule | Why |
| --- | --- |
| **Require a pull request** | No direct pushes, ever |
| **Required approvals: 1+** | Someone other than the author looked |
| **Dismiss stale approvals** | New commits invalidate the old approval |
| **Require review from Code Owners** | The right person, not any person |
| **Require status checks** | The pipeline must pass |
| **Require branches up to date** | Tested against current `main` |
| **Block force pushes** | History cannot be rewritten |
| **Restrict deletions** | The branch cannot vanish |
| **Require signed commits** | Authorship is verifiable |
| **Require linear history** | No merge commits, if that is your model |

```bash
gh api repos/:owner/:repo/rulesets --jq '.[].name'
gh api repos/:owner/:repo/branches/main/protection
```

> **WARNING - "Include administrators" is off by default**
>
> Without it, every rule above is advisory for the people most worth targeting. An admin account
> is the highest-value credential in the organisation precisely because it can bypass the
> controls. Turn it on, and use a documented break-glass procedure instead of a standing
> exemption.

> **NOTE - Dismiss stale approvals matters more than it sounds**
>
> Without it, a reviewer approves a two-line fix, the author pushes twenty more commits, and the
> merge carries an approval that applies to code nobody read. This is a realistic self-approval
> path that does not look like one.

## 3. CODEOWNERS

`CODEOWNERS` maps paths to the people who must review changes to them.

```
# .github/CODEOWNERS
*                       @myorg/engineering
/infra/                 @myorg/platform
/.github/workflows/     @myorg/platform @myorg/security
/src/payments/          @myorg/payments-team
*.tf                    @myorg/platform
package-lock.json       @myorg/security
```

Last matching pattern wins - order matters.

```mermaid
flowchart LR
    A["PR touches<br/>.github/workflows/"]
    B["CODEOWNERS match"]
    C["platform + security<br/>auto-requested"]
    D["Merge blocked until<br/>they approve"]
    A --> B
    B --> C
    C --> D
```

> **Why it matters:** The most dangerous file in most repositories is the **workflow file**, because whoever edits it controls what runs with your CI credentials. A PR that adds one innocuous-looking step to a workflow can exfiltrate every secret the job can read. Owning `.github/workflows/` is the highest-leverage line in the file.

## 4. Signed commits

Git's author field is plain text you set yourself. Signing proves who really made the commit.

```bash
git config --global user.name "Someone Else"
git config --global user.email "someone.else@example.com"
git commit -m "looks legitimate"
```

That commit is now attributed to a person who never touched it.

**SSH signing** - simplest if you already have an SSH key:

```bash
git config --global gpg.format ssh
git config --global user.signingkey ~/.ssh/id_ed25519.pub
git config --global commit.gpgsign true
git config --global tag.gpgsign true
```

**Manual step:** *GitHub → Settings → SSH and GPG keys → New SSH key → Key type: **Signing Key***.
The same key must be added a second time as a signing key - an authentication key alone will not
verify commits.

```bash
git log --show-signature -1
git verify-commit HEAD
```

| Badge | Meaning |
| --- | --- |
| **Verified** | Signature valid, key belongs to that account |
| **Partially verified** | Signed, but the committer differs from the signer |
| **Unverified** | Signature present but the key is unknown to GitHub |
| *(none)* | Not signed |

> **Why it matters:** Without signing, a stolen token is enough to author commits as anyone in the organisation, and the history looks completely normal. With required signing, an attacker with push access still cannot produce a commit that verifies as you. It is the only control that makes authorship an actual fact rather than a claim.

> **TIP - Turn on vigilant mode**
>
> *Settings → SSH and GPG keys → Flag unsigned commits as unverified.* Without it, unsigned
> commits show no badge at all, which reads as normal. With it they are explicitly marked
> Unverified - so a forged commit stands out instead of blending in.

## 5. Secret scanning and push protection

There are two different features and the difference is the whole point.

```mermaid
flowchart TB
    D1["Secret scanning"]
    D2["Scans commits already pushed"]
    D3["Alerts after the fact"]
    D4["Secret is already in history"]
    P1["Push protection"]
    P2["Blocks the push itself"]
    P3["Secret never enters the repo"]
    D1 --> D2
    D2 --> D3
    D3 --> D4
    P1 --> P2
    P2 --> P3
```

**Manual step:** *Settings → Code security → Secret scanning → Enable*, then **Push protection →
Enable**. Turn both on at organisation level for all repositories.

> **WARNING - Detection is not prevention**
>
> By the time a scanning alert fires, the credential is in the repository, in every clone, in
> every fork, and possibly already harvested by a bot watching public pushes. Push protection is
> the control that matters; scanning is the safety net for what predates it.

**When a secret has been committed:**

```mermaid
flowchart TB
    S1["Secret found in history"]
    S2["ROTATE THE CREDENTIAL"]
    S3["Remove from history<br/>git filter-repo"]
    S4["Force push, coordinate team"]
    S5["Ask provider to purge caches"]
    S1 --> S2
    S2 --> S3
    S3 --> S4
    S4 --> S5
```

```bash
pip install git-filter-repo
git filter-repo --path config/secrets.yml --invert-paths
git push --force-with-lease --all
```

> **WARNING - Rotation comes first, and history removal never substitutes for it**
>
> Cleaning history is cosmetic. The value was exposed the moment it was pushed, forks and caches
> may retain it, and rewriting history breaks every clone. Rotate first, always; then decide
> whether the rewrite is worth the disruption.

## 6. Dependency security

```mermaid
flowchart LR
    Y["Your code<br/>~2%"]
    D["Direct dependencies"]
    T["Transitive dependencies<br/>~98% of shipped code"]
    Y --> D
    D --> T
```

| Control | What it does |
| --- | --- |
| **Lockfile, committed** | Everyone and every build resolves identical versions |
| **Dependabot alerts** | Notifies on known vulnerabilities |
| **Dependabot updates** | Opens PRs to bump versions |
| **Dependency review** | Blocks a PR that introduces a vulnerable package |
| **`npm ci` / `pip install -r`** | Installs from the lockfile, never re-resolves |

```yaml
# .github/dependabot.yml
version: 2
updates:
  - package-ecosystem: npm
    directory: /
    schedule:
      interval: weekly
    groups:
      dev-dependencies:
        dependency-type: development
    open-pull-requests-limit: 5

  - package-ecosystem: github-actions
    directory: /
    schedule:
      interval: weekly
```

```yaml
- uses: actions/dependency-review-action@v4
  with:
    fail-on-severity: high
```

> **Why it matters:** Grouping updates is not cosmetic. Ungrouped Dependabot opens thirty PRs a week, the team stops reading them, and they get merged on autopilot - which is precisely the state an attacker needs. A small number of reviewed updates is safer than a large number of ignored ones.

> **NOTE - Automerging patch updates is a real decision**
>
> It keeps you current, and it means a compromised patch release reaches your `main` without a
> human ever looking. If you automerge, require the full test suite plus dependency review to
> pass first, and never automerge anything that runs at build time.

## 7. Pinning GitHub Actions

An action is someone else's code running inside your job with access to your secrets.

```yaml
- uses: actions/checkout@v4                 # a tag - movable
- uses: some-org/some-action@main           # a branch - changes silently
- uses: actions/checkout@b4ffde65f46336ab8  # a commit SHA - immutable
```

```mermaid
flowchart TB
    A1["You reference @v4"]
    A2["Maintainer account compromised"]
    A3["v4 tag moved to malicious commit"]
    A4["Your next run executes it"]
    A5["Secrets exfiltrated"]
    A1 --> A2
    A2 --> A3
    A3 --> A4
    A4 --> A5
```

**Manual step:** *Settings → Actions → General* and set:

| Setting | Value |
| --- | --- |
| Actions permissions | Allow selected actions only |
| Workflow permissions | **Read repository contents** by default |
| Allow PR approvals from Actions | **Off** |
| Fork PR workflows | Require approval for all outside collaborators |

```yaml
permissions:
  contents: read          # default everything to read

jobs:
  publish:
    permissions:
      contents: read
      packages: write     # widen only where needed
      id-token: write
```

> **WARNING - `pull_request_target` runs with full secrets**
>
> Unlike `pull_request`, it runs in the context of the base repository with access to secrets. If
> such a workflow checks out the PR's code and executes anything from it - a build script, a
> test, a lint config - an outside contributor gets your secrets. Never check out untrusted code
> in a `pull_request_target` workflow.

## 8. Provenance, SBOM and signing artefacts

Branch protection secures the input. These secure the output.

| Artefact | Question it answers |
| --- | --- |
| **SBOM** | What is inside this build? |
| **Provenance / attestation** | Which workflow, from which commit, produced it? |
| **Signature** | Has it been altered since it was built? |

```yaml
- name: Generate build provenance
  uses: actions/attest-build-provenance@v1
  with:
    subject-name: ghcr.io/myorg/myapp
    subject-digest: ${{ steps.build.outputs.digest }}
    push-to-registry: true

- name: Generate SBOM
  uses: anchore/sbom-action@v0
  with:
    image: ghcr.io/myorg/myapp@${{ steps.build.outputs.digest }}
    format: spdx-json
```

```bash
gh attestation verify oci://ghcr.io/myorg/myapp:1.4.2 --owner myorg
cosign verify ghcr.io/myorg/myapp:1.4.2 \
  --certificate-identity-regexp "https://github.com/myorg/myapp/.*" \
  --certificate-oidc-issuer https://token.actions.githubusercontent.com
```

> **Why it matters:** When the next widely-used package is compromised, the only question that matters is "are we affected?" With SBOMs you query your builds and answer in minutes. Without them you read dependency trees by hand across every service, under time pressure, and you are never quite sure you finished.

> **PRACTICE - Practice now**
>
> 1. On a test repository, create a ruleset on `main`: require a PR, one approval, block force
>    pushes, include administrators.
> 2. Try `git push origin main` directly. Read the rejection message.
> 3. Enable secret scanning **and** push protection. Commit a fake AWS key
>    (`AKIAIOSFODNN7EXAMPLE`) and push - watch the push be blocked, not merely reported.
> 4. Turn on SSH commit signing and push a commit. Confirm the **Verified** badge.
> 5. Add a `.github/CODEOWNERS` that assigns `.github/workflows/` to yourself, then open a PR
>    touching a workflow and confirm you are auto-requested.
> 6. Pin every action in one workflow to a commit SHA. Note how much less readable it is - that
>    is the real cost, and it is why Dependabot updates for `github-actions` matter.

## 9. Where this maps: SLSA

SLSA is a maturity ladder for how trustworthy a build is.

| Level | Requirement | You get this from |
| --- | --- | --- |
| **1** | Build is scripted, provenance exists | Any CI workflow with attestation |
| **2** | Hosted build service, signed provenance | GitHub-hosted runners + attestations |
| **3** | Non-falsifiable provenance, isolated builds | Hardened reusable workflows |
| **4** | Two-person review, hermetic reproducible builds | Rarely reached |

> **NOTE - Level 2 is a realistic target**
>
> Hosted runners plus `attest-build-provenance` plus pinned actions gets most teams there in an
> afternoon. Level 3 requires build isolation guarantees that are genuine engineering work, and
> level 4's hermetic reproducible builds are a research-grade commitment. Claiming a level you
> have not implemented is worse than claiming none.

> **ASSIGNMENT - Assignment**
>
> Harden one repository end to end and write down what each control stops.
>
> 1. Ruleset on `main`: PR required, 1 approval, dismiss stale approvals, required checks, block
>    force push, include administrators, require signed commits.
> 2. `CODEOWNERS` with `.github/workflows/` owned by a security-minded reviewer.
> 3. Secret scanning + push protection on, org-wide if you can.
> 4. `dependabot.yml` covering your language **and** `github-actions`, with grouped updates.
> 5. Every action in every workflow pinned to a commit SHA.
> 6. Default `permissions: contents: read` at workflow level, widened per job.
> 7. Build provenance attestation and an SBOM published with the image.
> 8. Then attempt each of the six attacks from section 1 against your own repository and record
>    exactly which control blocked it. Anything you cannot block is a real gap - write that down
>    too.

## 10. Interview drill

<details>
<summary><b>What is the minimum branch protection you would insist on?</b></summary>

Require a pull request with at least one approving review, require status checks to pass, block
force pushes, restrict deletion, and apply all of it to administrators. The administrator clause
is the one people omit, and it makes every other rule advisory for exactly the accounts most
worth compromising. Dismissing stale approvals matters more than it appears, because otherwise an
approval given for a small change survives twenty more commits that nobody reviewed.

</details>

<details>
<summary><b>Why sign commits when the repository already requires authentication?</b></summary>

Because authentication proves who pushed, not who authored. The author and committer fields are
free text set by local config, so anyone with push access can attribute a commit to a colleague
and the history will look entirely normal. Signing binds the commit to a key, so a required
signature means a compromised push token still cannot produce a commit that verifies as someone
else. Vigilant mode is worth enabling too, so unsigned commits are marked Unverified rather than
showing no badge at all.

</details>

<details>
<summary><b>Secret scanning vs push protection?</b></summary>

Scanning inspects what has already been pushed and raises an alert - by which point the
credential is in history, in every clone and fork, and possibly already harvested by bots
watching public activity. Push protection blocks the push itself, so the secret never enters the
repository. Only one of them is a control; the other is a safety net for what predates it.

</details>

<details>
<summary><b>A secret has been committed. What do you do, in order?</b></summary>

Rotate the credential first - that is the only step that actually removes the exposure. Then
assess whether it was used, using provider audit logs. Only then consider rewriting history with
`git filter-repo`, understanding that it breaks every existing clone and must be coordinated, and
that forks and provider caches may still hold the value. Cleaning history without rotating is
theatre; it makes the repository look clean while the credential stays valid.

</details>

<details>
<summary><b>Why pin GitHub Actions to a commit SHA?</b></summary>

Because a tag is a movable pointer. If a maintainer account is compromised, `v4` can be
repointed at malicious code and your next run executes it with access to whatever secrets the
job can read. A commit SHA is content-addressed and cannot be repointed. The cost is readability
and manual updates, which is exactly why you also enable Dependabot for the `github-actions`
ecosystem so the pins get bumped deliberately.

</details>

<details>
<summary><b>Which file in a repository is the most dangerous to change?</b></summary>

The workflow file, because it defines what executes with the pipeline's credentials. A single
added step in a workflow can print or exfiltrate every secret the job can read, and it reviews as
a small configuration change rather than as code. That is why `.github/workflows/` should be
owned in CODEOWNERS by someone who will read it carefully, and why default workflow permissions
should be read-only and widened per job.

</details>

<details>
<summary><b>What is the risk with `pull_request_target`?</b></summary>

It runs in the context of the base repository with access to secrets, unlike `pull_request` which
runs without them for forks. If a `pull_request_target` workflow checks out the pull request's
code and then executes anything from it - a build script, a test, a lint configuration - an
outside contributor achieves arbitrary code execution with your secrets. It exists for labelling
and commenting on fork PRs, and untrusted code should never be checked out inside it.

</details>

<details>
<summary><b>What is an SBOM for, practically?</b></summary>

Answering "are we affected?" quickly when a widely-used package turns out to be compromised. It
is a machine-readable inventory of everything in a build, so you query your artefacts and get an
answer in minutes rather than reading dependency trees by hand across every service under
incident pressure. Paired with provenance attestation - which records the workflow and commit
that produced the artefact - it also lets a consumer verify the thing they are running came from
where it claims.

</details>

<details>
<summary><b>What does SLSA describe, and what level is realistic?</b></summary>

It is a framework describing how trustworthy a build process is, from scripted builds with
provenance at level 1 up to hermetic, reproducible, two-person-reviewed builds at level 4. Level
2 - a hosted build service producing signed provenance - is achievable for most teams quickly
using GitHub-hosted runners, build attestations and pinned actions. Level 3 requires genuine
build isolation guarantees, and level 4 is a research-grade commitment. Claiming a level you have
not actually implemented is worse than claiming none.

</details>

<details>
<summary><b>If you could turn on only three things, which?</b></summary>

Branch protection including administrators, because it is the precondition for every other
control being meaningful; push protection for secrets, because it prevents rather than detects
the most common real-world leak; and OIDC instead of long-lived cloud keys, because it removes
the credential that is worth stealing in the first place. Signing, pinning and SBOMs matter, but
those three close the paths that actual incidents most often take.

</details>

---

[← Module 23](23-gitops.md) &nbsp;&nbsp;|&nbsp;&nbsp; [Module 25: Git and pipelines for Salesforce →](25-salesforce.md)
