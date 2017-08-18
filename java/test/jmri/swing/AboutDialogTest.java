package jmri.swing;

import apps.tests.Log4JFixture;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 * Test simple functioning of AboutDialog
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AboutDialogTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // create a frame to be the dialog parent so that nothing attempts to
        // remove the SwingUtilities$SharedOwnerFrame instance
        JFrame frame = new JFrame();
        AboutDialog dialog = new AboutDialog(frame, true);
        Assert.assertNotNull(dialog);
        dialog.dispose();
        frame.dispose();
    }

    @Test
    public void testShowAndClose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame frame = new JFrame();
        AboutDialog dialog = new AboutDialog(frame, true);

        new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("TitleAbout", jmri.Application.getApplicationName()));
            jdo.close();
        }).start();
        ThreadingUtil.runOnGUI(() -> {
            dialog.setVisible(true);
        });
        JUnitUtil.waitFor(() -> {
            return !dialog.isVisible();
        }, "About dialog did not close");
        dialog.dispose();
        frame.dispose();
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetWindows(true);
        JUnitUtil.initConnectionConfigManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetWindows(false); // don't display the list of windows,
        // it will display only show the ones
        // from the current test.
        Log4JFixture.tearDown();
    }
}
