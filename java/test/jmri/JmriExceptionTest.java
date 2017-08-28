package jmri;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
