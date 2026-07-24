package jmri.jmrix;

import java.beans.PropertyChangeListener;
import jmri.JmriException;
import jmri.PowerManager;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Abstract base class for PowerManager tests in specific jmrix. packages.
 *
 * This is not itself a test class, e.g. should not be added to a suite.
 * Instead, this forms the base for test classes, including providing some
 * common tests.
 *
 * @author Bob Jacobsen Copyright 2007
 * @author Bob Jacobsen Copyright (C) 2017
 */
public abstract class AbstractPowerManagerTestBase {

    // required setup routine, must set p to an appropriate value.
    public abstract void setUp();

    // service routines to simulate receiving on, off from interface
    protected abstract void hearOn();

    protected abstract void hearOff();

    protected abstract void hearIdle();

    protected abstract void sendOnReply();  // get a reply to On command from layout

    protected abstract void sendOffReply(); // get a reply to Off command from layout
    
    protected abstract void sendIdleReply();

    protected abstract int numListeners();

    protected abstract int outboundSize();

    protected abstract boolean outboundOnOK(int index);

    protected abstract boolean outboundOffOK(int index);

    protected abstract boolean outboundIdleOK(int index);

    protected PowerManager p = null; // holds objects under test

    protected boolean listenerResult = false;

    protected class Listen implements PropertyChangeListener {

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            listenerResult = true;
        }
    }

    // test creation - real work is in the setup() routine
    @Test
    public void testCreate() {
        assertNotNull(p,"Power Manager Created");
    }

    // test setting power on, off, then getting reply from system
    @Test
    public void testSetPowerOn() throws JmriException {
        int initialSent = outboundSize();
        p.setPower(PowerManager.ON);
        // check one message sent, correct form, unknown state
        assertEquals(initialSent + 1, outboundSize(),"messages sent +1");
        assertTrue(outboundOnOK(initialSent),"message type OK");
        assertEquals(PowerManager.UNKNOWN, p.getPower(),"unknown state before reply ");
        // arrange for reply
        sendOnReply();
        assertEquals(PowerManager.ON, p.getPower(),"on state after reply ");
    }

    @Test
    public void testSetPowerOff() throws JmriException {
        int startingMessages = outboundSize();
        p.setPower(PowerManager.OFF);
        // check one message sent, correct form
        assertEquals(startingMessages + 1, outboundSize(),"messages sent");
        assertTrue(outboundOffOK(startingMessages),"message type OK");
        assertEquals(PowerManager.UNKNOWN, p.getPower(),"state before reply ");
        // arrange for reply
        sendOffReply();
        assertEquals(PowerManager.OFF, p.getPower(),"state after reply ");

    }

    @Test
    public void testSetPowerIdle() throws JmriException {
        Assumptions.assumeTrue(p.implementsIdle(),"Does not implement IDLE");
        assertTrue(p.implementsIdle(),"LocoNet implements IDLE");
        int initialSent = outboundSize();
        p.setPower(PowerManager.IDLE);
        // check one message sent, correct form, unknown state
        assertEquals(initialSent + 1, outboundSize(),"messages sent");
        assertTrue(outboundIdleOK(initialSent),"message type IDLE O.K.");
        assertEquals(PowerManager.UNKNOWN, p.getPower(),"state before reply ");
        // arrange for reply
        sendIdleReply();
        assertEquals(PowerManager.IDLE, p.getPower(),"state after reply ");

    }

    @Test
    public void testStateOn() throws JmriException {
        hearOn();
        assertEquals(PowerManager.ON, p.getPower(),"power state on");
    }

    @Test
    public void testStateOff() throws JmriException {
        hearOff();
        assertEquals(PowerManager.OFF, p.getPower(),"power state off");
    }

    @Test
    public void testStateIdle() throws JmriException {
        Assumptions.assumeTrue(p.implementsIdle(),"Does not implement IDLE");
        hearIdle();
        assertEquals(PowerManager.IDLE, p.getPower(), "power state idle");
    }

    @Test
    public void testAddListener() throws JmriException {
        p.addPropertyChangeListener(new Listen());
        listenerResult = false;
        p.setPower(PowerManager.ON);
        sendOnReply();
        assertTrue(listenerResult,"listener invoked by GPOFF");
        listenerResult = false;
        p.setPower(PowerManager.OFF);
        sendOffReply();
        assertTrue(listenerResult,"listener invoked by GPON");
    }

    @Test
    public void testRemoveListener() {
        Listen ln = new Listen();
        p.addPropertyChangeListener(ln);
        p.removePropertyChangeListener(ln);
        listenerResult = false;
        hearOn();
        assertFalse(listenerResult,"listener should not have heard message after removeListener");
    }

    @Test
    public void testDispose1() throws JmriException {
        p.setPower(PowerManager.ON); // in case registration is deferred
        int startingListeners = numListeners();

        p.dispose();
        assertEquals(startingListeners -1,numListeners(),"controller listeners remaining");
    }

    @Test
    public void testDispose2() throws JmriException {
        p.addPropertyChangeListener(new Listen());
        p.setPower(PowerManager.ON);
        sendOnReply();
        int initialOutboundSize = outboundSize();
        p.dispose();

        Exception ex = Assertions.assertThrows(JmriException.class,
            () -> p.setPower(PowerManager.OFF),
            "Should have thrown exception after dispose()");
        assertNotNull(ex);
        assertEquals(initialOutboundSize, outboundSize(),"just the first messages sent");
    }

    @Test
    public void testImplementsIdle() {
        // assumes that Idle is not implemented; override this test for cases
        // where idle is implemented.
        assertFalse(p.implementsIdle());
    }

}
