package jmri.util.davidflanagan;

import javax.swing.JFrame;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * CompatibleHardcopyWriterTest.java Test for the CompatibleHardcopyWriter class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class CompatibleHardcopyWriterTest {

    @Test
    @DisabledIfHeadless
    public void testCtor() {
        JFrame frame = new JFrame();
        try {
            CompatibleHardcopyWriter hcw = new CompatibleHardcopyWriter(frame, "test", 10, 10, 10, 10, 10, true);
            Assertions.assertNotNull(hcw, "CompatibleHardcopyWriter constructor");
            hcw.dispose();
        } catch (CompatibleHardcopyWriter.PrintCanceledException pce) {
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
