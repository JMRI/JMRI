package jmri.swing;

import apps.tests.Log4JFixture;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test simple functioning of AboutDialog
 *
 * @author	Paul Bender Copyright (C) 2017
 */
public class AboutDialogTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame frame = new JFrame("Test Frame");
        AboutDialog dialog = new AboutDialog(frame,true);
    }

    @Test
    public void testShowAndClose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConnectionConfigManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
