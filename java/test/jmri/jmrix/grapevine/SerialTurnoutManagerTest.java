package jmri.jmrix.grapevine;

import jmri.Turnout;
import jmri.util.JUnitUtil;

import java.beans.PropertyVetoException;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the SerialTurnoutManager class.
 *
 * @author Bob Jacobsen Copyright 2004, 2007, 2008
 */
public class SerialTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    private GrapevineSystemConnectionMemo memo = null; 

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        memo = new GrapevineSystemConnectionMemo();
        SerialTrafficController tc = new SerialTrafficControlScaffold(memo);
        memo.setTrafficController(tc);
        tc.registerNode(new SerialNode(1, SerialNode.NODE2002V6, tc));
        // create and register the manager object
        l = new SerialTurnoutManager(memo);
        jmri.InstanceManager.setTurnoutManager(l);
    }

    @Override
    public String getSystemName(int n) {
        return "GT" + n;
    }
    
    @Override
    protected String getASystemNameWithNoPrefix() {
        return "1107";
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("GT1105", "my name");

        Assertions.assertNotNull( o);
        Assertions.assertTrue(o instanceof SerialTurnout);

        // make sure loaded into tables
        Assertions.assertNotNull( l.getBySystemName("GT1105"));
        Assertions.assertNotNull( l.getByUserName("my name"));

    }

    @Override
    @Test
    public void testRegisterDuplicateSystemName() throws PropertyVetoException, NoSuchFieldException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        testRegisterDuplicateSystemName(l,
                l.makeSystemName("1107"),
                l.makeSystemName("1109"));
    }

    @Override
    @Test
    public void testMakeSystemName() {
        String s = l.makeSystemName("1107");
        Assert.assertNotNull(s);
        Assert.assertFalse(s.isEmpty());
    }

    /**
     * Number of turnout to test. Use 9th output on node 1.
     * {@inheritDoc}
     */ 
    @Override
    protected int getNumToTest1() {
        return 1109;
    }

    @Override
    protected int getNumToTest2() {
        return 1107;
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialTurnoutManagerTest.class);

}
