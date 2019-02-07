package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.GraphicsEnvironment;
import java.util.ResourceBundle;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.engines.EnginesTableFrame;
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
public class PrintEngineRosterActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EnginesTableFrame etf = new EnginesTableFrame();
        PrintEngineRosterAction t = new PrintEngineRosterAction("Test Action",true,etf);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(etf);
    }
    
    @Test
    public void testPrintPreview() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();
        EnginesTableFrame ctf = new EnginesTableFrame();
        PrintEngineRosterAction pcra = new PrintEngineRosterAction("Test Action", true, ctf);
        Assert.assertNotNull("exists", pcra);
        
        pcra.printEngines();
        
        // confirm print preview window is showing
        ResourceBundle rb = ResourceBundle
                .getBundle("jmri.util.UtilBundle");
        JmriJFrame printPreviewFrame = JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") + " " +Bundle.getMessage("TitleEngineRoster"));
        Assert.assertNotNull("exists", printPreviewFrame);
        
        JUnitUtil.dispose(printPreviewFrame);
        JUnitUtil.dispose(ctf);
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintEngineRosterActionTest.class);

}
