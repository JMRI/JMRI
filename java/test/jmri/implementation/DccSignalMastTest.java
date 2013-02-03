// DccSignalMastTest.java

package jmri.implementation;

import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.NamedBeanHandle;
import jmri.util.JUnitUtil;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the DccSignalMast implementation
 * @author	Bob Jacobsen  Copyright (C) 2013
 * @version $Revision$
 */
public class DccSignalMastTest extends TestCase {

	public void testCtor1() {
        DccSignalMast s = new DccSignalMast("IF$dsm:AAR-1946:PL-1-high-abs(1)");
        
        Assert.assertEquals("system name", "IF$dsm:AAR-1946:PL-1-high-abs(1)", s.getSystemName());
        Assert.assertEquals("Send count", 0, sentPacketCount);
	}


	public void testStopAspect() {
        DccSignalMast s = new DccSignalMast("IF$dsm:AAR-1946:PL-1-high-abs(1)");
        s.setOutputForAppearance("Stop", 31);

        s.setAspect("Stop");
        
        Assert.assertEquals("Send count", 1, sentPacketCount);
        Assert.assertEquals("Packet length", 4, lastSentPacket.length);
        Assert.assertEquals("Packet byte 0", 0x80, lastSentPacket[0]&0xFF);
        Assert.assertEquals("Packet byte 1", 0x71, lastSentPacket[1]&0xFF);
        Assert.assertEquals("Packet byte 2", 0x1F, lastSentPacket[2]&0xFF);
        Assert.assertEquals("Packet byte 3", 0xEE, lastSentPacket[3]&0xFF);
        
    }
    
    
    
	// from here down is testing infrastructure
    
	public DccSignalMastTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {DccSignalMastTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(DccSignalMastTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() throws Exception { 
        super.setUp();
        apps.tests.Log4JFixture.setUp(); 
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        
        CommandStation c = new CommandStation(){
            public void sendPacket(byte[] packet, int repeats) {
                lastSentPacket = packet;
                sentPacketCount++;
            }  
            public String getUserName() { return null; }   
            public String getSystemPrefix() { return "I"; }           
        };
        InstanceManager.store(c, CommandStation.class);
        lastSentPacket = null;
        sentPacketCount = 0;
    }
    byte[] lastSentPacket;
    int sentPacketCount;
    
    protected void tearDown() throws Exception { 
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown(); 
    }
}
