package com.yiworld;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * LRU: 最近最少使用算法 。 最近最少使用的元素，在接下来一段时间内，被访问的概率也很低。
 * 即最近被使用的元素，在接下来一段时间内，被访问概率较高。
 * <p>
 * 用链表的结构：
 * 链表尾表示最近被访问的元素，越靠近链表头表示越早之前被访问的元素
 * <p>
 * 插入一个元素，cache 不满，插到链表尾，满，移除cache链头元素再插入链表尾
 * 访问一个元素，从链表尾部开始遍历, 访问到之后，将其从原位置删除，重新加入链表尾部
 * <p>
 * 实现1：用双向链表实现。
 * put、get 时间复杂度:O(n)       效率低
 * <p>
 */
public class LRUCache {

    LinkedList<Node> cache;

    int capacity;

    public LRUCache(int capacity) {
        this.cache = new LinkedList<>();
        this.capacity = capacity;
    }

    // -1 表示没找到
    public int get(int key) {
        Iterator<Node> iterator = cache.descendingIterator();
        int result = -1;
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (node.key == key) {
                result = node.val;
                iterator.remove();
                put(key, result); //添加到链表尾部
                break;
            }
        }
        return result;
    }

    public void put(int key, int value) {
        //先遍历查找是否有key 的元素, 有则删除，重新添加到链尾
        Iterator<Node> iterator = cache.iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (node.key == key) {
                iterator.remove();
                break;
            }
        }

        if (capacity == cache.size()) {
            //缓存已满，删除一个 最近最少访问的元素（链表头）
            cache.removeFirst();
        }
        cache.add(new Node(key, value));
    }


    class Node {
        int key;
        int val;

        public Node(int key, int val) {
            this.key = key;
            this.val = val;
        }
    }

}
