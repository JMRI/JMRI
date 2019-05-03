package jmri.jmrix.ieee802154;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * IEEE802154NodeTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.IEEE802154Node
 * class
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class IEEE802154NodeTest{

    IEEE802154Node m;

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", m);
    }

    @Test
    public void testResetMustSend() {
        m.resetMustSend();
        Assert.assertFalse("Must Send after reset",m.mustSend());
    }

    @Test
    public void testSetMustSend() {
        m.setMustSend();
        Assert.assertTrue("Must Send after set",m.mustSend());
    }

    @Test
    public void checkNodeAddress() {
        // should always return true.
        Assert.assertTrue("Check Node Address",m.checkNodeAddress(50));
    }

    @Test
    public void checkSetAndGetPanAddress() {
        byte ba[]={0x01,0x02};
        m.setPANAddress(ba);
        Assert.assertEquals("PAN Address after set",ba[0],m.getPANAddress()[0]);
        Assert.assertEquals("PAN Address after set",ba[1],m.getPANAddress()[1]);
    }

    @Test
    public void checkSetAndGetUserAddress() {
        byte ba[]={0x01,0x02};
        m.setUserAddress(ba);
        Assert.assertEquals("User Address after set",ba[0],m.getUserAddress()[0]);
        Assert.assertEquals("User Address after set",ba[1],m.getUserAddress()[1]);
    }

    @Test
    public void checkSetAndGetGlobalAddress() {
        byte ba[]={0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08};
        m.setGlobalAddress(ba);
        for(int i=0;i<8;i++) {
           Assert.assertEquals("Global Address after set",ba[i],m.getGlobalAddress()[i]);
        }
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        m = new IEEE802154Node() {
            @Override
            public AbstractMRMessage createInitPacket() {
                return null;
            }
            @Override
            public AbstractMRMessage createOutPacket() {
                return null;
            }
            @Override
            public boolean getSensorsActive(){
                   return false;
            }
            @Override
            public boolean handleTimeout(AbstractMRMessage m, AbstractMRListener l){
                   return false;
            }
            @Override
            public void resetTimeout(AbstractMRMessage m){
            }
        };
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
