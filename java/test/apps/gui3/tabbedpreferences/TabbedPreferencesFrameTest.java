package apps.gui3.tabbedpreferences;

import java.awt.GraphicsEnvironment;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TabbedPreferencesFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TabbedPreferencesFrame t = new TabbedPreferencesFrame();
        Assert.assertNotNull("exists",t);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.resetPreferencesProviders();
    }
    
    @After
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TabbedPreferencesFrameTest.class);

}
