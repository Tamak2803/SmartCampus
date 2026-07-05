# SmartCampus Connect: Project Handover & Local Setup Guide

This guide is prepared to help you set up, compile, run, and verify the **SmartCampus Connect** distributed platform on your local development machine.

Please follow the verification and installation checklists carefully before attempting to launch the services to prevent runtime conflicts.

## 1\. System Dependencies & Prerequisites Checklist

Our distributed platform runs on **Java 21**, uses **Maven** for dependency orchestration, and relies on **RabbitMQ** (via **Docker**) for asynchronous event routing. On Windows, **Git Bash** is required to run our automation shell scripts (.sh).

### 1.1 Git Bash (For Windows Users)

Since Windows Command Prompt does not natively support Unix shell scripts (.sh), we use **Git Bash** to compile and run the backend stack.

#### How to Check

Press the Windows Key, type **"Git Bash"**, and see if the application appears. Alternatively, in your current terminal, run:

git --version

- **Correct Output**: Returns a version number like git version 2.x.x.windows.1.
- **If Missing**:

1\. Download the **Git for Windows** installer from the official Git-SCM Download Page.

2\. Launch the installer executable (e.g., Git-2.x.x-64-bit.exe).

3\. During the installation setup:

- - - Ensure **"Git Bash"** is selected under the _Components_ checklist.
      - Keep the default configurations for the terminal emulator and line-ending conversions (recommended to select _Checkout Windows-style, commit Unix-style line endings_ to prevent CRLF file-lock errors on Windows).

4\. Click **Install** to finish.

5\. You can now right-click anywhere in your Windows File Explorer and select **"Open Git Bash here"** to launch the terminal pre-navigated to your active folder.

### 1.2 Java Development Kit (JDK 21)

The project is compiled to target **Java 21**. Standard older versions (such as Java 8, 11, or 17) will cause immediate compilation or runtime class version crashes.

#### How to Check

Open your newly installed **Git Bash** terminal and run:

java -version

- **Correct Output**: You should see java version "21.x.x" or higher.
- **If Missing or Incorrect**:

1\. Download the **Eclipse Temurin JDK 21** installer from Adoptium OpenJDK 21.

2\. During the Windows installation wizard, make sure to check the boxes for:

- - - **Add to PATH**
      - **Set JAVA_HOME variable**

3\. Restart your Git Bash terminal and run java -version again to confirm.

### 1.3 Docker Desktop (For RabbitMQ Broker)

We use a Docker container to host our RabbitMQ message broker. You do not need to install RabbitMQ natively on your operating system, but you must have Docker running.

#### How to Check

Verify that Docker is configured in your system command line:

docker --version  
docker compose version

- **Correct Output**: Both commands should return active version numbers.
- **If Missing or Incorrect**:

1\. Download and install **Docker Desktop for Windows** from Docker's Official Site.

2\. Open Docker Desktop and ensure the engine has started (the status icon in the bottom-left corner of the Docker Desktop UI must turn solid **green**).

### 1.4 Apache Maven (For Compiling and Packaging)

The parent project orchestrates and packages the 5 microservice modules using Maven.

#### How to Check

mvn -version

- **Correct Output**: Returns the Maven version and links it to your active JDK 21 installation.
- **If Missing**: You can download it from Apache Maven and add its /bin directory to your System PATH variables, or use your IDE's embedded Maven wrapper.

## 2\. Directory Layout & Module Ownership

The repository is structured as a single Maven parent repository containing 5 autonomous backend modules:

smartcampus-connect/  
├── pom.xml <-- Parent Maven POM (defines modules)  
├── docker-compose.yml <-- Starts the RabbitMQ Broker  
├── run-stack.sh <-- One-click build & startup script  
├── load-test.sh <-- Multi-threaded lock test script  
├── dashboard.html <-- HTML Management Console  
├── SmartCampus_Connect.postman_collection.json  
├── library.wsdl  
├── student-service/ <-- REST (Port 8081)  
├── enrolment-service/ <-- REST (Port 8082)  
├── library-service/ <-- SOAP (Port 8083)  
├── notification-service/ <-- AMQP Consumer & REST API (Port 8084)  
└── analytics-service/ <-- CQRS Analytics Dashboard (Port 8085)

## 3\. Local Setup & Project Execution

On Windows, **Git Bash (MINGW64)** is the required terminal for running the build and startup pipelines.

### Step 1: Open Terminal

Right-click inside your project directory in Windows Explorer and select **"Open Git Bash here"** (or open Git Bash and navigate to the project manually):

cd /path/to/smartcampus-connect

### Step 2: Grant Script Permissions (First-time only)

Ensure the helper shells are allowed to execute:

chmod +x run-stack.sh  
chmod +x load-test.sh

### Step 3: Launch the Infrastructure & Services

Run the main startup script:

./run-stack.sh

_This automated script compiles all code modules, pulls and starts the RabbitMQ Docker container, pauses for 10 seconds to allow the RabbitMQ broker to accept sockets, and then boots all 5 microservices as background processes, redirecting console outputs to local \`.log\` files._

## 4\. Verifying the Local Deployment

To verify that your local environment is correctly configured and that all background services successfully loaded without port binds or database exceptions:

### 1\. Check Active Processes

Run this native Windows command inside your Git Bash terminal to verify that your system is running the active Java threads:

powershell -Command "Get-Process -Name java"

_You should see a list of multiple active Java processes with distinct Process IDs (PIDs)._

### 2\. View Service Logs

If you need to debug a service or confirm its boot status, inspect its redirect log file. For example:

cat student-service.log

_Expected output: Standard Spring Boot startup logs concluding with: \`Tomcat started on port 8081\`._

## 5\. Visual Demonstration (Dashboard Testing)

Once the local platform is validated as online, you can test the entire transactional distributed workflow using your browser:

1\. Open your Windows File Explorer and navigate into the smartcampus-connect/ folder.

2\. Double-click **\`dashboard.html\`** to load the console in your web browser \[1\].

3\. Test the workflow in this order:

- - **Create Profile** (Form 1): Generates a student profile inside student_db.
    - **SOAP Book Loan** (Form 3): Sends an XML payload to the legacy service on Port 8083. If you type FAIL as the student ID, you will see a structured **SOAP Fault** displayed on the screen.
    - **Submit Enrolment** (Form 2): Executes the course enrolment process.
    - **Observe Alerts**: The _Live Alerts_ panel on your right will poll your notification service and capture the asynchronous RabbitMQ event payload within 2 seconds.
    - **Observe Stats**: The _Course Registrations Stats_ panel will increment its statistics.

## 6\. How to Shut Down the Project Locally

Because the Java engines run as detached background processes, closing your terminal window will **not** stop them. If you attempt to recompile or run the project while they are still running, you will get port bind or file lock compilation errors.

To shut down your local workspace completely, run:

\# 1. Force close all active background Java processes on Windows  
taskkill -F -IM java.exe  
<br/>\# 2. Stop the local RabbitMQ container  
docker compose down