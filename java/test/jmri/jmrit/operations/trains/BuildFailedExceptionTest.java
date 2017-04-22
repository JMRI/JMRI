package jmri.jmrit.operations.trains;

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
   public void setUp(){
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
   }

   @After
   public void tearDown(){
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
   }

}
