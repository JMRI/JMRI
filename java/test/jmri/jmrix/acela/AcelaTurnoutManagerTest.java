package jmri.jmrix.acela;

import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.acela.AcelaTurnoutManager class.
 *
 * @author	Bob Coleman Copyright 2008
 */
public class AcelaTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    private AcelaTrafficControlScaffold tcis = null; 
    private AcelaSystemConnectionMemo memo = null; 

    @Override
    public String getSystemName(int i) {
        return "AT" + i;
    }

    @Test
    public void testConstructor() {
        // create and register the manager object
        AcelaTurnoutManager atm = new AcelaTurnoutManager(new AcelaSystemConnectionMemo(tcis) );
        Assert.assertNotNull("Acela Turnout Manager creation", atm);
    }

    @Test
    public void testAsAbstractFactory() {
        // a Turnout Manager object is created and registered in setUp.
        // ask for a Turnout, and check type
        TurnoutManager t = jmri.InstanceManager.turnoutManagerInstance();

        Turnout o = t.newTurnout("AT11", "my name");

        log.debug("received turnout value {}", o);
        Assert.assertTrue(null != (AcelaTurnout) o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: {}", t.getBySystemName("AT11"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   {}", t.getByUserName("my name"));
        }

        Assert.assertTrue(null != t.getBySystemName("AT11"));
        Assert.assertTrue(null != t.getByUserName("my name"));

    }

    AcelaNode a0, a1, a2, a3;

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();

        tcis = new AcelaTrafficControlScaffold();
        memo = new AcelaSystemConnectionMemo(tcis);

        // We need to delete the nodes so we can re-allocate them
        // otherwise we get another set of nodes for each test case
        // which really messes up the addresses.
        // We also seem to need to explicitly init each node.
        if ( tcis.getNumNodes() > 0) {
            //    tcis.deleteNode(3);
            //    tcis.deleteNode(2);
            //    tcis.deleteNode(1);
            //    tcis.deleteNode(0);
            tcis.resetStartingAddresses();
        }
        if (tcis.getNumNodes() <= 0) {
            a0 = new AcelaNode(0, AcelaNode.AC,tcis);
            a0.initNode();
            a1 = new AcelaNode(1, AcelaNode.TB,tcis);
            a1.initNode();
            a2 = new AcelaNode(2, AcelaNode.D8,tcis);
            a2.initNode();
            a3 = new AcelaNode(3, AcelaNode.SY,tcis);
            a3.initNode();
        } else {
            a0 = (AcelaNode) (tcis.getNode(0));
            tcis.initializeAcelaNode(a0);
            a1 = (AcelaNode) (tcis.getNode(1));
            tcis.initializeAcelaNode(a1);
            a2 = (AcelaNode) (tcis.getNode(2));
            tcis.initializeAcelaNode(a2);
            a3 = (AcelaNode) (tcis.getNode(3));
            tcis.initializeAcelaNode(a3);
        }

        // create and register the manager object
        l = new AcelaTurnoutManager(memo);
        jmri.InstanceManager.setTurnoutManager(l);
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    private final static Logger log = LoggerFactory.getLogger(AcelaTurnoutManagerTest.class);

}
