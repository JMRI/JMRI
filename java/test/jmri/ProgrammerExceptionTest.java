package jmri;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ProgrammerException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class ProgrammerExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("ProgrammerException constructor",new ProgrammerException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("ProgrammerException string constructor",new ProgrammerException("test exception"));
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
