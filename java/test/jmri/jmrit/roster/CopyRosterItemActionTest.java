package jmri.jmrit.roster;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmrit.roster.RosterEntryPane class.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002
 */
public class CopyRosterItemActionTest {

    /**
     * Really just checks that the thing can init; doesn't really copy the file,
     * etc. Should do that some day!
     */
    @Test
    public void testCopy() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // create a special roster
        //Roster r = RosterTest.createTestRoster();
        // make that the default; not that test roster uses special name
        InstanceManager.reset(Roster.class);
        InstanceManager.setDefault(Roster.class, new Roster(null));

        // copy the item
        CopyRosterItemAction a = new CopyRosterItemAction("copy", new javax.swing.JFrame()) {
            @Override
            protected boolean selectFrom() {
                return false;  // aborts operation
            }
        };
        a.actionPerformed(null);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
