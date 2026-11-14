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

   // A connection that starts after DCC4PC (LocoNet often does) must still be offered as the Programmer.
   @Test
   public void testProgrammerChoicesIncludeLaterConnection() {
      SerialDriverAdapter a = new SerialDriverAdapter();
      new jmri.jmrix.internal.InternalSystemConnectionMemo("T", "Test Station", false);
      Assert.assertTrue("later connection offered",
              java.util.Arrays.asList(a.getOptionChoices("Programmer")).contains("Test Station"));

      // a saved choice whose connection isn't running stays selectable
      a.configureOption1("Not Running");
      Assert.assertTrue("saved choice kept",
              java.util.Arrays.asList(a.getOptionChoices("Programmer")).contains("Not Running"));
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
