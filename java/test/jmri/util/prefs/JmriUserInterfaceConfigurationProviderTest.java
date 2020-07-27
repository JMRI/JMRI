package jmri.util.prefs;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JmriUserInterfaceConfigurationProviderTest {

    @Test
    public void testCTor() {
        JmriUserInterfaceConfigurationProvider t = new JmriUserInterfaceConfigurationProvider(jmri.profile.ProfileManager.getDefault().getActiveProfile());
        Assert.assertNotNull("exists", t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.resetProfileManager();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JmriUserInterfaceConfigurationProviderTest.class);
}
