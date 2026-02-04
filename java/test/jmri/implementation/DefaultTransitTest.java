package jmri.implementation;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for Transit class.
 *
 * @author Bob Jacobsen Copyright (C) 2017
 * @author Paul Bender Copyright (C) 2016
 **/

public class DefaultTransitTest {

   @Test
   public void sysNameConstructorTest(){
      Assertions.assertNotNull( new DefaultTransit("TT1"), "Constructor");
   }

   @Test
   public void twoNameStringConstructorTest(){
      Assertions.assertNotNull( new DefaultTransit("TT1", "user name"), "Constructor");
   }

   @BeforeEach
   public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
   }

   @AfterEach
   public void tearDown(){
        JUnitUtil.tearDown();
   }

}
