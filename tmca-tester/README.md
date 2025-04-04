# TMCA Tester

## Table of Contents

- [About](#about)
- [Getting Started](#getting_started)
- [Usage](#usage)

## About <a name = "about"></a>

This is a simple Node.js application to enable testing of the TMC REST interface with certificates.

## Getting Started <a name = "getting_started"></a>

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

As this is a Node.js application, Node.js is required to run it. Download from the [Node.js website](https://nodejs.org/download/release/latest/), add to path and verify operation:
```
node -v
```

Certificates are provided by the signing entity. This will be placed under the 'creds' directory and the application requires three files: 

*   'AppTLSCaCert.pem' : the CA cert (from signing authority)
*   'clientCrt.pem'    : the client cert (from signing authority)
*   'clientKey.pem'    : the client's secret key (generated when creating CSR)

### Installing

Install required components:

```
npm i
```

## Usage <a name = "usage"></a>

To run the test application, simply execute:

```
node .\tmca-tester.js
```

This will launch the application with a readline menu as the main entry point. From here, follow the instructions in the console to perform various tests.