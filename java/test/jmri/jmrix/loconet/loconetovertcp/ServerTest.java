package jmri.jmrix.loconet.loconetovertcp;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for Server class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class ServerTest {

   @Test
   @Ignore("needs more setup")
   public void getInstanceTest(){
      Assert.assertNotNull("Server getInstance",Server.getInstance());
      Server.getInstance().disable();  // turn the server off after enabled durring creation.
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
