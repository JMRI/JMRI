package apps;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SystemConsoleConfigPanelTest {

    @Test
    public void testCTor() {
        SystemConsoleConfigPanel t = new SystemConsoleConfigPanel();
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.InstanceManager.setDefault(apps.systemconsole.SystemConsolePreferencesManager.class,new apps.systemconsole.SystemConsolePreferencesManager());
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SystemConsoleConfigPanelTest.class);

}
