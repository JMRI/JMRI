package jmri.jmrix.rfid.swing;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Tests for RfidPanel class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class RfidPanelTest {
        
   // private RfidSystemConnectionMemo memo = null;

   @Test
   @DisabledIfHeadless
   public void testRfidPanelMemoConstructor(){

      Assertions.assertNotNull( new RfidPanel(){
           // class under test is abstract, but doesn't appear to
           // have any abstract methods. 
     }, "RfidPanel constructor");
   }

   @BeforeEach
   public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
        // memo = new RfidSystemConnectionMemo();
   }

   @AfterEach
   public void tearDown(){
        // memo=null;
        JUnitUtil.tearDown();
   }

}
