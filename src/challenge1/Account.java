public class Account {
    private int balance;
    private final String id;

    public Account(String id, int balance) {
        this.id = id;
        this.balance = balance;
    }

    public void withdraw(int amount) {
        balance -= amount;
    }

    public void deposit(int amount) {
        balance += amount;
    }

    public int getBalance() {
        return balance;
    }

    public String getId() {
        return id;
    }

    // FIXED: Deadlock-free transfer using consistent lock ordering
    public static void transferMoney(Account from, Account to, int amount) {

        Account firstLock, secondLock;
        
        //lock accounts in the same order to prevent circular wait
        if (from.id.compareTo(to.id) < 0) {
            firstLock = from;
            secondLock = to;
        } else {
            firstLock = to;
            secondLock = from;
        }

        synchronized (firstLock) {
            System.out.println(Thread.currentThread().getName() + " locked " + firstLock.id);
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (secondLock) {
                System.out.println(Thread.currentThread().getName() + " locked " + secondLock.id);
                
                // Performing the actual transfer
                from.withdraw(amount);
                to.deposit(amount);
                
                System.out.println(Thread.currentThread().getName() + 
                    " - Transfer successful! " + from.id + " -> " + to.id);
                System.out.println("Balances: " + from.id + "=" + from.getBalance() + 
                    ", " + to.id + "=" + to.getBalance());
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Account accountA = new Account("A", 1000);
        Account accountB = new Account("B", 1000);

        // Thread 1: Transfers from A to B
        Thread t1 = new Thread(() -> transferMoney(accountA, accountB, 100), "Thread-1");
        
        // Thread 2: Transfers from B to A
        Thread t2 = new Thread(() -> transferMoney(accountB, accountA, 100), "Thread-2");
        
        t1.start();
        t2.start();
        
        t1.join();
        t2.join();
        
        System.out.println("\nFinal Balances:");
        System.out.println("Account A: " + accountA.getBalance());
        System.out.println("Account B: " + accountB.getBalance());
    }
}