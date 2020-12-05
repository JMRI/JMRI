package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

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
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class IgnoreUsedTrackActionTest extends OperationsTestCase {
    
    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Location location = lManager.getLocationById("20");
        Track track = location.getTrackById("20s1");
        
        IgnoreUsedTrackAction a = new IgnoreUsedTrackAction(track);
        Assert.assertNotNull("exists", a);
        
        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        
        JmriJFrame f = JmriJFrame.getFrame(Bundle.getMessage("MenuItemPlannedPickups"));
        Assert.assertNotNull("exists", f);
        JUnitUtil.dispose(f);
    }

    // private final static Logger log = LoggerFactory.getLogger(IgnoreUsedTrackActionTest.class);

}
