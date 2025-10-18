import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CustomClassLoader extends ClassLoader {
    
    private final String classPath;
    
    public CustomClassLoader(String classPath) {
        super(CustomClassLoader.class.getClassLoader()); // Set parent
        this.classPath = classPath;
    }
    
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }
    
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // Step 1: Checking if class is already loaded
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            System.out.println("Class already loaded: " + name);
            return loadedClass;
        }
        
        // Step 2: Delegating core Java classes to parent
        // To prevent security issues and ClassCastException
        if (name.startsWith("java.") || name.startsWith("javax.") || 
            name.startsWith("sun.") || name.startsWith("jdk.")) {
            System.out.println("Delegating system class to parent: " + name);
            return super.loadClass(name, resolve);
        }
        
        // Step 3 (CORE TASK): Trying to load class ourselves FIRST (child-first)
        try {
            System.out.println("Attempting to load from custom path: " + name);
            loadedClass = findClassInCustomPath(name);
            
            if (loadedClass != null) {
                System.out.println("Successfully loaded by CustomClassLoader: " + name);
                
                // Step 4: Link class if needed
                if (resolve) {
                    resolveClass(loadedClass);
                }
                
                return loadedClass;
            }
        } catch (Exception e) {
            System.out.println("Failed to load from custom path: " + e.getMessage());
        }
        
        // Step 5: If we couldn't find it, delegate to parent
        System.out.println("Delegating to parent class loader: " + name);
        return super.loadClass(name, resolve);
    }
    
    private Class<?> findClassInCustomPath(String name) throws IOException {
        // Converting class name to file path
        String classFile = name.replace('.', File.separatorChar) + ".class";
    
        Path fullPath = Paths.get(classPath, classFile);
        File file = fullPath.toFile();
        
        System.out.println("Looking for file: " + file.getAbsolutePath());
        
        if (!file.exists()) {
            throw new IOException("Class file not found at: " + file.getAbsolutePath());
        }
        
    
        byte[] classBytes = Files.readAllBytes(fullPath);
        System.out.println("Read " + classBytes.length + " bytes from file");
        
        return defineClass(name, classBytes, 0, classBytes.length);
    }
    

    public static void main(String[] args) {
        try {
            System.out.println("=== Custom ClassLoader Demo ===\n");
            
            // Custom class directory
            String customClassPath = "G:\\custom-classes";
            
            // Creating the directory if it doesn't exist
            File dir = new File(customClassPath);
            if (!dir.exists()) {
                dir.mkdirs();
                System.out.println("Created directory: " + dir.getAbsolutePath());
            }
            
            System.out.println("Custom class path: " + new File(customClassPath).getAbsolutePath());
            System.out.println("Current working directory: " + new File(".").getAbsolutePath());
            System.out.println("\n--- Test 1: Loading System Class ---");
            
            CustomClassLoader customLoader = new CustomClassLoader(customClassPath);
            
        
            try {
                Class<?> stringClass = customLoader.loadClass("java.lang.String");
                System.out.println("Loaded: " + stringClass.getName());
                System.out.println("Loader: " + stringClass.getClassLoader());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            
            System.out.println("\n--- Test 2: Loading MyTestClass ---");
            
            // Checking if MyTestClass.class exists
            File testClassFile = new File(customClassPath + File.separator + "MyTestClass.class");
            if (!testClassFile.exists()) {
                System.out.println("WARNING: MyTestClass.class not found!");
                System.out.println("Expected location: " + testClassFile.getAbsolutePath());
                System.out.println("\n   Please follow these steps:");
                System.out.println("   1. Create MyTestClass.java (see instructions below)");
                System.out.println("   2. Compile: javac MyTestClass.java");
                System.out.println("   3. Move: move MyTestClass.class custom-classes\\");
                System.out.println("   4. Run this program again");
            } else {
                System.out.println("Found: " + testClassFile.getAbsolutePath());
                
                try {
                    Class<?> myClass = customLoader.loadClass("MyTestClass");
                    System.out.println("\nSuccessfully loaded MyTestClass!");
                    System.out.println("  Class name: " + myClass.getName());
                    System.out.println("  Loaded by: " + myClass.getClassLoader().getClass().getName());
                    
                    // Creating instance and invoke methods
                    System.out.println("\n--- Creating instance and calling methods ---");
                    Object instance = myClass.getDeclaredConstructor().newInstance();
                    
                    myClass.getMethod("greet").invoke(instance);
                  
                    Object result = myClass.getMethod("calculate", int.class, int.class)
                                          .invoke(instance, 10, 20);
                    System.out.println("calculate(10, 20) = " + result);
                    
                } catch (Exception e) {
                    System.out.println("Error loading/using MyTestClass: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

