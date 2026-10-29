package com.wpits.accountmanagement.utils;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class IdGenerator {

    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();

    public synchronized String generate(String prefix) {
        AtomicLong counter = counters.computeIfAbsent(prefix, k -> new AtomicLong(0));
        long val = counter.incrementAndGet();
        return String.format("%s-%03d", prefix, val);
    }
}
