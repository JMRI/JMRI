package jmri.jmrit.operations.routes.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.routes.Route;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintRouteActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Route r = new Route("Test Route", "Test ID");
        PrintRouteAction t = new PrintRouteAction(true, r);
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Route r = JUnitOperationsUtil.createThreeLocationTurnRoute();
        PrintRouteAction pra = new PrintRouteAction(true, r);
        Assert.assertNotNull("exists", pra);
        pra.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

        ResourceBundle rb = ResourceBundle.getBundle("jmri.util.UtilBundle");

        JmriJFrame f = JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") +
                " " +
                MessageFormat.format(Bundle.getMessage("TitleRoute"),
                        new Object[]{r.getName()}));
        Assert.assertNotNull("exists", f);
        JUnitUtil.dispose(f);
    }
}
