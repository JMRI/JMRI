package jmri.jmrix.serialsensor;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
