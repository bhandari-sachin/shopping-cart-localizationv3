# Shopping Cart Application with Database Localization

## Overview
A JavaFX shopping cart application that stores UI localization strings in a database and saves cart calculations to MySQL. The project uses SonarQube and JaCoCo for static code analysis and test coverage reporting.

---

## Table of Contents
1. [Features](#features)
2. [Prerequisites](#prerequisites)
3. [Setup Instructions](#setup-instructions)
4. [Code Quality Analysis (SonarQube)](#code-quality-analysis-sonarqube)
5. [Docker Setup](#docker-setup-optional)

---

## Features
- Multi-language support (English, Finnish, Swedish, Japanese, Arabic)
- Dynamic item generation
- Cart total calculation
- Database storage for cart records and items
- Localized UI messages from database
- Static code analysis via SonarQube
- Test coverage reporting via JaCoCo

---

## Prerequisites
- Java JDK 17 or higher (SonarQube requires JDK 17 — ensure `JAVA_HOME` points to it)
- MySQL 8.0+
- Maven 3.6+
- SonarQube Community Edition (for code quality analysis)

---

## Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/bhandari-sachin/shopping-cart-localizationv3.git
cd shopping-cart-localization
```

---

### 2. Database Setup
```bash
# Start MySQL
mysql -u root -p

# Run schema script
source database-schema.sql;
```

---

### 3. Configure Database Connection
Set environment variables for the database connection:

**Linux / Git Bash:**
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=shopping_cart_localization
export DB_USER=root
export DB_PASSWORD=yourpassword
```

**PowerShell:**
```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "3306"
$env:DB_NAME = "shopping_cart_localization"
$env:DB_USER = "root"
$env:DB_PASSWORD = "yourpassword"
```

---

### 4. Build and Run the Application
```bash
mvn clean compile
mvn javafx:run
```

---

## Code Quality Analysis (SonarQube)

This project is configured to run SonarQube analysis via the `sonar-maven-plugin`. JaCoCo generates the test coverage report that SonarQube reads automatically.

### Prerequisites
- SonarQube Community Edition installed and running at `http://localhost:9000`
- Java JDK 17 set as `JAVA_HOME` (required by SonarQube)
- A valid SonarQube user token

---

### Step 1: Start SonarQube

**Windows:**
```cmd
cd C:\sonarqube\bin\windows-x86-64
StartSonar.bat
```

Wait until the console shows `SonarQube is operational`, then open:
```
http://localhost:9000
```
Default login: `admin / admin` (you will be prompted to change this on first login).

---

### Step 2: Generate a SonarQube Token
1. Go to `http://localhost:9000`
2. Click your avatar (top-right) → **My Account** → **Security**
3. Enter a token name (e.g. `mytoken`), select **User Token**, click **Generate**
4. **Copy the token immediately** — it will not be shown again

---

### Step 3: Set the Token as an Environment Variable

Set `SONAR_TOKEN` so your token is never exposed in plain text in commands.

**PowerShell (current session only):**
```powershell
$env:SONAR_TOKEN = "your_token_here"
```

**PowerShell (permanent — survives new sessions):**
```powershell
[System.Environment]::SetEnvironmentVariable("SONAR_TOKEN", "your_token_here", "User")
```

Verify it is set correctly:
```powershell
echo $env:SONAR_TOKEN
```

**Linux / Git Bash (current session):**
```bash
export SONAR_TOKEN="your_token_here"
```

**Linux / Git Bash (permanent):**
```bash
echo 'export SONAR_TOKEN="your_token_here"' >> ~/.bashrc
source ~/.bashrc
```

---

### Step 4: Run the Analysis

Make sure SonarQube is running at `http://localhost:9000`, then from your project root:

**PowerShell:**
```powershell
mvn clean verify sonar:sonar "-Dsonar.token=$env:SONAR_TOKEN"
```

**Linux / Git Bash:**
```bash
mvn clean verify sonar:sonar "-Dsonar.token=$SONAR_TOKEN"
```

> `clean` removes previous build artifacts, `verify` compiles and runs tests generating the JaCoCo coverage report, and `sonar:sonar` sends everything to SonarQube. The project key, host URL, and source paths are already configured in `pom.xml`.

---

### Step 5: View Results

Once the build completes with `BUILD SUCCESS`, open:
```
http://localhost:9000/projects
```
You will see your project listed with metrics for Security, Reliability, Maintainability, Coverage, and Duplications.

---

### Troubleshooting

| Problem | Fix |
|---|---|
| `Not authorized` error | Token is missing or wrong — re-check `echo $env:SONAR_TOKEN` and regenerate if needed |
| `BUILD FAILURE` before Sonar runs | Run `mvn clean verify` alone first to confirm tests pass |
| `localhost:9000` not loading | SonarQube takes 1–2 min to start — wait and refresh |
| Coverage shows `0.0%` | Ensure tests exist and `mvn verify` ran successfully before Sonar |
| Scanner fails with Java error | Confirm `JAVA_HOME` points to JDK 17 — run `java -version` to check |

---

## Docker Setup (Optional)

### 1. Build Docker Image
```bash
docker build -t shopping-cart-localization .
```

### 2. Run Docker Container
```bash
docker run -e DB_HOST=host.docker.internal -e DB_PASSWORD=yourpassword shopping-cart
```