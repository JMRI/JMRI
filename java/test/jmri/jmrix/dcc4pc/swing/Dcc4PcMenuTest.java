package jmri.jmrix.dcc4pc.swing;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.dcc4pc.Dcc4PcSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Tests for Dcc4PcMenu class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class Dcc4PcMenuTest {

   private Dcc4PcSystemConnectionMemo memo = null;

   @Test
   public void MemoConstructorTest(){
      Assume.assumeFalse(GraphicsEnvironment.isHeadless());
      Assert.assertNotNull("Dcc4PcMenu constructor",new Dcc4PcMenu(memo));
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
