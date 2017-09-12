package jmri.jmrit.symbolicprog.tabbedframe;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.roster.RosterEntry;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PaneServiceProgFrameTest {

    @Test
    public void testCTor() {
        jmri.Programmer p = jmri.InstanceManager.getDefault(jmri.ProgrammerManager.class).getGlobalProgrammer();
        DecoderFile df = new DecoderFile();
        RosterEntry re = new RosterEntry();
        PaneServiceProgFrame t = new PaneServiceProgFrame(df,re,"","",p);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDebugProgrammerManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(PaneServiceProgFrameTest.class.getName());

}
