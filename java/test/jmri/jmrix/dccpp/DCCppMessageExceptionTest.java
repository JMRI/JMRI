package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for DCCppMessageException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class DCCppMessageExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("DCCppMessageException constructor",new DCCppMessageException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("DCCppMessageException string constructor",new DCCppMessageException("test exception"));
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
