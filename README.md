# SoftwareEngineeringGroup16B

Ticketing system project for managing events, venues, users, reservations, and virtual queues.

## Requirements

- Java 17+
- Maven
- Node.js + npm, for the frontend
- PostgreSQL, if running with the database profile/configuration

## Running Tests

From the project root:

```bash
mvn test
```

To run a specific test class:

```bash
mvn -Dtest=VirtualQueueConfigTests test
```

## Running the Backend

From the project root:

```bash
mvn spring-boot:run
```

The backend configuration is controlled through:

```text
src/main/resources/application.properties
```

## Running the Frontend

From the `frontend` directory:

```bash
npm install
npm run dev
```

## External Configuration

The system uses `application.properties` for initialization/configuration values instead of hard-coding them directly into the domain logic.

Main configuration file:

```text
src/main/resources/application.properties
```

Test configuration file:

```text
src/test/resources/application.properties
```

The test configuration is separate so tests can run with controlled values without changing the main runtime configuration.

## Virtual Queue Configuration

The virtual queue pass number is configured externally:

```properties
virtual-queue.pass-num=50
```

This value controls how many users may pass the virtual queue for an event at the same time.

The value is injected into the services that create virtual queues:

```text
EventService
StartupService
```

Those services pass the configured value into the `VirtualQueue` constructor:

```java
new VirtualQueue(eventId, virtualQueuePassNum)
```

The `VirtualQueue` domain object stores the configured value in its `pass_num` field and uses it when moving users from the waiting queue into the passed queue.

The value is not hard-coded in `VirtualQueue`.

## Virtual Queue Configuration Flow

```text
application.properties
        ↓
virtual-queue.pass-num
        ↓
EventService / StartupService
        ↓
VirtualQueue constructor
        ↓
pass_num field
```

## Virtual Queue Validation

The configured value must be positive.

Invalid values such as:

```properties
virtual-queue.pass-num=0
```

or:

```properties
virtual-queue.pass-num=-1
```

are rejected.

Validation exists in the services that receive the configured value and in the `VirtualQueue` constructor.

This prevents the system from creating a virtual queue with an invalid pass number.

## Virtual Queue Config Tests

The project includes tests for the virtual queue configuration behavior:

```text
VirtualQueueConfigTests
```

The tests verify:

```text
1. A valid virtual-queue.pass-num value does not crash the service setup.
2. An invalid virtual-queue.pass-num value crashes with an IllegalArgumentException.
```

Run them with:

```bash
mvn -Dtest=VirtualQueueConfigTests test
```

## Startup Default Admin Configuration

The default admin values are also configured externally:

```properties
startup.default-admin.username=admin123
startup.default-admin.password=password
startup.default-admin.email=mail@example.com
```

These values are used by `StartupService` when initializing the default system admin if needed.

## Server Configuration

The server address and port are configured in `application.properties`:

```properties
server.address=0.0.0.0
server.port=8080
```

## Notes for Developers

Do not hard-code initialization values inside domain classes.

For configurable runtime values:

```text
1. Add the value to application.properties.
2. Inject it into the relevant Spring service.
3. Pass it into the domain object through a constructor or method argument.
4. Validate the value before using it.
5. Add tests for valid and invalid configuration values.
```

For JPA entities, do not inject configuration directly into the entity using `@Value`.

Instead, inject configuration into the Spring-managed service that creates the entity, then pass the value into the entity constructor.

## Current Virtual Queue Design

`VirtualQueue` does not contain a hard-coded default pass number.

The old hard-coded constant was removed:

```java
private static final Integer PASS_NUM = 50;
```

Queue creation now requires an explicit pass number:

```java
public VirtualQueue(int id, int pass_num)
```

Production code gets this value from:

```properties
virtual-queue.pass-num=50
```

and passes it through:

```text
EventService
StartupService
```

This satisfies the requirement that initialization parameters such as the number of users allowed to reserve before waiting in queue are defined externally rather than hard-coded.