package com.twiiiins.newsletter;

import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.*;

@Component
public class RequestThrottle {
    private final Map<String, Deque<Instant>> requests = new HashMap<>();
    public synchronized boolean allow(String key, int count, long seconds) {
        Instant now = Instant.now();
        requests.entrySet().removeIf(e -> e.getValue().isEmpty() || e.getValue().peekLast().isBefore(now.minusSeconds(3600)));
        if (requests.size() > 10000 && !requests.containsKey(key)) return false;
        Deque<Instant> times = requests.computeIfAbsent(key, k -> new ArrayDeque<>());
        while (!times.isEmpty() && times.peekFirst().isBefore(now.minusSeconds(seconds))) times.removeFirst();
        if (times.size() >= count) return false;
        times.add(now);
        return true;
    }
}
