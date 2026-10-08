# Day 01 Assignment: Custom HashMap Implementation

## 🎯 Goal
Solidify your understanding of hashing, collision resolution, and array indexing by implementing a functional generic `SimpleHashMap<K, V>`.

---

## 📝 Task Specification

Create a Java class `SimpleHashMap<K, V>` that implements the following methods:

1. `void put(K key, V value)`:
   - Handle `null` keys (store them in bucket 0 or reject with custom exception, choose and document your decision).
   - Compute hash and index using power-of-2 bitwise mask.
   - Handle collisions using a linked list (`Entry<K, V>`).
   - If key already exists (via `equals()`), replace existing value and return old value.
   - If key does not exist, insert new node.
   - Check if `size > capacity * loadFactor` (default capacity = 16, load factor = 0.75). If exceeded, implement a `resize()` method that doubles capacity and re-indexes all elements.

2. `V get(K key)`:
   - Returns the value associated with key, or `null` if not found.

3. `V remove(K key)`:
   - Removes entry from the bucket chain, adjusts `size`, and returns the removed value (or `null`).

4. `int size()`:
   - Returns the count of key-value mappings.

---

## 💡 Starter Template

```java
public class SimpleHashMap<K, V> {
    
    static class Entry<K, V> {
        final K key;
        V value;
        Entry<K, V> next;

        public Entry(K key, V value, Entry<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private static final int DEFAULT_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private Entry<K, V>[] table;
    private int size;
    private int threshold;

    @SuppressWarnings("unchecked")
    public SimpleHashMap() {
        this.table = new Entry[DEFAULT_CAPACITY];
        this.threshold = (int) (DEFAULT_CAPACITY * DEFAULT_LOAD_FACTOR);
        this.size = 0;
    }

    // TODO: Implement put, get, remove, resize, and hash helper methods
}
```

---

## 🧪 Verification Questions (To answer in your mind or code)
1. What is the time complexity of your `put` and `get` operations on average vs worst case?
2. What happens if a caller overrides `equals()` on their key class, but does not override `hashCode()`? Does your `SimpleHashMap` work correctly with it?
