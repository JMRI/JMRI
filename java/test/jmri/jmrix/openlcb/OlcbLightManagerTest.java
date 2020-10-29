package jmri.jmrix.openlcb;


import java.beans.PropertyVetoException;

import jmri.Light;
import jmri.ProvidingManager;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.openlcb.*;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbLightManager class.
 *
 * @author Jeff Collell
 */
public class OlcbLightManagerTest extends jmri.managers.AbstractLightMgrTestBase {

    private static OlcbSystemConnectionMemo memo;
    static Connection connection;
    static NodeID nodeID = new NodeID(new byte[]{1, 0, 0, 0, 0, 0});
    static java.util.ArrayList<Message> messages;

    @Override
    public String getSystemName(int i) {
        throw new UnsupportedOperationException("olcb lights need 2 addresses");
    }
    
    @Override
    public String getASystemNameWithNoPrefix() {
        return "x0102030405060701;x0102030405060702";
    }
    
    public String getSystemName(int on, int off) {
        return "MLx010203040506070" + on +";x010203040506070" + off;
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", l);
    }
    
    @Override
    @Test
    public void testProvideName() {
        // create
        // olcb addresses are hex values requirng 16 digits.
        Light t = l.provide("MLx010203040506070" + getNumToTest1() +";x010203040506070" + getNumToTest2());
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertEquals("system name correct " + t.getSystemName(), t, l.getBySystemName(getSystemName(getNumToTest1(), getNumToTest2())));
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        // olcb addresses are hex values requirng 16 digits.
        Light t = l.provideLight("MLx010203040506070" + getNumToTest1() +";x010203040506070" + getNumToTest2());
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertEquals("system name correct " + t.getSystemName(), t, l.getBySystemName(getSystemName(getNumToTest1(), getNumToTest2())));
    }

    @Override
    @Test
    public void testUpperLower() {
        // olcb addresses are hex values requirng 16 digits.
        Light t = l.provideLight("MLx010203040506070" + getNumToTest1() + ";x010203040506070" + getNumToTest2());
        String name = t.getSystemName();
        Assert.assertNull(l.getLight(name.toLowerCase()));
    }
    
    @Override
    @Test
    public void testSingleObject() {
        // test that you always get the same representation
        Light t1 = l.newLight(getSystemName(getNumToTest1(), getNumToTest2()), "mine");
        Assert.assertNotNull("t1 real object returned ", t1);
        Assert.assertEquals("same by user ", t1, l.getByUserName("mine"));
        Assert.assertEquals("same by system ", t1, l.getBySystemName(getSystemName(getNumToTest1(), getNumToTest2())));

        Light t2 = l.newLight(getSystemName(getNumToTest1(), getNumToTest2()), "mine");
        Assert.assertNotNull("t2 real object returned ", t2);
        // check
        Assert.assertEquals("same new ", t1, t2);
    }
    
    @Override
    @Test
    public void testLightPutGet() {
        // create
        Light t = l.newLight(getSystemName(getNumToTest1(), getNumToTest2()), "mine");
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertEquals("user name correct ", t, l.getByUserName("mine"));
        Assert.assertEquals("system name correct ", t, l.getBySystemName(getSystemName(getNumToTest1(), getNumToTest2())));
    }
    
    @Override
    @Test
    public void testRename() {
        // get light
        Light t1 = l.newLight(getSystemName(getNumToTest1(), getNumToTest2()), "before");
        Assert.assertNotNull("t1 real object ", t1);
        t1.setUserName("after");
        Light t2 = l.getByUserName("after");
        Assert.assertEquals("same object", t1, t2);
        Assert.assertNull("no old object", l.getByUserName("before"));
    }

    @Test
    public void testDotted() {
        // olcb addresses are hex values requirng 16 digits.
        Light t = l.provideLight("ML01.02.03.04.05.06.07.0" + getNumToTest1() + ";01.02.03.04.05.06.07.0" + getNumToTest2());
        String name = t.getSystemName();
        Assert.assertNull(l.getLight(name.toLowerCase()));
    }
    
    @Override
    @Test
    public void testRegisterDuplicateSystemName() throws PropertyVetoException, NoSuchFieldException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        String s1 = l.makeSystemName("x0102030405060701;x0102030405060702");
        String s2 = l.makeSystemName("x0102030405060703;x0102030405060704");
        testRegisterDuplicateSystemName(l, s1, s2);
    }

    @Override
    @BeforeEach
    public void setUp() {
        l = new OlcbLightManager(memo);
    }

    @AfterEach
    public void tearDown() {
        l.dispose();
        l = null;
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
        JUnitUtil.tearDown();
    }

}
