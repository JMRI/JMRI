package jmri.jmrit.progsupport;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

   @Before
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
   }

   @After
   public void tearDown(){
        JUnitUtil.tearDown();
   }

}
