package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.locations.gui.YardEditFrame;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrackLoadEditActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        YardEditFrame tf = new YardEditFrame();
        TrackLoadEditAction t = new TrackLoadEditAction(tf);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Location location = lManager.getLocationById("20");
        Track track = location.getTrackById("20s1");
        YardEditFrame tf = new YardEditFrame();
        tf.initComponents(location, track);
        
        TrackLoadEditAction a = new TrackLoadEditAction(tf);
        Assert.assertNotNull("exists", a);
        
        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        
        JmriJFrame f = JmriJFrame.getFrame(Bundle.getMessage("TitleEditTrackLoads"));
        Assert.assertNotNull("frame exists", f);
        JUnitUtil.dispose(f);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrackLoadEditActionTest.class);

}
