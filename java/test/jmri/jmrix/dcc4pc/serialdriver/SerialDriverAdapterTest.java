package jmri.jmrix.dcc4pc.serialdriver;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for SerialDriverAdapter class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SerialDriverAdapterTest {

   @Test
   public void testDccppSerialAdapterConstructor(){
      Assert.assertNotNull("SerialDriverAdapter constructor",new SerialDriverAdapter());
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
