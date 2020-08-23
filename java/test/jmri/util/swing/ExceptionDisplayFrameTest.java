package jmri.util.swing;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@Timeout(10)
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
    @Disabled("The JDialogOperator is having trouble finding the dialog")
    public void testSetVisible() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Exception ex = new Exception("Test");
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(ex.getClass().getSimpleName());
            jdo.requestClose();
        });
        t.setName("Exception Dialog Close Thread");
        t.start();
        ExceptionDisplayFrame dialog = new ExceptionDisplayFrame(ex, null);
        dialog.setVisible(true);
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        JUnitUtil.waitFor(() -> {
            return !dialog.isVisible();
        }, "Exception Frame did not close");
        JUnitUtil.dispose(dialog);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ExceptionDisplayFrameTest.class);
}
