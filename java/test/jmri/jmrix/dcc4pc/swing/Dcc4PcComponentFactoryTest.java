package jmri.jmrix.dcc4pc.swing;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.dcc4pc.Dcc4PcSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

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

   @BeforeEach
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new Dcc4PcSystemConnectionMemo();
   }

   @AfterEach
   public void tearDown(){
        memo=null;
        JUnitUtil.tearDown();
   }

}
