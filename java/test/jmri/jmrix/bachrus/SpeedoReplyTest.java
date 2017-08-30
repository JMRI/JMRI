package jmri.jmrix.bachrus;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SpeedoReplyTest.java
 *
 * Description:	tests for the jmri.jmrix.bachrus.SpeedoReply class
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SpeedoReplyTest {

    @Test public void integerConstructorTest() {
        SpeedoReply m = new SpeedoReply();
        Assert.assertNotNull(m);
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
