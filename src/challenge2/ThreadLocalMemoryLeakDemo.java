import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class ThreadLocalMemoryLeakDemo {
    
    private static final ThreadLocal<byte[]> threadLocalData = new ThreadLocal<>();
    
    // Task that CAUSES memory leak
    static class LeakyTask implements Runnable {
        private final int taskId;
        
        public LeakyTask(int taskId) {
            this.taskId = taskId;
        }
        
        @Override
        public void run() {
            // Allocating 1 MB
            byte[] largeObject = new byte[1024 * 1024];
            
            // Store in ThreadLocal - CAUSES LEAK
            threadLocalData.set(largeObject);
            
            System.out.println("Task " + taskId + " on " + 
                Thread.currentThread().getName() + " - Stored 1MB in ThreadLocal");
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
        }
    }
    
    // Task that FIXES memory leak
    static class FixedTask implements Runnable {
        private final int taskId;
        
        public FixedTask(int taskId) {
            this.taskId = taskId;
        }
        
        @Override
        public void run() {
            try {
        
                byte[] largeObject = new byte[1024 * 1024];
                
                // Store in ThreadLocal
                threadLocalData.set(largeObject);
                
                System.out.println("Task " + taskId + " on " + 
                    Thread.currentThread().getName() + " - Stored 1MB in ThreadLocal");
                
                Thread.sleep(100);
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                //FIX: clean up ThreadLocal
                threadLocalData.remove();
                System.out.println("Task " + taskId + " - ThreadLocal cleaned up!");
            }
        }
    }
    
    public static void demonstrateLeak() throws InterruptedException {
        System.out.println("=== DEMONSTRATING MEMORY LEAK ===");
        System.out.println("2-thread pool, 10 tasks, NO cleanup\n");
        
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        for (int i = 1; i <= 10; i++) {
            executor.submit(new LeakyTask(i));
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        System.out.println("\n LEAK: ThreadLocal NOT cleaned up!\n");
    }
    
    public static void demonstrateFix() throws InterruptedException {
        System.out.println("=== DEMONSTRATING THE FIX ===");
        System.out.println("2-thread pool, 10 tasks, WITH cleanup\n");
        
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        for (int i = 1; i <= 10; i++) {
            executor.submit(new FixedTask(i));
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        System.out.println("\n FIXED: ThreadLocal properly cleaned!\n");
    }
    
    public static void main(String[] args) throws InterruptedException {
        demonstrateLeak();
        System.out.println("\n" + "=".repeat(50) + "\n");
        demonstrateFix();
    }
}