# Shopping Cart Application with Database Localization

## Overview
A JavaFX shopping cart application that stores UI localization strings in a database and saves cart calculations to MySQL.

## Features
- Multi-language support (English, Finnish, Swedish, Japanese, Arabic)
- Dynamic item generation
- Cart total calculation
- Database storage for cart records and items
- Localized UI messages from database

## Prerequisites
- Java 17 or higher
- MySQL 8.0+
- Maven

## Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/bhandari-sachin/shopping-cart-localizationv3.git
cd shopping-cart-localization
```
## 2. Database Setup
# start mysql
mysql -u root -p
# run schema script
source database-schema.sql;

### 3. Configure Database Connection
set environment variables for database connection:
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=shopping_cart_localization
export DB_USER=root
export DB_PASSWORD=yourpassword
```

### 4. Build and Run the Application
```bash
mvn clean compile
mvn javafx:run

## Docker Setup (Optional)
### 1. Build Docker Image
```bash
docker build -t shopping-cart-localization .
```
### 2. Run Docker Container
```bash
docker run -e DB_HOST=host.docker.internal -e DB_PASSWORD=yourpassword shopping-cart