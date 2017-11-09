package jmri.jmrix.dccpp.dccppovertcp;

import jmri.util.JUnitUtil;
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
   public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
   }

   @After
   public void tearDown(){
        JUnitUtil.tearDown();
   }

}
