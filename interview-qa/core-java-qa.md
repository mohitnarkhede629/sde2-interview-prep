# Core Java Interview Q&A (Mobile Quick Revision)

This document contains punchy, high-frequency interview questions and ready-to-speak responses designed for rapid revision on mobile.

---

### Q1: What is the difference between JVM Stack and Heap memory?
**Answer:**
* **Scope & Lifetime**: Stack is thread-private (created when thread starts, destroyed when it ends). Heap is shared across all threads (exists for the lifetime of the JVM).
* **Contents**: Stack holds Stack Frames containing local primitive variables, operand stacks, and references to objects. Heap stores actual object instances created via `new`, instance variables, and arrays.
* **Failure Modes**: Deep recursion overflows the stack throwing `StackOverflowError`. Running out of memory for new objects throws `OutOfMemoryError: Java heap space`.
* **Thread Safety**: Stack memory is thread-safe by design since each thread has its own stack. Heap memory is shared and requires synchronization or thread-safe constructs.

---

### Q2: Why was PermGen replaced by Metaspace in Java 8?
**Answer:**
* **PermGen (Java 7 and earlier)**: Was located inside the JVM Heap with a fixed contiguous memory size (defaulting to 64–82MB). Dynamic class generation by frameworks like Spring (CGLIB proxies) or Hibernate frequently caused `java.lang.OutOfMemoryError: PermGen space`.
* **Metaspace (Java 8+)**: Moved class metadata out of the JVM Heap into **Native OS Memory**. It automatically expands up to available system RAM by default, virtually eliminating PermGen OOM issues unless explicitly capped with `-XX:MaxMetaspaceSize`.

---

### Q3: Is Java Pass-By-Value or Pass-By-Reference? Explain with proof.
**Answer:**
**Java is strictly 100% Pass-By-Value.**
When passing an object, Java copies the **reference value (memory address)** onto the called method's stack frame.
* If you modify fields of the object (e.g., `obj.setName("new")`), the change reflects on the shared Heap.
* If you reassign the reference variable itself (e.g., `obj = new MyClass()`), it only reassigns the local stack pointer copy; the caller's reference variable remains pointing to the original object.

---

### Q4: What is the Parent Delegation Model in ClassLoaders and why does it exist?
**Answer:**
When a ClassLoader needs to load a class, it delegates the request to its parent before attempting to load it itself (Application $\to$ Platform/Extension $\to$ Bootstrap). Only if the parent fails to find the class does the child load it.
* **Why it exists**: Security and consistency. It prevents malicious or duplicate classes from overriding core JDK classes (e.g., a malicious user cannot inject their own rogue `java.lang.Object` or `java.lang.String`).

---

### Q5: How does HashMap handle hash collisions in Java 8 compared to Java 7?
**Answer:**
* **Java 7**: Used a pure singly linked list with **head insertion**. Under high collisions, lookups degraded to $O(N)$. In multi-threaded environments, concurrent resizing caused circular references (infinite loop / 100% CPU).
* **Java 8**: Uses **tail insertion** to prevent circular reference bugs. Additionally, if the number of elements in a bucket reaches **8** (`TREEIFY_THRESHOLD`) and the table's total capacity is at least **64**, the bucket converts into a **Red-Black Tree** (`TreeNode`), improving worst-case search time from $O(N)$ to $O(\log N)$. If elements drop to **6** during resizing, it untreeifies back to a linked list.

---

### Q6: Why is the capacity of a HashMap always a power of 2?
**Answer:**
Two primary reasons:
1. **Performance**: It allows the modulo operation `hash % length` to be replaced with a single bitwise operation: `(length - 1) & hash`. Bitwise AND executes in 1 CPU cycle.
2. **Uniform Distribution**: When length is $2^k$, `length - 1` consists entirely of binary `1`s (e.g., $16 - 1 = 15 = 1111_2$). This ensures all bits of the hash can participate. If length was odd (say 15, mask = 14 = $1110_2$), the lowest bit would always be 0, so odd-numbered bucket indices could never be populated.

---

### Q7: What is the purpose of the perturbation function `h ^ (h >>> 16)` in Java's HashMap?
**Answer:**
In Java, `hashCode()` returns a 32-bit signed integer, but initial HashMap tables are small (e.g., length 16). Without the perturbation function, `(n - 1) & hash` would only use the lowest 4 bits, completely ignoring the top 28 bits and causing excessive collisions.
Right-shifting by 16 bits and XORing folds the high-order bits into the low-order bits, ensuring both high and low bits contribute to the bucket index.

---

### Q8: What happens if two distinct keys return the same `hashCode()`?
**Answer:**
1. HashMap computes the same bucket index for both.
2. It detects a collision at that bucket.
3. It iterates through the bucket nodes and checks equality using `key.equals(existingKey)`.
4. Since the keys are distinct, `equals()` returns `false`.
5. The new key-value pair is appended to the bucket (as a new node in the linked list or inserted into the Red-Black tree).

---

### Q9: What happens if you override `equals()` but forget to override `hashCode()`?
**Answer:**
It violates the general Java Object contract. Two logically identical objects will inherit `Object.hashCode()` based on memory address/identity, generating two completely different hash codes.
When you insert `map.put(key1, value)` and then query `map.get(key2)` where `key1.equals(key2) == true`, `key2` will map to a different bucket and return `null`.

---

### Q10: Why is `String` or `Integer` commonly used as a HashMap key?
**Answer:**
1. **Immutability**: Their internal state cannot change after creation, meaning their `hashCode()` is guaranteed to remain constant.
2. **Cached HashCode**: In `java.lang.String`, the `hashCode` is calculated once and cached in a private field, making subsequent lookups extremely fast.
3. If a mutable object is used as a key and its fields change, its `hashCode` shifts, rendering the entry unretrievable and causing a memory leak.

---

### Q11: Why is `HashMap` not thread-safe? What are the risks of using it concurrently?
**Answer:**
`HashMap` does not synchronize any read or write operations. Concurrently accessing it can lead to:
1. **Lost Updates**: Two threads inserting into the same bucket simultaneously can overwrite each other's next pointer.
2. **Data Corruption**: Inconsistent view during resizing where entries become unreachable.
3. **Fail-Fast `ConcurrentModificationException`**: If an iterator detects structural modifications from another thread.
*Solution*: Use `ConcurrentHashMap` for thread-safe concurrent access.
