package jmri.jmrit.roster;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RosterConfigPaneTest {

    @Test
    public void testCTor() {
        RosterConfigPane t = new RosterConfigPane();
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.InstanceManager.setDefault(RosterConfigManager.class,new RosterConfigManager());
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RosterConfigPaneTest.class);

}
