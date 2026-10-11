package jmri.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.*;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.jmrix.internal.InternalTurnoutManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests the ProxyTurnoutManager.
 *
 * @author Bob Jacobsen 2003, 2006, 2008, 2014, 2018
 */
public class ProxyTurnoutManagerTest extends AbstractProxyManagerTestBase<ProxyTurnoutManager,Turnout> {

    public String getSystemName(int i) {
        return "JT" + i;
    }

    @Test
    public void testDispose() {
        l.dispose();  // all we're really doing here is making sure the method exists
    }

    @Test
    public void testPutGet() {
        // create
        Turnout t = l.newTurnout(getSystemName(getNumToTest1()), "mine");
        // check
        assertNotNull( t, "real object returned ");
        assertSame( t, l.getByUserName("mine"), "user name correct ");
        assertSame( t, l.getBySystemName(getSystemName(getNumToTest1())),
            "system name correct ");
    }

    @Test
    public void testDefaultSystemName() {
        // create
        Turnout t = l.provideTurnout("" + getNumToTest1());
        // check
        assertNotNull( t, "real object returned ");
        assertSame( t, l.getBySystemName(getSystemName(getNumToTest1())),
            "system name correct ");
    }

    @Test
    public void testProvideFailure() {
        IllegalArgumentException assertThrows = assertThrows(IllegalArgumentException.class, () -> l.provideTurnout(""));
        assertTrue(assertThrows.getMessage().contains("System name must start with \"JT\""),"msg was: "+assertThrows.getMessage());
        JUnitAppender.assertErrorMessage("Invalid system name for Turnout: System name must start with \"" + l.getSystemNamePrefix() + "\".");
    }

    @Test
    public void testSingleObject() {
        // test that you always get the same representation
        Turnout t1 = l.newTurnout(getSystemName(getNumToTest1()), "mine");
        assertNotNull( t1, "t1 real object returned ");
        assertSame( t1, l.getByUserName("mine"), "same by user ");
        assertSame( t1, l.getBySystemName(getSystemName(getNumToTest1())),
            "same by system ");

        Turnout t2 = l.newTurnout(getSystemName(getNumToTest1()), "mine");
        assertNotNull( t2, "t2 real object returned ");
        // check
        assertSame( t1, t2, "same new ");
    }

    @Test
    public void testMisses() {
        // try to get nonexistant objects
        assertNull(l.getByUserName("foo"));
        assertNull(l.getBySystemName("bar"));
    }

    @Test
    public void testUpperLower() {
        Turnout t = l.provideTurnout("" + getNumToTest2());
        String name = t.getSystemName();
        assertNull(l.getTurnout(name.toLowerCase()));
    }

    @Test
    public void testRename() {
        // get
        Turnout t1 = l.newTurnout(getSystemName(getNumToTest1()), "before");
        assertNotNull( t1, "t1 real object ");
        t1.setUserName("after");
        Turnout t2 = l.getByUserName("after");
        assertEquals( t1, t2, "same object");
        assertNull( l.getByUserName("before"), "no old object");
    }


    @Test
    public void testNextSystemName() throws JmriException {
        // create
        Turnout t = l.newTurnout(getSystemName(getNumToTest1()), "mine");

        String next = l.getNextValidSystemName(t);

        assertNotNull( t, "real object returned ");
        assertEquals( "JT9", t.getSystemName(), "based on ");
        assertEquals( "JT10", next, "correct next name ");
    }

    @Test
    public void testTwoNames() {
        Turnout jl212 = l.provideTurnout("JT212");
        Turnout jl211 = l.provideTurnout("JT211");

        assertNotNull(jl212);
        assertNotNull(jl211);
        assertNotSame(jl212, jl211);
    }

    @Test
    public void testDefaultNotInternal() {
        Turnout lut = l.provideTurnout("211");

        assertNotNull(lut);
        assertEquals("JT211", lut.getSystemName());
    }

    @Test
    public void testProvideUser() {
        Turnout l1 = l.provideTurnout("211");
        l1.setUserName("user 1");
        Turnout l2 = l.provideTurnout("user 1");
        Turnout l3 = l.getTurnout("user 1");

        assertNotNull(l1);
        assertNotNull(l2);
        assertNotNull(l3);
        assertEquals(l1, l2);
        assertEquals(l3, l2);
        assertEquals(l1, l3);

        Turnout l4 = l.getTurnout("JLuser 1");
        assertNull(l4);
    }

    @Test
    public void testOutputInterval() {
        assertEquals( 250, l.getOutputInterval(), "default outputInterval");
        l.setOutputInterval(50);
        assertEquals( 50, l.getOutputInterval(), "Internal outputInterval");
    }

    @Test
    public void testInstanceManagerIntegration() {
        JUnitUtil.resetInstanceManager();
        assertNotNull(InstanceManager.getDefault(TurnoutManager.class));

        JUnitUtil.initInternalTurnoutManager();

        assertInstanceOf(ProxyTurnoutManager.class,
            InstanceManager.getDefault(TurnoutManager.class));

        assertNotNull(InstanceManager.getDefault(TurnoutManager.class));
        assertNotNull(InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IS1"));

        InternalTurnoutManager m = new InternalTurnoutManager(new InternalSystemConnectionMemo("J", "Juliet"));
        InstanceManager.setTurnoutManager(m);

        assertNotNull(InstanceManager.getDefault(TurnoutManager.class).provideTurnout("JS1"));
        assertNotNull(InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IS2"));
    }

    /**
     * Number of unit to test.
     * Made a separate method so it can be overridden in subclasses that 
     * do or don't support various numbers.
     * @return a number appropriate for jmrix system.
     */
    protected int getNumToTest1() {
        return 9;
    }

    protected int getNumToTest2() {
        return 7;
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // create and register the manager object
        TurnoutManager itm = new InternalTurnoutManager(new InternalSystemConnectionMemo("J", "Juliet"));
        InstanceManager.setTurnoutManager(itm);
        TurnoutManager pl = InstanceManager.getDefault(TurnoutManager.class);
        assertInstanceOf( ProxyTurnoutManager.class, pl,
            "TurnoutManager is not a ProxyTurnoutManager");
        l = (ProxyTurnoutManager) pl;
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
