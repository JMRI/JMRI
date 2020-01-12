package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.TrainsTableFrame;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintTrainsActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainsTableFrame ttf = new TrainsTableFrame();
        PrintTrainsAction t = new PrintTrainsAction("Test Action", true, ttf);
        Assert.assertNotNull("exists", t);
        
        JUnitUtil.dispose(ttf);
    }
    
    @Test
    public void testPrintAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();

        TrainsTableFrame ttf = new TrainsTableFrame();
        PrintTrainsAction pta = new PrintTrainsAction("Test Action", true, ttf);
        Assert.assertNotNull("exists", pta);

        pta.actionPerformed(new ActionEvent(this, 0, null));

        // confirm print preview window is showing
        ResourceBundle rb = ResourceBundle
                .getBundle("jmri.util.UtilBundle");
        JmriJFrame printPreviewFrame = JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") +
                " " +  Bundle.getMessage("TitleTrainsTable"));

        Assert.assertNotNull("exists", printPreviewFrame);

        JUnitUtil.dispose(printPreviewFrame);
        JUnitUtil.dispose(ttf);
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintTrainsActionTest.class);

}
