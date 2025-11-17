package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.GraphicsEnvironment;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.engines.gui.EnginesTableFrame;
import jmri.util.*;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintEngineRosterFrameTest extends OperationsTestCase {

    @Test
    public void testPrintPreview() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();
        EnginesTableFrame ctf = new EnginesTableFrame(true, null, null);

        PrintEngineRosterFrame f = new PrintEngineRosterFrame(true, ctf);
        Assert.assertNotNull("exists", f);

        JemmyUtil.enterClickAndLeave(f.okayButton); // closes window

        // confirm print preview window is showing
        ResourceBundle rb = ResourceBundle
                .getBundle("jmri.util.UtilBundle");
        JmriJFrame printPreviewFrame =
                JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") + " " + Bundle.getMessage("TitleEngineRoster"));
        Assert.assertNotNull("exists", printPreviewFrame);

        JUnitUtil.dispose(printPreviewFrame);
        JUnitUtil.dispose(ctf);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
}
