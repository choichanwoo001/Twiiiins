package com.twiiiins.newsletter;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class RequestThrottleTest {
    static class MutableClock extends Clock {
        Instant now=Instant.parse("2026-10-31T00:00:00Z");
        public ZoneId getZone(){return ZoneOffset.UTC;}
        public Clock withZone(ZoneId zone){return this;}
        public Instant instant(){return now;}
    }
    @Test void quotaAndBoundaryAreDeterministicWithoutSleeping() {
        MutableClock clock=new MutableClock(); RequestThrottle throttle=new RequestThrottle(clock);
        assertTrue(throttle.allow("a",2,60)); assertTrue(throttle.allow("a",2,60)); assertFalse(throttle.allow("a",2,60));
        assertTrue(throttle.allow("b",2,60)); clock.now=clock.now.plusSeconds(60);
        assertFalse(throttle.allow("a",2,60)); clock.now=clock.now.plusNanos(1);
        assertTrue(throttle.allow("a",2,60));
        clock.now=clock.now.plusSeconds(3601); assertTrue(throttle.allow("a",2,60));
    }
}
