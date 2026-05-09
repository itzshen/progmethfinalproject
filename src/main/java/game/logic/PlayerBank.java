package game.logic;

/**
 * Stores and updates the player's currency balance.
 */
public class PlayerBank {
    private double balance;

    /**
     * Creates a bank with a zero balance.
     */
    public PlayerBank() {
        this(0.0);
    }

    /**
     * Creates a bank with the given starting balance.
     *
     * @param initialBalance the starting balance
     */
    public PlayerBank(double initialBalance) {
        this.balance = initialBalance;
    }

    /**
     * @return the current balance
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Adds money to the balance.
     *
     * @param amount the non-negative amount to add
     * @throws IllegalArgumentException if amount is negative
     */
    public void deposit(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
        this.balance += amount;
    }

    /**
     * Attempts to subtract money from the balance.
     *
     * @param amount the non-negative amount to spend
     * @return true if the spend succeeded, false if insufficient funds
     * @throws IllegalArgumentException if amount is negative
     */
    public boolean trySpend(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
        if (this.balance < amount) {
            return false;
        }
        this.balance -= amount;
        return true;
    }
}
