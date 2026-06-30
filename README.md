# SoftwareEngineeringGroup16B

## Configuration

The system uses Spring configuration files under `src/main/resources`.

There are two main configuration files:

- `application.properties` - Spring, server, database, JWT base secret, and JPA configuration.
- `system-config.properties` - project-specific system configuration.

`application.properties` imports `system-config.properties` using:

```properties
spring.config.import=classpath:system-config.properties
```

### `application.properties`

This file contains infrastructure-level configuration, including:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=...
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

server.address=0.0.0.0
server.port=8080

jwt.secret=...
```

The database connection details are configured here and are not hardcoded in the application code.

### `system-config.properties`

This file contains project-specific configuration used during startup and runtime:

```properties
# External systems
external.wsep.base-url=https://damp-lynna-wsep-1984852e.koyeb.app/
external.photon.base-url=https://photon.komoot.io/api/
external.photon.limit=1

# Startup behavior
startup.validate-external-systems=true

# Default admin created when no admins exist
system.default-admin.username=admin123
system.default-admin.password=password
system.default-admin.email=mail@example.com

# Virtual queue settings
virtual-queue.default-pass-num=50
virtual-queue.pass-timeout-ms=600000

# JWT settings
jwt.admin-secret=change-this-admin-secret-to-a-long-random-string-at-least-32-characters
jwt.user-expiration-ms=3600000
jwt.admin-expiration-ms=900000
```

### Configuration values

| Property | Meaning |
|---|---|
| `external.wsep.base-url` | Base URL of the external WSEP payment/ticketing system. |
| `external.photon.base-url` | Base URL of the Photon location service. |
| `external.photon.limit` | Maximum number of Photon results requested. |
| `startup.validate-external-systems` | If `true`, startup validates external systems such as WSEP. If `false`, startup skips this validation. |
| `system.default-admin.username` | Username of the default system admin created when no system admins exist. |
| `system.default-admin.password` | Password of the default system admin created when no system admins exist. |
| `system.default-admin.email` | Email of the default system admin created when no system admins exist. |
| `virtual-queue.default-pass-num` | Number of users allowed to pass the virtual queue simultaneously for a newly created event. |
| `virtual-queue.pass-timeout-ms` | Time in milliseconds before a passed queue entry expires. |
| `jwt.admin-secret` | Secret used for admin JWT signing/validation. Must be at least 32 characters. |
| `jwt.user-expiration-ms` | User JWT expiration time in milliseconds. |
| `jwt.admin-expiration-ms` | Admin JWT expiration time in milliseconds. |

### Test configuration

Tests use separate configuration files under `src/test/resources`:

- `src/test/resources/application.properties`
- `src/test/resources/system-config.properties`
- `src/test/resources/invalid-system-config.properties`

The test `application.properties` imports the test `system-config.properties` using:

```properties
spring.config.import=classpath:system-config.properties
```

The test configuration uses test-safe values such as local fake external-system URLs and test admin credentials.

`invalid-system-config.properties` is used by configuration tests to verify that invalid configuration prevents the Spring test context from starting.

For example:

```properties
virtual-queue.default-pass-num=not-a-number
```

Since the application expects `virtual-queue.default-pass-num` to be an integer, this invalid value causes configuration loading to fail.

### Configuration tests

Configuration loading is tested by:

```text
src/test/java/com/group16b/ApplicationLayer/ConfigurationFileTests.java
```

The tests verify:

1. A valid `system-config.properties` file loads successfully.
2. An invalid configuration file prevents the Spring context from starting.

Run all tests with:

```bash
mvn test
```