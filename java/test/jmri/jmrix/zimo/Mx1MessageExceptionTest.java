package jmri.jmrix.zimo;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for Mx1MessageException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class Mx1MessageExceptionTest {

   @Test
   public void testMx1MessageExceptionCtor(){
      Assertions.assertNotNull( new Mx1MessageException(), "Mx1MessageException constructor");
   }

   @Test
   public void testMx1MessageExceptionStringCtor(){
      Assertions.assertNotNull( new Mx1MessageException("test exception"), "Mx1MessageException string constructor");
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
