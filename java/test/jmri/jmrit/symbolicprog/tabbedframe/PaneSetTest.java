package jmri.jmrit.symbolicprog.tabbedframe;

import java.awt.GraphicsEnvironment;
import javax.swing.JPanel;
import jmri.jmrit.roster.RosterEntry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PaneSetTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.Programmer p = jmri.InstanceManager.getDefault(jmri.AddressedProgrammerManager.class).getAddressedProgrammer(false,42);
        RosterEntry re = new RosterEntry();
        PaneProgFrame pc = new PaneProgFrame(null, re,
                "test frame", "programmers/Basic.xml",
                p, false) {
            // dummy implementations
            @Override
            protected JPanel getModePane() {
                return null;
            }
        };
        PaneSet t = new PaneSet(pc,re,p);
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

}
