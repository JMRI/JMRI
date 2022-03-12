package jmri.jmrix;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for SerialConfigException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SerialConfigExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("SerialConfigException constructor",new SerialConfigException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("SerialConfigException string constructor",new SerialConfigException("test exception"));
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
