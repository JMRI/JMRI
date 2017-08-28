package jmri.jmrix.rfid.generic.standalone;

import jmri.util.JUnitUtil;
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
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
