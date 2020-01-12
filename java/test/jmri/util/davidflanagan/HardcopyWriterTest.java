package jmri.util.davidflanagan;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * HardcopyWriterTest.java
 *
 * Description: tests for the HardcopyWriter class
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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

