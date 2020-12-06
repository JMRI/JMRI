package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintTrainsByCarTypesActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PrintTrainsByCarTypesAction t = new PrintTrainsByCarTypesAction(true);
        Assert.assertNotNull("exists", t);
    }
    
    @Test
    public void testPrintAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        PrintTrainsByCarTypesAction pa = new PrintTrainsByCarTypesAction(true);
        Assert.assertNotNull("exists", pa);
        
        pa.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        
        // confirm print preview window is showing
        ResourceBundle rb = ResourceBundle
                .getBundle("jmri.util.UtilBundle");
        JmriJFrame printPreviewFrame = JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") +
                " " + Bundle.getMessage("TitleTrainsByType"));
        Assert.assertNotNull("exists", printPreviewFrame);

        JUnitUtil.dispose(printPreviewFrame);      
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintTrainsByCarTypesActionTest.class);

}
