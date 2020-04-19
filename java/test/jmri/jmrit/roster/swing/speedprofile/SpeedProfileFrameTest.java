package jmri.jmrit.roster.swing.speedprofile;

import java.awt.GraphicsEnvironment;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SpeedProfileFrameTest extends jmri.util.JmriJFrameTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new SpeedProfileFrame();
        }
    }

    @After
    @Override
    public void tearDown() {
        InstanceManager.getDefault(ShutDownManager.class).deregister(InstanceManager.getDefault(BlockManager.class).shutDownTask);
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SpeedProfileFrameTest.class);
}
