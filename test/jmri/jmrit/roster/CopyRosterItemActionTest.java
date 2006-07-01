package jmri.jmrit.roster;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.roster.RosterEntryPane class.
 * @author	Bob Jacobsen     Copyright (C) 2001, 2002
 * @version	$Revision: 1.2 $
 */
public class CopyRosterItemActionTest extends TestCase {

    /**
     * Really just checks that the thing can init; doesn't really copy
     * the file, etc.  Should do that some day!
     * @throws IOException
     */
    public void testCopy() throws java.io.IOException {
        // create a special roster
        Roster r = RosterTest.createTestRoster();
        // make that the default; not that test roster uses special name
        r.resetInstance();
        r.instance();

        // copy the item
        CopyRosterItemAction a = new CopyRosterItemAction("copy", null){
            boolean selectFrom() {
                return false;  // aborts operation
            }
        };
        a.actionPerformed(null);
    }

    // from here down is testing infrastructure

    public CopyRosterItemActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {CopyRosterItemActionTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CopyRosterItemActionTest.class);
        return suite;
    }

    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

}
