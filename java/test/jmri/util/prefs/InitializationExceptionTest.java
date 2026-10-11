package jmri.util.prefs;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for InitializationException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class InitializationExceptionTest {

   @Test
   public void testCtor(){
      Assertions.assertNotNull( new InitializationException("test exception",null), "InitializationException constructor");
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
