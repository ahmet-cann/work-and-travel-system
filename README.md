# Work and Travel Student Management System ✈️

A professional relational database-driven desktop application developed in Java, designed to manage the complex workflow of the Work and Travel (WAT) program.

## 🚀 Overview
This project provides a comprehensive graphical user interface (GUI) and a robust Microsoft SQL Server back-end to coordinate university students, consultancy agencies, U.S.-based employers, and housing providers. It was developed collaboratively by our team as the final group project for the COM2058 Database Management Systems course.

## ✨ Key Features
* **Normalized Schema (3NF):** A 9-table relational database architecture ensuring strict referential integrity.
* **Real-Time Concurrency:** Transactional JDBC logic prevents a single job offer from being filled by multiple students simultaneously.
* **Dynamic Student Dashboard:** Live rendering of visa status, job placements, and housing assignments using multi-table `LEFT JOIN` queries.
* **Graceful Degradation:** Built-in `isServerAvailable()` probing that safely alerts the user instead of crashing when the database server is offline.
* **Rich Analytical Queries:** Features advanced T-SQL operations including `NOT EXISTS` subqueries and `GROUP BY` aggregations.

## 🛠️ Tech Stack
* **Language:** Java (JDK 21)
* **GUI Framework:** Java Swing
* **Database:** Microsoft SQL Server Express 2022
* **Driver:** `mssql-jdbc` (v12.6.0)
* **Build Tool:** Apache Maven

## ⚙️ Getting Started

### Prerequisites
* Java Development Kit (JDK) 21 or higher.
* Microsoft SQL Server (configured for SQL Server Authentication).
* Apache Maven.

### Running the Application
1. Clone the repository:
   ```bash
   git clone [https://github.com/ahmet-cann/work-and-travel-system.git](https://github.com/ahmet-cann/work-and-travel-system.git)