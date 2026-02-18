package jmri.util.davidflanagan;

import javax.swing.JFrame;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * OriginalHardcopyWriterTest.java Test for the OriginalHardcopyWriter class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class OriginalHardcopyWriterTest {

    @Test
    @DisabledIfHeadless
    public void testCtor() {
        JFrame frame = new JFrame();
        try {
            OriginalHardcopyWriter hcw = new OriginalHardcopyWriter(frame, "test", 10, 10, 10, 10, 10, true);
            Assertions.assertNotNull(hcw, "OriginalHardcopyWriter constructor");
            hcw.dispose();
        } catch (OriginalHardcopyWriter.PrintCanceledException pce) {
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
