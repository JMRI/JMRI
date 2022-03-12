package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintSwitchListActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Location l = new Location("Test id", "Test Name");
        PrintSwitchListAction t = new PrintSwitchListAction(l, true);
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Location l = new Location("Test id", "Test Name");
        PrintSwitchListAction a = new PrintSwitchListAction(l, true);
        Assert.assertNotNull("exists", a);

        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        
        ResourceBundle rb = ResourceBundle.getBundle("jmri.util.UtilBundle");

        JmriJFrame f = JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") + " " + l.getName());
        Assert.assertNotNull("frame exists", f);
        JUnitUtil.dispose(f);
    }

    // private final static Logger log =
    // LoggerFactory.getLogger(PrintSwitchListActionTest.class);

}
