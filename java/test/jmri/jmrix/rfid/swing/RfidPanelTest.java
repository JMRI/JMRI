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
 * Tests for RfidPanel class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class RfidPanelTest {
        
   // private RfidSystemConnectionMemo memo = null;

   @Test
   public void MemoConstructorTest(){
      Assume.assumeFalse(GraphicsEnvironment.isHeadless());
      Assert.assertNotNull("RfidPanel constructor",new RfidPanel(){
           // class under test is abstract, but doesn't appear to
           // have any abstract methods. 
     });
   }

   @Before
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        // memo = new RfidSystemConnectionMemo();
   }

   @After
   public void tearDown(){
        // memo=null;
        JUnitUtil.tearDown();
   }

}
