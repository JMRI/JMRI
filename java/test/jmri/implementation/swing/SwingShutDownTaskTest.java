package jmri.implementation.swing;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SwingShutDownTaskTest {

    private boolean modalDialogStopsTest = false;
    private final static Logger log = LoggerFactory.getLogger(SwingShutDownTaskTest.class);

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SwingShutDownTask t = new SwingShutDownTask("SwingShutDownTask Window Check",
                "Do Something quits, click Continue Qutting to quit, Cancel Quit to continue",
                "Do Something and Stop",
                null) {
                    @Override
                    public boolean checkPromptNeeded() {
                        return false;
                    }
                };
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCreate1() {

        // Just display for test
        SwingShutDownTask t = new SwingShutDownTask("SwingShutDownTask Window Check",
                "Do Something quits, click Continue Qutting to quit, Cancel Quit to continue",
                "Do Something and Stop",
                null) {
                    @Override
                    public boolean checkPromptNeeded() {
                        log.debug("mDST {}", modalDialogStopsTest);
                        return !modalDialogStopsTest;
                    }
                };

        // and display
        t.run();
    }

    @Test
    public void testCreate2() {

        // Just display for test
        SwingShutDownTask t = new SwingShutDownTask("SwingShutDownTask Window Check",
                "Do Something repeats, click Continue Qutting to quit, Cancel Quit to continue",
                "Do Something and repeats",
                null) {
                    @Override
                    public boolean checkPromptNeeded() {
                        log.debug("mDST {}", modalDialogStopsTest);
                        return !modalDialogStopsTest;
                    }

                    @Override
                    public boolean doPrompt() {
                        return false;
                    }
                };

        // and display
        t.run();
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        this.modalDialogStopsTest = System.getProperty("modalDialogStopsTest", "false").equals("true");
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
