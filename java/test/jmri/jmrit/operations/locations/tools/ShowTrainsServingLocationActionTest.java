package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class ShowTrainsServingLocationActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Location l = new Location("Location Test Attridutes id", "Location Test Name");
        Track tr = new Track("Test id", "Test Name", "Test Type", l);
        ShowTrainsServingLocationAction t = new ShowTrainsServingLocationAction(l, tr);
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location l = lmanager.getLocationById("20");
        Track t = l.getTrackById("20s1");

        ShowTrainsServingLocationAction a = new ShowTrainsServingLocationAction(l, t);
        Assert.assertNotNull("exists", a);

        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

        JmriJFrame f = JmriJFrame.getFrame(MessageFormat.format(Bundle.getMessage("TitleShowTrains"), new Object[]{t.getName()}));
        Assert.assertNotNull("frame exists", f);
        JUnitUtil.dispose(f);
    }

    // private final static Logger log =
    // LoggerFactory.getLogger(ShowTrainsServingLocationActionTest.class);

}
