package jmri.jmrit.roster.swing;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RosterGroupsPanelTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        RosterGroupsPanel t = new RosterGroupsPanel();
        Assert.assertNotNull("exists", t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RosterGroupsPanelTest.class);
}
