package jmri.jmrix.dccpp.dccppovertcp;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ServerAction class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class ServerActionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("ServerAction constructor",new ServerAction());
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
