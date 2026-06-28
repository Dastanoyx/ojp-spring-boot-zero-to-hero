# Contributing

This is a community learning guide for Open J Proxy (OJP) + Spring Boot. Contributions that make it clearer, more accurate, or more complete are very welcome.

## Ground rules

- **Accuracy first.** OJP is pre-1.0 and moves fast. If you add a property key, artifact coordinate, or port, verify it against the [official OJP repo](https://github.com/Open-J-Proxy/ojp) and link the source. When unsure, say "verify against the docs" rather than asserting.
- **Every module stays runnable.** If you change a `pom.xml` or properties file, run the module end to end against the `infra` stack before opening a PR.
- **Keep the level structure.** Each level builds on the last. New material should slot into an existing level or propose a clearly-numbered new one.

## Running locally

```bash
cd infra
bash drivers/download-drivers.sh
docker compose up -d
cd ../01-hello-ojp && ./mvnw spring-boot:run
```

## What's especially useful

- Confirmed exact `ojp.*` config keys per version (with source links).
- Real load-test numbers with documented hardware + parameters.
- A verified Grafana dashboard with correct OJP metric names.
- Quarkus / Micronaut sibling examples (OJP supports them too).

## Reporting issues

Open an issue describing your OJP version, Spring Boot version, database, and the exact symptom. Include logs (with secrets redacted).

## A note on scope

This repo teaches OJP; it is **not** an official OJP project. Bug reports about OJP itself belong upstream at https://github.com/Open-J-Proxy/ojp.
