# Rewriting history safely

*Module 15 · Power tools*

Every command here changes commits that already exist. Used on your own unpushed work they turn
a messy afternoon into a clean, reviewable series. Used on a shared branch they break other
people's repositories. The techniques are identical - only the blast radius differs.

[Course home](../index.md) / Module 15

## 1. The rule, restated once

> **Rewrite freely what nobody else has. Never rewrite what they have.**

```mermaid
flowchart LR
    S0["Commits exist only on your machine"]
    S1["amend, squash, reorder, drop"]
    S2["Push a clean series"]
    S3["Nobody affected"]
    S0 --> S1
    S1 --> S2
    S2 --> S3
    F0["Rewrite commits others have pulled"]
    F1["Every hash changes"]
    F2["Their history and yours no longer match"]
    F3["Duplicate commits, then a force push, then lost work"]
    S0 -.->|"fails"| F0
    F0 --> F1
    F1 --> F2
    F2 --> F3
    classDef bad fill:#fdecea,stroke:#c62828;
    class F0,F1,F2,F3 bad;
```

> **Why it matters:** Rewriting produces **new commits with new hashes** - module 09. The originals live on in everyone else's clone. There is one narrow exception, section 6, where the content must be destroyed and everyone is told in advance.

## 2. `git commit --amend`

The smallest rewrite: change the most recent commit.

```bash
git commit --amend                     # edit the message
git commit --amend --no-edit           # add staged changes, keep the message
git commit --amend --author="Name <email>"
git commit --amend --date="2026-09-01T10:00:00"
```

The two situations it exists for:

```bash
# forgot a file
git add forgotten.js
git commit --amend --no-edit

# bad message
git commit --amend -m "fix(auth): handle expired refresh tokens"
```

> **NOTE - Amend creates a new commit**
>
> It does not edit the old one - it builds a replacement with a different hash and moves the branch pointer. The original is orphaned and recoverable via `git reflog`. That is exactly why amending a pushed commit needs a force push.

## 3. Interactive rebase

The main tool. Take the last N commits and restructure them however you like.

```bash
git rebase -i HEAD~5
git rebase -i abc1234          # everything after this commit
git rebase -i --root           # the entire history
```

An editor opens:

```text
pick a1b2c3d feat: add login form
pick d4e5f6a wip
pick 7g8h9i0 fix typo
pick j1k2l3m wip2
pick n4o5p6q feat: add validation

# Commands:
# p, pick   = use commit
# r, reword = use commit, but edit the message
# e, edit   = use commit, but stop to amend it
# s, squash = meld into previous commit, combine messages
# f, fixup  = like squash, but discard this commit's message
# d, drop   = remove the commit
# b, break  = stop here, then continue manually
```

Edit it into what you meant:

```text
pick a1b2c3d feat: add login form
fixup d4e5f6a wip
fixup 7g8h9i0 fix typo
drop j1k2l3m wip2
reword n4o5p6q feat: add validation
```

| Verb | Result |
| --- | --- |
| `pick` | Keep as is |
| `reword` | Keep the changes, edit the message |
| `edit` | Stop here so you can amend the content |
| `squash` | Merge into the previous commit, and combine both messages |
| `fixup` | Merge into the previous commit, **discard** this message |
| `drop` | Remove entirely |
| **Reorder lines** | Reorders the commits |

```bash
git rebase --continue
git rebase --abort
git rebase --skip
```

> **TIP - `fixup` is what you want most of the time**
>
> `squash` opens an editor to combine two messages, which for a commit called `wip` is pure noise. `fixup` silently discards it and keeps the parent's message. A branch of five commits where four are `wip` becomes one clean commit with four `fixup` lines and no editor prompts.

## 4. `--fixup` and `--autosquash`

The workflow that makes this effortless during review.

```mermaid
flowchart LR
    S0["Reviewer comments on commit a1b2c3d"]
    S1["Fix it, then: git commit --fixup a1b2c3d"]
    S2["A commit named 'fixup! feat: add login form'"]
    S3["git rebase -i --autosquash HEAD~5"]
    S4["Git pre-arranges it directly under its target"]
    S5["Confirm - history looks as if the mistake never happened"]
    S0 --> S1
    S1 --> S2
    S2 --> S3
    S3 --> S4
    S4 --> S5
```

> **Why it matters:** Without this, addressing review feedback either adds noise commits ("fix review comment") or forces you to reorder by hand in an interactive rebase. `--fixup` records the target commit in the message, and `--autosquash` reads it and does the arranging for you.

```bash
git commit --fixup a1b2c3d
git commit --squash a1b2c3d
git rebase -i --autosquash HEAD~5
git config --global rebase.autoSquash true      # always on
```

## 5. Splitting a commit

You committed two things together and want them apart.

```bash
git rebase -i HEAD~3
# mark the commit as: edit
```

```bash
git reset HEAD~                 # undo the commit, keep the changes unstaged
git add -p                      # stage only the first concern
git commit -m "fix(auth): handle expired tokens"
git add .
git commit -m "refactor(auth): extract token parser"
git rebase --continue
```

## 6. Removing something from all of history

The narrow exception - a secret, or a large file that should never have been committed.

```mermaid
flowchart LR
    S0["A credential was committed"]
    S1["1. ROTATE IT IMMEDIATELY"]
    S2["2. Rewrite history with git-filter-repo"]
    S3["3. Force push, tell everyone to re-clone"]
    S4["4. Delete or recreate the remote if it is public"]
    S0 --> S1
    S1 --> S2
    S2 --> S3
    S3 --> S4
    F0["Rewrite history first, treat it as solved"]
    F1["The credential is already scraped and in use"]
    F2["Force push leaves old commits reachable by SHA on GitHub"]
    F3["Forks and clones still contain it"]
    S1 -.->|"fails"| F0
    F0 --> F1
    F1 --> F2
    F2 --> F3
    classDef bad fill:#fdecea,stroke:#c62828;
    class F0,F1,F2,F3 bad;
```

> **Why it matters:** **Rotating the secret is step one and it is not optional.** Public repositories are scraped within minutes, so by the time you notice, the credential must be assumed compromised. History rewriting is damage limitation afterwards - it never makes an exposed secret safe.

```bash
pip install git-filter-repo

git filter-repo --path secrets.env --invert-paths          # remove a file from all history
git filter-repo --path-glob '*.pem' --invert-paths
git filter-repo --replace-text replacements.txt            # redact strings in place
```

```text
# replacements.txt
AKIAIOSFODNN7EXAMPLE==>REDACTED
literal:sk_live_abc123==>REDACTED
```

```bash
git remote add origin <url>          # filter-repo removes the remote deliberately
git push --force --all
git push --force --tags
```

> **WARNING - Force push does not remove commits from GitHub**
>
> This is the trap. After a force push the old commits are unreachable from any branch, but they remain accessible at `github.com/owner/repo/commit/<sha>` until GitHub garbage collects, which is not something you can trigger or rely on. Forks keep their own copies entirely. The only guaranteed removal is to **delete and recreate the repository** - or contact GitHub Support to purge. Verify with `gh api repos/OWNER/REPO/commits/<old-sha>` and expect a 404.

| Tool | For |
| --- | --- |
| **`git filter-repo`** | The current recommended tool - fast, safe defaults |
| `git filter-branch` | Deprecated, slow, error-prone. Do not use |
| **BFG Repo-Cleaner** | Simpler for the common cases of "delete this file" or "replace this string" |

## 7. Force pushing correctly

```bash
git push --force-with-lease
git push --force-with-lease --force-if-includes    # even stricter
```

| | `--force` | `--force-with-lease` |
| --- | --- | --- |
| Checks the remote first | No | **Yes** |
| Overwrites a colleague's push | Silently | Refuses |
| Safe on your own feature branch | Yes | Yes |
| Acceptable on `main` | **Never** | Never - protect the branch instead |

> **TIP - Alias the safe one over the dangerous one**
>
> ```bash
> git config --global alias.pushf "push --force-with-lease"
> ```
> Then `git pushf` becomes the reflex and plain `--force` requires deliberate effort - which is exactly the friction you want.

## 8. Recovering from a bad rewrite

```bash
git rebase --abort               # while it is running
git reset --hard ORIG_HEAD       # immediately after
git reflog                       # any time within ~90 days
git reset --hard HEAD@{7}
```

Nothing you rewrite is destroyed at the moment of rewriting - the originals are orphaned, not
deleted, and survive until garbage collection. Module 10 covers the recovery kit.

## 9. Extra points

- **Interactive rebase before every push** is a good habit on a feature branch: `git rebase -i
  origin/main` and tidy the series a reviewer is about to read.
- **`git rebase -i` cannot reorder past a merge commit** cleanly. Use `--rebase-merges` if you
  must, or avoid merges inside a branch you intend to tidy.
- **Rewriting changes the committer, not the author.** `git log --format="%an %ae | %cn %ce"`
  shows both - useful when history looks like someone else wrote your code.
- **`git range-diff main..old main..new`** compares two versions of a rewritten branch, which is
  how you check a rebase preserved what you meant.
- **Signed commits break on rewrite** - the signature covers the old content. Re-sign with
  `git rebase --exec 'git commit --amend --no-edit -S'`.

> **PRACTICE - Practice now**
>
> 1. Build a deliberately messy branch:
>    ```bash
>    mkdir rewrite-demo && cd rewrite-demo && git init
>    echo "1" > a.txt && git add . && git commit -m "feat: add feature"
>    echo "2" >> a.txt && git commit -am "wip"
>    echo "3" >> a.txt && git commit -am "fix typo"
>    echo "4" >> a.txt && git commit -am "wip2"
>    echo "5" > b.txt && git add . && git commit -m "add validation"
>    git log --oneline
>    ```
> 2. **Amend the last commit's message:**
>    ```bash
>    git commit --amend -m "feat: add input validation"
>    git log --oneline -1
>    git reflog | Select-Object -First 3
>    ```
>    Note the new hash, and that the original is still in the reflog.
> 3. **Clean the whole branch with interactive rebase:**
>    ```bash
>    git rebase -i HEAD~5
>    ```
>    Mark the `wip` commits as `fixup`, `reword` the first, and confirm:
>    ```bash
>    git log --oneline
>    ```
> 4. **Use the `--fixup` workflow:**
>    ```bash
>    echo "6" >> a.txt && git commit -am "feat: another change"
>    git log --oneline
>    echo "7" >> a.txt
>    git add . && git commit --fixup <hash of "feat: another change">
>    git log --oneline
>    git rebase -i --autosquash HEAD~3
>    git log --oneline
>    ```
>    Git arranged it for you - you only confirmed.
> 5. **Split a commit in two:**
>    ```bash
>    printf "auth fix\nrefactor\n" > mixed.txt
>    git add . && git commit -m "chore: two things at once"
>    git rebase -i HEAD~1        # mark it: edit
>    git reset HEAD~
>    git add -p mixed.txt        # stage only one part
>    git commit -m "fix(auth): first concern"
>    git add . && git commit -m "refactor: second concern"
>    git rebase --continue
>    git log --oneline
>    ```
> 6. **Reorder and drop:**
>    ```bash
>    git rebase -i HEAD~4
>    ```
>    Move a line up, mark another `drop`, save, and inspect the result.
> 7. **Practise recovery.** Deliberately ruin the branch, then get it back:
>    ```bash
>    git rebase -i --root      # drop several commits at random
>    git log --oneline
>    git reflog
>    git reset --hard <the pre-rebase hash>
>    git log --oneline
>    ```
> 8. **Remove a file from all history:**
>    ```bash
>    "SECRET=abc123" | Set-Content leaked.env
>    git add . && git commit -m "oops: commit a secret"
>    echo "more" >> a.txt && git commit -am "chore: later work"
>    git log --oneline --stat | Select-String "leaked"
>    pip install git-filter-repo
>    git filter-repo --path leaked.env --invert-paths --force
>    git log --oneline --stat | Select-String "leaked"
>    ```
>    Gone from every commit. Now note that in a real incident this would be **step two**, after
>    rotating the credential.
> 9. **Compare two versions of a rewritten branch:**
>    ```bash
>    git range-diff <old-base>..<old-tip> <new-base>..<new-tip>
>    ```

> **ASSIGNMENT - Assignment**
>
> Write a "leaked credential" runbook for your team: the exact order of operations, starting with rotation and containment, then history rewriting with `git filter-repo`, then the force push, then what to tell colleagues, then how to verify the old commits actually return 404 on the hosting platform - including the fact that a force push alone does not achieve that. End it with the prevention section: a secret-scanning pre-commit hook and push protection enabled on the repository. This is a genuine incident-response document, and being the person who already has one written is a career-defining difference during a real incident.

## 10. Interview drill

<details>
<summary><b>What does `git commit --amend` actually do?</b></summary>

It creates a **new** commit replacing the most recent one, combining the old commit's content
with anything currently staged and whatever message you supply, then moves the branch pointer to
it. The original commit is not modified - it is orphaned and recoverable through `git reflog`.
Because the hash changes, amending a commit that has been pushed requires a force push, which is
why it is safe only on unshared work.

</details>

<details>
<summary><b>What is interactive rebase and what can you do with it?</b></summary>

`git rebase -i <base>` opens an editable list of the commits after that base. You can `reword`
messages, `squash` or `fixup` commits together, `drop` them, `edit` one to change its content or
split it, and reorder them by moving lines. It is how a messy working branch becomes a clean
series before review. Every operation creates new commits with new hashes, so it applies to
unpushed or personally-owned branches only.

</details>

<details>
<summary><b>What is the difference between `squash` and `fixup`?</b></summary>

Both meld a commit into the one above it. `squash` opens an editor so you can combine the two
messages; `fixup` silently discards the melded commit's message and keeps the parent's. For
commits called `wip` or `fix typo` the message is worthless, so `fixup` is almost always what you
want and avoids an editor prompt per commit.

</details>

<details>
<summary><b>How do you address review comments without adding noise commits?</b></summary>

Make the fix, then `git commit --fixup <target-hash>`, which creates a commit whose message
starts with `fixup!` naming its target. When the review is finished,
`git rebase -i --autosquash origin/main` automatically positions each fixup under its target and
marks it for melding - you just confirm. The result is a clean series with no "address review
comments" commits, and `rebase.autoSquash true` makes it the default.

</details>

<details>
<summary><b>A password was committed and pushed to a public repository. Walk me through it.</b></summary>

Rotate the credential first - assume it is compromised, because public repositories are scraped
within minutes and history rewriting does nothing about a key already in use. Then remove it from
history with `git filter-repo --path <file> --invert-paths` or `--replace-text`, force push all
branches and tags, and tell colleagues to re-clone rather than pull. Then verify: a force push
leaves the old commits reachable by direct SHA on GitHub and forks keep their own copies, so
guaranteed removal means deleting and recreating the repository or asking support to purge.
Finally, prevent recurrence with secret scanning and push protection.

</details>

<details>
<summary><b>Why prefer `--force-with-lease` over `--force`?</b></summary>

`--force` overwrites the remote branch unconditionally, so any commits a colleague pushed since
your last fetch are silently destroyed. `--force-with-lease` verifies the remote is still at the
position you last saw and aborts if it has moved, which lets you rewrite your own history while
protecting theirs. Aliasing it to something short makes it the reflex, and plain `--force` should
require conscious effort - on `main` neither is appropriate, because branch protection should
prevent it entirely.

</details>

---

[← Module 14](14-tags-and-releases.md) &nbsp;&nbsp;|&nbsp;&nbsp; [Module 16: Hooks and local automation →](16-hooks.md)

---

Git & Pipelines: Zero to Architect · Himanshu Kumar.
