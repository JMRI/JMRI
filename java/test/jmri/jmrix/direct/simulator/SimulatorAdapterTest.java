package jmri.jmrix.direct.simulator;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SimulatorAdapter class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SimulatorAdapterTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("SimulatorAdapter constructor", new SimulatorAdapter());
   }

   @Before
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
   }

   @After
   public void tearDown(){
        JUnitUtil.tearDown();
   }

}
