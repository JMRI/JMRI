// DccSignalHeadTest.java

package jmri.implementation;

import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.util.JUnitUtil;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the DccSignalHead implementation
 * @author	Bob Jacobsen  Copyright (C) 2013
 * @version $Revision$
 */
public class DccSignalHeadTest extends TestCase {

	public void testCtor1() {
        DccSignalHead s = new DccSignalHead("IH$1");
        
        Assert.assertEquals("system name", "IH$1", s.getSystemName());
        Assert.assertEquals("Send count", 0, sentPacketCount);
	}


	public void testRedAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.RED);
        
        Assert.assertEquals("Send count", 1, sentPacketCount);
        Assert.assertEquals("Packet length", 4, lastSentPacket.length);
        Assert.assertEquals("Packet byte 0", 0x80, lastSentPacket[0]&0xFF);
        Assert.assertEquals("Packet byte 1", 0x71, lastSentPacket[1]&0xFF);
        Assert.assertEquals("Packet byte 2", 0x00, lastSentPacket[2]&0xFF);
        
    }
    
	public void testDarkAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.RED);  // Default is DARK
        s.setAppearance(SignalHead.DARK);
        
        Assert.assertEquals("Send count", 2, sentPacketCount);
        Assert.assertEquals("Packet length", 4, lastSentPacket.length);
        Assert.assertEquals("Packet byte 0", 0x80, lastSentPacket[0]&0xFF);
        Assert.assertEquals("Packet byte 1", 0x71, lastSentPacket[1]&0xFF);
        Assert.assertEquals("Packet byte 2", 0x08, lastSentPacket[2]&0xFF);
        
    }
    
	public void testLunarAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.LUNAR);
        
        Assert.assertEquals("Send count", 1, sentPacketCount);
        Assert.assertEquals("Packet length", 4, lastSentPacket.length);
        Assert.assertEquals("Packet byte 0", 0x80, lastSentPacket[0]&0xFF);
        Assert.assertEquals("Packet byte 1", 0x71, lastSentPacket[1]&0xFF);
        Assert.assertEquals("Packet byte 2", 0x03, lastSentPacket[2]&0xFF);
        
    }
    
	public void testYellowAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.YELLOW);
        
        Assert.assertEquals("Send count", 1, sentPacketCount);
        Assert.assertEquals("Packet length", 4, lastSentPacket.length);
        Assert.assertEquals("Packet byte 0", 0x80, lastSentPacket[0]&0xFF);
        Assert.assertEquals("Packet byte 1", 0x71, lastSentPacket[1]&0xFF);
        Assert.assertEquals("Packet byte 2", 0x01, lastSentPacket[2]&0xFF);
        
    }

	public void testGreenAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.GREEN);
        
        Assert.assertEquals("Send count", 1, sentPacketCount);
        Assert.assertEquals("Packet length", 4, lastSentPacket.length);
        Assert.assertEquals("Packet byte 0", 0x80, lastSentPacket[0]&0xFF);
        Assert.assertEquals("Packet byte 1", 0x71, lastSentPacket[1]&0xFF);
        Assert.assertEquals("Packet byte 2", 0x02, lastSentPacket[2]&0xFF);
        
    }
    
	public void testFlashRedAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.FLASHRED);
        
        Assert.assertEquals("Send count", 1, sentPacketCount);
        Assert.assertEquals("Packet length", 4, lastSentPacket.length);
        Assert.assertEquals("Packet byte 0", 0x80, lastSentPacket[0]&0xFF);
        Assert.assertEquals("Packet byte 1", 0x71, lastSentPacket[1]&0xFF);
        Assert.assertEquals("Packet byte 2", 0x04, lastSentPacket[2]&0xFF);
        
    }
    
	public void testFlashLunarAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.FLASHLUNAR);
        
        Assert.assertEquals("Send count", 1, sentPacketCount);
        Assert.assertEquals("Packet length", 4, lastSentPacket.length);
        Assert.assertEquals("Packet byte 0", 0x80, lastSentPacket[0]&0xFF);
        Assert.assertEquals("Packet byte 1", 0x71, lastSentPacket[1]&0xFF);
        Assert.assertEquals("Packet byte 2", 0x07, lastSentPacket[2]&0xFF);
        
    }
    
	public void testFlashYellowAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.FLASHYELLOW);
        
        Assert.assertEquals("Send count", 1, sentPacketCount);
        Assert.assertEquals("Packet length", 4, lastSentPacket.length);
        Assert.assertEquals("Packet byte 0", 0x80, lastSentPacket[0]&0xFF);
        Assert.assertEquals("Packet byte 1", 0x71, lastSentPacket[1]&0xFF);
        Assert.assertEquals("Packet byte 2", 0x05, lastSentPacket[2]&0xFF);
        
    }

	public void testFlashGreenAppearance() {
        DccSignalHead s = new DccSignalHead("IH$1");

        s.setAppearance(SignalHead.FLASHGREEN);
        
        Assert.assertEquals("Send count", 1, sentPacketCount);
        Assert.assertEquals("Packet length", 4, lastSentPacket.length);
        Assert.assertEquals("Packet byte 0", 0x80, lastSentPacket[0]&0xFF);
        Assert.assertEquals("Packet byte 1", 0x71, lastSentPacket[1]&0xFF);
        Assert.assertEquals("Packet byte 2", 0x06, lastSentPacket[2]&0xFF);
        
    }
    
    
   
    
	// from here down is testing infrastructure
    
	public DccSignalHeadTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {DccSignalHeadTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(DccSignalHeadTest.class);
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
