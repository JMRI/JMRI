package jmri.jmrix.rfid.swing;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Tests for RfidComponentFactory class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class RfidComponentFactoryTest {
        
   private RfidSystemConnectionMemo memo = null;

   @Test
   public void MemoConstructorTest(){
      Assert.assertNotNull("RfidComponentFactory constructor",new RfidComponentFactory(memo));
   }

   @Test
   public void getMenu(){
      Assume.assumeFalse(GraphicsEnvironment.isHeadless());
      RfidComponentFactory zcf = new RfidComponentFactory(memo);
      Assert.assertNotNull("Component Factory getMenu method",zcf.getMenu());
   }

   @Test
   public void getMenuDisabled(){
      memo.setDisabled(true);
      RfidComponentFactory zcf = new RfidComponentFactory(memo);
      Assert.assertNull("Disabled Component Factory getMenu method",zcf.getMenu());
   }

   @BeforeEach
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new RfidSystemConnectionMemo();
   }

   @AfterEach
   public void tearDown(){
        memo=null;
        JUnitUtil.tearDown();
   }

}
