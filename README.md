# Finance

## 📖 Project Overview
A finance manager web app to help you keep track of your expenses. Built with Angular + Java (Springboot) + PostgreSQL.

## 🚀 Quick Start Guide

### **Prerequisites**
```bash
# Required Software
- Java 25+                    # JDK for Spring Boot
- Maven 3.8+                 # Build tool
- MySQL 8.0+                 # Relational database
- MongoDB 7.0+               # NoSQL database
- Git 2.40+                  # Version control
```

### **1. Clone & Setup**
```bash
# Clone repository
git clone https://github.com/rhondadavid/finance-java-test.git
cd finance-java-test

# Build project
mvn clean install
```

### **2. Configure Environment**
```.env.example
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_HOST=localhost
POSTGRES_DATABASE=finances
POSTGRES_PORT=5432
POSTGRES_CREATE_DROP=update

CORS_ALLOWED_ORIGINS=http://localhost:4200
SWAGGER_ENABLED=false

JWT_EXPIRATION_TIME=3600000
SECRET_KEY=mysecretkey
```
You can customize here.

### **3. Run Application**
```bash
# Using Maven
mvn spring-boot:run

```