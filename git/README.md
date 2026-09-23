# Git & Pipelines: Zero to Architect

Version control taught the way you would defend it in a design review - what Git actually
stores, why each command exists, what breaks in a real team, and how the whole thing turns
into a delivery pipeline.

> Written by **Himanshu Kumar**. Part of [Enterprise Stack: Zero to Architect](../README.md).

**All Markdown, nothing to download.** Every lesson renders right here on GitHub, with Mermaid
diagrams showing the happy path and the failure path side by side. Start at
[the course overview](index.md) or [module 01](lessons/01-why-git-exists.md).

---

## Why this exists

Most Git material gives you eight commands and a diagram. That is the easy 20%. The other 80%
is:

- Why `git pull` sometimes creates a commit you did not ask for
- What the staging area is actually **for**, rather than what it is
- Why `reset`, `revert`, `restore` and `checkout` all seem to undo things - and are not
  interchangeable
- Why rebasing a shared branch makes you unpopular
- How to recover work you are certain you destroyed
- Why your pipeline is green and production is still broken
- When a monorepo is right, and when it quietly ruins your build times

Every command in this course is taught the same way: **what it does, where you use it, why it
exists**, and the manual steps where something cannot be scripted.

---

## Modules

| # | Module | What it covers |
| --- | --- | --- |
| **Foundations** | | |
| 01 | [Why Git exists, and what Git is](lessons/01-why-git-exists.md) | The problem before version control, centralised vs distributed, snapshots not diffs, Git vs GitHub |
| 02 | [Install and configure Git](lessons/02-install-and-configure.md) | Install per OS, identity, the three config levels, line endings on Windows, SSH keys, credential helper |
| 03 | [The three trees](lessons/03-three-trees.md) | Working directory, staging area, repository - and why the middle one exists |
| 04 | [Your first repository](lessons/04-first-repository.md) | `init`, `status`, `add`, `commit`, `log`, `diff`, `.gitignore` |
| **Daily work** | | |
| 05 | [Undoing things safely](lessons/05-undoing-things.md) | `restore`, `reset`, `revert`, `checkout`, `clean` - which one, and when |
| 06 | [Branching and merging](lessons/06-branching-and-merging.md) | What a branch really is, fast-forward vs three-way merge |
| 07 | [Remotes](lessons/07-remotes.md) | `clone`, `fetch`, `pull`, `push`, tracking branches, `pull --rebase` |
| 08 | [Merge conflicts](lessons/08-merge-conflicts.md) | Reading them, resolving them, and the tools that help |
| 09 | [Rebase vs merge](lessons/09-rebase-vs-merge.md) | The honest comparison, and the golden rule |
| 10 | [The rescue kit](lessons/10-rescue-kit.md) | `stash`, `cherry-pick`, `reflog`, `bisect` - recovering from anything |
| **Working with people** | | |
| 11 | [Pull requests and review](lessons/11-pull-requests.md) | The workflow, review that helps, draft PRs, CODEOWNERS |
| 12 | [Branching strategies](lessons/12-branching-strategies.md) | Trunk-based, GitHub Flow, GitFlow - and which your team should use |
| 13 | [Commit hygiene](lessons/13-commit-hygiene.md) | Atomic commits, conventional commits, messages that survive |
| 14 | [Tags, releases, versioning](lessons/14-tags-and-releases.md) | Annotated tags, semantic versioning, changelogs |
| **Power tools** | | |
| 15 | [Rewriting history safely](lessons/15-rewriting-history.md) | `amend`, interactive rebase, squash, `filter-repo`, force-with-lease |
| 16 | [Hooks and local automation](lessons/16-hooks.md) | Client hooks, `pre-commit`, and why server-side matters more |
| 17 | [Large and multi-repo setups](lessons/17-large-repos.md) | Monorepo vs polyrepo, submodules, subtrees, sparse checkout, LFS |
| 18 | [Git internals](lessons/18-git-internals.md) | Objects, refs, packfiles - what a commit really is on disk |
| **Pipelines** | | |
| 19 | [CI/CD fundamentals](lessons/19-cicd-fundamentals.md) | What a pipeline is for, the stages, and what "green" should mean |
| 20 | [Your first GitHub Actions pipeline](lessons/20-github-actions-basics.md) | Workflow anatomy, triggers, jobs, steps, runners |
| 21 | [A real pipeline](lessons/21-real-pipeline.md) | Lint, test, build, scan, publish - with caching and matrices |
| 22 | [Deployment](lessons/22-deployment.md) | Environments, approvals, secrets, OIDC instead of long-lived keys |
| 23 | [GitOps and release engineering](lessons/23-gitops.md) | Git as source of truth, promotion, rollback |
| 24 | [Securing the repo and supply chain](lessons/24-securing-supply-chain.md) | Branch protection, signed commits, secret scanning, SBOM |
| **Applied** | | |
| 25 | [Git and pipelines for Salesforce](lessons/25-salesforce.md) | `sf` CLI in CI, scratch orgs, delta deploys, JWT auth |

Every module follows the same six blocks: **mental model → mechanics → build it → what breaks →
cost & performance → interview drill**.

---

## House rules

1. **Type every command.** Git is muscle memory. Reading it teaches you nothing you will still
   have next week.
2. **Break things on purpose.** Every module makes you cause the failure before it shows you
   the fix. That is the half you get paid for.
3. **Understand the three trees first.** Most Git confusion is really confusion about which
   tree a command touches.
4. **Nothing is lost until it is garbage collected.** `git reflog` has saved more careers than
   any backup.
5. **Every lesson ends with interview questions,** answered the way an architect answers them.

---

## Attribution & ownership

Copyright (c) 2026 Himanshu Kumar. All rights reserved.

This material is my original work. Reading it here and linking to it is welcome.
Downloading, copying, mirroring, forking, redistributing, or using it to train an
AI model requires **prior written permission**.

Connect: [LinkedIn](https://www.linkedin.com/in/himanshukumar-sf/) · [X](https://x.com/kum60094) · [GitHub](https://github.com/89himanshu-dwivedi) · [Email](mailto:himanshu.jee.1996@gmail.com)

## License

See [LICENSE](../LICENSE) &mdash; proprietary, all rights reserved, permission required.
