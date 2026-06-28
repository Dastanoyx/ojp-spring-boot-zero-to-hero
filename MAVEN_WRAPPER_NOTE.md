# About `./mvnw`

The module READMEs use `./mvnw` (the Maven Wrapper) so contributors don't need a
specific Maven version installed. This starter repo does **not** vendor the wrapper
binaries to keep the download small. Generate them once per module (or at the root)
with an installed Maven:

```bash
# from inside any module directory, e.g. 01-hello-ojp/
mvn -N wrapper:wrapper -Dmaven=3.9.9
```

Or simply use your local `mvn` instead of `./mvnw` everywhere:

```bash
mvn spring-boot:run
```

Both work identically.
