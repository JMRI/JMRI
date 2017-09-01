package jmri;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ProgWriteException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class ProgWriteExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("ProgWriteException constructor",new ProgWriteException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("ProgWriteException string constructor",new ProgWriteException("test exception"));
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
