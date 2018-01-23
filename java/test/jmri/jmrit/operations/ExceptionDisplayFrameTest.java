package jmri.jmrit.operations;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ExceptionDisplayFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ExceptionContext ec = new ExceptionContext(new Exception("Test"), "Test", "Test");
        ExceptionDisplayFrame dialog = new ExceptionDisplayFrame(ec, null);
        Assert.assertNotNull("exists", dialog);
        Assert.assertEquals(Exception.class.getSimpleName(), dialog.getTitle());
        JUnitUtil.dispose(dialog);
    }

    @Test
    public void testSetVisible() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ExceptionContext ec = new ExceptionContext(new Exception("Test"), "Test", "Test");
        JFrame testFrame = new JFrame();
        new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(ec.getTitle());
            jdo.requestClose();
        }).start();
        ExceptionDisplayFrame dialog = new ExceptionDisplayFrame(ec, testFrame);
        dialog.setName(ec.getTitle());
        Assert.assertNotNull("exists", dialog);
        dialog.setVisible(true);
        JUnitUtil.waitFor(() -> {
            return !dialog.isVisible();
        }, "Exception Frame did not close");
        JUnitUtil.dispose(dialog);
        JUnitUtil.dispose(testFrame);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ExceptionDisplayFrameTest.class);
}
