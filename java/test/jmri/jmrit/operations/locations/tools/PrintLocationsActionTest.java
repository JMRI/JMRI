package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.util.*;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintLocationsActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        PrintLocationsAction t = new PrintLocationsAction(true);
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testActionPerformed() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();
        Location location = InstanceManager.getDefault(LocationManager.class).getLocationByName("North Industries");

        PrintLocationsAction pla = new PrintLocationsAction(true, location);
        Assert.assertNotNull("exists", pla);

        pla.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

        // confirm print option window is showing
        JmriJFrame f = JmriJFrame.getFrame(Bundle.getMessage("MenuItemPreview"));
        Assert.assertNotNull("exists", f);

        PrintLocationsFrame plf = (PrintLocationsFrame) f;
        JemmyUtil.enterClickAndLeave(plf.okayButton); // closes window

        // confirm print preview window is showing
        ResourceBundle rb = ResourceBundle
                .getBundle("jmri.util.UtilBundle");
        JmriJFrame printPreviewFrame =
                JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") + " " + location.getName());
        Assert.assertNotNull("exists", printPreviewFrame);

        JUnitUtil.dispose(printPreviewFrame);

        JUnitUtil.dispose(plf);
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintLocationsActionTest.class);

}
