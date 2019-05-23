package jmri.jmrix;

import java.beans.PropertyChangeListener;
import jmri.JmriException;
import jmri.PowerManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Abstract base class for PowerManager tests in specific jmrix. packages.
 *
 * This is not itself a test class, e.g. should not be added to a suite.
 * Instead, this forms the base for test classes, including providing some
 * common tests.
 *
 * @author	Bob Jacobsen Copyright 2007
 * @author	Bob Jacobsen Copyright (C) 2017
 */
public abstract class AbstractPowerManagerTestBase {

    // required setup routine, must set p to an appropriate value.
    @Before
    abstract public void setUp();

    // service routines to simulate receiving on, off from interface
    protected abstract void hearOn();

    protected abstract void hearOff();

    protected abstract void hearIdle();

    protected abstract void sendOnReply();	  // get a reply to On command from layout

    protected abstract void sendOffReply();   // get a reply to Off command from layout
    
    protected abstract void sendIdleReply();

    protected abstract int numListeners();

    protected abstract int outboundSize();

    protected abstract boolean outboundOnOK(int index);

    protected abstract boolean outboundOffOK(int index);

    protected abstract boolean outboundIdleOK(int index);

    protected PowerManager p = null;	// holds objects under test

    static protected boolean listenerResult = false;

    protected class Listen implements PropertyChangeListener {

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            listenerResult = true;
        }
    }

    // test creation - real work is in the setup() routine
    @Test
    public void testCreate() {
       Assert.assertNotNull("Power Manager Created",p);
    }

    // test setting power on, off, then getting reply from system
    @Test
    public void testSetPowerOn() throws JmriException {
        int initialSent = outboundSize();
        p.setPower(PowerManager.ON);
        // check one message sent, correct form, unknown state
        Assert.assertEquals("messages sent", initialSent + 1, outboundSize());
        Assert.assertTrue("message type OK", outboundOnOK(initialSent));
        Assert.assertEquals("state before reply ", PowerManager.UNKNOWN, p.getPower());
        // arrange for reply
        sendOnReply();
        Assert.assertEquals("state after reply ", PowerManager.ON, p.getPower());
    }

    @Test
    public void testSetPowerOff() throws JmriException {
        int startingMessages = outboundSize();
        p.setPower(PowerManager.OFF);
        // check one message sent, correct form
        Assert.assertEquals("messages sent", startingMessages + 1, outboundSize());
        Assert.assertTrue("message type OK", outboundOffOK(startingMessages));
        Assert.assertEquals("state before reply ", PowerManager.UNKNOWN, p.getPower());
        // arrange for reply
        sendOffReply();
        Assert.assertEquals("state after reply ", PowerManager.OFF, p.getPower());

    }

    @Test
    public void testSetPowerIdle() throws JmriException {
        if (p.implementsIdle()) {
            Assert.assertTrue("LocoNet implements IDLE", p.implementsIdle());
            int initialSent = outboundSize();
            p.setPower(PowerManager.IDLE);
            // check one message sent, correct form, unknown state
            Assert.assertEquals("messages sent", initialSent + 1, outboundSize());
            Assert.assertTrue("message type IDLE O.K.", outboundIdleOK(initialSent));
            Assert.assertEquals("state before reply ", PowerManager.UNKNOWN, p.getPower());
            // arrange for reply
            sendIdleReply();
            Assert.assertEquals("state after reply ", PowerManager.IDLE, p.getPower());
        }
    }

    @Test
    public void testStateOn() throws JmriException {
        hearOn();
        Assert.assertEquals("power state", PowerManager.ON, p.getPower());
    }

    @Test
    public void testStateOff() throws JmriException {
        hearOff();
        Assert.assertEquals("power state", PowerManager.OFF, p.getPower());
    }

    @Test
    public void testStateIdle() throws JmriException {
        if (p.implementsIdle()) {
            hearIdle();
            Assert.assertEquals("power state", PowerManager.IDLE, p.getPower());
        }
    }

    @Test
    public void testAddListener() throws JmriException {
        p.addPropertyChangeListener(new Listen());
        listenerResult = false;
        p.setPower(PowerManager.ON);
        sendOnReply();
        Assert.assertTrue("listener invoked by GPOFF", listenerResult);
        listenerResult = false;
        p.setPower(PowerManager.OFF);
        sendOffReply();
        Assert.assertTrue("listener invoked by GPON", listenerResult);
    }

    @Test
    public void testRemoveListener() {
        Listen ln = new Listen();
        p.addPropertyChangeListener(ln);
        p.removePropertyChangeListener(ln);
        listenerResult = false;
        hearOn();
        Assert.assertTrue("listener should not have heard message after removeListener",
                !listenerResult);
    }

    @Test
    public void testDispose1() throws JmriException {
        p.setPower(PowerManager.ON); // in case registration is deferred
        int startingListeners = numListeners();
        p.getPower();
        p.dispose();
        Assert.assertEquals("controller listeners remaining", startingListeners -1 , numListeners());
    }

    @Test
    public void testDispose2() throws JmriException {
        p.addPropertyChangeListener(new Listen());
        p.setPower(PowerManager.ON);
        sendOnReply();
        int initialOutboundSize = outboundSize();
        p.dispose();
        try {
            p.setPower(PowerManager.OFF);
        } catch (JmriException e) {
            // this is OK branch, check message not sent
            Assert.assertEquals("messages sent", initialOutboundSize, outboundSize()); // just the first
            return;
        }
        Assert.fail("Should have thrown exception after dispose()");
    }

    @Test
    public void testImplementsIdle() {
        // assumes that Idle is not implemented; override this test for cases
        // where idle is implemented.
        Assert.assertFalse(p.implementsIdle());
    }

}
