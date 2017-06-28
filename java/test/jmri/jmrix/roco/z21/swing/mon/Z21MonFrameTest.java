package jmri.jmrix.roco.z21.swing.mon;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import jmri.jmrix.roco.z21.RocoZ21CommandStation;
import jmri.jmrix.roco.z21.Z21SystemConnectionMemo;
import jmri.jmrix.roco.z21.Z21InterfaceScaffold;

import java.awt.GraphicsEnvironment;

/**
 * Tests for Z21MonFrame class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class Z21MonFrameTest {
        
   private Z21SystemConnectionMemo memo = null;
   private Z21InterfaceScaffold tc = null; 

   @Test
   public void MemoConstructorTest(){
      Assume.assumeFalse(GraphicsEnvironment.isHeadless());
      Assert.assertNotNull("Z21MonFrame constructor",new Z21MonFrame(memo));
   }

   @Before
   public void setUp(){
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new Z21SystemConnectionMemo();
        tc = new Z21InterfaceScaffold();
        memo.setTrafficController(tc);
        memo.setRocoZ21CommandStation(new RocoZ21CommandStation());
   }

   @After
   public void tearDown(){
        memo=null;
        tc=null;
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
   }

}
