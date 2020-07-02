package apps;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ManagerDefaultsConfigPaneTest {

    @Test
    public void testCTor() {
        ManagerDefaultsConfigPane t = new ManagerDefaultsConfigPane();
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetPreferencesProviders();
        JUnitUtil.initConfigureManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ManagerDefaultsConfigPaneTest.class);

}
