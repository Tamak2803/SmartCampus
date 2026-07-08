Here is your complete, updated **`README.md`** file containing the detailed project overview, functional module descriptions, and the step-by-step dependencies setup guide:

---


# SmartCampus Connect
Demo link : https://youtu.be/z9me8u2aZok
SmartCampus Connect is a decentralized, microservices-based backend platform designed to deliver core campus services to a university environment. The application brings together distributed system design methodologies: strict database-per-service isolation, synchronous RESTful orchestration, asynchronous AMQP choreography, multi-threaded concurrency locking, legacy SOAP interoperability, and circuit-breaker failure tolerance.

---

## 1. Project Overview & Problem Statement

### What is SmartCampus Connect?
In a modern university ecosystem, campus services such as student record registries, academic enrolments, library loans, alerting systems, and academic reporting dashboards are traditionally handled by isolated legacy platforms. When these systems are tightly coupled or forced to share a single database, the entire campus faces high latency, service propagation delays, and single points of failure (e.g., a crash in the library system bringing down student course registration).

**SmartCampus Connect** addresses these issues by decoupling these operations into a distributed, service-oriented architecture (SOA). By isolating each business capability into an independent, containerized, and database-isolated microservice, the platform ensures:
*   **High Scalability**: Heavily congested services (like Course Enrolment during registration week) can be scaled independently of stable services (like Student Profiles).
*   **Fault Isolation**: If a legacy system (like the Library Service) goes offline, transactional registration gates continue to operate normally.
*   **Data Integrity under Load**: The system handles concurrent transactional limits at the application tier to protect database resources.

---

## 2. Functional Modules & Service Capabilities

The platform exposes five primary modules, each designed as a self-contained microservice with strict boundaries:

### 2.1 Student Profile Service (Port 8081)
*   **Purpose**: Functions as the single source of truth (system of record) for student demographic and academic profiles.
*   **Key Functions**:
    *   Exposes a standard RESTful JSON interface.
    *   Provides full Create, Read, Update, and Delete (CRUD) operations mapped to standard HTTP verbs (`POST`, `GET`, `PUT`, `DELETE`).
    *   Maintains its own isolated database schema (`student_db`), ensuring student records cannot be modified directly by external database writes.

### 2.2 Course Enrolment Service (Port 8082)
*   **Purpose**: Manages semester course registrations and enforces strict seat limits under heavy concurrent workloads.
*   **Key Functions**:
    *   **Synchronous Orchestration**: Before executing any registration, it performs a real-time REST client lookup against port `8081` to verify the student profile exists and is eligible.
    *   **Concurrency Safeguards**: Implements application-tier re-entrant locks (`ReentrantLock`) mapped dynamically per course code. This serializes registration threads attempting to claim the same course code, preventing race conditions or double-booking.
    *   **Provisional Fallback**: Employs network timeout mapping. If the Student Profile Service is down, the system triggers a circuit-breaker fallback, gracefully approving the registration "provisionally" rather than failing the transaction.
    *   **Event Generation**: Upon a committed write to `enrolment_db`, it serializes and publishes a JSON event payload to RabbitMQ.

### 2.3 Notification Service (Port 8084)
*   **Purpose**: An asynchronous messaging consumer that alerts students of campus activities.
*   **Key Functions**:
    *   **Asynchronous Processing**: Uses background `@RabbitListener` threads to listen to registration events on `queue.notifications`.
    *   **Loose Coupling**: Completely decoupled from writing transactions. If the notification consumer is offline during a registration, RabbitMQ queues the messages persistently and delivers them automatically once the service comes back online.
    *   **Dashboard REST Bridge**: Stores the last 5 alerts in a thread-safe in-memory collection and exposes them over a CORS-enabled REST API for the live dashboard.

### 2.4 Library / Booking Service (Port 8083)
*   **Purpose**: Simulates integration with a legacy campus application, processing book loans and room bookings.
*   **Key Functions**:
    *   **SOAP/WSDL Compatibility**: Rather than using modern JSON, this module exposes legacy XML interfaces mapped to standard contracts defined in `library.xsd`.
    *   **Dynamic Contract Publishing**: Publishes its compiled bindings and WSDL documentation dynamically at `/ws/library.wsdl`.
    *   **Structured Exception Faults**: Validates incoming XML payloads and intercepts invalid inputs to generate structured `<soapenv:Fault>` exceptions returned under an HTTP 500 status code.

### 2.5 Reporting / Analytics Service (Port 8085)
*   **Purpose**: Compiles aggregate real-time statistical reports (e.g., student enrolment registration counts per academic course) for administrative consoles.
*   **Key Functions**:
    *   **CQRS Pattern**: To prevent slow, cross-database SQL join queries that degrade database read performance, this service subscribes independently to the registration events.
    *   **Data Aggregation**: Parses JSON events from RabbitMQ and increments registration counters in its own isolated database (`analytics_db`), enabling immediate, high-performance report delivery via a REST endpoint.

---

## 3. Port Allocation & Endpoint Registry

Each service is compiled independently, runs in an isolated JVM, and maintains its own in-memory database to satisfy the **Database-per-Service (R3)** architectural constraint.

| Service Name | Port | Protocols | Database | Core Responsibilities |
| :--- | :--- | :--- | :--- | :--- |
| **Student Profile Service** | `8081` | REST / JSON | H2 (`student_db`) | Student demographic data CRUD. |
| **Course Enrolment Service** | `8082` | REST / JSON | H2 (`enrolment_db`) | Semester enrolment, capacity checks, and eligibility checks. |
| **Library / Booking Service** | `8083` | SOAP / XML | H2 (`library_db`) | Book loans and room bookings (Legacy SOAP Endpoint). |
| **Notification Service** | `8084` | AMQP / REST | *None* | Background asynchronous listener for system events. |
| **Reporting / Analytics Service** | `8085` | REST / JSON | H2 (`analytics_db`) | CQRS aggregate read-model compiling course stats. |

---

## 4. System Architecture Topology

```
                                +---------------------------+
                                |  Unified HTML Dashboard   |
                                +-------------+-------------+
                                              |
                     +------------------------+------------------------+
                     | (REST APIs)                                     | (SOAP/XML)
                     v                                                 v
         +-----------------------+                         +-----------------------+
         | Student Profile Serv. |                         |  Library/Booking Serv |
         |   (DB: student_db)    |                         |   (DB: library_db)    |
         +-----------^-----------+                         +-----------+-----------+
                     |                                                 |
                     | (Sync REST RPC)                                 |
                     |                                                 |
         +-----------+-----------+                                     | (Publish Event)
         | Course Enrolment Serv.|                                     v
         |  (DB: enrolment_db)   +----------------------------->+---------------+
         +-----------+-----------+      (Publish Event)         |   RabbitMQ    |
                     |                                          |    Broker     |
                     +----------------------------------------->+-------+-------+
                                                                        |
                                                                        | (Consume Event)
                                                                        v
         +-----------------------+                         +------------+----------+
         |  Reporting/Analytics  |<------------------------+  Notification Serv.   |
         |  (DB: analytics_db)   |      (Event Sync)       |    (No Database)      |
         +-----------------------+                         +-----------------------+
```

---

## 5. Dependencies & Environment Setup Guide

Before attempting to compile or run the platform, verify that your local development machine satisfies all the software dependencies below. Follow the step-by-step installation instructions if any component is missing.

### 5.1 Git Bash (Required for Windows Users)
Since Windows Command Prompt does not natively support Unix shell scripts (`.sh`), we use **Git Bash** to compile, orchestrate, and execute the backend services.

*   **How to Check**: Press the `Windows Key`, type **"Git Bash"**, and see if the application appears. Alternatively, in your active terminal, run:
    ```bash
    git --version
    ```
    *Expected Output*: `git version 2.x.x.windows.1` (or similar).
*   **How to Install (If Missing)**:
    1. Download the **Git for Windows** installer from the [Official Git-SCM Download Page](https://git-scm.com/download/win).
    2. Launch the installer executable (e.g., `Git-2.x.x-64-bit.exe`).
    3. During the installation wizard:
        * Ensure **"Git Bash"** is selected under the *Components* checklist.
        * Keep the default configurations for the terminal emulator and line-ending conversions. (It is highly recommended to select *Checkout Windows-style, commit Unix-style line endings* to prevent CRLF file-lock errors on Windows).
    4. Click **Install** to finish.
    5. You can now right-click inside any folder in Windows File Explorer and select **"Open Git Bash here"** to open a terminal pre-navigated to that directory.

---

### 5.2 Java Development Kit (JDK 21 - Required)
The project utilizes Java 21 compilation targets. Running the modules on older environments (such as Java 8, 11, or 17) will cause immediate compilation failures or runtime `UnsupportedClassVersionError` crashes.

*   **How to Check**: Open your Git Bash terminal and run:
    ```bash
    java -version
    ```
    *Expected Output*: `java version "21.x.x"` or higher.
*   **How to Install (If Missing or on an older version)**:
    1. Download the **Eclipse Temurin JDK 21** installer from [Adoptium OpenJDK 21](https://adoptium.net/temurin/releases/?version=21).
    2. Run the installer wizard on Windows.
    3. **Crucial**: Ensure you check the boxes to enable:
        * **Add to PATH**
        * **Set JAVA_HOME variable**
    4. Complete the installation, restart your Git Bash terminal, and run `java -version` again to confirm.

---

### 5.3 Docker Desktop (Required for RabbitMQ Broker)
We use a Docker container to host our RabbitMQ message broker. You do not need to install RabbitMQ natively on your operating system, but Docker must be running to host the container.

*   **How to Check**: Run these commands in your terminal:
    ```bash
    docker --version
    docker compose version
    ```
    *Expected Output*: Both commands should return active version numbers.
*   **How to Install (If Missing)**:
    1. Download and install **Docker Desktop for Windows** from [Docker's Official Site](https://www.docker.com/products/docker-desktop/).
    2. Complete the installer and restart your computer if prompted.
    3. Launch **Docker Desktop** from your Start Menu.
    4. Wait until the status icon in the bottom-left corner of the Docker Desktop dashboard UI turns solid **green** (indicating "Engine Running").

---

### 5.4 Apache Maven (Required for Compiling)
The parent project orchestrates and packages all 5 microservice modules using Maven.

*   **How to Check**: Run this command in your terminal:
    ```bash
    mvn -version
    ```
    *Expected Output*: Returns your Maven version and links it to your active JDK 21 environment.
*   **How to Install (If Missing)**:
    1. Download the binary zip archive from [Apache Maven](https://maven.apache.org/download.cgi).
    2. Extract the folder to a permanent directory (e.g., `C:\Program Files\maven`).
    3. Add the path to the `/bin` directory (e.g., `C:\Program Files\maven\bin`) to your Windows **System Environment Variables (PATH)**.
    4. Restart your terminal to apply the changes and verify using `mvn -version`.

---

## 6. Running the Project Locally

Follow these steps to build, package, and launch the entire multi-service platform:

### Step 1: Clone the Repository
Open **Git Bash** and navigate to your working directory:
```bash
git clone https://github.com/Tamak2803/SmartCampus.git
cd SmartCampus
```

### Step 2: Grant Execution Permissions to Shell Scripts
```bash
chmod +x run-stack.sh
chmod +x load-test.sh
```

### Step 3: Run the Startup Pipeline
```bash
./run-stack.sh
```
*This command runs a clean Maven build, packages the binaries into executable JARs, spins up the RabbitMQ Docker container, pauses for 10 seconds to allow the broker to initialize, and boots all 5 backend engines as background processes.*

### Step 4: Verify Background Processes are Active
Check your system thread pool using the native Windows utility via Git Bash:
```bash
powershell -Command "Get-Process -Name java"
```
*You should see a table listing multiple active Java processes with distinct Process IDs (PIDs).*

---

## 7. How to Verify & Demo

Once your local deployment is online, you can verify every service using our unified, single-page browser console:

### Method A: Unified Browser Console
1. Open your Windows File Explorer and navigate into your root `SmartCampus/` directory.
2. Double-click the **`dashboard.html`** file to launch the console in your web browser.
3. Conduct your tests in this order:
   * **Create Profile** (Form 1): Generates a student profile inside `student_db`.
   * **SOAP Book Loan** (Form 3): Sends an XML payload to the legacy service on Port `8083`. If you type `FAIL` as the student ID, you will see a structured **SOAP Fault** displayed on the screen.
   * **Submit Enrolment** (Form 2): Executes the course enrolment process.
   * **Observe Alerts**: The *Live Alerts* panel on your right will poll your notification service and capture the asynchronous RabbitMQ event payload within 2 seconds.
   * **Observe Stats**: The *Course Registrations Stats* panel will increment its statistics.

### Method B: Automated Concurrency Load Test (R5 Verification)
To verify that your re-entrant locks protect your shared course seats from double-booking under heavy load, run:
```bash
./load-test.sh
```
*Expected Result*: 10 parallel requests are fired simultaneously. Exactly **3** requests will return `HTTP Status: 201` (successful registration), while the other **7** requests will be rejected with `HTTP Status: 400` with the error `Registration failed: Capacity limit reached.`

### Method C: Postman Collections
We have provided an exported Postman test suite in the root directory: **`SmartCampus_Connect.postman_collection.json`**.
1. Open Postman.
2. Click **Import** in the top-left corner, and upload the collection file.
3. You will see a folder-structured test suite to run manual requests and view raw XML SOAP responses and faults.

---

## 8. Clean Shutdown

Because our services run in the background, closing your terminal window will **not** stop them. If you attempt to recompile or run the project while they are still active, you will get port bind or file lock compilation errors.

To shut down your local environment completely, run:

```bash
# 1. Force close all active background Java processes on Windows
taskkill -F -IM java.exe

# 2. Stop the local RabbitMQ broker container
docker compose down
```

---

## 9. Project Team & Contributions

### 1. Darmendren A/L Thava Singh (B032410539)
*   **Student Profile Service**: Developed the REST CRUD API for student demographic records.
*   **Course Enrolment Service**: Developed the core enrolment logic, including the re-entrant concurrency locks (`ReentrantLock`) and the REST-based synchronous validation client.
*   **Failure Resilience**: Programmed the timeout and fallback/graceful degradation methods.
*   **Automated Scripts**: Created the `run-stack.sh` execution script and the `load-test.sh` concurrency testing harness.

### 2. Lavanyaa A/P Selapan (B032410529)
*   **Library / Booking Service**: Developed the legacy SOAP XML endpoint, custom JAX-B request/response structures, and the structured SOAP Fault error-mapping logic.
*   **Unified Web Dashboard**: Programmed the single-page HTML client (`dashboard.html`), integrating the vanilla JavaScript REST fetch calls and the DOM XML SOAP parser.
*   **CORS Filters**: Configured cross-origin access filters on both REST and SOAP servlet contexts so the browser could query the services.

### 3. Azwalidiya Binti Azman (B032410884)
*   **Notification Service**: Developed the decoupled background message listener that processes incoming RabbitMQ broker events.
*   **Reporting / Analytics Service**: Developed the CQRS read-model aggregator that compiles registration statistics asynchronously.
*   **RabbitMQ Message Broker**: Configured the Topic Exchange, queues, routing keys, and JSON serialization format.
*   **Postman Collection**: Created and exported the JSON test collection (`SmartCampus_Connect.postman_collection.json`).
```
```
