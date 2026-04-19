# A Test Driven Development (TDD) approach to learning REST in Spring Boot

This repository contains a simple Spring Boot application that demonstrates how to build a RESTful API using Test Driven Development (TDD). The application is a basic CRUD (Create, Read, Update, Delete) service for managing a list of cashcards.


## Cashcards
A cashcard is a simple object that has an ID, a name, and a balance. The API allows you to create new cashcards, retrieve existing cashcards, update cashcards, and delete cashcards.

## Prerequisites
- Java 17 or higher
- Maven
- An IDE (like IntelliJ IDEA or Eclipse) 
- Postman or any API testing tool
- Git
- Docker (optional, for running the application in a container)
- A web browser (for accessing the API documentation)

## Getting Started

### Spring Initializr
#### Project Metadata
- Group: com.wilsonks1982
- Artifact: rest-api-cashcard
- Name: Cashcard API
- Description: A simple RESTful API for managing cashcards using Spring Boot and TDD
- Package Name: com.wilsonks1982.cashcard2
- Packaging: Jar
- Java: 17
#### Create a new Spring Boot project using Spring Initializr with the following dependencies:
- Spring Web
- Spring Data JDBC
- Spring Security
- H2 Database
- Lombok

### Test Driven Development (TDD) Approach

#### Unit Tests
##### CashCard Record
##### CashCard JSON Serialization/Deserialization

#### Integration Tests
##### Testing the GET API
##### Testing the POST API
##### Testing the PUT API
##### Testing the DELETE API


### Running the Application

### Building the Application

### Deploying the Application

### To Do
#### Remove hardcoded credentials → Use environment variables
#### Replace In-Memory Users with Database
#### Implement Role-Based Access Control (RBAC)
