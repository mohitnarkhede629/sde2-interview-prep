# Deep Dive 01: JVM Architecture & Runtime Memory Model

## 1. High-Level JVM Architecture

The Java Virtual Machine (JVM) executes compiled Java bytecode (`.class` files). It is composed of three primary subsystems:

```
      [ Source: .java ] ──javac──> [ Bytecode: .class ]
                                            │
   ┌────────────────────────────────────────┴────────────────────────────────────────┐
   │ 1. CLASSLOADER SUBSYSTEM                                                        │
   │    • Loading (Bootstrap, Platform, Application ClassLoaders)                    │
   │    • Linking (Verification, Preparation, Resolution)                            │
   │    • Initialization (Static initializers & <clinit>)                            │
   └────────────────────────────────────────┬────────────────────────────────────────┘
                                            │
   ┌────────────────────────────────────────▼────────────────────────────────────────┐
   │ 2. RUNTIME DATA AREAS (JVM Memory Space)                                        │
   │    ├── Per-Thread (Isolated & Thread-Safe):                                     │
   │    │   ├── Program Counter (PC) Register                                        │
   │    │   ├── JVM Stack (Stack Frames per method invocation)                       │
   │    │   └── Native Method Stack (JNI / C++ code)                                 │
   │    └── Shared Across All Threads:                                               │
   │        ├── Heap Memory (Young Gen: Eden/S0/S1, Old Gen)                         │
   │        └── Metaspace (Native OS Memory, Class Metadata, Bytecode)               │
   └────────────────────────────────────────┬────────────────────────────────────────┘
                                            │
   ┌────────────────────────────────────────▼────────────────────────────────────────┐
   │ 3. EXECUTION ENGINE                                                             │
   │    • Interpreter (Interprets bytecode line-by-line)                             │
   │    • JIT Compiler (C1 / C2 compilers compile hot spots to machine code)         │
   │    • Garbage Collector (Reclaims unreachable heap memory)                       │
   └─────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. JVM Memory Layout: Thread-Private vs. Shared

```
┌─────────────────────────────────────────────────────────────────────────┐
│                       SHARED ACROSS ALL THREADS                         │
│                                                                         │
│  ┌─────────────────────────────────────┐  ┌───────────────────────────┐ │
│  │ HEAP MEMORY                         │  │ METASPACE (Native Memory) │ │
│  │                                     │  │                           │ │
│  │  • All Objects (`new Order()`)      │  │  • Class Metadata         │ │
│  │  • All Arrays                       │  │  • Method Bytecode        │ │
│  │  • Instance Variables               │  │  • Static Variables (ref) │ │
│  │  • String Constant Pool (Java 7+)   │  │  • Runtime Constant Pool  │ │
│  └─────────────────────────────────────┘  └───────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                    PRIVATE TO EACH INDIVIDUAL THREAD                    │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │ THREAD 1 JVM STACK               THREAD 2 JVM STACK               │  │
│  │                                                                   │  │
│  │ ┌─────────────────────────┐     ┌─────────────────────────┐       │  │
│  │ │ Stack Frame: methodB()  │     │ Stack Frame: run()      │       │  │
│  │ ├─────────────────────────┤     └─────────────────────────┘       │  │
│  │ │ Stack Frame: methodA()  │                                       │  │
│  │ │  • Local Primitives     │                                       │  │
│  │ │  • Object References    │                                       │  │
│  │ │  • Operand Stack        │                                       │  │
│  │ └─────────────────────────┘                                       │  │
│  │                                                                   │  │
│  │  [PC Register: 0x004F]           [PC Register: 0x0012]            │  │
│  └───────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Comparison of Memory Areas

| Area | Scope | What Lives Here? | Error Thrown When Full |
|---|---|---|---|
| **JVM Stack** | Thread-Private | Local primitive variables, object reference pointers, operand stacks | `StackOverflowError` |
| **Heap Memory** | Shared by All | All objects created via `new`, arrays, instance fields | `OutOfMemoryError: Java heap space` |
| **Metaspace** | Shared by All | Class metadata, bytecode instructions, static field references | `OutOfMemoryError: Metaspace` |
| **PC Register** | Thread-Private | Address of currently executing bytecode instruction | None (hardware pointer) |

---

## 4. PermGen vs. Metaspace (Why Java 8 Changed It)

| Feature | PermGen (Java 7 and earlier) | Metaspace (Java 8+) |
|---|---|---|
| **Location** | Contiguous inside JVM Heap | Outside JVM Heap in **Native OS Memory** |
| **Default Size** | Fixed default (64MB–82MB) | Dynamically unbounded (limited by OS RAM) |
| **Common Issue** | `OutOfMemoryError: PermGen space` caused by dynamic classloading (e.g. Spring, Hibernate proxies) | Rarely throws OOM; automatically expands |
| **Tuning Flag** | `-XX:MaxPermSize=256m` | `-XX:MaxMetaspaceSize=256m` |

---

## 5. Strict Pass-By-Value in Java

Java is **strictly Pass-By-Value**. When an object is passed into a method, Java copies the **reference value (memory address)** onto the new stack frame.

### Visual Proof:
```java
void modify(Customer c) {
    c.setName("Updated"); // Modifies the object on Heap via copied reference!
    c = new Customer("Another"); // Reassigns the local stack pointer only!
}
```
* Calling `c.setName()` alters the object on the shared Heap.
* Calling `c = new Customer()` only changes what the **local stack reference** points to; the caller's reference remains pointed to the original object.

---

## 6. The Classloader Parent Delegation Model

```
       Bootstrap ClassLoader (C/C++ core, loads rt.jar, java.base)
                       ▲
                       │ delegates up
        Platform / Extension ClassLoader (loads ext modules)
                       ▲
                       │ delegates up
        Application / System ClassLoader (loads user classpath)
```

### Why Parent Delegation?
1. **Security**: Prevents untrusted user code from replacing core classes (e.g., custom malicious `java.lang.SecurityManager` or `java.lang.Object`).
2. **Avoiding Class Duplication**: Ensures standard classes are loaded exactly once in the JVM runtime.
