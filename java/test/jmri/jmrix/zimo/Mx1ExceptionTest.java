package jmri.jmrix.zimo;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for Mx1Exception class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class Mx1ExceptionTest {

   @Test
   public void testMx1ExceptionStringCtor(){
      Assertions.assertNotNull( new Mx1Exception("test exception"), "Mx1Exception string constructor");
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
