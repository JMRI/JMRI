package jmri.jmrix.bachrus;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SpeedoTrafficControllerTest.java
 *
 * Description:	tests for the jmri.jmrix.bachrus.SpeedoTrafficController class
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SpeedoTrafficControllerTest {

    @Test public void integerConstructorTest() {
        SpeedoSystemConnectionMemo m = new SpeedoSystemConnectionMemo();
        SpeedoTrafficController tc = new SpeedoTrafficController(m);
        Assert.assertNotNull(tc);
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
