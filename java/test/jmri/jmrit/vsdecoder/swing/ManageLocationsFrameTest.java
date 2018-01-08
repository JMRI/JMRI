package jmri.jmrit.vsdecoder.swing;

import java.awt.GraphicsEnvironment;
import java.util.List;
import jmri.ReporterManager;
import jmri.BlockManager;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.vsdecoder.listener.ListeningSpot;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ManageLocationsFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ListeningSpot s = new ListeningSpot();
        ReporterManager rmgr = jmri.InstanceManager.getDefault(jmri.ReporterManager.class);
        String[] reporterNameArray = rmgr.getSystemNameArray();
        Object[][] reporterTable = new Object[reporterNameArray.length][6];
        BlockManager bmgr = jmri.InstanceManager.getDefault(jmri.BlockManager.class);
        String[] blockNameArray = bmgr.getSystemNameArray();
        Object[][] blockTable = new Object[blockNameArray.length][6];

        LocationManager lmgr = LocationManager.instance();
        List<Location> locations = lmgr.getLocationsByIdList();
        Object[][] opsTable = new Object[locations.size()][6];


        ManageLocationsFrame t = new ManageLocationsFrame(s,reporterTable,blockTable,opsTable);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(ManageLocationsFrameTest.class.getName());

}
