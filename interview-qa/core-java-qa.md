# Core Java Interview Q&A (Mobile Quick Revision)

This document contains punchy, high-frequency interview questions and ready-to-speak responses designed for rapid revision on mobile.

---

### Q1: How does HashMap handle hash collisions in Java 8 compared to Java 7?
**Answer:**
* **Java 7**: Used a pure singly linked list with **head insertion**. Under high collisions, lookups degraded to $O(N)$. In multi-threaded environments, concurrent resizing caused circular references (infinite loop / 100% CPU).
* **Java 8**: Uses **tail insertion** to prevent circular reference bugs. Additionally, if the number of elements in a bucket reaches **8** (`TREEIFY_THRESHOLD`) and the table's total capacity is at least **64**, the bucket converts into a **Red-Black Tree** (`TreeNode`), improving worst-case search time from $O(N)$ to $O(\log N)$. If elements drop to **6** during resizing, it untreeifies back to a linked list.

---

### Q2: Why is the capacity of a HashMap always a power of 2?
**Answer:**
Two primary reasons:
1. **Performance**: It allows the modulo operation `hash % length` to be replaced with a single bitwise operation: `(length - 1) & hash`. Bitwise AND executes in 1 CPU cycle.
2. **Uniform Distribution**: When length is $2^k$, `length - 1` consists entirely of binary `1`s (e.g., $16 - 1 = 15 = 1111_2$). This ensures all bits of the hash can participate. If length was odd (say 15, mask = 14 = $1110_2$), the lowest bit would always be 0, so odd-numbered bucket indices could never be populated.

---

### Q3: What is the purpose of the perturbation function `h ^ (h >>> 16)` in Java's HashMap?
**Answer:**
In Java, `hashCode()` returns a 32-bit signed integer, but initial HashMap tables are small (e.g., length 16). Without the perturbation function, `(n - 1) & hash` would only use the lowest 4 bits, completely ignoring the top 28 bits and causing excessive collisions.
Right-shifting by 16 bits and XORing folds the high-order bits into the low-order bits, ensuring both high and low bits contribute to the bucket index.

---

### Q4: What happens if two distinct keys return the same `hashCode()`?
**Answer:**
1. HashMap computes the same bucket index for both.
2. It detects a collision at that bucket.
3. It iterates through the bucket nodes and checks equality using `key.equals(existingKey)`.
4. Since the keys are distinct, `equals()` returns `false`.
5. The new key-value pair is appended to the bucket (as a new node in the linked list or inserted into the Red-Black tree).

---

### Q5: What happens if you override `equals()` but forget to override `hashCode()`?
**Answer:**
It violates the general Java Object contract. Two logically identical objects will inherit `Object.hashCode()` based on memory address/identity, generating two completely different hash codes.
When you insert `map.put(key1, value)` and then query `map.get(key2)` where `key1.equals(key2) == true`, `key2` will map to a different bucket and return `null`.

---

### Q6: Why is `String` or `Integer` commonly used as a HashMap key?
**Answer:**
1. **Immutability**: Their internal state cannot change after creation, meaning their `hashCode()` is guaranteed to remain constant.
2. **Cached HashCode**: In `java.lang.String`, the `hashCode` is calculated once and cached in a private field, making subsequent lookups extremely fast.
3. If a mutable object is used as a key and its fields change, its `hashCode` shifts, rendering the entry unretrievable and causing a memory leak.

---

### Q7: Why is `HashMap` not thread-safe? What are the risks of using it concurrently?
**Answer:**
`HashMap` does not synchronize any read or write operations. Concurrently accessing it can lead to:
1. **Lost Updates**: Two threads inserting into the same bucket simultaneously can overwrite each other's next pointer.
2. **Data Corruption**: Inconsistent view during resizing where entries become unreachable.
3. **Fail-Fast `ConcurrentModificationException`**: If an iterator detects structural modifications from another thread.
*Solution*: Use `ConcurrentHashMap` for thread-safe concurrent access.
