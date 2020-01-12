package jmri.jmrit.operations.locations.tools;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LocationTrackBlockingOrderActionTest extends OperationsTestCase {

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        Location ni = InstanceManager.getDefault(LocationManager.class).getLocationByName("North Industries");
        Assert.assertNotNull("exists", ni);
        LocationTrackBlockingOrderAction ltb = new LocationTrackBlockingOrderAction(ni);
        Assert.assertNotNull("exists", ltb);
        ltb.actionPerformed(new ActionEvent("Test Action", 0, null));
        // confirm window exists
        JmriJFrame bof = JmriJFrame.getFrame(Bundle.getMessage("TitleTrackBlockingOrder"));
        Assert.assertNotNull("exists", bof);
        JUnitUtil.dispose(bof);
    }

    // private final static Logger log = LoggerFactory.getLogger(LocationTrackBlockingOrderActionTest.class);

}
