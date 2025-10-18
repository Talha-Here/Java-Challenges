import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

// Data classes
class UserInfo {
    private String userId;
    private String name;
    private String email;
    
    public UserInfo(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }
    
    @Override
    public String toString() {
        return "UserInfo{userId='" + userId + "', name='" + name + "', email='" + email + "'}";
    }
}

class UserBalance {
    private String userId;
    private double balance;
    
    public UserBalance(String userId, double balance) {
        this.userId = userId;
        this.balance = balance;
    }
    
    @Override
    public String toString() {
        return "UserBalance{userId='" + userId + "', balance=$" + balance + "}";
    }
}

class LatestOrder {
    private String userId;
    private String orderId;
    private double amount;
    
    public LatestOrder(String userId, String orderId, double amount) {
        this.userId = userId;
        this.orderId = orderId;
        this.amount = amount;
    }
    
    @Override
    public String toString() {
        return "LatestOrder{userId='" + userId + "', orderId='" + orderId + "', amount=$" + amount + "}";
    }
}

// Combined dashboard
class UserDashboard {
    private UserInfo userInfo;
    private UserBalance userBalance;
    private LatestOrder latestOrder;
    
    public UserDashboard(UserInfo userInfo, UserBalance userBalance, LatestOrder latestOrder) {
        this.userInfo = userInfo;
        this.userBalance = userBalance;
        this.latestOrder = latestOrder;
    }
    
    @Override
    public String toString() {
        return "\n=== USER DASHBOARD ===\n" +
               userInfo + "\n" +
               userBalance + "\n" +
               latestOrder + "\n" +
               "=====================";
    }
}

class AsyncServiceOrchestration {
    
    // Service 1: 1-second delay
    public static CompletableFuture<UserInfo> getUserInfo(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Fetching user info on: " + Thread.currentThread().getName());
            
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            
            System.out.println(" User info fetched!");
            return new UserInfo(userId, "Talha K", "talha.k@gmail.com");
        });
    }
    
    // Service 2: 1.5-second delay
    public static CompletableFuture<UserBalance> getUserBalance(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Fetching user balance on: " + Thread.currentThread().getName());
            
            try {
                TimeUnit.MILLISECONDS.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            
            System.out.println(" User balance fetched!");
            return new UserBalance(userId, 5420.75);
        });
    }
    
    // Service 3: 2-second delay
    public static CompletableFuture<LatestOrder> getLatestOrder(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Fetching latest order on: " + Thread.currentThread().getName());
            
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            
            System.out.println(" Latest order fetched!");
            return new LatestOrder(userId, "ORD-12345", 249.99);
        });
    }
    
    // METHOD 1: Using thenCombine
    public static CompletableFuture<UserDashboard> getUserDashboard(String userId) {
        CompletableFuture<UserInfo> userInfoFuture = getUserInfo(userId);
        CompletableFuture<UserBalance> userBalanceFuture = getUserBalance(userId);
        CompletableFuture<LatestOrder> latestOrderFuture = getLatestOrder(userId);
        
        // Combine all three futures
        return userInfoFuture.thenCombine(userBalanceFuture, (info, balance) -> {
            return new Object[]{info, balance};
        }).thenCombine(latestOrderFuture, (infoAndBalance, order) -> {
            UserInfo info = (UserInfo) infoAndBalance[0];
            UserBalance balance = (UserBalance) infoAndBalance[1];
            return new UserDashboard(info, balance, order);
        });
    }
    
    // METHOD 2: Using allOf (cleaner approach)
    public static CompletableFuture<UserDashboard> getUserDashboardAllOf(String userId) {
        CompletableFuture<UserInfo> userInfoFuture = getUserInfo(userId);
        CompletableFuture<UserBalance> userBalanceFuture = getUserBalance(userId);
        CompletableFuture<LatestOrder> latestOrderFuture = getLatestOrder(userId);
        
        return CompletableFuture.allOf(userInfoFuture, userBalanceFuture, latestOrderFuture)
            .thenApply(v -> {
                UserInfo info = userInfoFuture.join();
                UserBalance balance = userBalanceFuture.join();
                LatestOrder order = latestOrderFuture.join();
                
                return new UserDashboard(info, balance, order);
            });
    }
    
    public static void main(String[] args) {
        String userId = "TK123";
        
        System.out.println("Starting dashboard fetch for: " + userId);
        System.out.println("Main thread: " + Thread.currentThread().getName());
        System.out.println("Services will run in PARALLEL...\n");
        
        long startTime = System.currentTimeMillis();
        
        // Fetching (non-blocking)
        CompletableFuture<UserDashboard> dashboardFuture = getUserDashboard(userId);
        
        System.out.println("\n Main thread NOT blocked, services running...\n");
        
        // Blocked only at the end
        UserDashboard dashboard = dashboardFuture.join();
        
        long endTime = System.currentTimeMillis();
        
        System.out.println(dashboard);
        System.out.println("\n Total time: " + (endTime - startTime) + "ms");
        System.out.println("(Should be ~2000ms, not 4500ms - proving parallelism!)");
    }
}