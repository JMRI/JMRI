package jmri.jmrit.dispatcher;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.logix.WarrantPreferences;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AutoTrainsFrameTest extends jmri.util.JmriJFrameTestBase {
        
    DispatcherFrame d = null;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");  // exist?
        JUnitUtil.initDebugThrottleManager();
        if (!GraphicsEnvironment.isHeadless()) {
            WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
            d = InstanceManager.getDefault(DispatcherFrame.class);
            frame = new AutoTrainsFrame(d);
        }
    }

    @After
    @Override
    public void tearDown() {
        if (d!=null) {
            JUnitUtil.dispose(d);
            d = null;
        }
        super.tearDown(); // handles frame member
    }

    // private final static Logger log = LoggerFactory.getLogger(AutoTrainsFrameTest.class);

}
