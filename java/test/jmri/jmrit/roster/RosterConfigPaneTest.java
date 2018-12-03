package jmri.jmrit.roster;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.InstanceManager.setDefault(RosterConfigManager.class,new RosterConfigManager());
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RosterConfigPaneTest.class);

}
