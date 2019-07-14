package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.GraphicsEnvironment;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.CarsTableFrame;
import jmri.jmrit.operations.rollingstock.cars.tools.PrintCarRosterAction.CarPrintOptionFrame;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintCarRosterActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarsTableFrame ctf = new CarsTableFrame(true, null, null);
        PrintCarRosterAction t = new PrintCarRosterAction("Test Action", true, ctf);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(ctf);
    }

    @Test
    public void testPrintPreview() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();
        CarsTableFrame ctf = new CarsTableFrame(true, null, null);
        PrintCarRosterAction pcra = new PrintCarRosterAction("Test Action", true, ctf);
        Assert.assertNotNull("exists", pcra);
        
        CarPrintOptionFrame f = pcra.new CarPrintOptionFrame(pcra);
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
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintCarRosterActionTest.class);

}
