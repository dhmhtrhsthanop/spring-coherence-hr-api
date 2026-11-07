# High-Performance Spring Boot REST API with Oracle Coherence

A cloud-native, high-performance Human Resources REST API built with **Spring Boot**. This project demonstrates advanced enterprise patterns, utilizing **Oracle Coherence** on **Kubernetes** for distributed caching (Cache-Aside pattern) and a dockerized **Oracle Database XE** initialized automatically via Infrastructure-as-Code.

## 🚀 Architecture

This project strictly separates the persistence layer, the distributed caching tier, and the application client:

* **Backend Framework:** Spring Boot (Java), Spring Data JPA, RESTful API
* **Distributed Cache:** Oracle Coherence CE (Deployed on Kubernetes)
* **Database:** Oracle Database XE 21c (Running on Docker)
* **Infrastructure as Code:** Docker Compose,  K8s Manifests
* **Build Tool:** Maven

## 🛠️ Prerequisites
Before you begin, ensure you have the following installed:

* Java 21
* Maven (or use the included mvnw wrapper)
* Docker Desktop (with Kubernetes enabled)
* kubectl command-line tool
* Your favourite IDE (exp. Intellij)

# Getting Started
Follow these steps to run the complete infrastructure and application locally.

## Project Installation

To get a local copy up and running, follow these simple steps:

* Clone the repository

      git clone [https://github.com/dhmhtrhsthanop/spring-coherence-hr-api.git](https://github.com/dhmhtrhsthanop/spring-coherence-hr-api.git)
   
* Navigate to the project directory

      cd spring-coherence-hr-api
  
## Database Setup
We utilize the official Oracle XE 21c Docker image and mount a custom initialization script (init-hr-schema.sql) to add HR user and Employee/Departement/Job tables.

* Run the Docker Compose file while on the Infrastructure Folder:
    
      cd infrastructure
      docker-compose up -d

* When the Container is ready, access the SQL+ with:
    
      docker exec -it oracle-xe2 sqlplus sys/Oracle123 as sysdba

* Lastly, run the sql spript with:

      @/opt/oracle/scripts/setup/init-hr-schema.sql

Database is now running and registrations for the HR user are available.

## Cache Setup
This project utilizes **Oracle Coherence CE** for a highly available distributed caching tier, managed natively on Kubernetes via the **Coherence Operator**.

* Install and Set up the Coherence Operator and make sure it creates a namespace called **coherence**: 

      https://oracle.github.io/coherence-operator/docs/latest/#/docs/about/03_quickstart

Instead of embedding the cache within the Spring Boot client, the caching cluster runs independently.
The cluster size, operational settings, and cache configurations are defined declaratively in the `infrastructure/coherence-cluster.yaml`.


* While on infrastructure folder, apply the cache configuration:

      kubectl apply -f cache-config.yaml

* Then, apply the coherence cluster configuration:

      kubectl apply -f coherence-cluster.yaml

* You can review the data cluster creation with:

      kubectl get pods -n coherence -w

* To access the cache and start connection on a port (exp. 20000):

      kubectl port-forward svc/data-cluster-extend 20000:20000 -n coherence

## Configuration Setup
The application relies on standard local configurations and is ready to use with **Deafult Database Credentials**. 
Ensure you update the Database Username and Password on the application.yaml properties to either deafult or your own.

(Recommended) Use **Deafult Credentials** on application.yaml.

      datasource:
          url: jdbc:oracle:thin:@localhost:1521/XEPDB1
          username: # YOUR_USERNAME / DEFAULT IS: hr
          password: # YOUR_PASSWORD / DEFAULT IS: HRpass

(Optional) To use your **Own Database Credentials**, make sure you also update infrastructure/init-hr-schema.sql:

      CREATE USER YOUR_USERNAME IDENTIFIED BY YOUR_PASSWORD;
      GRANT CONNECT, RESOURCE TO YOUR_USERNAME;
      ALTER USER YOUR_USERNAME QUOTA UNLIMITED ON USERS;
      ALTER SESSION SET CURRENT_SCHEMA = YOUR_USERNAME;

## Build the Springboot App

The Spring Boot Maven plugin compiles the code and immediately starts the embedded server in a single, convenient step.

Using global Maven:

    mvn spring-boot:run
    
Or using the included Maven Wrapper (if you don't have Maven installed):

    ./mvnw spring-boot:run

## 📖 Using the API (Swagger)
Once the Spring Boot application is running, you can explore and test the endpoints directly from your browser:

Swagger UI: http://localhost:8080/swagger-ui.html