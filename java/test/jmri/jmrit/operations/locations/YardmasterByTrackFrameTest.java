package jmri.jmrit.operations.locations;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsPanel;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class YardmasterByTrackFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Location l = new Location("Location Test Attridutes id", "Location Test Name");
        YardmasterByTrackFrame t = new YardmasterByTrackFrame(l);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }
    
    @Test
    public void testPanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitOperationsUtil.initOperationsData();
        // increase coverage by requiring text headers
        Setup.setPrintHeadersEnabled(true);
        
        // create data by building a train
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
                
        Train train = tmanager.getTrainByName("STF");
        train.build();
        
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        
        Location location = lmanager.getLocationByName("North Industries");
        Assert.assertNotNull("exists", location);

        YardmasterByTrackFrame f = new YardmasterByTrackFrame(location);
        Assert.assertNotNull("exists", f);
        
        OperationsPanel p = f.getContentPane();
        YardmasterByTrackPanel yp = (YardmasterByTrackPanel)p;
        
        JemmyUtil.enterClickAndLeave(yp.nextButton);
        JUnitUtil.dispose(f);
    }

    // private final static Logger log = LoggerFactory.getLogger(YardmasterByTrackFrameTest.class);
}
