package jmri.jmrix.rfid.generic.standalone;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * StandaloneSystemConnectionMemoTest.java
 *
 * Description:	tests for the StandaloneSystemConnectionMemo class
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class StandaloneSystemConnectionMemoTest {

    @Test
    public void testCTor() {
        Assert.assertNotNull(new StandaloneSystemConnectionMemo());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
