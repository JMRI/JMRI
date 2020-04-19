package jmri.jmrit.vsdecoder.swing;

import java.awt.GraphicsEnvironment;
import java.util.List;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.ReporterManager;
import jmri.ShutDownManager;
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
        Object[][] reporterTable = new Object[rmgr.getObjectCount()][6];
        
        BlockManager bmgr = jmri.InstanceManager.getDefault(jmri.BlockManager.class);
        Object[][] blockTable = new Object[bmgr.getObjectCount()][6];

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
        InstanceManager.getDefault(ShutDownManager.class).deregister(InstanceManager.getDefault(BlockManager.class).shutDownTask);
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ManageLocationsFrameTest.class.getName());

}
