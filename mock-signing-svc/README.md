# mock-signing-svc

## Overview

The **mock-signing-svc** is a Spring Boot-based mock service designed to simulate signature-related functionalities. 
It can be used for testing and development purposes in environments where an external signature service is required 
but unavailable or not ready for integration.

**Important:** This is not intended for production usage!

---

## Features

- Simulates signature service responses for testing.
- Easily configurable for different environments (e.g., development, testing).
- Lightweight and extendable.
- Implements Spring Boot for easy dependency management and RESTful APIs.

---

## Prerequisites

To run and configure this Spring Boot application, you need the following:

- **Java:** JDK 21 or later (application uses Eclipse Temurin 21).
- **Maven:** Installed and configured to build the application.
- **Docker (optional):** If deploying the service within a containerized environment.

---

## Usage

### Step 1: Build the Application

Before running the application, ensure it is built using Maven:

```bash
mvn clean package
```

This will generate a runnable `.jar` file under the `target` directory.

---

### Step 2: Run the Application

Run the application from the terminal using `java`:

```bash
java -jar target/mock-signing-svc.jar
```

By default, the application runs on port `8091`. You can configure the port using the `application.yaml` file or pass it as a command-line argument:

```bash
java -jar target/mock-signing-svc.jar --server.port=9090
```

---

### Step 3: Access the Mock API

After starting the service, you can access its endpoints. Here is an example of how to interact with the mock:

- **API Base URL:** `http://localhost:8091/`
- **Sample Endpoint:**
    - `/mock-signer/sign` - Simulates a mock response for signature-related requests.

### Step 4: Stop the Application

Simply stop the process using `CTRL+C` in the terminal.

---

## Configuration

### Properties

The application's behavior can be customized using the `application.yaml` file located under the `/src/main/resources/` directory.

### Environment Variables

You can override default properties using environment variables defined in a `.env` file.
Or provide them directly when running the application:

```bash
java -jar target/mock-signing-svc.jar --server.port=9090
```

---

## Running with Docker (Optional)

The application can be containerized and run using Docker. A `Dockerfile` is provided.

### Build Docker Image

Run the following command to build the Docker image:

```bash
docker build -t mock-signing-svc .
```

### Run Docker Container

Run the container with the following command:

```bash
docker run -p 8091:8091 mock-signing-svc
```

---

## Development

### Project Structure

The project follows the standard Spring Boot directory structure:

```plaintext
mock-signing-svc/
├── src/
│   ├── main/
│   │   ├── java/           # Application source code
│   │   ├── resources/      # Configuration files (application.yaml)
│   └── test/               # Unit and integration tests
├── pom.xml                 # Maven configuration
└── README.md               # Project documentation
```

---

## Frequently Asked Questions (FAQ)

1. **What is the purpose of this service?**
    - The service acts as a mock for simulating signature interactions during development and testing. It eliminates the need for an actual signature service in non-production environments.

2. **Can I extend this mock service with new endpoints?**
    - Yes! The service is built with Spring Boot, allowing developers to easily add new REST endpoints by creating additional controllers.

3. **How do I change the default configuration?**
    - Define the properties using environment variables in your `.env` (use the sample stored in the parent directory [here](../sample.env)) file.

---