package jmri.jmrit.operations.trains;

import java.io.File;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrainCsvSwitchListsTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        TrainCsvSwitchLists t = new TrainCsvSwitchLists();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCreateCsvSwtichList() {
        TrainCsvSwitchLists tcs = new TrainCsvSwitchLists();
        Assert.assertNotNull("exists",tcs);
        
        jmri.util.JUnitOperationsUtil.initOperationsData();
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location location = lmanager.getLocationByName("North End Staging");
        Assert.assertNotNull(location);
        
        // create some work
        Train train1 = InstanceManager.getDefault(TrainManager.class).getTrainById("1");
        Assert.assertTrue(train1.build());
                       
        File file = tcs.buildSwitchList(location);
        Assert.assertTrue(file.exists());
        
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainCsvSwitchListsTest.class);

}
