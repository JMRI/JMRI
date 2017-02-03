package jmri.swing;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

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
        Assert.assertNotNull("exists", dialog);
        Thread waitThread = new Thread(){
           public void run(){
              JFrameOperator jfo = new JFrameOperator("Test Frame");
              JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("TitleAbout","JMRI"));
              jdo.close();
           }
        };
        waitThread.start();
        ThreadingUtil.runOnGUI( () -> {
            frame.setVisible(true);
            dialog.setVisible(true);
        }); 
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
