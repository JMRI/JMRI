package jmri.jmrix.openlcb;

import jmri.Turnout;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbTurnoutManager class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010, 2011
 */
public class OlcbTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTest {

    @Override
    public String getSystemName(int i) {
        return "MTX00000" + i + ";X00000" + (i-1);
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists",l);
    }

    @Override
    @Test
    public void testUpperLower() {
        Turnout t = l.provideTurnout("MTX00000" + getNumToTest2()+ ";X00000" + (getNumToTest2()-1) );

        Assert.assertNull(l.getTurnout(t.getSystemName().toLowerCase()));
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        Turnout t = l.provideTurnout("MTX00000" + getNumToTest1() + ";X00000" + (getNumToTest1()-1) );
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }



    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();

        OlcbSystemConnectionMemo m = new OlcbSystemConnectionMemo();
        m.setTrafficController(new jmri.jmrix.can.TestTrafficController());
        l = new OlcbTurnoutManager(m);

    }

    @After
    public void tearDown() {
        l.dispose();
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
