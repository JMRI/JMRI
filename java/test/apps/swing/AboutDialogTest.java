package apps.swing;

import javax.swing.JFrame;

import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 * Test simple functioning of AboutDialog
 *
 * @author Paul Bender Copyright (C) 2017
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class AboutDialogTest {

    @Test
    public void testCtor() {
        // create a frame to be the dialog parent so that nothing attempts to
        // remove the SwingUtilities$SharedOwnerFrame instance
        JFrame frame = new JFrame();
        AboutDialog dialog = new AboutDialog(frame, true);
        Assertions.assertNotNull(dialog);
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testShowAndClose() {
        JFrame frame = new JFrame();
        AboutDialog dialog = new AboutDialog(frame, true);

        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("TitleAbout", jmri.Application.getApplicationName()));
            JButtonOperator jbo = new JButtonOperator(jdo, "OK");
            jbo.doClick();
            jdo.waitClosed();
        });
        t.setName("About Dialog Close Thread");
        t.start();
        ThreadingUtil.runOnGUI(() -> dialog.setVisible(true));
        JUnitUtil.waitFor(() -> {
            return !t.isAlive();
        }, "About dialog did not close");
        JUnitUtil.dispose(frame);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initConnectionConfigManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
