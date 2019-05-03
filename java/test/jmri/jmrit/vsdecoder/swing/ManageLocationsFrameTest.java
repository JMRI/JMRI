package jmri.jmrit.vsdecoder.swing;

import java.awt.GraphicsEnvironment;
import java.util.List;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.ReporterManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.vsdecoder.listener.ListeningSpot;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ManageLocationsFrameTest extends jmri.util.JmriJFrameTestBase {

    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        ListeningSpot s = new ListeningSpot();
        ReporterManager rmgr = jmri.InstanceManager.getDefault(jmri.ReporterManager.class);
        String[] reporterNameArray = rmgr.getSystemNameArray(); // deprecated, but we test until removed
        jmri.util.JUnitAppender.suppressWarnMessage("Manager#getSystemNameArray() is deprecated");

        Object[][] reporterTable = new Object[reporterNameArray.length][6];
        BlockManager bmgr = jmri.InstanceManager.getDefault(jmri.BlockManager.class);
        String[] blockNameArray = bmgr.getSystemNameArray(); // deprecated, but we test until removed
        jmri.util.JUnitAppender.suppressWarnMessage("Manager#getSystemNameArray() is deprecated");
        Object[][] blockTable = new Object[blockNameArray.length][6];

        LocationManager lmgr = InstanceManager.getDefault(LocationManager.class);
        List<Location> locations = lmgr.getLocationsByIdList();
        Object[][] opsTable = new Object[locations.size()][6];


        if(!GraphicsEnvironment.isHeadless()){
           frame = new ManageLocationsFrame(s,reporterTable,blockTable,opsTable);
        }
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ManageLocationsFrameTest.class.getName());

}
