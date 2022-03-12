package jmri.jmrix.roco.z21.swing;

import jmri.jmrix.roco.z21.Z21InterfaceScaffold;
import jmri.jmrix.roco.z21.Z21SystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for Z21Menu class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class Z21MenuTest {
        
   private Z21SystemConnectionMemo memo = null;
   private Z21InterfaceScaffold tc = null; 

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("Z21Menu constructor",new Z21Menu("Z21",memo));
   }

   @Test
   public void MemoConstructorTest(){
      Assert.assertNotNull("Z21Menu constructor",new Z21Menu(memo));
   }

   @BeforeEach
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new Z21SystemConnectionMemo();
        tc = new Z21InterfaceScaffold();
        memo.setTrafficController(tc);
   }

   @AfterEach
   public void tearDown(){
        memo=null;
        tc.terminateThreads();
        tc=null;
        JUnitUtil.tearDown();
   }

}
