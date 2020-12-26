package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Tests for JmriException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class JmriExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("JmriException constructor",new JmriException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("JmriException string constructor",new JmriException("test exception"));
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
