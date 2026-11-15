package com.twiiiins.newsletter;

import java.util.concurrent.atomic.LongAdder;
import org.springframework.stereotype.Component;

@Component
class SignupCounts {
    private final LongAdder registered = new LongAdder();
    private final LongAdder duplicate = new LongAdder();
    private final LongAdder limited = new LongAdder();
    private final LongAdder rejected = new LongAdder();
    private final LongAdder verificationUnavailable = new LongAdder();

    void subscription(boolean activated) { (activated ? registered : duplicate).increment(); }
    void limited() { limited.increment(); }
    void rejected() { rejected.increment(); }
    void verificationUnavailable() { verificationUnavailable.increment(); }

    record Snapshot(long registered, long duplicate, long limited, long rejected, long verificationUnavailable) {}
    Snapshot snapshot() {
        return new Snapshot(registered.sum(), duplicate.sum(), limited.sum(), rejected.sum(), verificationUnavailable.sum());
    }
}
