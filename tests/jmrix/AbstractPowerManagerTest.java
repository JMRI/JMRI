/** 
 * AbstractPowerManagerTest.java
 *
 * Description:	    AbsBaseClass for PowerManager tests in specific jmrix. packages
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests.jmrix;

import jmri.*;

import java.io.*;
import java.beans.PropertyChangeListener;
import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public abstract class AbstractPowerManagerTest extends TestCase {

	public AbstractPowerManagerTest(String s) {
		super(s);
	}
	// service routines to simulate recieving on, off from interface
	protected abstract void hearOn();	
	protected abstract void hearOff();
	protected abstract int numListeners();
	protected abstract int outboundSize();
	protected abstract boolean outboundOnOK(int index);
	protected abstract boolean outboundOffOK(int index);
	
	protected PowerManager p = null;	// holds objects under test

	static private boolean listenerResult = false;
	private class Listen implements PropertyChangeListener {
		public void propertyChange(java.beans.PropertyChangeEvent e) {
			listenerResult = true;
			System.out.println("event: "+e);
		}
	}
		
	// test creation - real work is in the setup() routine
	public void testCreate() {
	}

	// test power
	public void testSetPowerOn() throws JmriException {
		p.setPower(PowerManager.ON);
		// check one message sent, correct form
		Assert.assertEquals("messages sent", 1, outboundSize());
		Assert.assertTrue("message type OK", outboundOnOK(0));
		
	}

	public void testSetPowerOff() throws JmriException {
		p.setPower(PowerManager.OFF);
		// check one message sent, correct form
		Assert.assertEquals("messages sent", 1, outboundSize());
		Assert.assertTrue("message type OK", outboundOffOK(0));
		
	}

	public void testStateOn() throws JmriException {
		hearOn();
		Assert.assertEquals("power state", PowerManager.ON, p.getPower());;
	}
	
	public void testStateOff() throws JmriException {
		hearOff();
		Assert.assertEquals("power state", PowerManager.OFF, p.getPower());;
	}
	
	public void testAddListener() {
		p.addPropertyChangeListener(new Listen());
		listenerResult = false;
		hearOff();
		Assert.assertTrue("listener invoked by GPOFF", listenerResult);
		listenerResult = false;
		hearOn();
		Assert.assertTrue("listener invoked by GPON", listenerResult);
	}
	
	public void testRemoveListener() {
		Listen ln = new Listen();
		p.addPropertyChangeListener(ln);
		p.removePropertyChangeListener(ln);
		listenerResult = false;
		hearOn();		
		System.out.println("after xmit");
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
		hearOn();
		p.dispose();
		hearOff();
		Assert.assertEquals("Should still be ON", PowerManager.ON, p.getPower());
	}

}
