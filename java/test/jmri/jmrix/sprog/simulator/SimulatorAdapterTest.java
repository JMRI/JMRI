package jmri.jmrix.sprog.simulator;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for sprog SimulatorAdapter.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SimulatorAdapterTest {

   @Test
   public void ConstructorTest(){
       SimulatorAdapter a = new SimulatorAdapter();
       Assert.assertNotNull(a);
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
