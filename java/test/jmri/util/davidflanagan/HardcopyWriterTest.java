package jmri.util.davidflanagan;

import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * HardcopyWriterTest.java
 *
 * Test for the HardcopyWriter class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class HardcopyWriterTest {

    @Test
    public void testCtor(){
      Assume.assumeFalse(GraphicsEnvironment.isHeadless());
      JFrame frame = new JFrame();
      try {
          HardcopyWriter hcw = new HardcopyWriter(frame,"test",10,10,10,10,10,true);
          Assert.assertNotNull("HardcopyWriter constructor",hcw);
          hcw.dispose();
      } catch (HardcopyWriter.PrintCanceledException pce) {
          // this isn't an error for this test.
      }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

