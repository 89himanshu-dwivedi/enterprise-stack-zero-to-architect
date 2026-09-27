# 🚀 GIT PUSH  --- Git Zero → Architect Master Notes (Enriched Edition)

> **25 Modules • Hinglish • Print-Friendly • No Important Concept Skipped • Interview Ladder**
>
> **Question ladder:** 🟢 Beginner → 🔵 Junior/Developer → 🟡 Senior → 🟠 Lead → 🔴 Architect
>
> **Core rule:** Git command yaad karne se pehle **Working Tree → Staging → Commit → Branch → Remote → PR → CI/CD → Deployment** ka mental model clear karo.

**What's new in this enriched edition:** every module now has a **🧪 Try It Yourself** mini-lab, a **💡 Extra Insight** callout, sample command *output* (not just the command), a **🩹 Common Error & Fix** box, and short model-answer notes have been added at the end for the trickiest interview questions. A new **Glossary**, **Common Error Messages Reference**, and **Command Reference by Task** section have been added at the end.

---

# 🗺️ 25-MODULE ROADMAP

| #  | Module                | Core Focus                                  |
|----|-----------------------|----------------------------------------------|
| 01 | Why Git Exists        | Version control + Git mental model           |
| 02 | Install & Configure   | Identity, config, auth, line endings         |
| 03 | Three Trees           | Working Tree, Index, Repository              |
| 04 | First Repository      | init/clone/add/commit/log                    |
| 05 | Undoing Things        | restore/revert/amend/reset                   |
| 06 | Branching & Merging   | branches, merge, fast-forward                |
| 07 | Remotes               | clone/fetch/pull/push/tracking               |
| 08 | Merge Conflicts       | detect → resolve → validate                  |
| 09 | Rebase vs Merge       | history design + risks                       |
| 10 | Rescue Kit            | reflog, recovery, lost work                  |
| 11 | Pull Requests         | review, CI, approval, merge                  |
| 12 | Branching Strategies  | GitHub Flow, trunk-based, release models     |
| 13 | Commit Hygiene        | atomic commits, messages, staging            |
| 14 | Tags & Releases       | version markers + release traceability       |
| 15 | Rewriting History     | amend/reset/rebase/filtering concepts         |
| 16 | Hooks                 | local automation + policy                    |
| 17 | Large Repositories    | performance, monorepo, LFS                   |
| 18 | Git Internals         | blob/tree/commit/DAG/SHA                     |
| 19 | CI/CD Fundamentals    | build/test/validate/deploy                   |
| 20 | GitHub Actions Basics | workflows/jobs/steps/secrets                 |
| 21 | Real Pipeline         | PR → CI → artifact → UAT → prod              |
| 22 | Deployment            | artifact promotion + rollback                |
| 23 | GitOps                | Git as desired-state source                  |
| 24 | Securing Supply Chain | secrets, dependencies, provenance            |
| 25 | Salesforce            | DX, metadata, packages, CI/CD                |

---

# 01 --- WHY GIT EXISTS

## Problem Before Git

Without version control:

```text
report_final.doc
report_final_v2.doc
report_final_latest.doc
report_final_latest_REAL.doc
```

Problems:

- Kya change hua?
- Kisne change kiya?
- Why change kiya?
- Parallel work kaise karein?
- Known-good version par kaise wapas jaayein?
- Review kaise karein?

## Git Gives 5 Core Capabilities

1. **Change tracking**
2. **Author + history**
3. **Parallel development**
4. **Rollback/recovery**
5. **Review before integration**

## Centralized vs Distributed

```text
SVN/TFS:
Server = history
Local = mostly working files

Git:
Server = repository copy
Local clone = full repository/history
```

Git ka distributed model:

- commits local ho sakte hain
- history local inspect ho sakti hai
- diff local hota hai
- branches cheap hain
- offline work possible hai

## Git Stores Snapshots

> **Git stores snapshots, not a list of diffs.**

```text
Commit A → project snapshot
Commit B → next snapshot
Commit C → next snapshot
```

Unchanged content ko Git reuse/re-reference karta hai; diff demand par calculate hota hai.

## Git ≠ GitHub

```text
Git     = version-control software
GitHub  = hosting + collaboration platform
```

GitHub adds: remote hosting, PRs, reviews, issues, permissions, branch protection, CI/CD integration. (Note: GitLab, Bitbucket, Azure Repos are equivalent hosting platforms — same idea, different vendor.)

## Delivery Mental Model

```text
Code → Commit → Push → PR → Review + CI → Merge → Deploy
```

## When Git Alone Is Not Enough

- Huge binaries → Git LFS / artifact storage
- Secrets → secret manager
- Generated build output → artifact registry
- Very large game/media assets → specialized storage/versioning

### ⚠️ Gotcha

**Committed secret = history problem.** `.gitignore` future tracking rokta hai; already committed secret ko history se magically remove nahi karta. Secret ko rotate/revoke bhi karna padta hai.

### 🧪 Try It Yourself

```bash
mkdir git-lab-01 && cd git-lab-01
git init
echo "hello world" > notes.txt
git add notes.txt
git commit -m "Add notes"
git log --oneline
```

Expected-shape output:

```text
a1b2c3d Add notes
```

Notice the commit already has a stable identifier before you've pushed anywhere — that's the "local repo = full history" idea in action.

### 💡 Extra Insight

People often say "Git backs up my code." More precise: **Git preserves intent and history of change**, not just bytes. A ZIP backup tells you "this is what existed on date X." Git tells you "this is what existed, who changed it, why (via message), and what came before/after."

### 🩹 Common Error & Fix

```text
git: 'init' is not a git command. See 'git --help'.
```
Usually a typo or Git isn't installed/on PATH — verify with `git --version` first.

### 🎤 Questions

**🟢 Beginner:** Git kya hai?
**🔵 Junior:** Git distributed kyun hai?
**🟡 Senior:** Git snapshots store karta hai to storage manageable kaise rehta hai?
**🟠 Lead:** Git workflow review/rollback ko kaise improve karta hai?
**🔴 Architect:** Git source-of-truth ko runtime/deployment state se kaise separate karoge?

**Model-answer hint (Senior):** Git snapshots each commit fully, but object storage is content-addressed — unchanged files across commits point to the *same* blob object, and Git periodically packs objects (`git gc`, packfiles with delta compression) so storage stays efficient despite "full snapshot per commit" semantics.

---

# 02 --- INSTALL & CONFIGURE

## Install

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install -y git

# RHEL/Fedora
sudo dnf install -y git

# macOS
brew install git
```

Windows: official Git installer use karo (includes Git Bash).

Verify:

```bash
git --version
```

## Identity

```bash
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
```

> GitHub account login aur Git commit author same concept nahi hain. Commit association email se hoti hai.

## Config Levels

```text
System → Global → Local
```

```bash
git config --system   # /etc/gitconfig (all users on machine)
git config --global   # ~/.gitconfig (this user, all repos)
git config --local    # .git/config inside a repo (this repo only)
```

Effective precedence: `local > global > system`

Inspect:

```bash
git config --list --show-origin
```

Sample output:

```text
file:/etc/gitconfig            core.editor=nano
file:C:/Users/you/.gitconfig   user.name=Your Name
file:.git/config               user.email=work-email@company.com
```

## Line Endings

```bash
# Windows
git config --global core.autocrlf true

# macOS/Linux
git config --global core.autocrlf input
```

Better team control via `.gitattributes`:

```gitattributes
* text=auto
*.sh text eol=lf
*.ps1 text eol=crlf
*.png binary
*.jar binary
```

## Useful Defaults

```bash
git config --global init.defaultBranch main
git config --global fetch.prune true
git config --global rebase.autoStash true
git config --global push.autoSetupRemote true
git config --global core.editor "code --wait"   # optional: use VS Code for commit messages
git config --global alias.st status              # optional: shortcuts
git config --global alias.co checkout
git config --global alias.lg "log --oneline --graph --decorate --all"
```

## HTTPS vs SSH

```text
HTTPS → token/credential helper
SSH   → SSH key pair
```

Never share the **private** key; share only the **public** key where required.

Test SSH:

```bash
ssh -T git@github.com
```

Expected-shape output:

```text
Hi <username>! You've successfully authenticated, but GitHub does not provide shell access.
```

### ⚠️ Gotchas

- Wrong email → commits may not associate correctly with your GitHub profile.
- `core.autocrlf` mismatch → noisy diffs (whole file shows "changed" because of line endings only).
- Password authentication should not be assumed for modern Git hosting — use a Personal Access Token or SSH key.
- Credential/token should have least privilege (e.g., repo-scoped, not full account access).
- Global `.gitignore` is for machine/editor junk (`.DS_Store`, `.idea/`); project-specific ignores belong in repo `.gitignore`.

### 🧪 Try It Yourself

```bash
git config --global --edit
```

This opens your global config in your editor so you can see every setting in one place.

### 💡 Extra Insight

Aliases (`git lg`) are a small but high-leverage investment — most senior engineers have 5–10 muscle-memory aliases. Start with `alias.lg` above; it turns raw history into a readable graph instantly.

### 🩹 Common Error & Fix

```text
fatal: unable to auto-detect email address (got 'you@yourhost.(none)')
```
Fix: run `git config --global user.email "you@example.com"` before your first commit.

### 🎤 Questions

**🟢:** `git config --global` kya karta hai?
**🔵:** Local config global se kaise override hoti hai?
**🟡:** CRLF/LF issue kaise diagnose karoge?
**🟠:** Enterprise team ke liye HTTPS vs SSH ka decision kaise loge?
**🔴:** Identity, authentication aur authorization ko Git architecture mein kaise separate karoge?

---

# 03 --- THE THREE TREES

## Three Places

```text
1. Working Tree
   ↓ git add
2. Staging Area / Index
   ↓ git commit
3. Repository / History
```

### Working Tree

Actual editable files, exactly as you see them in your folder/IDE.

### Staging Area (Index)

Next commit mein exactly kya jaana hai uska selected state — a "draft" of the next commit.

### Repository

`.git` ke andar commits/objects/history — the permanent record.

### HEAD

HEAD **fourth tree nahi** hai. It is a reference/pointer to current branch/commit context — "where am I right now."

## Main Commands

```bash
git add file            # Working → Staging
git commit               # Staging → Repository
git restore --staged file   # Staging → Working
git restore file             # discard working-tree changes
```

## Status

```bash
git status
git status -s
```

Short status codes:

```text
 M   = modified in working tree, not staged
M    = staged
MM   = staged, then modified again (two different versions!)
??   = untracked
A    = newly added (staged)
D    = deleted
```

Sample `git status -s` output:

```text
M  src/app.js
 M README.md
?? notes/todo.txt
```

Read this as: `app.js` fully staged; `README.md` modified but not staged; `todo.txt` is new and untracked.

## Diff

```bash
git diff             # Working vs Staging
git diff --staged    # Staging vs HEAD
git diff HEAD        # Working vs HEAD (combines both)
```

> `git diff` empty hona after staging everything **normal** hai — it means "nothing left un-staged," not "nothing changed."

## Selective Staging

```bash
git add fileA
git add -p
```

`git add -p` se same file ke selected hunks stage kar sakte ho — useful when one file has two unrelated changes that should go in two commits.

Sample interactive prompt:

```text
Stage this hunk [y,n,q,a,d,s,e,?]?
```

### ⚠️ Dangerous

```bash
git restore file
```

Uncommitted working-tree change genuinely destroy kar sakta hai — there is no undo for this one (unless your editor has local history).

### 🧪 Try It Yourself

```bash
echo "line 1" > demo.txt
git add demo.txt
git commit -m "Add demo"
echo "line 2" >> demo.txt
git status -s
git diff
git add -p demo.txt
git diff --staged
```

Watch how `git diff` and `git diff --staged` show *different* content at each step.

### 💡 Extra Insight

Think of staging as a "shopping cart" for your next commit. `git add` = put in cart. `git commit` = checkout. `git restore --staged` = take back out of cart (item stays in your hands/working tree). This mental model eliminates 90% of staging confusion.

### 🩹 Common Error & Fix

```text
error: pathspec 'file.txt' did not match any file(s) known to git
```
Usually a typo in the filename, or the file is genuinely untracked and doesn't exist at that path — check with `git status` first.

### 🎤 Questions

**🟢:** Three trees kya hain?
**🔵:** `git add` exactly kya karta hai?
**🟡:** `git diff` vs `git diff --staged`?
**🟠:** `MM` ka meaning?
**🔴:** Staging layer large-team code review/release quality ko kaise improve karti hai?

---

# 04 --- FIRST REPOSITORY

## Create

```bash
mkdir demo
cd demo
git init
```

## First Flow

```bash
git status
git add README.md
git commit -m "Add README"
git log
```

## Clone Existing Repo

```bash
git clone <url>
cd <repo>
```

## Useful History

```bash
git log
git log -10
git log --oneline
git log --oneline --graph
git log --oneline --graph --decorate
```

Sample output:

```text
* 7f3a2c1 (HEAD -> main, origin/main) Add login validation
* 4d9e8b2 Fix typo in README
* 1a2b3c4 Initial commit
```

## Inspect Commit

```bash
git show <SHA>
```

## Compare

```bash
git diff
git diff --staged
git diff <commit1> <commit2>
git diff main feature
git diff v1.0 v1.1
```

## Basic Lifecycle

```text
Untracked → (add) → Staged → (commit) → Tracked/Committed → (edit) → Modified → (add) → Staged again
```

### 🧪 Try It Yourself

```bash
git log --pretty=format:"%h | %an | %ad | %s" --date=short -5
```

This gives a compact table-like view: short hash, author, date, subject — genuinely useful for daily standups.

### 💡 Extra Insight

`git log` has *dozens* of useful flags beyond `--oneline`: `--author="name"`, `--since="2 weeks ago"`, `-- path/to/file` (history of just that file), and `--stat` (files touched + line counts per commit). Learning 3–4 of these saves real debugging time.

### 🩹 Common Error & Fix

```text
fatal: not a git repository (or any of the parent directories): .git
```
You ran a Git command outside a repo folder, or `git init` was never run — `cd` into the right folder or initialize it.

### 🎤 Questions

**🟢:** `git init` vs `git clone`?
**🔵:** `git log --oneline` kyun useful hai?
**🟡:** `git show` aur `git diff` mein difference?
**🟠:** New repo ko team-standard banane ke liye kya configure karoge?
**🔴:** Repository bootstrap mein code, policy, CI aur security kaise establish karoge?

---

# 05 --- UNDOING THINGS

Sabse important distinction:

```text
restore → working/staging content
revert  → new commit
amend   → latest commit rewrite
reset   → branch/history pointer move
rebase  → commits recreate on new base
```

## `git restore`

```bash
git restore file             # working file restore
git restore --staged file    # unstage
```

## `git revert`

Shared history ke liye safer:

```bash
git revert <SHA>
```

Creates a new inverse commit:

```text
A → B(bad) → C
             ↓ revert
A → B → C → D(revert B)
```

## Amend

```bash
git commit --amend
```

Use for latest local commit only: message fix, staged missing file add. Care needed on shared/public commits (amend changes the SHA).

## Reset

```bash
git reset --soft HEAD~1
git reset --mixed HEAD~1
git reset --hard HEAD~1
```

| Mode  | HEAD  | Index                 | Working Tree |
|-------|-------|------------------------|--------------|
| soft  | moves | keeps changes staged   | keeps        |
| mixed | moves | unstages               | keeps        |
| hard  | moves | resets                 | resets       |

`mixed` is the default (i.e., `git reset HEAD~1` == `git reset --mixed HEAD~1`).

### ⚠️ `--hard`

Uncommitted changes destroy kar sakta hai — always double-check `git status` before running it.

### Shared-history rule

```text
Shared/public history → prefer revert/new commit
Private/local history → amend/reset/rebase may be appropriate
```

### 🧪 Try It Yourself

```bash
echo "v1" > file.txt && git add file.txt && git commit -m "v1"
echo "v2" >> file.txt && git add file.txt && git commit -m "v2"
git log --oneline
git reset --soft HEAD~1
git status -s
```

Notice: the "v2" commit is gone from history, but its changes are still staged — nothing was lost.

### 💡 Extra Insight

A quick decision rule for real incidents: **"Has anyone else pulled this commit?"** If yes → `revert`. If no (only exists on your machine) → `amend`/`reset` are fine. This single question resolves most "which command do I use" confusion.

### 🩹 Common Error & Fix

```text
error: your local changes to the following files would be overwritten by merge/checkout
```
Git is protecting you — either commit/stash your changes first (`git stash`), or explicitly discard them with `git restore` if you're sure.

### 🎤 Questions

**🟢:** Revert kya karta hai?
**🔵:** Revert vs reset?
**🟡:** Soft/mixed/hard?
**🟠:** Production bug ke liye revert ya reset? Why?
**🔴:** Rollback strategy ko application rollback + data rollback + deployment rollback mein kaise separate karoge?

**Model-answer hint (Lead):** Revert, almost always — production history is shared/public by definition, and a revert commit preserves an honest audit trail ("this was deployed, this broke it, this is the fix"), whereas reset rewrites history other people/CI/deploy tooling may already reference.

---

# 06 --- BRANCHING & MERGING

## Branch Is Not a Folder

A branch is essentially a **movable pointer** into the commit graph.

```text
main    → C
feature → F
```

## Create/Switch

Modern style:

```bash
git switch -c feature/login
git switch main
```

Traditional:

```bash
git checkout -b feature/login
```

## Merge

```bash
git switch main
git merge feature/login
```

### Fast-Forward

No divergence between the branches:

```text
A---B---C
        \
         D---E
```

Main pointer directly moves to `E` — no merge commit needed.

### Diverged Merge

```text
       D---E
      /
A---B---C
      \
       F---G
```

Integration creates a merge commit (depending on strategy/flags used).

## Branch Cleanup

```bash
git branch -d feature/login   # safe delete (blocked if unmerged)
git branch -D feature/login   # force delete, use carefully
git push origin --delete feature/login   # delete on remote too
```

### 🧪 Try It Yourself

```bash
git switch -c feature/x
echo "new feature" > feature.txt
git add feature.txt && git commit -m "Add feature"
git switch main
git merge feature/x
git log --oneline --graph
```

Try it again but make a conflicting change on `main` first, before merging, to see a non-fast-forward merge commit appear.

### 💡 Extra Insight

`git branch -d` refusing to delete an "unmerged" branch is a safety net, not a bug — it's Git telling you "this branch has commits that don't exist anywhere else yet." Don't reach straight for `-D`; investigate first with `git log feature/login ^main`.

### 🩹 Common Error & Fix

```text
error: The branch 'feature/login' is not fully merged.
```
Either merge it first, or if you're intentionally discarding the work, use `-D` deliberately (not by habit).

### 🎤 Questions

**🟢:** Branch kya hai?
**🔵:** Feature branch kyun?
**🟡:** Fast-forward merge?
**🟠:** Merge commit kab useful hai?
**🔴:** Large enterprise mein branch topology release risk ko kaise affect karti hai?

---

# 07 --- REMOTES

## Remote Model

```text
Local Repository ←→ Remote Repository
```

```bash
git remote -v
git remote add origin <url>
git fetch
git pull
git push
```

## Clone

`remote → local` (full history copy).

## Fetch

`remote → local remote-tracking refs`. Fetch does **not** touch your local working branch automatically.

```bash
git fetch origin
```

## Pull

Conceptually: `git pull ≈ git fetch + integration`. The integration step (merge vs rebase) depends on configuration (`pull.rebase`).

## Push

```bash
git push origin feature/login
```

## Tracking Branch

```bash
git push -u origin feature/login
```

After upstream is set, plain `git push` / `git pull` work without specifying remote/branch every time.

### ⚠️ Gotchas

- Local commit ≠ remote commit — if you haven't pushed, your team can't see it.
- Fetch ≠ merge — fetching updates your knowledge of the remote, not your working branch.
- Pull ko blindly run karna conflict/rebase behavior trigger kar sakta hai.
- Remote branch delete aur local branch delete are separate actions.

### 🧪 Try It Yourself

```bash
git remote -v
git fetch origin
git log origin/main --oneline -5
git log main --oneline -5
```

Comparing these two logs shows you exactly how far your local branch is behind/ahead of the remote — without touching your working files.

### 💡 Extra Insight

`git fetch` is one of the safest commands in Git — it *only* downloads data and updates remote-tracking refs (`origin/main`); it never touches your working tree or local branches. When unsure, `fetch` first, inspect, then decide whether to `merge`/`rebase`/`pull`.

### 🩹 Common Error & Fix

```text
fatal: The current branch feature/login has no upstream branch.
```
Fix: `git push --set-upstream origin feature/login` (or `-u` shorthand) once, then plain `push`/`pull` work.

### 🎤 Questions

**🟢:** push vs pull?
**🔵:** fetch vs pull?
**🟡:** tracking branch kya hai?
**🟠:** Stale local branch diagnose kaise karoge?
**🔴:** Multi-region/enterprise repositories mein remote topology aur permissions kaise design karoge?

---

# 08 --- MERGE CONFLICTS

## Conflict Kab?

Usually: separate branches + overlapping changes + same logical lines/files. Git cannot decide what the final content should be.

## Conflict Markers

```text
<<<<<<< HEAD
current branch content
=======
incoming change content
>>>>>>> branch-name
```

Real example:

```text
<<<<<<< HEAD
const MAX_RETRIES = 3;
=======
const MAX_RETRIES = 5;
>>>>>>> feature/retry-tuning
```

## Resolution

```text
1. git status
2. Open conflicted files
3. Decide correct final content
4. Remove markers
5. Test
6. git add <file>
7. git commit
8. push
```

During rebase:

```bash
git add <file>
git rebase --continue
```

Abort:

```bash
git rebase --abort
git merge --abort
```

## Important Architect Rule

> **Git conflict resolved ≠ business logic validated.**

Especially in Salesforce:

```text
Conflict resolved → Metadata validation → Apex/LWC tests → Security → Integration → UAT
```

## Prevent Conflicts

- short-lived branches
- frequent fetch/pull
- small PRs
- atomic commits
- avoid unnecessary overlapping edits
- feature flags where appropriate
- communicate ownership

### 🧪 Try It Yourself

```bash
git switch -c branch-a
echo "value = 1" > config.txt && git add . && git commit -m "Set value to 1"
git switch main
git switch -c branch-b
echo "value = 2" > config.txt && git add . && git commit -m "Set value to 2"
git switch main
git merge branch-a
git merge branch-b   # this one will conflict
```

Open `config.txt`, resolve manually, then `git add config.txt && git commit`.

### 💡 Extra Insight

Use `git diff` inside a conflicted file (no path needed) to see a **combined diff view** highlighting both sides at once — often clearer than reading raw `<<<<<<<` markers, especially with 3+ line conflicts. Also, `git checkout --ours <file>` / `--theirs <file>` can auto-resolve a file entirely to one side when that's the correct call.

### 🩹 Common Error & Fix

```text
error: Pulling is not possible because you have unmerged files.
```
Finish resolving the current conflict (`add` + `commit`, or `merge --abort`) before pulling again.

### 🎤 Questions

**🟢:** Merge conflict kya hai?
**🔵:** Conflict markers kya mean karte hain?
**🟡:** Conflict resolve karne ke baad next command?
**🟠:** Conflict repeatedly kyun aa raha hai?
**🔴:** Conflict ko team/process/design problem ke signal ke roop mein kaise analyze karoge?

---

# 09 --- REBASE VS MERGE

## Merge

History relationship preserve karta hai.

```text
A---B---C
 \     /
  D---E
```

## Rebase

Feature commits ko new base par replay karta hai:

```text
Before:
A---B---C
     \
      D---E

After:
A---B---C---D'---E'
```

`D'`/`E'` are new commits (different SHAs).

## Why SHA Changes?

Commit identity depends on parent information.

```text
D parent = B
D' parent = C
∴ D ≠ D'
```

## Interactive Rebase

```bash
git rebase -i <base>
```

Common operations: `pick`, `reword`, `edit`, `squash`, `fixup`, `drop`. Useful for cleaning up local history before opening a PR.

## Rule

```text
Shared history → don't casually rebase
Private/local history → rebase can be useful
```

### Merge vs Rebase

| Merge                         | Rebase                     |
|-------------------------------|-----------------------------|
| history relationship visible  | linear-looking history      |
| merge commit possible         | commits recreated           |
| shared branches friendly      | shared history risky        |
| preserves branch integration  | rewrites commit identity    |

Neither is universally "best"; team policy + workflow decide.

### 🧪 Try It Yourself

```bash
git switch -c feature/y
echo "a" > y.txt && git add . && git commit -m "y: step 1"
echo "b" >> y.txt && git add . && git commit -m "y: step 2"
git switch main
echo "main update" > m.txt && git add . && git commit -m "main: unrelated update"
git switch feature/y
git rebase main
git log --oneline --graph --all
```

Compare the commit SHAs of `y: step 1`/`step 2` before and after the rebase — they change.

### 💡 Extra Insight

`git rebase -i` with `squash`/`fixup` is one of the highest-value habits for commit hygiene: turn "wip", "wip 2", "fix typo", "actually fix it" into one clean "Add retry logic to payment API" commit *before* opening the PR — reviewers and future-you will thank you.

### 🩹 Common Error & Fix

```text
CONFLICT (content): Merge conflict in y.txt
```
during a rebase — resolve like a normal conflict, then `git add y.txt && git rebase --continue` (not `git commit`, rebase handles that per-commit).

### 🎤 Questions

**🟢:** Rebase kya hai?
**🔵:** Merge vs rebase?
**🟡:** Rebase SHA kyun change karta hai?
**🟠:** Public branch rebase kyun dangerous?
**🔴:** Enterprise history design mein readability vs historical fidelity ka trade-off kaise decide karoge?

---

# 10 --- RESCUE KIT

## `git reflog`

```bash
git reflog
```

Records local **reference movements** (not file content history) — every place HEAD has pointed, including resets, rebases, checkouts.

Sample output:

```text
a1b2c3d HEAD@{0}: reset: moving to HEAD~1
9f8e7d6 HEAD@{1}: commit: Add payment retry logic
4c5d6e7 HEAD@{2}: checkout: moving from main to feature/pay
```

Useful after: accidental reset, rebase mistake, branch movement, detached HEAD work.

Recovery pattern:

```text
Something disappeared → git reflog → Find previous HEAD → git show <SHA> → git branch rescue <SHA>
```

Example:

```bash
git branch rescue <old-sha>
```

## Important Limitation

Reflog is **not a backup**. It's local-only (never pushed), and entries expire (default ~90 days, less for unreachable commits) plus garbage collection can remove the underlying objects.

## Detached HEAD

HEAD can point directly at a commit instead of a branch. If there's useful work there:

```bash
git switch -c rescue-branch
```

### Real Recovery Checklist

```text
STOP destructive commands → git status → git reflog → identify SHA → git show SHA → create rescue branch → validate
```

### 🧪 Try It Yourself

```bash
echo "important" > work.txt && git add . && git commit -m "Important work"
git reset --hard HEAD~1     # "accidentally" lose the commit
git reflog                  # find it
git branch rescue HEAD@{1}  # or the SHA shown
git log rescue --oneline
```

### 💡 Extra Insight

`git reflog` only tracks *your local* ref movements — if a teammate did something destructive on their machine and pushed a force-update, your reflog can't see their history, only theirs (and even then, only if they haven't run `git gc`). This is exactly why real backups (remote copies, protected branches) matter alongside reflog.

### 🩹 Common Error & Fix

```text
fatal: ambiguous argument 'HEAD@{1}': unknown revision or path not in the working tree.
```
Reflog entries roll over as you make more commands — re-run `git reflog` to get the current, correct entry number/SHA rather than assuming `{1}` always means the same thing.

### 🎤 Questions

**🟢:** reflog kya hai?
**🔵:** Lost commit kaise dhoondoge?
**🟡:** reflog vs log?
**🟠:** reset --hard ke baad recovery plan?
**🔴:** Recovery architecture mein Git ke bahar backups/artifacts kyun required hain?

---

# 11 --- PULL REQUESTS

## PR = Collaboration Boundary

```text
Commit    = unit of source history
PR        = unit of collaboration/review
Merge     = integration
Deployment = delivery to environment
```

## Typical PR Flow

```text
Issue → Feature Branch → Changes → Atomic Commits → Push → PR → Review → CI → Approval → Merge
```

## PR Should Cover

- What changed?
- Why?
- Testing done?
- Risk?
- Dependencies?
- Deployment impact?
- Rollback/fix-forward plan?

## Protected Main

Protection can require: PR, approvals, status checks, no direct pushes, conversation resolution, signed/verified commits where applicable.

## Review Types

- general comments
- line comments
- approval
- request changes

### Architect Insight

Large Salesforce PR should also ask: Why this data model? Why this integration? Why this sharing model? Why this package boundary? Why this deployment strategy? PR sirf code review nahi; architecture governance bhi ho sakta hai.

### 🧪 Try It Yourself

Write a PR description template you can reuse:

```markdown
## What changed
## Why
## How was it tested
## Risk / rollback plan
## Screenshots (if UI)
```

Save this as `.github/pull_request_template.md` in a real repo — GitHub auto-fills it for every new PR.

### 💡 Extra Insight

A PR's real value isn't the diff — it's the **conversation** attached to the diff. Line comments become searchable "why did we do it this way" documentation months later. Treat PR descriptions as documentation you're writing for your future self, not paperwork for a reviewer.

### 🩹 Common Error & Fix

```text
This branch has conflicts that must be resolved (GitHub UI message)
```
Pull the latest target branch locally, merge/rebase it into your feature branch, resolve conflicts locally, then push again — GitHub's web conflict editor is fine for trivial cases only.

### 🎤 Questions

**🟢:** PR kya hai?
**🔵:** PR aur commit difference?
**🟡:** Protected branch kyun?
**🟠:** PR quality kaise improve karoge?
**🔴:** PR ko enterprise architecture governance boundary kaise banaoge?

---

# 12 --- BRANCHING STRATEGIES

## GitHub Flow

```text
main → feature branch → PR → CI/review → main → deploy
```

Short-lived branches.

## Trunk-Based Thinking

```text
small branch → frequent integration → main/trunk
```

Feature flags allow incomplete functionality to stay safely hidden in production.

## Release Branches

```text
main → release/x → production
```

Useful when the organization needs controlled release stabilization. Exact model is organization-specific.

## Long-Lived Branch Problem

```text
2-month branch → huge divergence → massive merge → conflicts
```

Prefer frequent integration where practical.

### 🧪 Try It Yourself

Sketch (on paper or in a diagram) your own team's actual branch lifecycle for the last feature you shipped. Compare it against GitHub Flow / trunk-based / release-branch models — most real teams are a hybrid, and naming the hybrid explicitly helps onboarding.

### 💡 Extra Insight

Feature flags decouple "merge to main" from "visible to users" — this is the real secret behind trunk-based development at scale. Teams merge daily but *release* features gradually, which is very different from "merge = go live."

### 🩹 Common Error & Fix

Symptom: "every release week is chaos, huge merge conflicts." Root cause is almost always long-lived branches, not tooling — the fix is process (shorter branches, smaller PRs), not a Git command.

### 🎤 Questions

**🟢:** Feature branch kyun?
**🔵:** GitHub Flow kya hai?
**🟡:** Trunk-based development?
**🟠:** Release branch kab useful?
**🔴:** Global enterprise team ke liye branching model choose karte waqt kaunse dimensions evaluate karoge?

---

# 13 --- COMMIT HYGIENE

## Atomic Commit

Atomic ≠ one line. Atomic = small + logical + coherent unit of work.

Good:

```text
Add Account service
Add Account tests
Fix null handling
Update metadata
```

Bad:

```text
fix stuff
changes
final
final2
```

## Selective Staging

```bash
git add -p
```

Benefits: review easier, rollback safer, cherry-pick easier, debugging easier, history clearer.

## Commit Message

Prefer an imperative, clear summary:

```text
Add retry handling for payment API
```

A widely used convention worth knowing: **Conventional Commits** (`feat:`, `fix:`, `chore:`, `docs:`, `refactor:`), which enables automated changelogs. Team convention decides.

## Commit = Debugging Tool

Atomic commits make `git bisect`, `revert`, and `cherry-pick` genuinely easier.

### 🧪 Try It Yourself

```bash
git log --oneline | head -20
git bisect start
git bisect bad                # current commit is broken
git bisect good <old-known-good-sha>
# Git checks out a midpoint commit; test it, then:
git bisect good   # or: git bisect bad
# repeat until Git reports the exact breaking commit
git bisect reset
```

`git bisect` performs a binary search across your history — with atomic commits this pinpoints the exact bug-introducing commit in log₂(n) steps instead of guessing.

### 💡 Extra Insight

The real test of "was this commit atomic enough?" is: **could I `git revert` it in isolation without breaking something unrelated?** If reverting one commit silently also removes an unrelated fix, the original commit mixed concerns.

### 🩹 Common Error & Fix

Symptom: `git bisect` keeps landing on commits that don't even build. This usually means CI wasn't run per-commit — a reason to enforce "every commit should at least compile" as a team norm.

### 🎤 Questions

**🟢:** Atomic commit kya?
**🔵:** Good commit message?
**🟡:** `git add -p` kyun?
**🟠:** Atomic commits rollback ko kaise improve karte hain?
**🔴:** Commit granularity aur release governance ka relationship?

---

# 14 --- TAGS & RELEASES

## Tag

A tag gives an important Git point a named reference.

```text
v1.0.0
v2.3.1
```

```bash
git tag
git tag v1.0.0
git push origin v1.0.0
git show v1.0.0
```

Annotated tag (preferred for releases — stores tagger, date, message, and can be signed):

```bash
git tag -a v1.0.0 -m "Release 1.0.0"
```

Lightweight tag (just a pointer, no metadata):

```bash
git tag quick-marker
```

## Release Traceability

```text
Requirement → Commit → PR → Merge → Tag → Artifact → Environment
```

Tag alone isn't the deployment artifact; it identifies a source point.

## Semantic Versioning

```text
MAJOR.MINOR.PATCH
```

Example: `2.4.1` — MAJOR = breaking change, MINOR = backward-compatible feature, PATCH = backward-compatible fix. Exact versioning policy is team-specific.

### 🧪 Try It Yourself

```bash
git tag -a v0.1.0 -m "First internal milestone"
git push origin v0.1.0
git tag -l "v0.*"
git show v0.1.0
```

### 💡 Extra Insight

Push does **not** include tags by default — `git push` alone won't send your tags to the remote. You need `git push origin <tagname>` or `git push --tags` explicitly. This surprises a lot of people the first time.

### 🩹 Common Error & Fix

```text
fatal: tag 'v1.0.0' already exists
```
Tags are meant to be immutable pointers to a release point — if you must move one, use `git tag -f v1.0.0 <new-sha>` and `git push --force origin v1.0.0`, but treat this as exceptional, not routine (anyone who already fetched the old tag now has a mismatch).

### 🎤 Questions

**🟢:** Tag kya hai?
**🔵:** Tag vs branch?
**🟡:** Annotated tag?
**🟠:** Release traceability kaise maintain karoge?
**🔴:** Source commit, tag, artifact aur deployed version ko cryptographically/operationally correlate kaise karoge?

---

# 15 --- REWRITING HISTORY

History rewrite tools:

```bash
git commit --amend
git reset
git rebase -i
```

## Safe Area

```text
local/private commits
```

## Risk Area

```text
shared/public history
```

## Force Push

History rewrite ke baad push required ho sakta hai.

```bash
git push --force              # dangerous: can silently overwrite others' work
git push --force-with-lease   # safer: fails if remote has commits you haven't seen
```

Never treat force-push as a routine production workflow. Prefer `--force-with-lease` whenever a force push is genuinely necessary.

## Sensitive Data Removal

```text
Deleting file in new commit ≠ removing secret from all Git history
```

Secret incident response:

```text
revoke/rotate secret + identify exposure + clean history if required + coordinate affected clones/caches
```

For repository-wide history rewriting, use dedicated history-rewrite tooling (e.g., `git filter-repo`) and organizational procedure — not ad hoc `filter-branch`, which is officially discouraged for this purpose.

### 🧪 Try It Yourself

```bash
git commit --amend -m "Corrected commit message"
git log -1 --format="%H %s"
```

Compare the SHA before and after amending — it changes even though only the message did.

### 💡 Extra Insight

`--force-with-lease` should be your default over `--force`. It checks "has the remote branch moved since I last fetched?" and refuses if so — protecting a teammate's just-pushed work that you haven't pulled yet. `--force` has no such check.

### 🩹 Common Error & Fix

```text
! [rejected] main -> main (stale info)
```
(with `--force-with-lease`) — someone else pushed after your last fetch. Fetch, inspect what changed, then decide whether to re-force or integrate their work instead.

### 🎤 Questions

**🟢:** History rewrite kya?
**🔵:** Amend kab?
**🟡:** Force push risk?
**🟠:** Secret commit ho gaya to kya karoge?
**🔴:** Enterprise history rewrite ke operational/security implications kya hain?

---

# 16 --- HOOKS

## Git Hooks

Hooks run automation on Git lifecycle events.

```text
pre-commit
commit-msg
pre-push
post-merge
```

Use cases: formatting, linting, commit message validation, tests, policy checks.

## Example Mental Model

```text
git commit → pre-commit → commit-msg → commit
```

## Gotcha

Local hooks don't automatically guarantee same behavior across every clone (hooks live in `.git/hooks`, which is **not** version-controlled by default — unlike `.gitattributes` or `.gitignore`).

Enterprise policy should not depend only on a local hook.

```text
Developer convenience → local hook
Authoritative enforcement → CI/server/platform
```

### 🧪 Try It Yourself

```bash
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/sh
echo "Running pre-commit checks..."
grep -r "TODO" --include="*.js" . && echo "Warning: TODO found" 
exit 0
EOF
chmod +x .git/hooks/pre-commit
git commit -m "test hook"
```

For team-shared hooks, look at tools like **Husky** (Node) or `core.hooksPath` pointing to a versioned folder — this solves the "hooks aren't tracked" gotcha.

### 💡 Extra Insight

`git config core.hooksPath .githooks` lets you version-control your hooks folder and have every clone use the same scripts automatically — a simple fix for the "hooks aren't shared" limitation.

### 🩹 Common Error & Fix

```text
hint: The '.git/hooks/pre-commit' hook was ignored because it's not set as executable.
```
Fix: `chmod +x .git/hooks/pre-commit`.

### 🎤 Questions

**🟢:** Hook kya hai?
**🔵:** pre-commit use case?
**🟡:** Hook vs CI?
**🟠:** Why can't local hooks be sole security control?
**🔴:** Developer experience aur centralized enforcement ka architecture?

---

# 17 --- LARGE REPOSITORIES

Problems: clone time, fetch time, disk usage, CI performance, huge history, generated files, binaries.

## Common Solutions

```text
.gitignore
Git LFS
sparse checkout
shallow clone
monorepo tooling
artifact storage
repo decomposition
```

### Don't Commit

```text
node_modules
build output
logs
local secrets
temporary files
huge generated assets
```

## Git LFS

```text
Git → pointer/metadata
LFS storage → large object
```

```bash
git lfs install
git lfs track "*.psd"
git add .gitattributes
```

## Shallow Clone

```bash
git clone --depth 1 <url>
```

Fetches only the latest snapshot, not full history — much faster for CI runners that just need to build, not investigate history.

## Monorepo

One repository, multiple components.

Pros: shared changes, centralized tooling, atomic cross-project change.
Challenges: scale, ownership, CI optimization, dependency boundaries.

### 🧪 Try It Yourself

```bash
git clone --depth 1 <url> shallow-copy
cd shallow-copy
git log --oneline   # notice: only one commit visible
git fetch --unshallow   # convert back to full history if ever needed
```

### 💡 Extra Insight

Sparse checkout (`git sparse-checkout set path/a path/b`) lets a monorepo contributor check out only the folders relevant to their team, avoiding the "why do I have 40GB of unrelated code on my laptop" problem — increasingly common as monorepos scale.

### 🩹 Common Error & Fix

```text
this exceeds GitHub's file size limit of 100.00 MB
```
The file should have gone through Git LFS from the start; after committing it directly, you'll need history-rewrite tooling to remove it properly, not just delete it in a new commit.

### 🎤 Questions

**🟢:** Large repo issue kya?
**🔵:** Git LFS kya?
**🟡:** Monorepo vs multirepo?
**🟠:** CI performance kaise optimize karoge?
**🔴:** Enterprise repository topology ka decision framework?

---

# 18 --- GIT INTERNALS

## Object Model

```text
Blob → Tree → Commit → Parent Commit
```

### Blob

File content (no filename stored inside the blob itself).

### Tree

Directory-like structure + references (filenames + blob/tree pointers + modes).

### Commit

Snapshot tree + metadata (author, committer, message, timestamp) + parent relationship.

### DAG

Git history is a **Directed Acyclic Graph**:

```text
      B---C
     /
A---D
     \
      E---F
```

Branches are just references (pointers) into this graph.

## SHA

Git objects are identified by content-addressed hashes. Traditional Git repositories commonly use SHA-1; Git also supports the SHA-256 object format in appropriate configurations.

## Snapshot Model

Two identical file contents reuse the same stored blob object — this is why "Git stores full snapshots" doesn't mean "Git wastes space on every unchanged file."

## Useful Commands

```bash
git cat-file -p <SHA>
git cat-file -t <SHA>
git rev-parse HEAD
git log --graph --oneline --decorate --all
```

Sample:

```bash
$ git cat-file -t a1b2c3d
commit

$ git cat-file -p a1b2c3d
tree 4f2d6e...
parent 9f8e7d6...
author Your Name <you@example.com> 1699999999 +0530
committer Your Name <you@example.com> 1699999999 +0530

Add retry handling for payment API
```

### 🧪 Try It Yourself

```bash
git cat-file -p HEAD
git cat-file -p HEAD^{tree}
```

The first shows the commit object (message, tree pointer, parent); the second shows what that commit's top-level tree actually looked like (files/folders + their blob SHAs).

### 💡 Extra Insight

Understanding that a **branch is nothing but a 41-byte text file** in `.git/refs/heads/` containing a commit SHA demystifies almost everything else — `git branch -d` just deletes that small file; it never touches the commits themselves (which stay reachable from other refs, or become "dangling" and eventually garbage-collected).

### 🩹 Common Error & Fix

```text
fatal: Not a valid object name a1b2c3d
```
Either a typo in the SHA, or the object was garbage-collected because nothing referenced it anymore (see the reflog module for recovery windows).

### 🎤 Questions

**🟢:** Blob kya?
**🔵:** Tree kya?
**🟡:** Commit object kya reference karta hai?
**🟠:** Git DAG branching ko kaise represent karta hai?
**🔴:** Content-addressed storage tamper evidence, deduplication aur history integrity ko kaise support karta hai?

---

# 19 --- CI/CD FUNDAMENTALS

## CI

Continuous Integration: frequent changes → automated build/test/validation.

## CD

Depending on organization: **Continuous Delivery** (always ready to deploy, human approves the final step) or **Continuous Deployment** (every passing change deploys automatically).

## Pipeline

```text
Commit → Build → Unit Test → Static Analysis → Security → Package/Artifact → Deploy → Integration/UAT → Production
```

## Important Principle

CI's purpose isn't just "build green." It should detect: code defects, test failures, dependency issues, security problems, deployment compatibility.

## Artifact

```text
source → build artifact → promote same artifact
```

**Build once, promote many** — avoid environment-specific rebuilding where reproducibility matters, because rebuilding per environment risks "it built differently in prod" drift.

### 🧪 Try It Yourself

Sketch your own project's pipeline stages on paper, then mark which stages currently exist vs which are missing (most teams are missing "security" and "deployment compatibility" checks first).

### 💡 Extra Insight

A useful CI health metric: **mean time between a bug being introduced and CI catching it.** If that number is measured in days instead of minutes, your pipeline isn't really doing continuous integration — it's doing scheduled batch validation.

### 🩹 Common Error & Fix

Symptom: "CI passed, but prod broke." Common root cause: build was re-created per environment instead of promoting one artifact — check for "build once, deploy many" violations first.

### 🎤 Questions

**🟢:** CI kya?
**🔵:** CI vs CD?
**🟡:** Artifact kya?
**🟠:** Build once/promote many kyun?
**🔴:** Enterprise CI/CD architecture mein quality gates aur promotion boundaries kaise design karoge?

---

# 20 --- GITHUB ACTIONS BASICS

Typical workflow location:

```text
.github/workflows/*.yml
```

```text
Workflow → Jobs → Steps
```

Example:

```yaml
name: CI

on:
  pull_request:

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: npm ci
      - run: npm test
```

## Important Concepts

events/triggers, jobs, steps, runners, actions, artifacts, environments, secrets, permissions.

## Security

Least privilege:

```yaml
permissions:
  contents: read
```

Avoid unnecessarily broad permissions. Never hardcode secrets — use `${{ secrets.MY_SECRET }}` referencing repo/org-level secret storage.

### 🧪 Try It Yourself

Add a second job that depends on the first:

```yaml
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: npm ci
      - run: npm test

  deploy:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - run: echo "Deploying only after tests pass"
```

`needs: test` enforces the gate — `deploy` won't run if `test` fails.

### 💡 Extra Insight

Pin third-party actions to a full commit SHA (`uses: actions/checkout@<full-sha>`), not just a tag like `@v4` — tags can be moved/re-pointed by the action's maintainer (or an attacker who compromises their account), while a SHA is immutable. This is a real supply-chain control, not paranoia.

### 🩹 Common Error & Fix

```text
Error: Resource not accessible by integration
```
Usually the workflow's `permissions:` block is too restrictive for what a step is trying to do (e.g., commenting on a PR needs `pull-requests: write`) — grant only the specific permission needed, not `write-all`.

### 🎤 Questions

**🟢:** GitHub Actions kya hai?
**🔵:** Workflow/job/step difference?
**🟡:** Secrets kaise handle karoge?
**🟠:** PR workflow ko secure kaise karoge?
**🔴:** Untrusted PR code + secrets + deployment credentials ko safely isolate kaise karoge?

---

# 21 --- REAL PIPELINE

## Practical Pipeline

```text
Developer → Feature Branch → Atomic Commit → Push → PR → Code Review → CI
  ├─ build
  ├─ unit tests
  ├─ static analysis
  ├─ security
  └─ deployment validation
→ Merge → Artifact → UAT → Production
```

## Salesforce Pipeline

```text
Git → PR → Salesforce validation → Apex/LWC tests → Security checks → Package/deployment artifact → UAT → Production
```

## Gates

A production pipeline should have explicit gates for: code quality, tests, security, deployment compatibility, approvals where required, environment readiness.

### 🧪 Try It Yourself

For your own project, write down which of the 6 gates above currently exist as **automated, blocking** checks vs which exist only as "someone remembers to check manually." That gap list is your improvement backlog.

### 💡 Extra Insight

The single highest-leverage pipeline improvement most teams skip: making failing gates **actually block the merge button**, not just show a red X someone can override. A gate that can be silently bypassed isn't a gate.

### 🎤 Questions

**🟢:** Pipeline kya hai?
**🔵:** PR CI mein kya run hoga?
**🟡:** Artifact boundary kahan?
**🟠:** Salesforce metadata validation kaise integrate karoge?
**🔴:** Complete enterprise pipeline mein policy, security, artifact provenance aur rollback ko kaise combine karoge?

---

# 22 --- DEPLOYMENT

## Deployment ≠ Git Merge

```text
Merge = source integration
Deployment = environment change
```

One can succeed while the other fails.

## Promotion

```text
Build once → Artifact → Dev/Test → UAT → Prod
```

## Rollback

Code rollback: revert/new release. Environment rollback depends on technology.

### Salesforce Important

```text
Code rollback ≠ Business data rollback
```

Example: an Apex deployment being reverted does not automatically restore records the application already modified.

## Fix-Forward

```text
Issue → Fix → Test → Deploy
```

Sometimes safer than rollback, depending on the incident.

### 🧪 Try It Yourself

For your last production incident (or a hypothetical one), write out: was it resolved by rollback or fix-forward, and why was that the right call? This is a common interview question precisely because the "right" answer depends on situational judgment, not a fixed rule.

### 💡 Extra Insight

A good decision heuristic: rollback is safer when the *old* version is well-understood and the bug is isolated to the new change; fix-forward is safer when rolling back would itself be risky (e.g., a completed data migration that can't cleanly reverse).

### 🎤 Questions

**🟢:** Deployment kya?
**🔵:** Merge vs deployment?
**🟡:** Rollback vs fix-forward?
**🟠:** Salesforce code rollback vs data rollback?
**🔴:** Zero/low-downtime release architecture kaise design karoge?

---

# 23 --- GITOPS

## Core Idea

Git can represent desired infrastructure/application state.

```text
Git → Desired State → Controller/Automation → Environment
```

Instead of manually changing environment, prefer: Git change → Review → Automation → Environment reconciliation.

## GitOps Principles

declarative desired state, version controlled config, automated reconciliation, auditable changes, PR-based review.

## Drift

```text
Git desired state ≠ runtime state
```

Controller/monitoring should detect/reconcile as designed.

### Salesforce Analogy

```text
Git source → Validation → Deployment/package → Org
```

Salesforce source/package deployment can use similar desired-state thinking, although Salesforce is not simply Kubernetes-style GitOps.

### 🧪 Try It Yourself

If your team uses Kubernetes, look up whether `kubectl apply` is run manually by anyone, or only by a GitOps controller (e.g., ArgoCD/Flux) reading from a repo — that single fact tells you how far along GitOps maturity you actually are.

### 💡 Extra Insight

The real test of "are we doing GitOps" isn't "do we store YAML in Git" — it's "**can a human even make a manual change that sticks**, or does the controller detect and revert it automatically?" If manual changes survive, you have Git-adjacent config, not GitOps.

### 🎤 Questions

**🟢:** GitOps kya hai?
**🔵:** GitOps vs CI/CD?
**🟡:** Drift kya?
**🟠:** Manual production changes ka problem?
**🔴:** GitOps model Salesforce environment governance mein kahan fit hota hai aur kahan nahi?

---

# 24 --- SECURING SOFTWARE SUPPLY CHAIN

## Threat Model

```text
Developer → Source repo → Dependencies → Build runner → Artifact → Registry → Deployment
```

Attack surface exists at every boundary.

## Core Controls

### Secrets

Never commit passwords/tokens/private keys. Use a secret manager, CI secrets, or OIDC/short-lived credentials where supported.

### Dependency Security

Check: vulnerable packages, lockfiles, dependency provenance, license/policy, malicious package indicators.

### Branch Protection

Require: PR, CI, approvals, restricted push.

### Artifact Integrity

Useful controls: immutable artifacts, checksums/signatures, provenance/attestation, controlled registries.

### Least Privilege

```text
read > write > admin
```

Give CI only the minimum permission needed.

### Secret Incident

```text
Secret committed → Revoke/rotate immediately → Assess exposure → Clean history if required → Invalidate affected credentials → Audit
```

History cleaning alone is not enough — the credential must be rotated regardless.

### 🧪 Try It Yourself

Run a dependency audit on a real project:

```bash
npm audit
# or
pip list --outdated
```

Note how many vulnerabilities show up even in projects believed to be "clean" — this is exactly the class of risk this module covers.

### 💡 Extra Insight

A surprisingly common real-world gap: teams rotate a leaked secret but never revoke the *old* one's access explicitly, assuming rotation alone invalidates it. Depending on the system, the old credential may remain valid until explicitly revoked — always confirm revocation, not just "a new one now exists."

### 🩹 Common Error & Fix

Discovering a secret in history via `git log -p | grep -i "API_KEY"` confirms exposure, but running `git rm secret.txt && git commit` **does not** remove it from history — that commit still exists with the secret in it, reachable via reflog/old refs/existing clones.

### 🎤 Questions

**🟢:** Secret Git mein kyun nahi?
**🔵:** `.gitignore` secret security kyun nahi?
**🟡:** Least privilege?
**🟠:** CI credential compromise par response?
**🔴:** End-to-end software supply-chain trust/provenance architecture kaise design karoge?

---

# 25 --- SALESFORCE + GIT

## Salesforce DX Mental Model

```text
Git Branch → Salesforce Source → Scratch Org / Dev Environment → Validation → PR → CI → Package / Deployment → UAT → Production
```

## Salesforce Source Control

Typical source includes: Apex, LWC, Objects, Fields, Permission metadata, Flows, FlexiPages, other metadata.

## Metadata Dependencies

```text
Folder separation ≠ true dependency separation
```

Example: `Object → actionOverride → FlexiPage`. Hidden metadata dependencies can affect package design.

## Unlocked Packages

```text
Module → Package → Package Version → Install
```

If `Package B → depends on Package A`, then dependency order matters.

## Clean Installation

Don't test only in an existing org. Ask: does the package/metadata install/validate correctly in a **clean** environment with only its declared dependencies? An existing org can hide missing/implicit dependencies.

## Source-Based vs Package-Based

### Source-based

```text
Git → Deployment
```

### Package-based

```text
Git → Package Version → Install
```

Package gives a versioned artifact boundary.

## Salesforce PR Validation

At minimum consider: code correctness, Apex tests, LWC validation, metadata dependencies, security, deployment/package compatibility.

## Org Drift

```text
Git ≠ Production
```

Example: `Git: Field Label = A` vs `Prod: Field Label = B`. A manual production change can break the source-of-truth model.

## Emergency Fix

```text
Production incident → Hotfix branch → Atomic fix → PR/review → Tests → Production → Integrate fix back into normal development line
```

## Salesforce Architect Rule

> **Git conflict resolution is only source-level resolution. It is not proof that Salesforce metadata, security, tests, dependencies, integrations and runtime behavior are correct.**

### 🧪 Try It Yourself

If you have Salesforce CLI access:

```bash
sf org create scratch -f config/project-scratch-def.json -a my-scratch
sf project deploy start -o my-scratch
sf apex run test -o my-scratch
```

Running this against a **brand-new scratch org** (not your regular dev org) is the practical version of the "clean installation" test described above.

### 💡 Extra Insight

The most common real-world Salesforce+Git failure isn't a merge conflict — it's a manual click-based change in Production that nobody backported into Git. Every retrospective on "why doesn't Git match Prod" traces back to this one root cause: a change made directly in the org, outside the pipeline, under time pressure.

### 🩹 Common Error & Fix

```text
Error: Dependent class is invalid and needs recompilation
```
Usually a hidden metadata dependency issue (see the "Folder separation ≠ true dependency separation" note above) — deploy in dependency order, or use package-based deployment where Salesforce resolves this more reliably.

### 🎤 Questions

**🟢:** Salesforce DX source control kya hai?
**🔵:** Salesforce metadata Git mein kyun rakhte hain?
**🟡:** Unlocked package kya solve karta hai?
**🟠:** Metadata conflict resolve hone ke baad kya validate karoge?
**🔴:** Enterprise Salesforce architecture mein Git + CI/CD + packages + environments + security + rollback ka end-to-end model design karo.

---

# 📖 GLOSSARY (New)

| Term | Plain-language meaning |
|---|---|
| **Blob** | Git's object type for raw file content (no filename). |
| **Tree** | Git's object type for a directory listing (filenames + pointers). |
| **Commit** | A snapshot + metadata + pointer to parent commit(s). |
| **HEAD** | Pointer to "where you currently are" (usually a branch, sometimes a raw commit = detached HEAD). |
| **Fast-forward** | A merge where the target branch just moves forward, no new commit needed. |
| **Upstream / tracking branch** | The remote branch your local branch is linked to for simplified push/pull. |
| **Reflog** | Local log of every place HEAD has pointed — a short-term safety net, not a backup. |
| **Detached HEAD** | HEAD points directly at a commit instead of a branch name. |
| **Fast-forward vs three-way merge** | Fast-forward = no divergence; three-way = both branches changed, needs a merge commit. |
| **Rebase** | Replaying commits on top of a new base, creating new SHAs. |
| **Cherry-pick** | Applying one specific commit from elsewhere onto your current branch. |
| **Bisect** | Binary-search through history to find the commit that introduced a bug. |
| **Force-with-lease** | A safer force-push that fails if the remote has moved since your last fetch. |
| **Artifact** | The built, packaged, deployable output of a pipeline (built once, promoted through environments). |
| **GitOps** | Using Git as the single source of truth for desired infrastructure/app state, reconciled by automation. |
| **Drift** | When the actual running environment no longer matches what's declared in Git. |
| **Scratch org** | A temporary, disposable Salesforce environment for clean testing. |
| **Unlocked package** | A versioned, installable bundle of Salesforce metadata with explicit dependencies. |

---

# 🩺 COMMON ERROR MESSAGES REFERENCE (New)

| Error message (shortened) | Likely cause | Typical fix |
|---|---|---|
| `fatal: not a git repository` | Ran command outside a repo | `cd` into repo or `git init` |
| `fatal: unable to auto-detect email address` | No identity configured | `git config --global user.email ...` |
| `error: Your local changes...would be overwritten` | Uncommitted changes conflict with checkout/merge | Commit, stash, or `git restore` deliberately |
| `CONFLICT (content): Merge conflict in <file>` | Overlapping changes on same lines | Resolve markers, `add`, `commit`/`rebase --continue` |
| `fatal: The current branch has no upstream branch` | Never linked to remote branch | `git push -u origin <branch>` |
| `! [rejected]...(fetch first)` | Remote has commits you don't have locally | `git fetch` then merge/rebase, then push |
| `! [rejected]...(stale info)` (with `--force-with-lease`) | Remote moved since your last fetch | Fetch, inspect, decide before re-forcing |
| `fatal: A branch named '...' already exists` | Branch name collision | Choose a different name or switch to the existing one |
| `error: The branch is not fully merged` | Trying to `-d` a branch with unmerged unique commits | Merge first, or use `-D` deliberately |
| `this exceeds GitHub's file size limit` | Large binary committed directly | Should have used Git LFS; needs history rewrite to fully remove |

---

# ⚡ COMMAND REFERENCE BY TASK (New)

```bash
# ---- Setup ----
git init
git clone <url>
git config --global user.name "Name"
git config --global user.email "email"

# ---- See what's going on ----
git status
git status -s
git diff
git diff --staged
git log --oneline --graph --decorate --all
git show <SHA>

# ---- Stage & commit ----
git add <file>
git add -p
git commit -m "message"
git commit --amend

# ---- Branch & merge ----
git switch -c <branch>
git switch <branch>
git merge <branch>
git branch -d <branch>
git branch -D <branch>

# ---- Remote ----
git remote -v
git fetch
git pull
git push
git push -u origin <branch>
git push --force-with-lease

# ---- Undo ----
git restore <file>
git restore --staged <file>
git revert <SHA>
git reset --soft HEAD~1
git reset --mixed HEAD~1
git reset --hard HEAD~1

# ---- Rebase ----
git rebase <base>
git rebase -i <base>
git rebase --continue
git rebase --abort

# ---- Recovery ----
git reflog
git branch rescue <SHA>

# ---- Tags ----
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0

# ---- Debugging ----
git bisect start
git bisect bad
git bisect good <SHA>
git cherry-pick <SHA>

# ---- Internals ----
git cat-file -p <SHA>
git cat-file -t <SHA>
git rev-parse HEAD
```

---

# 🧠 MASTER DECISION TREE

```text
Need to create repository?           → git init / clone
Need to see changes?                 → git diff
Need exact next commit inspect?      → git diff --staged
Need stage?                          → git add / git add -p
Need commit?                         → git commit
Need share?                          → git push
Need remote changes?                 → git fetch / pull
Need isolate work?                   → branch
Need combine branches?               → merge
Need linearize private history?      → rebase
Need safely undo shared commit?      → revert
Need edit latest local commit?       → amend
Need move local history?             → reset
Lost reference?                      → reflog
Need collaboration?                  → PR
Need automation?                     → CI/CD
Need version marker?                 → tag
Need desired-state automation?       → GitOps
Need secure delivery?                → supply-chain controls
Need Salesforce delivery?            → DX + validation + package/deployment
```

---

# 🔥 MOST IMPORTANT GOTCHAS

1. **Git ≠ GitHub**
2. **Commit ≠ Push**
3. **Fetch ≠ Pull**
4. **Merge ≠ Deployment**
5. **Branch ≠ Folder**
6. **HEAD ≠ Tree**
7. **Staging ≠ Repository**
8. **Revert ≠ Reset**
9. **Rebase rewrites commit identities**
10. **`git restore` can destroy uncommitted work**
11. **Reflog is recovery help, not a backup**
12. **`.gitignore` does not erase committed secrets**
13. **Git conflict resolved ≠ Salesforce validation complete**
14. **Code rollback ≠ data rollback**
15. **Package dependency ≠ folder dependency**
16. **Local commit ≠ remote availability**
17. **Local hook ≠ authoritative security control**
18. **Build artifact ≠ source branch**
19. **Tag ≠ deployment itself**
20. **Production manual change creates drift**
21. **Tags don't push automatically with plain `git push`** *(new)*
22. **`--force` and `--force-with-lease` are not interchangeable safety-wise** *(new)*

---

# 📋 DAILY DEVELOPER CHECKLIST

```text
[ ] Work on feature branch
[ ] Keep branch short-lived where practical
[ ] Fetch/pull appropriate latest changes
[ ] Make small logical changes
[ ] Run local tests
[ ] Inspect git diff
[ ] Stage intentionally
[ ] Review git diff --staged
[ ] Write meaningful commit message
[ ] Push branch
[ ] Open PR early for meaningful work
[ ] Resolve CI failures
[ ] Review dependency/security impact
[ ] Merge through protected path
[ ] Delete obsolete branch
[ ] Verify deployment/result
```

# 👨‍💻 REVIEWER CHECKLIST

```text
[ ] Is the change logically scoped?
[ ] Is commit/PR understandable?
[ ] Tests included?
[ ] Security impact?
[ ] Dependency impact?
[ ] Metadata impact?
[ ] Breaking change?
[ ] Deployment order?
[ ] Rollback/fix-forward plan?
[ ] Observability/logging?
[ ] Salesforce sharing/permission impact?
[ ] Package boundary impact?
```

# 🏗️ ARCHITECT CHECKLIST

```text
Business requirement
      ↓
System design
      ↓
Repository/module boundary
      ↓
Branch strategy
      ↓
Atomic commits
      ↓
PR governance
      ↓
CI quality gates
      ↓
Security/supply-chain controls
      ↓
Artifact/package
      ↓
Environment promotion
      ↓
UAT
      ↓
Production
      ↓
Observability
      ↓
Rollback/Fix-forward
      ↓
Traceability
```

Architect should always ask:

1. What is the source of truth?
2. What is the deployable artifact?
3. What is the integration boundary?
4. What is the security boundary?
5. What is the rollback boundary?
6. How is drift detected?
7. How is production traceability maintained?
8. How are dependencies validated?
9. What happens during failure?
10. How does emergency change return to normal development?

---

# 🎯 INTERVIEW MASTER LADDER

## 🟢 BEGINNER

1. Git kya hai?
2. Git aur GitHub mein difference?
3. Repository kya hai?
4. Commit kya hai?
5. Branch kya hai?
6. `git add` kya karta hai?
7. `git commit` kya karta hai?
8. `git push` kya karta hai?
9. `git pull` kya karta hai?
10. `git status` kyun use karte hain?
11. `git clone` kya karta hai?
12. Three trees kya hain?

## 🔵 JUNIOR / DEVELOPER

1. `git fetch` vs `git pull`?
2. `git diff` vs `git diff --staged`?
3. `git revert` vs `git reset`?
4. `git reset --soft/mixed/hard`?
5. Merge conflict kaise resolve karte ho?
6. Feature branch kyun?
7. PR kyun?
8. Atomic commit kya?
9. `git add -p` ka use?
10. Reflog kya hai?
11. Merge vs rebase?
12. Tag vs branch?
13. `.gitignore` kya solve karta hai?
14. Git LFS kab use karoge?
15. GitHub Actions workflow/job/step kya hain?

## 🟡 SENIOR

1. Rebase SHA kyun change karta hai?
2. Shared branch rebase kyun risky?
3. `MM` status ka meaning?
4. Lost commit recover kaise?
5. Large repo optimize kaise?
6. CI pipeline mein artifact boundary kahan?
7. Build once/promote many kyun?
8. Protected branch ka role?
9. PR architecture review kaise banega?
10. Supply-chain attack surface kya hai?
11. Secret committed hone par incident response?
12. GitOps vs CI/CD?
13. Salesforce metadata dependency kaise validate?
14. Package vs source deployment?
15. Code rollback vs data rollback?

## 🟠 LEAD

1. Team ke liye GitHub Flow vs release branches kaise choose karoge?
2. Long-lived branch problem kaise reduce karoge?
3. PR size kaise control karoge?
4. CI failures ko developer productivity destroy kiye bina kaise manage karoge?
5. Branch protection policy kya hogi?
6. Emergency production hotfix ka workflow?
7. Artifact promotion model kaise design karoge?
8. Secrets/credentials CI mein kaise govern karoge?
9. Salesforce package dependency strategy?
10. Monorepo vs multirepo decision?
11. Git hooks aur CI enforcement ka boundary?
12. Drift detection kaise design karoge?

## 🔴 ARCHITECT

1. Git source of truth, artifact source of truth aur runtime state ko kaise model karoge?
2. Enterprise branching strategy ka decision framework kya hoga?
3. Rebase vs merge ko technical preference ke bajay governance decision kaise banaoge?
4. Software supply-chain trust model end-to-end kaise design karoge?
5. Build provenance aur artifact integrity kaise prove karoge?
6. CI/CD mein security gates ko developer velocity ke saath kaise balance karoge?
7. GitOps aur traditional deployment pipeline ko kaise integrate karoge?
8. Salesforce metadata dependency graph ko package architecture mein kaise translate karoge?
9. Salesforce org drift prevent/detect/remediate kaise karoge?
10. Production hotfix ko source-control lineage mein safely kaise return karoge?
11. Code rollback, configuration rollback, package rollback aur data remediation ko kaise separate karoge?
12. Multi-team Salesforce enterprise ke liye repository/module/package boundaries kaise define karoge?
13. PR ko architecture governance boundary kaise use karoge without turning it into a bottleneck?
14. Large monorepo ke CI cost ko intelligently kaise control karoge?
15. Failure ke time system kaise prove karega ki Production mein exactly kaunsa source/artifact/version deployed hai?

---

# 🧪 REAL-WORLD SCENARIOS

## Scenario 1 --- Production Bug

```text
Production bug → Identify deployed version → Find commit/tag/PR → Assess revert vs fix-forward → Hotfix branch if required → Test → Review → Deploy → Integrate fix back
```

## Scenario 2 --- Developer Lost 3 Commits

```text
STOP → git reflog → Find SHA → git show SHA → rescue branch → validate
```

## Scenario 3 --- Same Salesforce Metadata Changed by Two Developers

```text
PR conflict → Resolve source conflict → Validate metadata → Run Apex/LWC tests → Check dependencies/security → UAT
```

## Scenario 4 --- Secret Committed

```text
Revoke/rotate → Assess exposure → History cleanup if needed → Invalidate credentials → Audit → Prevent recurrence
```

## Scenario 5 --- Production Drift

```text
Git ≠ Production → Detect difference → Identify manual change → Decide source-of-truth correction → Return change through controlled Git workflow
```

## Scenario 6 --- CI Green but Prod Broke *(new)*

```text
Confirm what artifact was actually deployed → Compare against the artifact CI tested → If mismatch: fix "build once, promote many" violation → If match: gap is in test coverage, not process → Add missing test/gate → Retro
```

## Scenario 7 --- Accidental Force Push Overwrote a Teammate's Commits *(new)*

```text
STOP further pushes → Ask teammate for their local branch state (their local copy still has the commits) → They push again with --force-with-lease from their side → If neither local copy has it: check reflog on any machine that had it → If truly gone: this is why --force-with-lease + branch protection exist
```

---

# ⚡ 30-SECOND CHEAT SHEET

```bash
# Start
git init
git clone <url>

# Inspect
git status
git diff
git diff --staged
git log --oneline --graph --decorate

# Stage/Commit
git add <file>
git add -p
git commit -m "message"

# Branch
git switch -c feature/x
git switch main
git merge feature/x

# Remote
git fetch
git pull
git push

# Undo
git restore <file>
git restore --staged <file>
git revert <SHA>
git commit --amend
git reset --soft HEAD~1
git reset --mixed HEAD~1
git reset --hard HEAD~1

# Rebase
git rebase main
git rebase -i <base>

# Recovery
git reflog

# Tags
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0

# Internals
git show <SHA>
git cat-file -t <SHA>
git cat-file -p <SHA>
```

---

# 🧠 FINAL MASTER MENTAL MODEL

```text
                    BUSINESS CHANGE
                          ↓
                    FEATURE BRANCH
                          ↓
                    ATOMIC COMMITS
                          ↓
                         PUSH
                          ↓
                         PR
                    ┌─────┴─────┐
                    ↓           ↓
                 REVIEW         CI
                    └─────┬─────┘
                          ↓
                    MERGE / INTEGRATE
                          ↓
                     ARTIFACT
                          ↓
              ┌───────────┴───────────┐
              ↓                       ↓
           UAT/TEST              SECURITY
              └───────────┬───────────┘
                          ↓
                      PRODUCTION
                          ↓
                   OBSERVE / AUDIT
                          ↓
              ROLLBACK / FIX-FORWARD
                          ↓
                    TRACEABILITY
```

## One-Line Architect Summary

> **Git is not just a command-line tool; it is the source-control foundation for traceable change. Mature engineering connects Git history → review → CI → secure artifact → controlled deployment → runtime validation → recovery.**

---

# 🏆 FINAL REVISION RULE

```text
Beginner:  "Command kya karta hai?"
Junior:    "Workflow mein kaise use hota hai?"
Senior:    "Failure case mein kya hoga?"
Lead:      "Team/process par kya impact hoga?"
Architect: "System, security, governance, cost,
            traceability aur recovery par kya impact hoga?"
```

**This is the mental upgrade from Git user → Git engineer → Git/Release architect.**
