public class MyTestClass {
    public void greet() {
        System.out.println("\n=== Hello from MyTestClass! ===");
        System.out.println("I was loaded by: " + 
            this.getClass().getClassLoader().getClass().getName());
        System.out.println("================================\n");
    }
    
    public int calculate(int a, int b) {
        System.out.println("Calculating: " + a + " + " + b);
        return a + b;
    }
}