package jmri.jmrit.operations.routes.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintRoutesActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PrintRoutesAction t = new PrintRoutesAction(true);
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.createFiveLocationRoute();
        JUnitOperationsUtil.createThreeLocationRoute();
        PrintRoutesAction pra = new PrintRoutesAction(true);
        Assert.assertNotNull("exists", pra);
        pra.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

        ResourceBundle rb = ResourceBundle.getBundle("jmri.util.UtilBundle");

        JmriJFrame f =
                JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") + " " + Bundle.getMessage("TitleRoutesTable"));
        Assert.assertNotNull("exists", f);
        JUnitUtil.dispose(f);
    }

    // private final static Logger log =
    // LoggerFactory.getLogger(PrintRoutesActionTest.class);

}
