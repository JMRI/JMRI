package jmri.jmrit.vsdecoder.swing;

import java.awt.GraphicsEnvironment;
import java.util.List;

import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.ReporterManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.vsdecoder.listener.ListeningSpot;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ManageLocationsFrameTest extends jmri.util.JmriJFrameTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
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

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ManageLocationsFrameTest.class.getName());

}
