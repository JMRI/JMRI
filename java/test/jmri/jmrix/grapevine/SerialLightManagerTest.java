package jmri.jmrix.grapevine;

import jmri.Light;
import jmri.util.JUnitUtil;

import java.beans.PropertyVetoException;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the SerialLightManager class
 *
 * @author Bob Jacobsen Copyright 2004, 2007, 2008
 */
public class SerialLightManagerTest extends jmri.managers.AbstractLightMgrTestBase {

    private GrapevineSystemConnectionMemo memo = null; 

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();

        // replace the SerialTrafficController
        memo = new GrapevineSystemConnectionMemo();
        SerialTrafficController t = new SerialTrafficControlScaffold(memo);
        memo.setTrafficController(t);
        t.registerNode(new SerialNode(1, SerialNode.NODE2002V6, t));
        // create and register the manager object
        l = new SerialLightManager(memo);
        jmri.InstanceManager.setLightManager(l);
    }

    @Override
    public String getSystemName(int n) {
        return "GL" + n;
    }
    
    @Override
    protected String getASystemNameWithNoPrefix() {
        return "1106";
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Light, and check type
        Light o = l.newLight("GL1105", "my name");
        Assert.assertNotNull( o);
        Assert.assertTrue(o instanceof SerialLight);

        // make sure loaded into tables
        Assert.assertNotNull( l.getBySystemName("GL1105"));
        Assert.assertNotNull( l.getByUserName("my name"));

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
     * Number of light to test. Use 9th output on node 1.
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

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialLightManagerTest.class);

}
