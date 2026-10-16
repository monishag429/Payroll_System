import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Enterprise Employee and Payroll Management Platform
 * Core Java console application. Requires Java 8 or later.
 *
 * Example policy (illustrative; change to match an organization's rules):
 * Permanent: 10% bonus, 12% deductions
 * Contract: 5% bonus, 5% deductions
 * Intern: no automatic bonus, no deductions
 */
public class EmployeePayrollApp {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final EmployeeRepository<Employee> REPOSITORY = new EmployeeRepository<>();
    private static final int PAYROLL_BATCH_SIZE = 100;

    public static void main(String[] args) {
        seedSampleEmployees();
        boolean running = true;

        while (running) {
            printMenu();
            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1: addEmployee(); break;
                case 2: listEmployees(); break;
                case 3: searchEmployee(); break;
                case 4: filterByDepartment(); break;
                case 5: showPayrollReport(); break;
                case 6: showDepartmentReport(); break;
                case 7: showSalaryRanking(); break;
                case 8: showInvalidRecords(); break;
                case 9: runBatchPayroll(); break;
                case 10: demonstrateBonusOverloading(); break;
                case 0:
                    running = false;
                    System.out.println("Exiting Employee Payroll Platform. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid menu choice. Try again.");
                    continue;
            }
        }
        SCANNER.close();
    }

    private static void printMenu() {
        System.out.println("\n========== EMPLOYEE PAYROLL PLATFORM ==========");
        System.out.println("1. Add employee");
        System.out.println("2. List all employees");
        System.out.println("3. Search employee by ID");
        System.out.println("4. Filter employees by department");
        System.out.println("5. Payroll report (net salary descending)");
        System.out.println("6. Department-wise report (sorted departments)");
        System.out.println("7. Rank employees by net salary");
        System.out.println("8. Identify invalid employee records");
        System.out.println("9. Process payroll in batches");
        System.out.println("10. Demonstrate overloaded bonus methods");
        System.out.println("0. Exit");
        System.out.println("===============================================");
    }

    private static void seedSampleEmployees() {
        try {
            REPOSITORY.add(new PermanentEmployee("E1001", "Asha", "Engineering",
                    LocalDate.of(2022, 4, 11), 60000.00));
            REPOSITORY.add(new ContractEmployee("E1002", "Ravi", "Operations",
                    LocalDate.of(2024, 1, 8), 45000.00));
            REPOSITORY.add(new Intern("E1003", "Meena", "Engineering",
                    LocalDate.of(2025, 6, 2), 18000.00));
            REPOSITORY.add(new PermanentEmployee("E1004", "Kiran", "Finance",
                    LocalDate.of(2021, 9, 15), 75000.00));
        } catch (InvalidSalaryException | DuplicateEmployeeException ex) {
            System.out.println("Sample data could not be loaded: " + ex.getMessage());
        }
    }

    private static void addEmployee() {
        System.out.println("\nEmployee type: 1. Permanent  2. Contract  3. Intern");
        int type = readInt("Choose type: ");
        String id = readLine("Employee ID: ").trim();
        String name = readLine("Name: ").trim();
        String department = readLine("Department: ").trim();
        LocalDate joiningDate;
        try {
            joiningDate = LocalDate.parse(readLine("Joining date (YYYY-MM-DD): ").trim());
        } catch (DateTimeParseException ex) {
            System.out.println("Invalid date. Use YYYY-MM-DD.");
            return;
        }
        double baseSalary = readDouble("Monthly base salary (must be > 0): ");

        try {
            Employee employee;
            switch (type) {
                case 1: employee = new PermanentEmployee(id, name, department, joiningDate, baseSalary); break;
                case 2: employee = new ContractEmployee(id, name, department, joiningDate, baseSalary); break;
                case 3: employee = new Intern(id, name, department, joiningDate, baseSalary); break;
                default:
                    System.out.println("Unknown employee type.");
                    return;
            }
            REPOSITORY.add(employee);
            System.out.println("Employee added successfully.");
        } catch (InvalidSalaryException | DuplicateEmployeeException ex) {
            System.out.println("Could not add employee: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            System.out.println("Invalid employee data: " + ex.getMessage());
        }
    }

    private static void listEmployees() {
        List<Employee> employees = REPOSITORY.findAll();
        if (employees.isEmpty()) {
            System.out.println("No employees found.");
            return;
        }
        for (Employee employee : employees) {
            printEmployee(employee);
        }
    }

    private static void searchEmployee() {
        String id = readLine("Enter employee ID: ").trim();
        Employee employee = REPOSITORY.findById(id);
        if (employee == null) {
            System.out.println("No employee found for ID " + id);
        } else {
            printEmployee(employee);
            printPayroll(employee);
        }
    }

    private static void filterByDepartment() {
        String department = readLine("Department name: ").trim();
        List<Employee> matches = new ArrayList<>();
        for (Employee employee : REPOSITORY.findAll()) {
            if (employee.getDepartment().equalsIgnoreCase(department)) {
                matches.add(employee);
            }
        }
        matches.sort(Comparator.comparing(Employee::getName, String.CASE_INSENSITIVE_ORDER));
        if (matches.isEmpty()) {
            System.out.println("No employees in department " + department);
            return;
        }
        for (Employee employee : matches) printEmployee(employee);
    }

    private static void showPayrollReport() {
        List<Employee> employees = REPOSITORY.findAll();
        employees.sort(Comparator.comparingDouble(Employee::calculateNetSalary).reversed()
                .thenComparing(Employee::getId));
        System.out.println("\nID       Name                 Type       Gross       Deductions      Net");
        System.out.println("--------------------------------------------------------------------------");
        for (Employee employee : employees) printPayrollRow(employee);
    }

    private static void showDepartmentReport() {
        TreeMap<String, List<Employee>> departments = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Employee employee : REPOSITORY.findAll()) {
            departments.computeIfAbsent(employee.getDepartment(), key -> new ArrayList<>()).add(employee);
        }
        if (departments.isEmpty()) {
            System.out.println("No records available.");
            return;
        }
        for (Map.Entry<String, List<Employee>> entry : departments.entrySet()) {
            double totalNet = 0.0;
            System.out.println("\nDepartment: " + entry.getKey());
            for (Employee employee : entry.getValue()) {
                totalNet += employee.calculateNetSalary();
                printPayrollRow(employee);
            }
            System.out.printf(Locale.US, "Employees: %d | Total net payroll: %.2f%n",
                    entry.getValue().size(), totalNet);
        }
    }

    private static void showSalaryRanking() {
        TreeSet<Employee> ranking = new TreeSet<>(
                Comparator.comparingDouble(Employee::calculateNetSalary).reversed()
                        .thenComparing(Employee::getId));
        ranking.addAll(REPOSITORY.findAll());
        int rank = 1;
        for (Employee employee : ranking) {
            System.out.printf(Locale.US, "%d. %s (%s) - Net salary: %.2f%n",
                    rank++, employee.getName(), employee.getId(), employee.calculateNetSalary());
        }
    }

    private static void showInvalidRecords() {
        int invalidCount = 0;
        for (Employee employee : REPOSITORY.findAll()) {
            String problem = employee.validateRecord();
            if (problem != null) {
                invalidCount++;
                System.out.println(employee.getId() + ": " + problem);
            }
        }
        if (invalidCount == 0) {
            System.out.println("No invalid employee records found. Invalid data is also checked on entry.");
        } else {
            System.out.println("Invalid record count: " + invalidCount);
        }
    }

    private static void runBatchPayroll() {
        List<Employee> employees = REPOSITORY.findAll();
        if (employees.isEmpty()) {
            System.out.println("No employees to process.");
            return;
        }
        double totalNet = 0.0;
        int processed = 0;
        for (int start = 0; start < employees.size(); start += PAYROLL_BATCH_SIZE) {
            int end = Math.min(start + PAYROLL_BATCH_SIZE, employees.size());
            // Reuse the existing employee objects; do not create duplicate Employee objects per batch.
            for (int i = start; i < end; i++) {
                Employee employee = employees.get(i);
                totalNet += employee.calculateNetSalary();
                processed++;
            }
            System.out.printf("Processed batch %d (%d employees).%n",
                    (start / PAYROLL_BATCH_SIZE) + 1, end - start);
        }
        System.out.printf(Locale.US, "Payroll complete. Employees processed: %d | Total net payroll: %.2f%n",
                processed, totalNet);
    }

    private static void demonstrateBonusOverloading() {
        Employee employee = REPOSITORY.findById(readLine("Employee ID: ").trim());
        if (employee == null) {
            System.out.println("Employee not found.");
            return;
        }
        double percentage = readDouble("Bonus percentage (e.g., 8 for 8%): ");
        double fixedAmount = readDouble("Fixed bonus amount: ");
        System.out.printf(Locale.US, "Percentage bonus: %.2f%n", employee.calculateBonus(percentage));
        System.out.printf(Locale.US, "Fixed bonus: %.2f%n", employee.calculateBonus(fixedAmount, true));
    }

    private static void printEmployee(Employee employee) {
        System.out.printf("%-8s %-20s %-12s %-16s %-12s %10.2f%n",
                employee.getId(), employee.getName(), employee.getEmployeeType(),
                employee.getDepartment(), employee.getJoiningDate(), employee.getBaseSalary());
    }

    private static void printPayroll(Employee employee) {
        System.out.printf(Locale.US, "Gross: %.2f | Deductions: %.2f | Net: %.2f%n",
                employee.calculateGrossSalary(), employee.calculateDeductions(),
                employee.calculateNetSalary());
    }

    private static void printPayrollRow(Employee employee) {
        System.out.printf(Locale.US, "%-8s %-20s %-10s %10.2f %14.2f %12.2f%n",
                employee.getId(), employee.getName(), employee.getEmployeeType(),
                employee.calculateGrossSalary(), employee.calculateDeductions(),
                employee.calculateNetSalary());
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine();
    }

    private static int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt).trim());
            } catch (NumberFormatException ex) {
                System.out.println("Enter a valid whole number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            try {
                double value = Double.parseDouble(readLine(prompt).trim());
                if (!Double.isFinite(value)) {
                    System.out.println("Enter a finite numeric value.");
                    continue;
                }
                return value;
            } catch (NumberFormatException ex) {
                System.out.println("Enter a valid number.");
            }
        }
    }
}

interface Payable {
    double calculateGrossSalary();
    double calculateDeductions();
    double calculateNetSalary();
}

abstract class Employee implements Payable {
    private final String employeeId;
    private final String name;
    private final String department;
    private final LocalDate joiningDate;
    private final double baseSalary;

    protected Employee(String employeeId, String name, String department,
                       LocalDate joiningDate, double baseSalary) throws InvalidSalaryException {
        this.employeeId = requireText(employeeId, "Employee ID");
        this.name = requireText(name, "Name");
        this.department = requireText(department, "Department");
        this.joiningDate = Objects.requireNonNull(joiningDate, "Joining date cannot be null");
        if (!Double.isFinite(baseSalary) || baseSalary <= 0) {
            throw new InvalidSalaryException("Base salary must be a finite amount greater than zero.");
        }
        this.baseSalary = baseSalary;
    }

    // Constructor chaining: convenience constructor delegates to the main constructor.
    protected Employee(String employeeId, String name, String department, double baseSalary)
            throws InvalidSalaryException {
        this(employeeId, name, department, LocalDate.now(), baseSalary);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be blank.");
        }
        return value.trim();
    }

    public final String getId() { return employeeId; }
    public final String getName() { return name; }
    public final String getDepartment() { return department; }
    public final LocalDate getJoiningDate() { return joiningDate; }
    public final double getBaseSalary() { return baseSalary; }

    public abstract String getEmployeeType();
    protected abstract double getBonusRate();
    protected abstract double getDeductionRate();

    // Overloaded bonus methods: percentage-based and fixed-amount bonus.
    public double calculateBonus(double percentage) {
        if (!Double.isFinite(percentage) || percentage < 0) {
            throw new IllegalArgumentException("Bonus percentage cannot be negative or non-finite.");
        }
        return baseSalary * percentage / 100.0;
    }

    public double calculateBonus(double fixedAmount, boolean performanceTargetMet) {
        if (!Double.isFinite(fixedAmount) || fixedAmount < 0) {
            throw new IllegalArgumentException("Fixed bonus cannot be negative or non-finite.");
        }
        return performanceTargetMet ? fixedAmount : 0.0;
    }

    @Override
    public double calculateGrossSalary() {
        return baseSalary + calculateBonus(getBonusRate() * 100.0);
    }

    @Override
    public double calculateDeductions() {
        return calculateGrossSalary() * getDeductionRate();
    }

    @Override
    public double calculateNetSalary() {
        double net = calculateGrossSalary() - calculateDeductions();
        if (net < 0 || !Double.isFinite(net)) {
            throw new IllegalStateException("Calculated net salary is invalid for " + employeeId);
        }
        return net;
    }

    public String validateRecord() {
        if (employeeId.trim().isEmpty()) return "Employee ID is blank.";
        if (name.trim().isEmpty()) return "Name is blank.";
        if (department.trim().isEmpty()) return "Department is blank.";
        if (joiningDate == null) return "Joining date is missing.";
        if (!Double.isFinite(baseSalary) || baseSalary <= 0) return "Base salary is invalid.";
        return null;
    }

    @Override
    public String toString() {
        return employeeId + " | " + name + " | " + department + " | " + getEmployeeType();
    }
}

class PermanentEmployee extends Employee {
    public PermanentEmployee(String id, String name, String department, LocalDate joiningDate,
                             double baseSalary) throws InvalidSalaryException {
        super(id, name, department, joiningDate, baseSalary);
    }
    @Override public String getEmployeeType() { return "Permanent"; }
    @Override protected double getBonusRate() { return 0.10; }
    @Override protected double getDeductionRate() { return 0.12; }
}

class ContractEmployee extends Employee {
    public ContractEmployee(String id, String name, String department, LocalDate joiningDate,
                            double baseSalary) throws InvalidSalaryException {
        super(id, name, department, joiningDate, baseSalary);
    }
    @Override public String getEmployeeType() { return "Contract"; }
    @Override protected double getBonusRate() { return 0.05; }
    @Override protected double getDeductionRate() { return 0.05; }
}

class Intern extends Employee {
    public Intern(String id, String name, String department, LocalDate joiningDate,
                  double baseSalary) throws InvalidSalaryException {
        super(id, name, department, joiningDate, baseSalary);
    }
    @Override public String getEmployeeType() { return "Intern"; }
    @Override protected double getBonusRate() { return 0.00; }
    @Override protected double getDeductionRate() { return 0.00; }
}

class EmployeeRepository<T extends Employee> {
    private final HashMap<String, T> byId = new HashMap<>();
    private final ArrayList<T> employees = new ArrayList<>();

    public void add(T employee) throws DuplicateEmployeeException {
        Objects.requireNonNull(employee, "Employee cannot be null");
        if (byId.containsKey(employee.getId())) {
            throw new DuplicateEmployeeException("Duplicate employee ID: " + employee.getId());
        }
        byId.put(employee.getId(), employee);
        employees.add(employee);
    }

    public T findById(String id) {
        return byId.get(id);
    }

    public ArrayList<T> findAll() {
        // Return a shallow list copy so callers can sort without changing repository order.
        return new ArrayList<>(employees);
    }

    public int size() { return employees.size(); }
}

class InvalidSalaryException extends Exception {
    public InvalidSalaryException(String message) {
        super(message);
    }
}

class DuplicateEmployeeException extends RuntimeException {
    public DuplicateEmployeeException(String message) {
        super(message);
    }
}
