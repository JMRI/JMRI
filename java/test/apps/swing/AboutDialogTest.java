package apps.swing;

import javax.swing.JFrame;

import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 * Test simple functioning of AboutDialog
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AboutDialogTest {

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testCtor() {
        // create a frame to be the dialog parent so that nothing attempts to
        // remove the SwingUtilities$SharedOwnerFrame instance
        JFrame frame = new JFrame();
        AboutDialog dialog = new AboutDialog(frame, true);
        Assert.assertNotNull(dialog);
        JUnitUtil.dispose(dialog);
        JUnitUtil.dispose(frame);
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testShowAndClose() {
        JFrame frame = new JFrame();
        AboutDialog dialog = new AboutDialog(frame, true);

        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("TitleAbout", jmri.Application.getApplicationName()));
            jdo.close();
        });
        t.setName("About Dialog Close Thread");
        t.start();
        ThreadingUtil.runOnGUI(() -> {
            dialog.setVisible(true);
        });
        JUnitUtil.waitFor(() -> {
            return !dialog.isVisible();
        }, "About dialog did not close");
        JUnitUtil.dispose(dialog);
        JUnitUtil.dispose(frame);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initConnectionConfigManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows(false, false); // don't display the list of windows,
        // it will display only show the ones
        // from the current test.
        JUnitUtil.tearDown();
    }
}
