/**
 * AbstractPowerManagerTest.java
 *
 * Description:	AbsBaseClass for PowerManager tests in specific jmrix. packages
 *
 * @author	Bob Jacobsen Copyright 2007
 * @version	$Revision$
 */
package jmri.jmrix;

//import jmri.*;
import java.beans.PropertyChangeListener;
import jmri.JmriException;
import jmri.PowerManager;
import junit.framework.Assert;
import junit.framework.TestCase;

public abstract class AbstractPowerManagerTest extends TestCase {

    public AbstractPowerManagerTest(String s) {
        super(s);
    }

    // service routines to simulate recieving on, off from interface
    protected abstract void hearOn();

    protected abstract void hearOff();

    protected abstract void sendOnReply();	  // get a reply to On command from layou

    protected abstract void sendOffReply();   // get a reply to Off command from layout

    protected abstract int numListeners();

    protected abstract int outboundSize();

    protected abstract boolean outboundOnOK(int index);

    protected abstract boolean outboundOffOK(int index);

    protected PowerManager p = null;	// holds objects under test

    static protected boolean listenerResult = false;

    protected class Listen implements PropertyChangeListener {

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            listenerResult = true;
        }
    }

    // test creation - real work is in the setup() routine
    public void testCreate() {
    }

    // test setting power on, off, then getting reply from system
    public void testSetPowerOn() throws JmriException {
        p.setPower(PowerManager.ON);
        // check one message sent, correct form, unknown state
        Assert.assertEquals("messages sent", 1, outboundSize());
        Assert.assertTrue("message type OK", outboundOnOK(0));
        Assert.assertEquals("state before reply ", PowerManager.UNKNOWN, p.getPower());
        // arrange for reply
        sendOnReply();
        Assert.assertEquals("state after reply ", PowerManager.ON, p.getPower());
    }

    public void testSetPowerOff() throws JmriException {
        p.setPower(PowerManager.OFF);
        // check one message sent, correct form
        Assert.assertEquals("messages sent", 1, outboundSize());
        Assert.assertTrue("message type OK", outboundOffOK(0));
        Assert.assertEquals("state before reply ", PowerManager.UNKNOWN, p.getPower());
        // arrange for reply
        sendOffReply();
        Assert.assertEquals("state after reply ", PowerManager.OFF, p.getPower());

    }

    public void testStateOn() throws JmriException {
        hearOn();
        Assert.assertEquals("power state", PowerManager.ON, p.getPower());
    }

    public void testStateOff() throws JmriException {
        hearOff();
        Assert.assertEquals("power state", PowerManager.OFF, p.getPower());
    }

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

    public void testRemoveListener() {
        Listen ln = new Listen();
        p.addPropertyChangeListener(ln);
        p.removePropertyChangeListener(ln);
        listenerResult = false;
        hearOn();
        Assert.assertTrue("listener should not have heard message after removeListner",
                !listenerResult);
    }

    public void testDispose1() throws JmriException {
        p.setPower(PowerManager.ON); // in case registration is deferred
        p.getPower();
        p.dispose();
        Assert.assertEquals("controller listeners remaining", 0, numListeners());
    }

    public void testDispose2() throws JmriException {
        p.addPropertyChangeListener(new Listen());
        p.setPower(PowerManager.ON);
        sendOnReply();
        p.dispose();
        try {
            p.setPower(PowerManager.OFF);
        } catch (JmriException e) {
            // this is OK branch, check message not sent
            Assert.assertEquals("messages sent", 1, outboundSize()); // just the first
            return;
        }
        Assert.fail("Should have thrown exception after dispose()");
    }

}
