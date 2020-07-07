package apps.startup;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class StartupActionsPreferencesPanelTest {

    @Test
    public void testCTor() {
        StartupActionsPreferencesPanel t = new StartupActionsPreferencesPanel();
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();        jmri.util.JUnitUtil.initStartupActionsManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(StartupActionsPreferencesPanelTest.class);

}
