# Copilot Instructions for JMRI

Use this file as the default operating guide for repository work. Trust these instructions first, and only search further if the information here is incomplete or proven wrong.

## What this repository is

- **JMRI** is model railroad software with desktop apps, scripting, web UI assets, and large automated test coverage.
- **Project type:** large, long-lived Java monorepo (plus scripts and web assets).
- **Primary languages:** Java, XML, shell, Python/Jython, TypeScript/JavaScript.
- **Build systems in use:** Ant (`build.xml`) and Maven (`pom.xml`) in combination.
- **Runtime targets:** desktop Java apps (`DecoderPro`, `PanelPro`), tests, tooling scripts.

## Repo scale and structure (high signal map)

- `build.xml` — canonical Ant targets (compile, run apps, tests, lint-ish checks, packaging).
- `pom.xml` — Maven config, profiles, dependency graph, CI-oriented plugin orchestration.
- `.github/workflows/` — required CI definitions you should mirror locally.
- `java/src/` — main Java sources.
- `java/test/` — JUnit tests.
- `java/acceptancetest/` — Cucumber acceptance tests.
- `jython/` — Jython scripts and script-related checks.
- `web/ts/` and `web/js/` — TypeScript source and compiled JS output.
- `scripts/` — helper scripts used by CI and maintainers.
- `checkstyle.xml`, `.spotbugs-check.xml`, `archunit.properties`, `archunit_ignore_patterns.txt` — static/architecture gates.
- `archunit_store/` — frozen architecture baseline (do not expand casually).

## Toolchain and environment (validated locally in this workspace)

Verified on this machine:

- Java: `openjdk 17.0.18`
- Maven: `3.6.3`
- Ant: `1.10.7`
- Node: `v12.22.9`
- Yarn: **not installed** (command missing)

Repository docs indicate:

- Ant requires **>= 1.10.6** (`help/en/html/doc/Technical/Ant.shtml`).
- CI workflows use **JDK 11** and separate jobs for **JDK 25**.
- TypeScript CI uses **Node 20 + yarn install**, then `ant typescript`.

Practical guidance:

- For CI parity, prefer JDK 11 (and sometimes 25) even if local JDK 17 works for many tasks.
- TypeScript checks require `tsc` available in PATH (normally via yarn-installed dependencies).

## Build/test/lint/run command playbook

Run from repo root.

### 1) Bootstrap (always first in a fresh environment)

```bash
ant clean
```

Observed:

- Works and removes `target/`, `temp/`, and `tests.log`.

### 2) Compile/build

Preferred fast compile path used by workflows:

```bash
mvn antrun:run -Danttarget=debug -DskipTests
```

Observed:

- First run may take a long time due to dependency download.
- Re-run succeeded cleanly after dependency bootstrap.

### 3) Focused tests (fast local validation)

```bash
mvn -q -Dtest=jmri.util.FileUtilTest test -Djmri.skipTestsRequiringSeparateRunning=true -Djava.awt.headless=true
```

Observed:

- Passed in this environment.

### 4) Static-analysis subset (high-value pre-PR checks)

```bash
mvn -q antrun:run -Danttarget=tests-warnings-check
./scripts/test_stale_sources.sh
./scripts/test_BOM_and_tab.sh
./scripts/test_default_lcf.sh
```

Observed:

- All commands succeeded here.
- `tests-warnings-check` emitted a Graal-related warning but still returned success.

### 5) TypeScript consistency check (only when touching web/ts or web/js)

CI-equivalent flow:

```bash
yarn install
ant typescript
git diff --exit-code web/js
```

Observed in this environment:

- `ant typescript` **failed** because `tsc` is missing (`Cannot run program "tsc"`).
- `yarn` is not installed locally here, so TypeScript CI parity is currently unavailable without setup.

### 6) Running apps and single-class runs

Ant app launch:

```bash
ant decoderpro
ant panelpro
```

Single-class/test launcher:

```bash
./runtest.csh --help
```

Observed:

- `runtest.csh --help` works and auto-generates `.run.sh` if needed.

## CI pipelines to mirror before opening PR

Mandatory workflow behavior is defined in `.github/workflows/`:

- `windows-test.yml` / `windows-java25-test.yml` — broad test suite on Windows.
- `headless-test.yml` — headless Maven tests on Linux.
- `run-separate.yml` (+ `run-separate-LinkedWarrantTest.yml`) — separately flagged tests on macOS.
- `static-analysis.yml` / `static-analysis-25.yml` — ECJ warnings, SpotBugs, Checkstyle, Javadoc, help scan, architecture tests, and repository scripts.
- `typescript-check.yml` — Node 20 + yarn + `ant typescript` + diff check on `web/js`.

When changes are non-trivial, prioritize local replication in this order:

1. Compile (`debug` target via Maven antrun)
2. Focused tests for changed areas
3. Static-analysis subset scripts
4. TypeScript check if web files changed

## Architecture and quality guardrails

- Architecture tests rely on ArchUnit with store updates disabled by default (`archunit.properties`).
- Avoid updating `archunit_store/` as a workaround; fix violations instead.
- Checkstyle policy is in `checkstyle.xml` (includes line endings and tab checks).
- SpotBugs CI gate uses `.spotbugs-check.xml` suppressions; do not add suppressions without strong justification.
- Keep line endings LF; keep Python files tab-free (enforced by script).

## Common failure patterns and mitigations

- **Huge first Maven run:** expected due downloads; retry after dependencies populate local cache.
- **TypeScript build fails with missing `tsc`:** install Node/Yarn dependencies first (CI uses Node 20 + yarn).
- **Intermittent/flagged tests:** use `./scripts/run_flagged_tests_separately` after test compile (`mvn antrun:run -Danttarget=tests`).
- **Local environment mismatch vs CI JDK:** if odd failures appear on JDK 17, retry on JDK 11 (and optionally 25) to match workflows.

## Important files in repo root (quick reference)

High-priority root files:

- `build.xml`, `pom.xml`
- `checkstyle.xml`, `archunit.properties`, `archunit_ignore_patterns.txt`, `.spotbugs-check.xml`
- `project.properties`, `release.properties`, `jmri.conf`
- `README.md`
- `runtest.csh`
- `tests_lcf.xml`, `tests_jacoco_lcf.xml`, `default_lcf.xml`

## Working style for coding agents

- Prefer **small, surgical changes** in existing patterns over broad refactors.
- Always run at least one compile command and targeted tests relevant to changed code.
- If you cannot run a required check locally (for example TypeScript tooling missing), state that clearly in PR notes and list exact missing preconditions.
- Do not rely on discovery-heavy searching unless this file is insufficient or stale.

