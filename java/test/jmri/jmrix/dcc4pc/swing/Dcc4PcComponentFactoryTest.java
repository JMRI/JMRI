package jmri.jmrix.dcc4pc.swing;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import jmri.jmrix.dcc4pc.Dcc4PcSystemConnectionMemo;
import java.awt.GraphicsEnvironment;

/**
 * Tests for Dcc4PcComponentFactory class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class Dcc4PcComponentFactoryTest {
        
   private Dcc4PcSystemConnectionMemo memo = null;

   @Test
   public void MemoConstructorTest(){
      Assert.assertNotNull("Dcc4PcComponentFactory constructor",new Dcc4PcComponentFactory(memo));
   }

   @Test
   public void getMenu(){
      Assume.assumeFalse(GraphicsEnvironment.isHeadless());
      Dcc4PcComponentFactory zcf = new Dcc4PcComponentFactory(memo);
      Assert.assertNotNull("Component Factory getMenu method",zcf.getMenu());
   }

   @Test
   public void getMenuDisabled(){
      memo.setDisabled(true);
      Dcc4PcComponentFactory zcf = new Dcc4PcComponentFactory(memo);
      Assert.assertNull("Disabled Component Factory getMenu method",zcf.getMenu());
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
