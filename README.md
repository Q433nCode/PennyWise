# PennyWise — Personal Finance Manager

A beginner-friendly Java Swing desktop project for tracking income, expenses, and a monthly budget.

## Features
- Dashboard showing total income, total expenses, and available balance.
- Add income and expense transactions.
- Search transactions and filter by type.
- Delete selected transactions.
- Spending totals by category for the current month.
- Set an overall monthly spending budget.
- Saves transaction data locally in `pennywise-transactions.csv`.
- Saves the monthly budget in `pennywise-budget.txt`.
- Uses `BigDecimal` for monetary values.
- Uses Java Swing only; no external dependencies.

## Requirements
- JDK 8 or newer (JDK 17 or 21 recommended).
- Windows, macOS, or Linux.

## Run in VS Code
1. Install a JDK.
2. Install Visual Studio Code and the **Extension Pack for Java**.
3. Extract this project folder.
4. In VS Code, choose **File → Open Folder** and open the `PennyWise` folder.
5. Open `src/PennyWise.java`.
6. Click **Run** above the `main` method, or use the terminal commands below.

## Run in a terminal
Open a terminal inside the `PennyWise` folder.

Check Java:
```bash
java -version
javac -version
```

Compile:
```bash
javac -encoding UTF-8 -d out src/PennyWise.java
```

Run:
```bash
java -cp out PennyWise
```

On Windows, these commands work in Command Prompt or PowerShell as long as Java is on your PATH. In NetBeans, create a Java Application project and copy `PennyWise.java` into the source package (or use the IDE's Open Project option if you create a project wrapper).

## Data files
The app creates `pennywise-transactions.csv` and `pennywise-budget.txt` in the folder from which it is launched. Back up these files if you want to keep your data.

## OOP concepts demonstrated
- **Class and objects:** `PennyWise`, `Transaction`, `IncomeTransaction`, `ExpenseTransaction`.
- **Constructors:** initialize transaction data.
- **Encapsulation:** transaction fields are private and exposed through getters.
- **Inheritance:** income and expense transactions extend `Transaction`.
- **Polymorphism:** the app stores both subclasses in a `List<Transaction>` and handles them through the common parent type using `instanceof`.
- **Abstraction:** `Transaction` is abstract.
- **Loops and collections:** iterate over transactions and store them in an `ArrayList`.
- **Conditionals:** validate amounts, choose transaction types, and filter data.
- **Exception handling:** handle invalid amounts/dates and file errors.
- **File handling:** persist transactions and budget between runs.

## Good next improvements
- Add unit tests with JUnit.
- Add monthly date-range filters and CSV export.
- Add per-category budgets and a spending chart.
- Split classes into separate files/packages.
- Add an edit-transaction feature.
- Add an SQLite database when you're ready to learn JDBC.
