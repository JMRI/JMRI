package jmri.jmrix.rfid.swing;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

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
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new RfidSystemConnectionMemo();
   }

   @After
   public void tearDown(){
        memo=null;
        JUnitUtil.tearDown();
   }

}
