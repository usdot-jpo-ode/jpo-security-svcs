# Security Service Application Suite

## Overview

This repository contains a suite of Spring Boot applications that work together to provide security services
for the [jpo-ode](https://github.com/usdot-jpo-ode/jpo-ode).

The three main directories and components in this repository are:

1. **jpo-security-svcs**:
    - This is the core service of the suite and provides actual security-related functionalities.
    - The goal of this application is to offer secure operations like managing authentication, authorization, and other
      core security-related tasks.

2. **mock-signing-svc**:
    - A mock service that simulates signature-related operations during development and testing.
    - It is used for scenarios where an external signature service is required but either unavailable or unnecessary for
      local testing.

3. **tmca-tester**:
    - A simple Node.js application to enable testing of the TMC REST interface with certificates.
    - It is used for scenarios where an external signature service using MTLS is required.

---

## Getting Started

Follow the steps below to run the applications locally for development and testing purposes.

### Prerequisites

Ensure you have the following tools installed and configured on your machine:

- **Java Development Kit (JDK):** Version 21 or later
- **Maven:** For building the applications
- **Docker:** Optional, for running the services in a containerized environment

---

## Running the Applications Locally

### 1. **Configure your environment**

Make a copy of the contents of the [`sample.env`](sample.env) file and paste them into a file named `.env`.
Populate the relevant environment variables following the instructions in the comments.

### 2. **Running `jpo-security-svcs`**

The `jpo-security-svcs` directory contains the main security service. Follow these steps to build and run it:

#### Build the Application

Navigate to the `jpo-security-svcs` directory and build the project:

```bash
cd jpo-security-svcs
mvn clean package
```

#### Run the Application

Run the `jpo-security-svcs` application:

```bash
java -jar target/jpo-security-svcs.jar
```

By default, this service runs on `http://localhost:8090`. You can change the port using the `application.properties`
file or through environment variables.

---

### 3. **Running `mock-signing-svc`**

The `mock-signing-svc` simulates the external signature service. Follow these steps to build and run it:

#### Build the Mock Application

Navigate to the `mock-signing-svc` directory and build the mock service:

```bash
cd mock-signing-svc
mvn clean package
```

#### Run the Mock Service

Run `mock-signing-svc`:

```bash
java -jar target/mock-signing-svc.jar
```

By default, this service runs on `http://localhost:8091`. You can update the port similarly using
`application.properties` or environment variables.

---

### 4. **Interacting with the Services**

Once both applications are running:

1. You can use tools like **Postman** or **cURL** to send requests to the services.
2. The `jpo-security-svcs` will rely on `mock-signing-svc` for signature-related operations when performing
   security-related validations in testing environments.

---

## Running with Docker (Optional)

If you want to run the applications in Docker containers, you can use the provided `Dockerfile` in each subdirectory or
the docker-compose.yaml file in the root directory (this directory).

### Building and Running `jpo-security-svcs` with `mock-signing-svc` using docker-compose

From this directory:

```bash
docker-compose --profile local up --build -d
```

### Building and Running `jpo-security-svcs` with Docker

From the root of the `jpo-security-svcs` directory:

```bash
# Build image
docker build -t jpo-security-svcs .

# Run container
docker run -p 8080:8080 jpo-security-svcs
```

### Building and Running `mock-signing-svc` with Docker

From the root of the `mock-signing-svc` directory:

```bash
# Build image
docker build -t mock-signing-svc .

# Run container
docker run -p 9090:9090 mock-signing-svc
```
