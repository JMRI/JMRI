package jmri.jmrit.progsupport;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for ProgModeException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class ProgModeExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("ProgModeException constructor",new ProgModeException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("ProgModeException string constructor",new ProgModeException("test exception"));
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
