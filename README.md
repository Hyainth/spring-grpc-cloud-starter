# spring-grpc-cloud-starter

## Overview

`spring-grpc-cloud-starter` is a small reference repository for:

- Spring Boot 4
- Spring gRPC
- Spring Cloud DiscoveryClient
- Spring Cloud Alibaba Nacos
- JDK 25

The main goal is to make `discovery:///service-name` style gRPC calls work with the official Spring gRPC stack through a Spring Cloud Discovery based adaptation layer.

Current Spring gRPC does not provide a built-in Spring Cloud Discovery based `NameResolver`. This repository fills that gap with:

- `spring-grpc-discovery-starter`
- `spring-grpc-nacos-discovery-starter`

## Modules

- `demo-grpc-proto-sdk`
  Generates Java messages and gRPC stubs from the root `proto/` directory.
- `spring-grpc-discovery-starter`
  Generic client-side starter for `discovery:///service-name` based on `DiscoveryClient`.
- `spring-grpc-nacos-discovery-starter`
  Nacos-specific provider-side starter that publishes `gRPC_port` metadata automatically.
- `demo-grpc-provider`
  Minimal provider app with HTTP + gRPC.
- `demo-grpc-consumer`
  Minimal consumer app that calls provider through `discovery:///demo-grpc-provider`.

## Versions

- repository version: `1.0.0`
- JDK: `25`
- Spring Boot: `4.0.5`
- Spring Cloud: `2025.1.1`
- Spring Cloud Alibaba: `2025.1.0.0`
- Spring gRPC: `1.0.2`
- grpc-java: `1.77.1`

## External Nacos

This repository does not provide Docker Compose for Nacos.

Prepare an existing Nacos server and set these values in `env/demo.env`:

```bash
cp env/demo.env.example env/demo.env
```

Example:

```bash
NACOS_SERVER_ADDR=127.0.0.1:8848
NACOS_USERNAME=nacos
NACOS_PASSWORD=nacos
NACOS_NAMESPACE=demo
NACOS_GROUP=DEFAULT_GROUP
```

## Build

```bash
export JAVA_HOME=/usr/local/java/jdk-25.0.2.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"

mvn clean install
```

## Run Provider

```bash
set -a
source env/demo.env
set +a

export JAVA_HOME=/usr/local/java/jdk-25.0.2.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"

java -jar demo-grpc-provider/target/demo-grpc-provider-1.0.0.jar
```

Provider ports:

- HTTP: `8081`
- gRPC: `9091`

## Run Consumer

```bash
set -a
source env/demo.env
set +a

export JAVA_HOME=/usr/local/java/jdk-25.0.2.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"

java -jar demo-grpc-consumer/target/demo-grpc-consumer-1.0.0.jar
```

Consumer port:

- HTTP: `8082`

## Verify

Check Nacos first:

- `demo-grpc-provider` is registered
- `demo-grpc-consumer` is registered
- provider metadata contains `gRPC_port=9091`

Then call:

```bash
curl "http://127.0.0.1:8082/api/consumer/call?name=codex"
```

Expected response:

```json
{"message":"hello","source":"consumer-http"}
```

## Starter Usage

### 1. Discovery starter

Add dependency:

```xml
<dependency>
    <groupId>com.github.hyacinth</groupId>
    <artifactId>spring-grpc-discovery-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

Channel example:

```yaml
spring:
  grpc:
    client:
      channels:
        provider:
          address: discovery:///demo-grpc-provider

hyacinth:
  grpc:
    discovery:
      enabled: true
      load-balancing-policy: round_robin
```

### 2. Nacos support starter

Add dependency:

```xml
<dependency>
    <groupId>com.github.hyacinth</groupId>
    <artifactId>spring-grpc-nacos-discovery-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

When the application has both:

- Spring gRPC server enabled
- Nacos discovery enabled

the starter automatically publishes `gRPC_port` into Nacos metadata.

## Design Notes

- `spring-grpc-discovery-starter` stays generic and only depends on `DiscoveryClient`
- `spring-grpc-nacos-discovery-starter` keeps Nacos-specific provider registration logic out of the generic starter
- instance metadata key is fixed as `gRPC_port`
- if `gRPC_port` is missing, resolver falls back to the instance port
- if a later refresh temporarily returns no instances, the resolver keeps the last successful resolution instead of immediately dropping usable addresses

## Publishing Direction

This repository is intentionally small, but it is organized as a public starter repository instead of a one-off demo.

It is suitable for:

- validating `discovery:///service-name`
- extracting reusable starter code
- preparing an upstream issue or pull request reference for Spring gRPC
- publishing reusable starter artifacts to GitHub or Maven repositories

It is not intended to be a full platform or production template.
