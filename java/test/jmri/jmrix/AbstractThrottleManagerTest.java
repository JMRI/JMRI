package jmri.jmrix;

import jmri.SystemConnectionMemo;
import jmri.jmrix.debugthrottle.DebugThrottle;
import jmri.util.JUnitUtil;
import jmri.DccLocoAddress;

import org.mockito.Mockito;
import org.junit.jupiter.api.*;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class AbstractThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    private SystemConnectionMemo memo;
    private DebugThrottle throttle;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = Mockito.mock(SystemConnectionMemo.class);
        Mockito.when(memo.getUserName()).thenReturn("Test");
        Mockito.when(memo.getSystemPrefix()).thenReturn("T");
        tm = new AbstractThrottleManager(memo) {
            @Override
            public void requestThrottleSetup(jmri.LocoAddress a, boolean control) {
                if (!(a instanceof DccLocoAddress)){
                    Assertions.fail("DebugThrottle needs a dcclocoaddress : " + a );
                    return;
                }
                throttle = new DebugThrottle((DccLocoAddress) a, adapterMemo);
                notifyThrottleKnown(throttle, a);
            }

            @Override
            public boolean addressTypeUnique() {
                return false;
            }

            @Override
            public boolean canBeShortAddress(int address) {
                return true;
            }

            @Override
            public boolean canBeLongAddress(int address) {
                return true;
            }
        };
    }

    /**
     * A throttle window answers a failed request with a dialog: the thread
     * that reported the failure waits until the GUI thread has shown it and
     * the user has pressed OK. Here another thread stands in for the GUI
     * thread, and needs the manager meanwhile, as it does when a reply for
     * another throttle request arrives.
     *
     * @throws InterruptedException if interrupted while waiting
     */
    @Test
    public void testFailedRequestListenerCalledOutsideTheLock() throws InterruptedException {
        AbstractThrottleManager pending = new AbstractThrottleManager(memo) {
            @Override
            public void requestThrottleSetup(jmri.LocoAddress a, boolean control) {
                // the request stays pending until it fails
            }

            @Override
            public boolean addressTypeUnique() {
                return false;
            }

            @Override
            public boolean canBeShortAddress(int address) {
                return true;
            }

            @Override
            public boolean canBeLongAddress(int address) {
                return true;
            }
        };
        DccLocoAddress address = new DccLocoAddress(1234, true);
        java.util.concurrent.atomic.AtomicBoolean otherThreadGotIn = new java.util.concurrent.atomic.AtomicBoolean();
        jmri.ThrottleListener listener = new jmri.ThrottleListener() {
            @Override
            public void notifyThrottleFound(jmri.DccThrottle t) {
            }

            @Override
            public void notifyFailedThrottleRequest(jmri.LocoAddress a, String reason) {
                Thread gui = new Thread(() -> pending.getThrottleUsageCount(address)); // a synchronized method
                gui.start();
                try {
                    gui.join(2000); // as the dialog waits for the GUI thread
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                otherThreadGotIn.set(!gui.isAlive());
            }

            @Override
            public void notifyDecisionRequired(jmri.LocoAddress a, DecisionType question) {
            }
        };
        Assertions.assertTrue(pending.requestThrottle(address, listener, false));
        Thread layout = new Thread(() -> pending.failedThrottleRequest(address, "refused"));
        layout.start();
        layout.join(5000);
        Assertions.assertTrue(otherThreadGotIn.get(),
                "the manager is not locked while the listener is told: a dialog there can't block the GUI thread");
        Assertions.assertEquals(0, pending.getThrottleUsageCount(address));
        pending.dispose();
    }

    @AfterEach
    public void tearDown() {
        memo = null;
        tm = null;
        JUnitUtil.tearDown();
    }

    // private static final Logger log =
    // LoggerFactory.getLogger(AbstractThrottleManagerTest.class);

}
