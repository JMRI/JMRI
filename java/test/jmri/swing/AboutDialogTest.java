package jmri.swing;

import apps.tests.Log4JFixture;
import java.awt.GraphicsEnvironment;
<<<<<<< HEAD
import javax.swing.JFrame;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
=======
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
>>>>>>> JMRI/master
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
<<<<<<< HEAD
import org.junit.Ignore;
import org.junit.Test;
=======
import org.junit.Test;
import org.netbeans.jemmy.operators.JDialogOperator;
>>>>>>> JMRI/master

/**
 * Test simple functioning of AboutDialog
 *
<<<<<<< HEAD
 * @author	Paul Bender Copyright (C) 2017
=======
 * @author Paul Bender Copyright (C) 2017
>>>>>>> JMRI/master
 */
public class AboutDialogTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
<<<<<<< HEAD
        JFrame frame = new JFrame("Test Frame");
        AboutDialog dialog = new AboutDialog(frame,true);
=======
        AboutDialog dialog = new AboutDialog(null, true);
        Assert.assertNotNull(dialog);
>>>>>>> JMRI/master
    }

    @Test
    public void testShowAndClose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
<<<<<<< HEAD
        JFrame frame = new JFrame("About Dialog Test Frame");
        AboutDialog dialog = new AboutDialog(frame,true);

        Thread waitThread = new Thread(){
           @Override
           public void run(){
              // constructor for jfo and jdo will wait until the frame and
              // dialog are visible.
              JFrameOperator jfo = new JFrameOperator("About Dialog Test Frame");
              JDialogOperator jdo = new JDialogOperator(jfo,Bundle.getMessage("TitleAbout",jmri.Application.getApplicationName()));
              jdo.close();
           }
        };
        waitThread.start();
        frame.setVisible(true);
        dialog.setVisible(true);
=======
        AboutDialog dialog = new AboutDialog(null, true);

        new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("TitleAbout", jmri.Application.getApplicationName()));
            jdo.close();
        }).start();
        ThreadingUtil.runOnGUI( () -> { dialog.setVisible(true); });
        JUnitUtil.waitFor(() -> {
            return !dialog.isVisible();
        }, "About dialog did not close");
>>>>>>> JMRI/master
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
<<<<<<< HEAD
=======
        JUnitUtil.resetWindows(true);
>>>>>>> JMRI/master
        JUnitUtil.initConnectionConfigManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
<<<<<<< HEAD
=======
        JUnitUtil.resetWindows(false); // don't display the list of windows, 
                                       // it will display only show the ones
                                       // from the current test. 
>>>>>>> JMRI/master
        Log4JFixture.tearDown();
    }
}
