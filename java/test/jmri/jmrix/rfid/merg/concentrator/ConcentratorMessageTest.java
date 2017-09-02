package jmri.jmrix.rfid.merg.concentrator;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ConcentratorMessageTest.java
 *
 * Description:	tests for the jmri.jmrix.rfid.merge.concentrator.ConcentratorMessage class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class ConcentratorMessageTest {

    @Test
    public void testCtor() {
        ConcentratorMessage c = new ConcentratorMessage(20){
           @Override
           public String toMonitorString(){
               return "";
           }
        };
        Assert.assertNotNull(c);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
