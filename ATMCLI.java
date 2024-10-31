import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class Customer {
    private String name;
    private double balance;
    private Map<String, Double> owed;
    private Map<String, Double> owes;

    public Customer(String name) {
        this.name = name;
        this.balance = 0;
        this.owed = new HashMap<>();
        this.owes = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        balance += amount;
    }

    public boolean withdraw(double amount) {
        if (amount > balance) {
            System.out.println("Insufficient funds! Your balance is $" + balance + ".");
            return false;
        }
        balance -= amount;
        return true;
    }

    public void transfer(Customer target, double amount) {
        balance -= amount;
        target.receiveTransfer(amount);
    }

    public void receiveTransfer(double amount) {
        balance += amount;
    }

    public void addOwed(Customer target, double amount) {
        owed.put(target.getName(), owed.getOrDefault(target.getName(), 0.0) + amount);
    }
    public void addOwes(Customer currenCustomer, double amount) {
        owes.put(currenCustomer.getName(), owes.getOrDefault(currenCustomer.getName(), 0.0) + amount);
    }

    public void settleDebt(String targetName, double amount, String currentCustName) {
        if (owed.containsKey(targetName)) {
            double currentOwed = owed.get(targetName);
            if (amount >= currentOwed) {
                owed.remove(targetName); // Clear debt if paid off
                owes.remove(currentCustName);
            } else {
                owed.put(targetName, currentOwed - amount);
                owes.put(currentCustName, currentOwed - amount);
            }
        }
    }

    public double getOwedAmount(String targetName) {
        return owed.getOrDefault(targetName, 0.0);
    }

    public double getOwesAmount(String targetName) {
        return owes.getOrDefault(targetName, 0.0);
    }

    public void printStatus() {
        System.out.println("Hello, " + name + "!");
        System.out.println("Your balance is $" + balance + ".");
        if (!owed.isEmpty()) {
            StringBuilder owedString = new StringBuilder("Owed ");
            owed.forEach((key, value) -> owedString.append("$").append(value).append(" to ").append(key).append(", "));
            System.out.println(owedString.substring(0, owedString.length() - 2) + ".");
        }
        if (!owes.isEmpty()) {
            StringBuilder owesString = new StringBuilder("Owes ");
            owes.forEach((key, value) -> owesString.append("$").append(value).append(" to ").append(key).append(", "));
            System.out.println(owesString.substring(0, owesString.length() - 2) + ".");
        }
    }
}

class ATM {
    private Map<String, Customer> customers;
    private Customer currentCustomer;

    public ATM() {
        customers = new HashMap<>();
        currentCustomer = null;
    }

    public void login(String name) {
        currentCustomer = customers.computeIfAbsent(name, Customer::new);
        currentCustomer.printStatus();
    }

    public void deposit(double amount) {
        if (currentCustomer == null) {
            System.out.println("You need to login first.");
            return;
        }
        currentCustomer.deposit(amount);
        System.out.println("Your balance is $" + currentCustomer.getBalance() + ".");
    }

    public void withdraw(double amount) {
        if (currentCustomer == null) {
            System.out.println("You need to login first.");
            return;
        }
        if (currentCustomer.withdraw(amount)) {
            System.out.println("Your balance is $" + currentCustomer.getBalance() + ".");
        }
    }

    public void transfer(String targetName, double amount) {
        if (currentCustomer == null) {
            System.out.println("You need to login first.");
            return;
        }
        Customer targetCustomer = customers.get(targetName);
        if (targetCustomer == null) {
            System.out.println("Customer " + targetName + " does not exist.");
            return;
        }

        // Check if there is an owed amount that can be settled first
        double owedAmount = currentCustomer.getOwedAmount(targetName);
        if (owedAmount > 0) {
            if (amount <= owedAmount) {
                currentCustomer.settleDebt(targetName, amount, currentCustomer.getName());
                amount = 0; // All transfer is settled as owed
            } else {
                currentCustomer.settleDebt(targetName, owedAmount, currentCustomer.getName());
                amount -= owedAmount; // Remaining amount after settling owed
            }
        }

        // Perform the transfer if any amount is left
        if (amount > 0) {
            currentCustomer.transfer(targetCustomer, amount);
            System.out.println("Transferred $" + amount + " to " + targetName + ".");
        } else {
            System.out.println("No amount to transfer after settling debts.");
        }

        // Check the new balance
        if (currentCustomer.getBalance() < 0) {
            double owedAmountNew = Math.abs(currentCustomer.getBalance());
            currentCustomer.addOwed(targetCustomer, owedAmountNew);
            currentCustomer.addOwes(currentCustomer, owedAmountNew);
            currentCustomer.deposit(owedAmountNew); // Reset balance to zero
            System.out.println("Your balance is $0.");
        } else {
            System.out.println("Your balance is $" + currentCustomer.getBalance() + ".");
        }

        // Always display the owed amount after a transfer
        double newOwed = currentCustomer.getOwedAmount(targetName);
        System.out.println("You owe $" + newOwed + " to " + targetName + ".");
    }

    public void logout() {
        if (currentCustomer == null) {
            System.out.println("You are not logged in.");
            return;
        }
        System.out.println("Goodbye, " + currentCustomer.getName() + "!");
        currentCustomer = null;
    }
}

public class ATMCLI {
    public static void main(String[] args) {
        ATM atm = new ATM();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine().trim();
            String[] command = input.split(" ");

            if (command.length == 0) continue;
            String action = command[0];

            switch (action) {
                case "login":
                    if (command.length < 2) {
                        System.out.println("Please provide a name.");
                    } else {
                        atm.login(command[1]);
                    }
                    break;
                case "deposit":
                    if (command.length < 2) {
                        System.out.println("Please provide an amount.");
                    } else {
                        atm.deposit(Double.parseDouble(command[1]));
                    }
                    break;
                case "withdraw":
                    if (command.length < 2) {
                        System.out.println("Please provide an amount.");
                    } else {
                        atm.withdraw(Double.parseDouble(command[1]));
                    }
                    break;
                case "transfer":
                    if (command.length < 3) {
                        System.out.println("Please provide a target name and an amount.");
                    } else {
                        atm.transfer(command[1], Double.parseDouble(command[2]));
                    }
                    break;
                case "logout":
                    atm.logout();
                    break;
                default:
                    System.out.println("Unknown command.");
            }
        }
    }
}