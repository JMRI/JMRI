package jmri.util.davidflanagan;

import javax.swing.JFrame;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * HardcopyWriterTest.java
 *
 * Test for the HardcopyWriter class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class HardcopyWriterTest {

    @Test
    @DisabledIfHeadless
    public void testCtor(){
        JFrame frame = new JFrame();
        try {
            HardcopyWriter hcw = new HardcopyWriter(frame,"test",10,10,10,10,10,true);
            Assertions.assertNotNull( hcw, "HardcopyWriter constructor");
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

