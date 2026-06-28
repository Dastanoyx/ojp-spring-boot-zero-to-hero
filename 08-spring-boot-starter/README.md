# Level 8 — The Spring Boot starter

Levels 1–7 wired OJP by hand (exclude HikariCP, set `SimpleDriverDataSource`, prefix the URL). The OJP project also ships a **Spring Boot starter** that auto-configures most of that for you, so a new app needs even less ceremony.

## Manual wiring vs. the starter

**Manual (what every prior level did):**

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
  <exclusions>
    <exclusion><groupId>com.zaxxer</groupId><artifactId>HikariCP</artifactId></exclusion>
  </exclusions>
</dependency>
<dependency>
  <groupId>org.openjproxy</groupId>
  <artifactId>ojp-jdbc-driver</artifactId>
  <version>0.4.7-beta</version>
</dependency>
```
```properties
spring.datasource.url=jdbc:ojp[localhost:1059]_postgresql://localhost:5432/defaultdb
spring.datasource.driver-class-name=org.openjproxy.jdbc.Driver
spring.datasource.type=org.springframework.jdbc.datasource.SimpleDriverDataSource
```

**With the starter:**

```xml
<dependency>
  <groupId>org.openjproxy</groupId>
  <artifactId>spring-boot-starter-ojp</artifactId>
  <version>0.4.7-beta</version>
</dependency>
```
```properties
spring.datasource.url=jdbc:ojp[localhost:1059]_postgresql://localhost:5432/defaultdb
spring.datasource.username=testuser
spring.datasource.password=testpassword
```

The starter pulls in the OJP driver and auto-configures a non-pooling datasource, so you skip the manual `driver-class-name` / `type` lines (and you typically don't fight HikariCP, since the starter sets things up the OJP way).

> ⚠️ **Verify coordinates and property keys.** OJP is pre-1.0; artifact IDs and the exact
> auto-config property names can change between releases. Confirm against:
> - the starter module: https://github.com/Open-J-Proxy/ojp/tree/main/spring-boot-starter-ojp
> - the framework integration guide: https://github.com/Open-J-Proxy/ojp/blob/main/documents/java-frameworks/README.md
>
> The `pom.xml` and `application.properties` here include fallback comments showing the manual trio
> in case your version still wants them.

## Run it

```bash
cd ../infra && docker compose up -d && cd ../08-spring-boot-starter
./mvnw spring-boot:run

curl -X POST localhost:8080/books -H "Content-Type: application/json" -d '{"title":"Starter","author":"You"}'
curl localhost:8080/books
```

## When to use which

- **Starter** — new apps, greenfield, you want the least boilerplate and are happy to track the starter's conventions.
- **Manual** — you need precise control (multiple datasources like Level 3, custom datasource beans, unusual setups), or you're retrofitting an existing app and want to change as little as possible.

Both produce the same runtime behavior: traffic through OJP, pooling delegated to the server.

Next: [Level 9 — Production hardening](../09-production-hardening/).
