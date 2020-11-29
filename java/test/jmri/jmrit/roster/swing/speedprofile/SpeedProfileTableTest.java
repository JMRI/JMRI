package jmri.jmrit.roster.swing.speedprofile;

import java.awt.GraphicsEnvironment;

import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SpeedProfileTableTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        RosterEntry re = new RosterEntry();
        re.setSpeedProfile(new RosterSpeedProfile(re));
        SpeedProfileTable t = new SpeedProfileTable(re.getSpeedProfile(), re.getId());
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SpeedProfileTableTest.class);

}
