package jmri.jmrix.roco.z21.swing.configtool;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jmri.jmrix.roco.z21.Z21SystemConnectionMemo;
import jmri.jmrix.roco.z21.Z21InterfaceScaffold;

/**
 * Tests for Z21ConfigAction class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class Z21ConfigActionTest {
        
   private Z21SystemConnectionMemo memo = null;
   private Z21InterfaceScaffold tc = null; 

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("Z21ConfigAction constructor",new Z21ConfigAction("Z21",memo));
   }

   @Test
   public void MemoConstructorTest(){
      Assert.assertNotNull("Z21ConfigAction constructor",new Z21ConfigAction(memo));
   }

   @Before
   public void setUp(){
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new Z21SystemConnectionMemo();
        tc = new Z21InterfaceScaffold();
        memo.setTrafficController(tc);
   }

   @After
   public void tearDown(){
        memo=null;
        tc=null;
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
   }

}
