package jmri.jmrix.rfid.swing;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import java.awt.GraphicsEnvironment;

/**
 * Tests for RfidMenu class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class RfidMenuTest {
        
   private RfidSystemConnectionMemo memo = null;

   @Test
   public void MemoConstructorTest(){
      Assume.assumeFalse(GraphicsEnvironment.isHeadless());
      Assert.assertNotNull("RfidMenu constructor",new RfidMenu(memo));
   }

   @Before
   public void setUp(){
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new RfidSystemConnectionMemo();
   }

   @After
   public void tearDown(){
        memo=null;
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
   }

}
