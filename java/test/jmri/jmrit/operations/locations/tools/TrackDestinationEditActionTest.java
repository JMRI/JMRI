package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.*;
import jmri.util.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrackDestinationEditActionTest extends OperationsTestCase {
    
    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Location location = lManager.getLocationById("20");
        Track track = location.getTrackById("20s1");
        
        YardEditFrame yef = new YardEditFrame();
        yef.initComponents(track);
        
        TrackDestinationEditAction a = new TrackDestinationEditAction(yef);
        Assert.assertNotNull("exists", a);
        
        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        
        JmriJFrame f = JmriJFrame.getFrame(Bundle.getMessage("TitleEditTrackDestinations"));
        Assert.assertNotNull("frame exists", f);
        JUnitUtil.dispose(f);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrackDestinationEditActionTest.class);

}
