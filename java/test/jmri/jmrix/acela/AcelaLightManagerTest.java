package jmri.jmrix.acela;

import jmri.Light;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.acela.AcelaLightManager class.
 *
 * @author	Bob Coleman Copyright 2008
 */
public class AcelaLightManagerTest extends jmri.managers.AbstractLightMgrTestBase {

    private AcelaSystemConnectionMemo _memo = null;
    private AcelaTrafficControlScaffold tcis = null;

    @Override
    public String getSystemName(int i) {
        return "AL" + i;
    }

    @Test
    public void testConstructor(){
        AcelaLightManager alm = new AcelaLightManager(_memo);
        Assert.assertNotNull("Light Manager Creation",alm);
    }

    @Test
    public void testAsAbstractFactory() {
        Light tl = l.newLight("AL21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received light value " + tl);
        }
        Assert.assertTrue(null != (AcelaLight) tl);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + l.getBySystemName("AL21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + l.getByUserName("my name"));
        }

        Assert.assertTrue(null != l.getBySystemName("AL21"));
        Assert.assertTrue(null != l.getByUserName("my name"));

    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new AcelaTrafficControlScaffold();
        _memo = new jmri.jmrix.acela.AcelaSystemConnectionMemo(tcis);
        // create and register the manager object
        l = new AcelaLightManager(_memo);
        jmri.InstanceManager.setLightManager(l);
        AcelaNode a0 = new AcelaNode(0, AcelaNode.AC,tcis);
        a0.initNode();
        AcelaNode a1 = new AcelaNode(1, AcelaNode.TB,tcis);
        a1.initNode();
        AcelaNode a2 = new AcelaNode(2, AcelaNode.D8,tcis);
        a2.initNode();
        AcelaNode a3 = new AcelaNode(3, AcelaNode.D8,tcis);
        a3.initNode();
        AcelaNode a4 = new AcelaNode(4, AcelaNode.D8,tcis);
        a4.initNode();
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    private final static Logger log = LoggerFactory.getLogger(AcelaLightManagerTest.class);

}
