package jmri.jmrix.ieee802154.xbee;

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
public class XBeeConnectionMemoTest {

    @Test
    public void testCtor() {
        XBeeConnectionMemo m = new XBeeConnectionMemo();
        Assert.assertNotNull("exists", m);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
