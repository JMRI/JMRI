package jmri.util.prefs;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JmriUserInterfaceConfigurationProviderTest {

    @Test
    public void testCTor() {
        JmriUserInterfaceConfigurationProvider t = new JmriUserInterfaceConfigurationProvider(jmri.profile.ProfileManager.getDefault().getActiveProfile());
        Assertions.assertNotNull( t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JmriUserInterfaceConfigurationProviderTest.class);
}
