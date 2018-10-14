package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import java.util.ResourceBundle;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.tools.PrintLocationsAction.LocationPrintOptionFrame;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JemmyUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintLocationsActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        PrintLocationsAction t = new PrintLocationsAction("test action", true);
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testPrintPreview() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitOperationsUtil.initOperationsData();
        PrintLocationsAction pla = new PrintLocationsAction("test action", true);
        Assert.assertNotNull("exists", pla);
        // select all options
        pla.printSchedules.setSelected(true);
        pla.printComments.setSelected(true);
        pla.printDetails.setSelected(true);
        pla.printAnalysis.setSelected(true);
        pla.printErrorAnalysis.setSelected(true);
        pla.printLocations();
    }
    
    @Test
    public void testPrintOptionsFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitOperationsUtil.initOperationsData();
        PrintLocationsAction pla = new PrintLocationsAction("test action", true);
        Assert.assertNotNull("exists", pla);
        
        LocationPrintOptionFrame f = pla.new LocationPrintOptionFrame(pla);
        Assert.assertNotNull("exists", f);
        
        JemmyUtil.enterClickAndLeave(f.okayButton); // closes window
        
        // confirm print preview window is showing
        ResourceBundle rb = ResourceBundle
                .getBundle("jmri.util.UtilBundle");
        JmriJFrame printPreviewFrame = JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") + " " +Bundle.getMessage("TitleLocationsTable"));
        Assert.assertNotNull("exists", printPreviewFrame);
        
        JUnitUtil.dispose(printPreviewFrame);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintLocationsActionTest.class);

}
