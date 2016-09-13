package jmri.jmrix.acela;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import jmri.Light;
import jmri.LightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.acela.AcelaTurnoutManager class.
 *
 * @author	Bob Coleman Copyright 2008
 */
public class AcelaLightManagerTest  {

    private AcelaSystemConnectionMemo _memo = null;
    private AcelaLightManager l = null;
    private AcelaTrafficControlScaffold tcis = null;

    public String getSystemName(int i) {
        return "AL" + i;
    }

    @Test
    public void testConstructor(){
        AcelaLightManager alm = new AcelaLightManager(_memo);
        Assert.assertNotNull("Light Manager Creation",alm);
    }

    @Test
    @Ignore("not working")
    public void testAsAbstractFactory() {
        // ask for a Light, and check type
        LightManager lm = jmri.InstanceManager.lightManagerInstance();

        Light tl = lm.newLight("AL21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received light value " + tl);
        }
        Assert.assertTrue(null != (AcelaLight) tl);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + lm.getBySystemName("AL21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + lm.getByUserName("my name"));
        }

        Assert.assertTrue(null != lm.getBySystemName("AL21"));
        Assert.assertTrue(null != lm.getByUserName("my name"));

    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        tcis = new AcelaTrafficControlScaffold();
        _memo = new jmri.jmrix.acela.AcelaSystemConnectionMemo(tcis);
        // create and register the manager object
        l = new AcelaLightManager(_memo);
        jmri.InstanceManager.setLightManager(l);
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(AcelaLightManagerTest.class.getName());

}
