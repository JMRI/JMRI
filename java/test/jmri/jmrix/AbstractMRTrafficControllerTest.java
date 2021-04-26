package jmri.jmrix;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for AbstractMRTrafficController.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class AbstractMRTrafficControllerTest {

    // derived classes should set the value of tc appropriately.
    protected AbstractMRTrafficController tc;

    @Test
    public void testCtor() {
        Assert.assertNotNull(tc);
    }

    @Test
    public void testAddNullListener() {
        Assert.assertThrows(NullPointerException.class, () -> tc.addListener(null));
    }

    @Test
    public void testPortReadyToSendNullController() {
        Assert.assertFalse(tc.portReadyToSend(null));
    }

    @Test
    public void testGetLastSenderNull() {
        // new tc, so getLastSender should return null.
        Assert.assertNull(tc.getLastSender());
    }

    @Test
    public void testHasTimeouts() {
        // new tc, so hasTimeouts should return false.
        Assert.assertFalse(tc.hasTimeouts());
    }

    @Test
    public void testStatus() {
        // new tc, but unconnected, so status should return false.
        Assert.assertFalse(tc.status());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        tc = new AbstractMRTrafficController() {

            @Override
            protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
            }

            @Override
            protected AbstractMRMessage pollMessage() {
                return null;
            }

            @Override
            protected AbstractMRListener pollReplyHandler() {
                return null;
            }

            @Override
            protected AbstractMRMessage enterProgMode() {
                return null;
            }

            @Override
            protected AbstractMRMessage enterNormalMode() {
                return null;
            }

            @Override
            protected void forwardReply(AbstractMRListener client, AbstractMRReply m) {
            }

            @Override
            protected AbstractMRReply newReply() {
                return null;
            }

            @Override
            protected boolean endOfMessage(AbstractMRReply r) {
                return true;
            }
        };
    }

    @AfterEach
    public void tearDown() {
        tc.terminateThreads();
        JUnitUtil.tearDown();

    }

}
