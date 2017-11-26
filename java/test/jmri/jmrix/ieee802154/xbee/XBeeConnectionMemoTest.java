package jmri.jmrix.ieee802154.xbee;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XBeeConnectionMemoTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo
 * class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class XBeeConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        scm = new XBeeConnectionMemo();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
