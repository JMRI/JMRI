package jmri.util.davidflanagan;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JFrame;
import java.awt.GraphicsEnvironment;

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
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}

