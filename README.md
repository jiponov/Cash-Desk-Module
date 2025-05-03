# 💰 Cash Desk Module

A Spring Boot application that simulates basic cashier operations such as deposits, withdrawals, and cash balance management. Data is stored in `.txt` files for transparency and simplicity.

## 🚀️ Installation & Running Instructions

**Requirements:** Java 8 & Maven installed

### 1. Clone the repository
git clone 
https://github.com/jiponov/Cash-Desk-Module.git

### 2. Build the application
mvn clean install

### 3. Run the application
java -jar target/application-0.0.1-SNAPSHOT.jar

✅ When you see: Started Application in 1.158 seconds (JVM running for 1.596), it means the server is successfully running and you're ready to go!

✅ This means that the application runs locally on:
http://localhost:8080

### 4. Using Postman

**1.** Start the app Postman.

**2.** Import the collection:
`Cash Desk Module.postman_collection.json`

**3.** Select collection and run the requests in order: 1️⃣, 2️⃣, 3️⃣, 4️⃣, 5️⃣  

🧷 Collection includes:
* 2 deposit requests
* 2 withdrawal requests
* 1 balance check request

🔎 Additional test cases:
* ❌ Invalid API key → 401 Unauthorized
* ❌ Invalid amounts / missing fields → 400 Bad Request

### Optional: Run from IDE (IntelliJ / Eclipse)
**1.** Open the project in IntelliJ or Eclipse

**2.** Run the class:
Application.java

**3.** Done! Open Postman and test 🎯

---

## 🧾 Features

- Accepts **cash deposits and withdrawals** operations via REST API.
- Maintains and updates **cash balance** in a `.txt` file.
- Each operation is recorded into **transaction history** in a `.txt` file.
- Input is automatically validated using standard **Java validation annotations** like @NotBlank, @DecimalMin, and @Valid.
- Secured with an **API key** (`FIB-X-AUTH` header).
- Includes a **Postman collection** with pre-defined test requests.

---

## 🚀 Technologies

- Java 8
- Spring Boot 2.6.4
- Maven
- ModelMapper
- Spring Web
- Hibernate Validator (Bean Validation)
- Lombok

---

## 📂 Project Structure

```

app/
├── model/              → Domain models: Cashier, Balance, Operation
├── web/                → Controllers and DTOs
├── service/            → Business logic (CashService)
├── initialize/         → Initial data loader (CommandLineRunner)
├── shared/             → Configuration & custom exceptions
└── resources/
├── application.properties
└── \*.txt files (cash balance, transaction history)

```

---

## 🔐 API Security

All endpoints require the `FIB-X-AUTH` header:

```

FIB-X-AUTH: f9Uie8nNf112hx8s

````

❌ If missing or incorrect → returns `401 Unauthorized`.

---

## 📮 Endpoints

| Method | Endpoint               | Description              |
|--------|------------------------|--------------------------|
| POST   | `/api/v1/cash-operation` | Deposit / Withdraw       |
| GET    | `/api/v1/cash-balance`   | View current balance     |

---

## 🗂️ Text Output Files

* `CASH-BALANCE.txt` – Cashier's daily balance and breakdown (BGN & EUR)
* `TRANSACTION-HISTORY.txt` – Log of each deposit and withdrawal

Both are **human-readable** and updated on each request.

---

## ✅ Task Checklist

* [x] Clean project structure
* [x] REST API with validation
* [x] Security via API key
* [x] Postman test collection included
* [x] No database required
* [x] .txt file persistence
* [x] Logging and exception handling
* [x] Uses Java 8 + Maven

---

## 🧑‍💻 Author

Created with care as part of a Java/Spring Boot training exercise.
Designed to reflect real-world cashier operations using clean and structured Spring Boot practices.

---

## 📃 License

This project is for educational/demo purposes.
Open source. You’re free to use and modify it as needed.