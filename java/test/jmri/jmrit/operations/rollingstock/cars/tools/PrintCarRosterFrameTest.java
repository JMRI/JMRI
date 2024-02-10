package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.GraphicsEnvironment;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.CarsTableFrame;
import jmri.util.*;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintCarRosterFrameTest extends OperationsTestCase {

    @Test
    public void testPrintPreview() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();
        CarsTableFrame ctf = new CarsTableFrame(true, null, null);

        PrintCarRosterFrame f = new PrintCarRosterFrame(true, ctf);
        Assert.assertNotNull("exists", f);
        f.initComponents();

        JemmyUtil.enterClickAndLeave(f.okayButton); // closes window

        // confirm print preview window is showing
        ResourceBundle rb = ResourceBundle
                .getBundle("jmri.util.UtilBundle");
        JmriJFrame printPreviewFrame = JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") + " " +Bundle.getMessage("TitleCarRoster"));
        Assert.assertNotNull("exists", printPreviewFrame);

        JUnitUtil.dispose(f);
        JUnitUtil.dispose(printPreviewFrame);
        JUnitUtil.dispose(ctf);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
}
