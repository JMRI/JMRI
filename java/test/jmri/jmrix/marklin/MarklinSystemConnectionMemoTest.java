package jmri.jmrix.marklin;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * MarklinSystemConnectionMemoTest.java
 *
 * Description:	tests for the jmri.jmrix.marklin.MarklinSystemConnectionMemo class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class MarklinSystemConnectionMemoTest {

    @Test
    public void testCtor() {
        MarklinSystemConnectionMemo c = new MarklinSystemConnectionMemo();
        Assert.assertNotNull(c);
    }

    @Test
    public void testCtorWithTCParamter() {
        MarklinTrafficController tc = new MarklinTrafficController();
        MarklinSystemConnectionMemo c = new MarklinSystemConnectionMemo(tc);
        Assert.assertNotNull(c);
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
