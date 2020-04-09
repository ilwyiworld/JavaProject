package com.yiworld.hashmap;

//jdk7 HashMap
public class MyHashMap<K, V> {
    private Entry[] table;
    private int size;
    private static Integer CAPACITY = 8;

    public MyHashMap() {
        this.table = new Entry[CAPACITY];
    }

    public int size() {
        return size;
    }

    public V get(K key) {
        int hash = key.hashCode();
        int i = hash % 8;
        for (Entry<K, V> entry = table[i]; entry != null; entry = entry.next) {
            if (entry.key.equals(key)) {
                return entry.value;
            }
        }
        return null;
    }

    public V put(K key, V value) {
        int hash = key.hashCode();
        int i = hash % 8;
        for (Entry<K, V> entry = table[i]; entry != null; entry = entry.next) {
            if (entry.key.equals(key)) {
                V oldValue = entry.value;
                entry.value = value;
                return oldValue;
            }
        }
        addEntry(key, value, i);
        return null;
    }

    public V remove(K key) {
        return null;
    }

    private void addEntry(K key, V value, int i) {
        //每次都插在头节点上
        Entry entry = new Entry(key, value, table[i]);
        table[i] = entry;
        size++;
    }

    class Entry<K, V> {
        private K key;
        private V value;
        private Entry<K, V> next;

        public Entry(K key, V value, Entry<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    public static void main(String[] args) {
        MyHashMap<String, String> hashMap = new MyHashMap<>();
        hashMap.put("k1", "v1");
        hashMap.put("k1", "v2");
        System.out.println(hashMap.get("k1"));
    }
}
