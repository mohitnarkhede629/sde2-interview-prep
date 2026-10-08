/**
 * Assignment: Hands-on JVM Memory Verification
 * Run this file to verify how the JVM Stack, Heap, and Pass-by-Value work under the hood.
 */
public class JVM_Memory_Verification {

    static class Customer {
        private String name;

        public Customer(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    // 1. Pass-By-Value Proof: Reassignment vs Mutation
    public static void reassignReference(Customer c) {
        // Only modifies the local stack frame's copy of the reference pointer!
        c = new Customer("Reassigned Name");
    }

    public static void mutateObject(Customer c) {
        // Follows the copied reference pointer to the shared Heap and mutates the object!
        c.setName("Mutated In Heap");
    }

    // 2. Simulating StackOverflowError (Recursion pushing too many frames)
    public static void simulateStackOverflow(int depth) {
        // Each recursive call pushes a new Stack Frame containing LVT, Operand Stack
        simulateStackOverflow(depth + 1);
    }

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("1. VERIFYING STRICT PASS-BY-VALUE IN JAVA");
        System.out.println("=================================================");

        Customer original = new Customer("Original Name");
        System.out.println("Initial Name: " + original.getName());

        // Test 1: Reassignment
        reassignReference(original);
        System.out.println("After reassignReference(): " + original.getName() 
            + " (Expected: 'Original Name' -> Stack pointer was copied!)");

        // Test 2: Mutation
        mutateObject(original);
        System.out.println("After mutateObject(): " + original.getName() 
            + " (Expected: 'Mutated In Heap' -> Object on Heap was modified!)");

        System.out.println("\n=================================================");
        System.out.println("2. INSPECTING HEAP MEMORY VIA RUNTIME API");
        System.out.println("=================================================");

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long maxMemory = runtime.maxMemory() / (1024 * 1024);

        System.out.println("Max Heap Memory (-Xmx): " + maxMemory + " MB");
        System.out.println("Total Allocated Heap:  " + totalMemory + " MB");
        System.out.println("Free Heap Memory:       " + freeMemory + " MB");
        System.out.println("Used Heap Memory:       " + (totalMemory - freeMemory) + " MB");
        System.out.println("=================================================");
    }
}
