package jmri.jmrit.logixng.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import jmri.*;
import jmri.jmrix.debugthrottle.DebugThrottleManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ThrottleRequest.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class ThrottleRequestTest {

    private ThrottleManager tm;
    private final AtomicReference<DccThrottle> found = new AtomicReference<>();
    private final ThrottleListener listener = new ThrottleListener() {
        @Override
        public void notifyThrottleFound(DccThrottle t) {
            found.set(t);
        }

        @Override
        public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
        }

        @Override
        public void notifyDecisionRequired(LocoAddress address, ThrottleListener.DecisionType question) {
        }
    };

    @Test
    public void testNoProtocolLetsTheManagerChoose() {
        assertTrue( ThrottleRequest.requestThrottle(tm, 3, null, listener), "requested");
        assertEquals( new DccLocoAddress(3, false), found.get().getLocoAddress(), "short DCC, as requestThrottle(int)");

        assertTrue( ThrottleRequest.requestThrottle(tm, 1234, null, listener), "requested");
        assertEquals( new DccLocoAddress(1234, true), found.get().getLocoAddress(), "long DCC, as requestThrottle(int)");
    }

    @Test
    public void testProtocolGivesTheAddress() {
        assertTrue( ThrottleRequest.requestThrottle(tm, 72, LocoAddress.Protocol.MOTOROLA, listener), "requested");
        assertEquals( new DccLocoAddress(72, LocoAddress.Protocol.MOTOROLA), found.get().getLocoAddress());

        assertTrue( ThrottleRequest.requestThrottle(tm, 16390, LocoAddress.Protocol.MFX, listener), "requested");
        assertEquals( new DccLocoAddress(16390, LocoAddress.Protocol.MFX), found.get().getLocoAddress());
    }

    @Test
    public void testDccProtocolUsesTheManagersAddressRules() {
        // DCC asks the manager for the address: 3 can be short or long here, so short is used
        assertTrue( ThrottleRequest.requestThrottle(tm, 3, LocoAddress.Protocol.DCC, listener), "requested");
        assertEquals( new DccLocoAddress(3, LocoAddress.Protocol.DCC_SHORT), found.get().getLocoAddress());

        assertTrue( ThrottleRequest.requestThrottle(tm, 3, LocoAddress.Protocol.DCC_LONG, listener), "requested");
        assertEquals( new DccLocoAddress(3, LocoAddress.Protocol.DCC_LONG), found.get().getLocoAddress());
    }

    @Test
    public void testNoAddressForProtocol() {
        ThrottleManager noAddress = new DebugThrottleManager(
                InstanceManager.getDefault(InternalSystemConnectionMemo.class)) {
            @Override
            public LocoAddress getAddress(String value, LocoAddress.Protocol protocol) {
                return null;
            }
        };
        assertFalse( ThrottleRequest.requestThrottle(noAddress, 5, LocoAddress.Protocol.MFX, listener),
                "not requested");
        JUnitAppender.assertWarnMessage("No MFX address 5 on this throttle connection");
        assertNull( found.get(), "no throttle");
        noAddress.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDebugThrottleManager();
        tm = InstanceManager.getDefault(ThrottleManager.class);
    }

    @AfterEach
    public void tearDown() {
        tm = null;
        JUnitUtil.tearDown();
    }

}
