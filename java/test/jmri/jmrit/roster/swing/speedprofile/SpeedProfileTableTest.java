package jmri.jmrit.roster.swing.speedprofile;

import java.awt.GraphicsEnvironment;
import org.netbeans.jemmy.operators.JFrameOperator;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
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
public class SpeedProfileTableTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        RosterEntry re = new RosterEntry();
        re.setSpeedProfile(new RosterSpeedProfile(re));
        SpeedProfileTable t = new SpeedProfileTable(re.getSpeedProfile(), re.getId());
        Assert.assertNotNull("exists",t);
        new JFrameOperator("Speed Table for ").requestClose();        
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SpeedProfileTableTest.class);

}
