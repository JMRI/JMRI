package jmri;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ProgReadException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class ProgReadExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("ProgReadException constructor",new ProgReadException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("ProgReadException string constructor",new ProgReadException("test exception"));
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
