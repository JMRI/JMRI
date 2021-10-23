package jmri.jmrit.operations.locations;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
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
        YardmasterByTrackFrame t = new YardmasterByTrackFrame(null);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }
    
    @Test
    public void testPanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitOperationsUtil.initOperationsData();
        // increase coverage by requiring text headers
        Setup.setPrintHeadersEnabled(true);
        
        // place engines at start of train's route
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location locationNorthEnd = lmanager.getLocationByName("North End Staging");
        Track trackNorthEnd = locationNorthEnd.getTrackByName("North End 1", null);
        EngineManager eManager = InstanceManager.getDefault(EngineManager.class);
        Engine e1 = eManager.getByRoadAndNumber("PC", "5016");
        Assert.assertNotNull("engine exists", e1);
        e1.setLocation(locationNorthEnd, trackNorthEnd);
        
        Engine e2 = eManager.getByRoadAndNumber("PC", "5019");
        Assert.assertNotNull("engine exists", e2);
        e2.setLocation(locationNorthEnd, trackNorthEnd);
        
        // create data by building a train
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
                
        Train train = tmanager.getTrainByName("STF");
        train.setNumberEngines("2");
        train.build();
        
        // 1st location
        YardmasterByTrackFrame f = new YardmasterByTrackFrame(locationNorthEnd);
        Assert.assertNotNull("exists", f);
        YardmasterByTrackPanel yp = (YardmasterByTrackPanel)f.getContentPane();
        JemmyUtil.enterClickAndLeave(yp.nextButton);
        JUnitUtil.dispose(f);
        
        // 2nd location in train's route
        Location locationNorthIndustries = lmanager.getLocationByName("North Industries");
        f = new YardmasterByTrackFrame(locationNorthIndustries);
        Assert.assertNotNull("exists", f);
        yp = (YardmasterByTrackPanel)f.getContentPane();
        JemmyUtil.enterClickAndLeave(yp.nextButton);
        JUnitUtil.dispose(f);
        
        // 3rd location in train's route
        Location locationSouthEnd = lmanager.getLocationByName("South End Staging");
        f = new YardmasterByTrackFrame(locationSouthEnd);
        Assert.assertNotNull("exists", f);
        yp = (YardmasterByTrackPanel)f.getContentPane();
        JemmyUtil.enterClickAndLeave(yp.nextButton);
        JUnitUtil.dispose(f);
        
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    @Test
    public void testLoop2() {
        for (int i = 0; i < 1000; i++) {
            setUp();
            testPanel();
            tearDown();
        }
    }

    // private final static Logger log = LoggerFactory.getLogger(YardmasterByTrackFrameTest.class);
}
