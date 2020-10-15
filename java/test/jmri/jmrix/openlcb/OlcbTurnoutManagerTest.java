package jmri.jmrix.openlcb;

import java.beans.PropertyVetoException;

import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.openlcb.*;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbTurnoutManager class.
 *
 * @author Bob Jacobsen Copyright 2008, 2010, 2011
 */
public class OlcbTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    private static OlcbSystemConnectionMemo memo;
    static Connection connection;
    static NodeID nodeID = new NodeID(new byte[]{1, 0, 0, 0, 0, 0});
    static java.util.ArrayList<Message> messages;

    @Override
    public String getSystemName(int i) {
        return "MTX010203040506070" + i + ";X010203040506070" + (i - 1);
    }
    
    @Override
    protected String getASystemNameWithNoPrefix() {
        return "X0102030405060702;X0102030405060701";
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", l);
    }

    @Override
    @Test
    public void testProvideName() {
        // create
        Turnout t = l.provide(getSystemName(getNumToTest1()));
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertSame("system name correct ", t, l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Override
    @Test
    public void testUpperLower() {
        Turnout t = l.provide(getSystemName(getNumToTest1()));
        Assert.assertNull(l.getTurnout(t.getSystemName().toLowerCase()));
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        Turnout t = l.provide(getSystemName(getNumToTest1()));
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertSame("system name correct ", t, l.getBySystemName(getSystemName(getNumToTest1())));
    }
    
    @Override
    @Test
    public void testRegisterDuplicateSystemName() throws PropertyVetoException, NoSuchFieldException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        String s1 = l.makeSystemName("x0102030405060701;x0102030405060702");
        String s2 = l.makeSystemName("x0102030405060703;x0102030405060704");
        testRegisterDuplicateSystemName(l, s1, s2);
    }
    
    @Test
    @Override
    public void testSetAndGetOutputInterval() {
        Assert.assertEquals("default outputInterval", 100, l.getOutputInterval());
        l.getMemo().setOutputInterval(21);
        Assert.assertEquals("new outputInterval in memo", 21, l.getMemo().getOutputInterval()); // set & get in memo
        Assert.assertEquals("new outputInterval via manager", 21, l.getOutputInterval()); // get via turnoutManager
        l.setOutputInterval(50);
        Assert.assertEquals("new outputInterval from manager", 50, l.getOutputInterval()); // interval stored in AbstractTurnoutManager
        Assert.assertEquals("new outputInterval from manager", 50, l.getMemo().getOutputInterval()); // get from memo
    }

    @Override
    @BeforeEach
    public void setUp() {
        l = new OlcbTurnoutManager(memo);
    }
 
    @AfterEach
    public void tearDown() {
        l.dispose();
    }

    @BeforeAll
    static public void preClassInit() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        nodeID = new NodeID(new byte[]{1, 0, 0, 0, 0, 0});

        messages = new java.util.ArrayList<>();
        connection = new AbstractConnection() {
            @Override
            public void put(Message msg, Connection sender) {
                messages.add(msg);
            }
        };

        memo = new OlcbSystemConnectionMemo(); // this self-registers as 'M'
        memo.setProtocol(jmri.jmrix.can.ConfigurationManager.OPENLCB);
        memo.setInterface(new OlcbInterface(nodeID, connection) {
            @Override
            public Connection getOutputConnection() {
                return connection;
            }
        });
    
        jmri.util.JUnitUtil.waitFor(()-> (messages.size()>0),"Initialization Complete message");
    }

    @AfterAll
    public static void postClassTearDown() {
        if(memo != null && memo.getInterface() !=null ) {
            memo.getInterface().dispose();
        }
        memo = null;
        connection = null;
        nodeID = null;
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
