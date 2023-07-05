Jpo-security-svcs Release Notes
----------------------------

Version 1.2.0, released July 5th 2023
----------------------------------------

### **Summary**
The updates for jpo-security-svcs 1.2.0 involve CI/CD additions & an updated json version.

Enhancements in this release:
- Added CI/CD for Docker Build & Sonar Cloud.

Fixes in this release:
- Bumped json from 20180130 to 20230227

Known Issues
- There are no known issues at this time.

Version 1.1.0, released Mar 30th 2023
----------------------------------------

### **Summary**
The updates for jpo-security-svcs 1.1.0 include initial cloud signature functionality, keystore utilization, a signing tester application, and certificate handling modifications.

Enhancements in this release:
-	Added debug configuration.
-	Implemented initial cloud signature functioning.*
-	Added JKS and ca-certificate settings.
-	Added working keystore read from docker.
-	Added exclusions.
-	Added new required properties for MTLS.
-	Updated README.
-	Added signing tester application.
-	Changed the base image to eclipse-temurin:11-jre-alpine instead of the deprecated openjdk8u171-jre-alpine image.
-	Changed the version in the pom.xml to 1.1.0
-	Modified certificate handling in the Dockerfile & docker-compose.yml files.

Fixes in this release:
- There are no known fixes at this time.

Known Issues
- There are no known issues at this time.

Version 1.0.1, released Mar 8th 2021
----------------------------------------

### **Summary**
This release is to directs logging output to the console rather than a physical file. This update allows Docker to manage the log files.

Version 1.0.0, released Sep 30th 2020
----------------------------------------

### **Summary**
This release provides TIM signature compatibility to jpo-ode-1.2.0.
