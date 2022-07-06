package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

import jmri.implementation.DefaultTransit;

/**
 * Tests for Transit class.
 *
 * @author Bob Jacobsen Copyright (C) 2017
 * @author Paul Bender Copyright (C) 2016
 **/

public class TransitTest {

   @Test
   public void testSysNameConstructor(){
      Assert.assertNotNull("Constructor", new DefaultTransit("TT1"));
   }

   @Test
   public void testTwoNameStringConstructor(){
      Assert.assertNotNull("Constructor", new DefaultTransit("TT1", "user name"));
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
