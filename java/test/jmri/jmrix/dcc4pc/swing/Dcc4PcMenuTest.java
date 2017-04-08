package jmri.jmrix.dcc4pc.swing;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import jmri.jmrix.dcc4pc.Dcc4PcSystemConnectionMemo;
import java.awt.GraphicsEnvironment;

/**
 * Tests for Dcc4PcMenu class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class Dcc4PcMenuTest {
        
   private Dcc4PcSystemConnectionMemo memo = null;

   @Test
   public void MemoConstructorTest(){
      Assume.assumeFalse(GraphicsEnvironment.isHeadless());
      Assert.assertNotNull("Dcc4PcMenu constructor",new Dcc4PcMenu(memo));
   }

   @Before
   public void setUp(){
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new Dcc4PcSystemConnectionMemo();
   }

   @After
   public void tearDown(){
        memo=null;
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
   }

}
