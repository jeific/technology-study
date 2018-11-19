package com.broadtech.learn.algorithm;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 快速的LRU实现
 */
public class LRUFastImpl {
    private final Lock upgradeLinkLock = new ReentrantLock();
    private DataBlock head;
    private DataBlock tail;
    private ConcurrentHashMap<String, DataBlock> caches = new ConcurrentHashMap<>();
    private final int maxSize;

    public LRUFastImpl(int maxSize) {
        this.maxSize = maxSize;
    }


    public void cache(String key, DataBlock value) {
        exchange(value);
        if (caches.size() >= maxSize) {
            caches.remove(tail);
        }
        caches.putIfAbsent(key, value);
    }

    private void exchange(DataBlock curr) {
        try {
            upgradeLinkLock.lock();
            if (head == null) head = curr;
            else if (head != curr) {
                // 重新连接curr的上下节点
                curr.forward.backward = curr.backward;
                if (curr.backward != null) {
                    curr.backward.forward = curr.forward; // 修改curr向后指针的向前指向
                } else {
                    tail = curr.forward; // 重置链尾指针
                }
                // this交换到head之前
                head.forward = curr;
                curr.backward = head;
                head = curr;
            }
        } finally {
            upgradeLinkLock.unlock();
        }
    }
}
