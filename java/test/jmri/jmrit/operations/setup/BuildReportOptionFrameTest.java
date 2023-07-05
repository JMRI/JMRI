package jmri.jmrit.operations.setup;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class BuildReportOptionFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        BuildReportOptionFrame t = new BuildReportOptionFrame();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testBuildReportOptionFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        BuildReportOptionFrame f = new BuildReportOptionFrame();
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents();

        // TODO do more testing

        // done
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        BuildReportOptionFrame brof = new BuildReportOptionFrame();
        brof.initComponents();

        JFrameOperator jfo = new JFrameOperator(brof.getTitle());
        Assert.assertNotNull("visible and found", jfo);

        // confirm window appears
        JmriJFrame f = JmriJFrame.getFrame(Bundle.getMessage("TitleBuildReportOptions"));
        Assert.assertNotNull("exists", f);
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave")).doClick();
        f = JmriJFrame.getFrame(Bundle.getMessage("TitleBuildReportOptions"));
        Assert.assertNotNull("exists", f);
        // now close window with save button
        Setup.setCloseWindowOnSaveEnabled(true);
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave")).doClick();
        jfo.waitClosed();
        // confirm window is closed
        f = JmriJFrame.getFrame(Bundle.getMessage("TitleBuildReportOptions"));
        Assert.assertNull("does not exist", f);
    }

    // private final static Logger log = LoggerFactory.getLogger(BuildReportOptionFrameTest.class);

}
