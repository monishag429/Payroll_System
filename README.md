# Enterprise Employee and Payroll Management Platform

## Overview
A Core Java console application that stores employee records, calculates payroll for different employment types, detects invalid records, and generates reports.

## Requirements
- Java Development Kit (JDK) 8 or later
- Any editor/IDE: IntelliJ IDEA, Eclipse, VS Code, or a terminal

## Files
- `EmployeePayrollApp.java` — complete runnable source. The supporting classes are in the same file to make compilation and submission straightforward.

## Run from terminal
1. Open a terminal in this folder.
2. Compile:
   ```bash
   javac EmployeePayrollApp.java
   ```
3. Run:
   ```bash
   java EmployeePayrollApp
   ```

In an IDE, create a Java project, add `EmployeePayrollApp.java`, and run `EmployeePayrollApp`.

## Sample salary policy (illustrative)
These rates are examples because the problem statement does not provide official business rules.

| Employee type | Bonus | Deductions |
|---|---:|---:|
| Permanent | 10% of base salary | 12% of gross salary |
| Contract | 5% of base salary | 5% of gross salary |
| Intern | 0% | 0% |

Net salary = gross salary - deductions.
Gross salary = base salary + type-specific bonus.

## Menu
1. Add employee
2. List all employees
3. Search employee by ID
4. Filter by department
5. Payroll report sorted by net salary
6. Department-wise report in alphabetical department order
7. Rank employees by net salary
8. Identify invalid employee records
9. Process payroll in batches
10. Demonstrate overloaded bonus methods
0. Exit

The app starts with four sample employees so the reports can be demonstrated immediately. Duplicate IDs are rejected.

## Requirements mapped to Java
- **Abstraction:** `Employee` is abstract.
- **Encapsulation/access modifiers:** fields are `private final`; getters expose read-only data.
- **Constructor chaining:** the convenience `Employee` constructor calls `this(...)`; subclasses call `super(...)`.
- **Inheritance:** `PermanentEmployee`, `ContractEmployee`, and `Intern`.
- **Interface:** `Payable`.
- **Runtime polymorphism:** the application stores subclass instances as `Employee` and calls overridden salary methods.
- **Overloading:** `calculateBonus(double percentage)` and `calculateBonus(double fixedAmount, boolean performanceTargetMet)`.
- **Collections:** `HashMap` for ID lookup, `ArrayList` for collection/report processing, `TreeMap` for sorted department reports, `TreeSet` for net-salary ranking.
- **Exceptions:** `InvalidSalaryException` is checked; `DuplicateEmployeeException` extends `RuntimeException`; invalid arguments and invalid calculated state use unchecked exceptions.
- **Generics:** `EmployeeRepository<T extends Employee>`.
- **Batch processing:** payroll is processed in chunks of 100.
- **Control flow:** `Scanner`, loops, `switch`, `break`, and `continue`.

## Stack, heap, and garbage collection
- **Stack:** each method call creates a stack frame containing local variables and references. Frames are removed when methods return.
- **Heap:** employee instances, collection objects, and strings are allocated on the heap. The repository holds references to employees.
- **Garbage collection:** objects become eligible for collection when they are no longer reachable. The repository intentionally retains active employee objects; temporary list copies and report collections can be collected after use.
- **Avoiding unnecessary objects:** payroll batches reuse the existing employee objects and calculate totals directly instead of creating a second payroll object for each employee. `findAll()` returns one list copy so report sorting does not mutate repository order.
- **Scalability note:** this demonstration stores data in memory; records reset when the program exits. A production platform would normally add a database, audit logs, authorization, configurable payroll rules, payslip persistence, and automated tests.

## Suggested demo
1. Run the program and select option 5 to show the sample payroll.
2. Select option 6 to show sorted department reports.
3. Search for `E1001` with option 3.
4. Add a new employee, then try adding the same ID again to demonstrate duplicate detection.
5. Enter a non-positive salary to demonstrate checked salary validation.
6. Use option 7 to demonstrate net-salary ranking.
7. Use option 9 to demonstrate batch processing.

## Important note
This is an educational Core Java project using illustrative payroll rates, not a legally compliant payroll engine. Real payroll needs organization-specific policies, statutory tax/benefit rules, rounding standards, and secure persistence.
