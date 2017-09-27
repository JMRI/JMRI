package jmri.jmrit.operations.trains;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for BuildFailedException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class BuildFailedExceptionTest {

   @Test
   public void StringTypeConstructorTest(){
      Assert.assertNotNull("BuildFailedException constructor",new BuildFailedException("test exception","normal"));
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("BuildFailedException string constructor",new BuildFailedException("test exception"));
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
