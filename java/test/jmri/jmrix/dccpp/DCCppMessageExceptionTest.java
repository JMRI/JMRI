package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for DCCppMessageException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class DCCppMessageExceptionTest {

   @Test
   public void testDCCppMessageExceptionConstructor(){
      Assertions.assertNotNull(new DCCppMessageException(), "DCCppMessageException constructor");
   }

   @Test
   public void testDCCppMessageExceptionStringConstructor(){
      Assertions.assertNotNull(new DCCppMessageException("test exception"), "DCCppMessageException string constructor");
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
