Here is a complete, production-ready **`README.md`** file tailored specifically for your GitHub repository at `https://github.com/Tamak2803/SmartCampus.git`. 

It is designed to present your architectural design, port allocation, local setup steps, and team contributions to your assessors in a clean, professional, and academically rigorous format.

You can save this content directly as `README.md` in the root folder of your project.

---

```markdown
# SmartCampus Connect

SmartCampus Connect is a decentralized, microservices-based backend platform designed to deliver core campus services to a university environment. The application brings together distributed system design methodologies introduced in Weeks 1 through 7: strict database-per-service isolation, synchronous RESTful orchestration, asynchronous AMQP choreography, multi-threaded concurrency locking, legacy SOAP interoperability, and circuit-breaker failure tolerance.

---

## 1. System Architecture

The platform uses a hybrid architectural pattern combining **Synchronous REST-based Orchestration** (for transactional boundaries requiring real-time verification) and **Asynchronous AMQP-based Choreography** (for decoupled side-effects like background alerting and reporting stats).

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

## 2. Port Allocation & Endpoint Registry

Each service is compiled independently, runs in an isolated JVM, and maintains its own in-memory database to satisfy the **Database-per-Service (R3)** architectural constraint.

| Service Name | Port | Protocols | Database | Core Responsibilities |
| :--- | :--- | :--- | :--- | :--- |
| **Student Profile Service** | `8081` | REST / JSON | H2 (`student_db`) | Student demographic data CRUD. |
| **Course Enrolment Service** | `8082` | REST / JSON | H2 (`enrolment_db`) | Semester enrolment, capacity checks, and eligibility checks. |
| **Library / Booking Service** | `8083` | SOAP / XML | H2 (`library_db`) | Book loans and room bookings (Legacy SOAP Endpoint). |
| **Notification Service** | `8084` | AMQP / REST | *None* | Background asynchronous listener for system events. |
| **Reporting / Analytics Service** | `8085` | REST / JSON | H2 (`analytics_db`) | CQRS aggregate read-model compiling course stats. |

---

## 3. Technology Stack & Mandatory Requirements

Every requirement specified in the grading rubric is implemented across our codebase:
*   **R1: System Characterisation**: Explains location, access, concurrency, and failure transparencies in the final technical report.
*   **R2: Architectural Pattern Selection**: Implements a Multi-Tier Client-Server structure combined with CQRS read-side data replication.
*   **R3: SOA Principles**: Encapsulates 5 independently-deployable modules with database-per-service data isolation.
*   **R4: Service Composition**: Implements synchronous REST orchestration (Enrolment checking Student Profile) paired with asynchronous choreography (RabbitMQ events).
*   **R5: Multithreaded Server**: Implements a custom thread pool (`ExecutorService`) and `ReentrantLock` boundaries per course key to prevent over-enrolment race conditions.
*   **R6: Distributed Messaging**: Implements asynchronous routing over RabbitMQ using a Topic Exchange (`exchange.smartcampus`).
*   **R7: REST API**: Exposes JSON REST APIs with correct HTTP verbs and semantic status codes (`201`, `200`, `400`, `404`).
*   **R8: SOAP Service**: Exposes a JAX-WS compliant SOAP endpoint generating a WSDL contract and handling structured SOAP Faults.
*   **R9: Failure Handling**: Integrates client-side timeouts and circuit-breaker fallbacks allowing graceful degradation.
*   **R10: Version Control & Build**: Executed via a single parent Maven compile cycle and launched with a single shell script.

---

## 4. Prerequisites & Local Installation

Before attempting to boot the platform locally, ensure that your development machine has the following tools installed and configured:

### 1. Git Bash (Required for Windows Users)
Since Windows Command Prompt does not natively support Unix shell scripts (`.sh`), Windows users must run our startup pipelines using **Git Bash**.
*   Verify installation: `git --version`
*   If missing, download from: [Git SCM Windows](https://git-scm.com/download/win)

### 2. JDK 21 (Required)
The codebase uses Java 21 compilation targets. Standard older environments (Java 8, 11, or 17) will fail to run the modules.
*   Verify installation: `java -version`
*   If missing, download from: [Eclipse Temurin OpenJDK 21](https://adoptium.net/temurin/releases/?version=21)

### 3. Docker Desktop (Required)
Our asynchronous event broker relies on RabbitMQ hosted within a local Docker container.
*   Verify installation: `docker compose version`
*   If missing, download from: [Docker Desktop](https://www.docker.com/products/docker-desktop/)
*   **Note**: Ensure Docker Desktop is open and that the engine status bar in the bottom-left corner is **green** (running) before launching the services.

---

## 5. Running the Project Locally

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
*This command runs a clean Maven build, packages the binaries into executable JARs, spins up the RabbitMQ Docker container, pauses for 10 seconds to allow the broker to accept sockets, and boots all 5 backend engines as background processes.*

### Step 4: Verify Background Processes are Active
Check your system thread pool using the native Windows utility via Git Bash:
```bash
powershell -Command "Get-Process -Name java"
```
*You should see a table listing multiple active Java processes with distinct Process IDs (PIDs).*

---

## 6. How to Verify & Demo

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

## 7. Clean Shutdown

Because our services run in the background, closing your terminal window will **not** stop them. If you attempt to recompile or run the project while they are still active, you will get port bind or file lock compilation errors.

To shut down your local environment completely, run:

```bash
# 1. Force close all active background Java processes on Windows
taskkill -F -IM java.exe

# 2. Stop the local RabbitMQ broker container
docker compose down
```

---

## 8. Project Team & Contributions

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
