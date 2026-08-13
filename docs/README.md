# Kaiteyo Documentation

This directory contains the maintained engineering documentation for **Kaiteyo Android**. The product supports ARM64-v8a devices on Android 12 or newer. Phones are portrait-only; tablets and pads support portrait and landscape with the same mobile screen hierarchy.

## Core Documents

| Document | Purpose |
|---|---|
| [`AI_CONTEXT.md`](AI_CONTEXT.md) | Current implementation contract and guardrails |
| [`06_ARCHITECTURE.md`](06_ARCHITECTURE.md) | Android modules, source sets, data flow, and ownership |
| [`07_DEVELOPMENT_GUIDE.md`](07_DEVELOPMENT_GUIDE.md) | Local Android development and validation commands |
| [`08_CONTRIBUTING.md`](08_CONTRIBUTING.md) | Contribution and change-validation expectations |
| [`12_CODING_STANDARDS.md`](12_CODING_STANDARDS.md) | Kotlin, Compose, data, and test standards |
| [`14_RELEASE_PROCESS.md`](14_RELEASE_PROCESS.md) | Android build, release, and integrity checklist |
| [`internal-database-workflow.md`](internal-database-workflow.md) | Vendored dictionary source-data and export workflow |

## Operational References

| Location | Purpose |
|---|---|
| [`planning/`](planning/) | Active issues/TODO plus explicit historical records |
| [`troubleshooting/`](troubleshooting/) | Android build and runtime diagnostics |

The application is Android-only. Historical planning files may record previous work, but they are not active product scope. Do not add desktop, iOS, web, Appearance Studio, or Theme Studio targets to current documentation or implementation.
