package jmri.jmrix.acela;

import jmri.Turnout;
import jmri.TurnoutManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.acela.AcelaTurnoutManager class.
 *
 * @author	Bob Coleman Copyright 2008
 */
public class AcelaTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTest {

    public String getSystemName(int i) {
        return "AT" + i;
    }

    public void testAsAbstractFactory() {
        // create and register the manager object
        AcelaTurnoutManager atm = new AcelaTurnoutManager();
        jmri.InstanceManager.setTurnoutManager(atm);

        // ask for a Turnout, and check type
        TurnoutManager t = jmri.InstanceManager.turnoutManagerInstance();

        Turnout o = t.newTurnout("AT11", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received turnout value " + o);
        }
        Assert.assertTrue(null != (AcelaTurnout) o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + t.getBySystemName("AT11"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + t.getByUserName("my name"));
        }

        Assert.assertTrue(null != t.getBySystemName("AT11"));
        Assert.assertTrue(null != t.getByUserName("my name"));

    }

    // from here down is testing infrastructure
    public AcelaTurnoutManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", AcelaTurnoutManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AcelaTurnoutManagerTest.class);
        return suite;
    }

    AcelaNode a0, a1, a2, a3;

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();

        // We need to delete the nodes so we can re-allocate them
        // otherwise we get another set of nodes for each test case
        // which really messes up the addresses.
        // We also seem to need to explicitly init each node.
        if (AcelaTrafficController.instance().getNumNodes() > 0) {
            //    AcelaTrafficController.instance().deleteNode(3);
            //    AcelaTrafficController.instance().deleteNode(2);
            //    AcelaTrafficController.instance().deleteNode(1);
            //    AcelaTrafficController.instance().deleteNode(0);
            AcelaTrafficController.instance().resetStartingAddresses();
        }
        if (AcelaTrafficController.instance().getNumNodes() <= 0) {
            a0 = new AcelaNode(0, AcelaNode.AC);
            a0.initNode();
            a1 = new AcelaNode(1, AcelaNode.TB);
            a1.initNode();
            a2 = new AcelaNode(2, AcelaNode.D8);
            a2.initNode();
            a3 = new AcelaNode(3, AcelaNode.SY);
            a3.initNode();
        } else {
            a0 = (AcelaNode) (AcelaTrafficController.instance().getNode(0));
            AcelaTrafficController.instance().initializeAcelaNode(a0);
            a1 = (AcelaNode) (AcelaTrafficController.instance().getNode(1));
            AcelaTrafficController.instance().initializeAcelaNode(a1);
            a2 = (AcelaNode) (AcelaTrafficController.instance().getNode(2));
            AcelaTrafficController.instance().initializeAcelaNode(a2);
            a3 = (AcelaNode) (AcelaTrafficController.instance().getNode(3));
            AcelaTrafficController.instance().initializeAcelaNode(a3);
        }

        // create and register the manager object
        l = new AcelaTurnoutManager();
        jmri.InstanceManager.setTurnoutManager(l);
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(AcelaTurnoutManagerTest.class.getName());

}
