package jmri.jmrit.operations.trains;

import java.io.File;
import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrainCsvSwitchListsTest {

    @Test
    public void testCTor() {
        TrainCsvSwitchLists t = new TrainCsvSwitchLists();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCreateCsvSwtichList() {
        TrainCsvSwitchLists tcs = new TrainCsvSwitchLists();
        Assert.assertNotNull("exists",tcs);
        
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location location = lmanager.getLocationByName("North End Staging");
        Assert.assertNotNull(location);
        
        // create some work
        Train train1 = InstanceManager.getDefault(TrainManager.class).getTrainById("1");
        Assert.assertTrue(train1.build());
                       
        File file = tcs.buildSwitchList(location);
        Assert.assertTrue(file.exists());
        
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitOperationsUtil.resetOperationsManager();
        jmri.util.JUnitOperationsUtil.initOperationsData();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainCsvSwitchListsTest.class);

}
