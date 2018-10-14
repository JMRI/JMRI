package jmri.jmrix.roco.z21.simulator;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for Z21SimulatorZ21SimulatorConnectionConfig class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class Z21SimulatorConnectionConfigTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("Z21SimulatorConnectionConfig constructor",new Z21SimulatorConnectionConfig());
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
