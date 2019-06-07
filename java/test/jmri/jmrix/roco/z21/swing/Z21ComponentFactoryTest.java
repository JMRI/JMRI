package jmri.jmrix.roco.z21.swing;

import jmri.jmrix.roco.z21.Z21InterfaceScaffold;
import jmri.jmrix.roco.z21.Z21SystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for Z21ComponentFactory class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class Z21ComponentFactoryTest {
        
   private Z21SystemConnectionMemo memo = null;
   private Z21InterfaceScaffold tc = null; 

   @Test
   public void MemoConstructorTest(){
      Assert.assertNotNull("Z21ComponentFactory constructor",new Z21ComponentFactory(memo));
   }

   @Test
   public void getMenu(){
      Z21ComponentFactory zcf = new Z21ComponentFactory(memo);
      Assert.assertNotNull("Component Factory getMenu method",zcf.getMenu());
   }

   @Test
   public void getMenuDisabled(){
      memo.setDisabled(true);
      Z21ComponentFactory zcf = new Z21ComponentFactory(memo);
      Assert.assertNull("Disabled Component Factory getMenu method",zcf.getMenu());
   }

   @Before
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new Z21SystemConnectionMemo();
        tc = new Z21InterfaceScaffold();
        memo.setTrafficController(tc);
   }

   @After
   public void tearDown(){
        memo=null;
        tc.terminateThreads();
        tc=null;
        JUnitUtil.tearDown();
   }

}
