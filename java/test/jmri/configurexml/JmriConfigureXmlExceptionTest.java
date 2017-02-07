package jmri.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for JmriConfigureXmlException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class JmriConfigureXmlExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("JmriConfigureXmlException constructor",new JmriConfigureXmlException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("JmriConfigureXmlException string constructor",new JmriConfigureXmlException("test exception"));
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
