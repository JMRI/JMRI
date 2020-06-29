package apps;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.InstanceManager.setDefault(apps.systemconsole.SystemConsolePreferencesManager.class,new apps.systemconsole.SystemConsolePreferencesManager());
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SystemConsoleConfigPanelTest.class);

}
