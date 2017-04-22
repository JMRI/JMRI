package jmri.jmrix.powerline.cp290;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SpecificReply class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SpecificReplyTest {

   private SpecificTrafficController tc = null;

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("SpecificReply constructor",new SpecificReply(tc));
   }

   @Before
   public void setUp(){
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        tc = new SpecificTrafficController(new SpecificSystemConnectionMemo());

   }

   @After
   public void tearDown(){
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
        tc = null;
   }

}
