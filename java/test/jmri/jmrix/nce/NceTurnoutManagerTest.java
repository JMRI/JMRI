package jmri.jmrix.nce;

import jmri.Turnout;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.nce.NceTurnoutManager class
 *
 * @author	Bob Jacobsen
 */
public class NceTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    private NceInterfaceScaffold nis = null;

    @Override
    public String getSystemName(int n) {
        return "NT" + n;
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("NT21", "my name");

        Assert.assertNotNull(o);

        Assert.assertEquals(o, l.getBySystemName("NT21"));
        Assert.assertEquals(o, l.getByUserName("my name"));
    }

    @Override
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

        // prepare an interface, register
        nis = new NceInterfaceScaffold();
        // create and register the manager object
        l = new NceTurnoutManager(nis.getAdapterMemo());
        jmri.InstanceManager.setTurnoutManager(l);
    }

    @After
    public void tearDown() {
        nis = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NceTurnoutManagerTest.class);

}
