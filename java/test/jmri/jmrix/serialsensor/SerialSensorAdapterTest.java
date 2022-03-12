package jmri.jmrix.serialsensor;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for SerialSensorAdapter class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SerialSensorAdapterTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("SerialSensorAdapter constructor",new SerialSensorAdapter());
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
