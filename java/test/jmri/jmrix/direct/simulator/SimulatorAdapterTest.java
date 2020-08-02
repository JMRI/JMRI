package jmri.jmrix.direct.simulator;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

   @BeforeEach
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
   }

   @AfterEach
   public void tearDown(){
        JUnitUtil.tearDown();
   }

}
