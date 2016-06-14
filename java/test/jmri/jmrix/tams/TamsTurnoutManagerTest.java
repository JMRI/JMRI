package jmri.jmrix.tams;

import jmri.Turnout;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the TurnoutManager class
 *
 * @author	Bob Jacobsen  Copyright 2013, 2016
 */
public class TamsTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTest {

    private TamsInterfaceScaffold nis = null;
    private TamsSystemConnectionMemo tm = null;
    
    public void setUp() {
        // prepare an interface, register
        nis = new TamsInterfaceScaffold();
        tm = new TamsSystemConnectionMemo(nis);
        // create and register the manager object
        l = new TamsTurnoutManager(tm);
        jmri.InstanceManager.setTurnoutManager(l);
    }

    public String getSystemName(int n) {
        return "TM" + n;
    }

    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("TMT21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received turnout value " + o);
        }
        assertTrue(null != (TamsTurnout) o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + l.getBySystemName("TMT21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + l.getByUserName("my name"));
        }

        assertTrue(null != l.getBySystemName("TMT21"));
        assertTrue(null != l.getByUserName("my name"));

    }

    // from here down is testing infrastructure
    public TamsTurnoutManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {TamsTurnoutManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(TamsTurnoutManagerTest.class);
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(TamsTurnoutManagerTest.class.getName());

}
