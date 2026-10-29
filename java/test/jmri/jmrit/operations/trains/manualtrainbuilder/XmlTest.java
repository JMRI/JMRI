package jmri.jmrit.operations.trains.manualtrainbuilder;

import java.io.IOException;
import java.util.List;

import org.jdom2.JDOMException;
import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.trains.*;
import jmri.util.JUnitOperationsUtil;

/**
 * Test train builder save and restore
 *
 * @author Daniel Boudreau Copyright (C) 2026
 */
public class XmlTest extends OperationsTestCase {

 
    @Test
    public void testXMLCreateManualBuild() throws JDOMException, IOException {
        
        JUnitOperationsUtil.initOperationsData();

        TrainManager manager = InstanceManager.getDefault(TrainManager.class);
        Train train = manager.getTrainByName("STF");
        Assert.assertNotNull(train);
        
        TrainManualBuildManager tmbm = InstanceManager.getDefault(TrainManualBuildManager.class);
        TrainManualBuild tmb = tmbm.newManualBuild(train.getId());
        
        // add two manual build items
        TrainManualBuildItem tmbi1 = tmb.addItem();
        tmbi1.setTypeName("Boxcar");
        tmbi1.setLoadName("LoadName");
        tmbi1.setFailEnabled(true);
        tmbi1.setRemoveEnabled(true);
        tmbi1.setRouteLocation(train.getTrainDepartsRouteLocation());
        Location location = train.getTrainDepartsRouteLocation().getLocation();
        Assert.assertNotNull(location);
        Track locationTrack = location.getTrackById("1s1");
        Assert.assertNotNull(locationTrack);
        tmbi1.setLocationTrack(locationTrack);
        
        TrainManualBuildItem tmbi2 = tmb.addItem();
        tmbi2.setRoadName("SP");
        tmbi2.setCount(2);
        tmbi2.setWarnEnabled(true);
        Location destination = train.getTrainTerminatesRouteLocation().getLocation();
        Assert.assertNotNull(destination);
        tmbi2.setDestination(destination);
        Track destTrack = destination.getTrackById("3s2");
        Assert.assertNotNull(destTrack);
        tmbi2.setDestinationTrack(destTrack);
        
        tmb.setComment("tmb comment");
        Assert.assertEquals("comment", "tmb comment", tmb.getComment());
        InstanceManager.getDefault(TrainManagerXml.class).writeOperationsFile();
        
        tmbm.deregister(tmb);
        tmb = tmbm.newManualBuild(train.getId());
        Assert.assertEquals("confirm dispose", "", tmb.getComment());
        List<TrainManualBuildItem> list = tmb.getItemsBySequenceList();
        Assert.assertEquals("number of items", 0, list.size());
        
        // need to get rid of this train manual build so reload works correctly
        tmbm.deregister(tmb);
        
        // now reload data from file
        InstanceManager.getDefault(TrainManagerXml.class).readFile(InstanceManager.getDefault(TrainManagerXml.class).getDefaultOperationsFilename());
        tmb = tmbm.newManualBuild(train.getId());
        Assert.assertEquals("comment", "tmb comment", tmb.getComment());
        list = tmb.getItemsBySequenceList();
        Assert.assertEquals("number of items", 2, list.size());
        
        tmbi1 = list.get(0);
        Assert.assertEquals("Type", "Boxcar", tmbi1.getTypeName());
        Assert.assertEquals("Load", "LoadName", tmbi1.getLoadName());
        Assert.assertEquals("Road", "", tmbi1.getRoadName());
        Assert.assertEquals("Count", 1, tmbi1.getCount());
        Assert.assertEquals("Warn", false, tmbi1.isWarnEnabled());
        Assert.assertEquals("Fail", true, tmbi1.isFailEnabled());
        Assert.assertEquals("Remove", true, tmbi1.isRemoveEnabled());
        Assert.assertEquals("Route Location", train.getTrainDepartsRouteLocation(), tmbi1.getRouteLocation());
        Assert.assertEquals("Location track", locationTrack, tmbi1.getLocationTrack());
        Assert.assertEquals("Destination", null, tmbi1.getDestination());
        Assert.assertEquals("Destination track", null, tmbi1.getDestinationTrack());
        
        tmbi2 = list.get(1);
        Assert.assertEquals("Type", "", tmbi2.getTypeName());
        Assert.assertEquals("Load", "", tmbi2.getLoadName());
        Assert.assertEquals("Road", "SP", tmbi2.getRoadName());
        Assert.assertEquals("Count", 2, tmbi2.getCount());
        Assert.assertEquals("Warn", true, tmbi2.isWarnEnabled());
        Assert.assertEquals("Fail", false, tmbi2.isFailEnabled());
        Assert.assertEquals("Remove", false, tmbi2.isRemoveEnabled());
        Assert.assertEquals("Route Location", null, tmbi2.getRouteLocation());
        Assert.assertEquals("Location track", null, tmbi2.getLocationTrack());
        Assert.assertEquals("Destination", destination, tmbi2.getDestination());
        Assert.assertEquals("Destination track", destTrack, tmbi2.getDestinationTrack());

    }

    // from here down is testing infrastructure
    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

}
