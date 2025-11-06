package jmri.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jmri.*;
import jmri.jmrix.internal.InternalLightManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test the ProxyLightManager.
 *
 * @author Bob Jacobsen 2003, 2006, 2008
 */
public class ProxyLightManagerTest extends AbstractProxyManagerTestBase<ProxyLightManager,Light> {

    public String getSystemName(int i) {
        return "JL" + i;
    }

    @Test
    public void testDispose() {
        l.dispose();  // all we're really doing here is making sure the method exists
    }

    @Test
    public void testLightPutGet() {
        // create
        Light t = l.newLight(getSystemName(getNumToTest1()), "mine");
        // check
        assertNotNull( t, "real object returned " );
        assertSame( t, l.getByUserName("mine"), "user name correct ");
        assertSame( t, l.getBySystemName(getSystemName(getNumToTest1())), "system name correct ");
    }

    @Test
    public void testDefaultSystemName() {
        // create
        Light t = l.provideLight("" + getNumToTest1());
        // check
        assertNotNull( t, "real object returned ");
        assertSame( t, l.getBySystemName(getSystemName(getNumToTest1())), "system name correct ");
    }

    @Test
    public void testProvideFailure() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> l.provideLight(""));
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("Invalid system name for Light: System name must start with \"" + l.getSystemNamePrefix() + "\".");
    }

    @Test
    public void testSingleObject() {
        // test that you always get the same representation
        Light t1 = l.newLight(getSystemName(getNumToTest1()), "mine");
        assertNotNull( t1, "t1 real object returned ");
        assertSame( t1, l.getByUserName("mine"), "same by user ");
        assertSame( t1, l.getBySystemName(getSystemName(getNumToTest1())), "same by system ");

        Light t2 = l.newLight(getSystemName(getNumToTest1()), "mine");
        assertNotNull( t2, "t2 real object returned ");
        // check
        assertSame( t1, t2, "same new ");
    }

    @Test
    public void testMisses() {
        // try to get nonexistant lights
        assertNull( l.getByUserName("foo"));
        assertNull( l.getBySystemName("bar"));
    }

    @Test
    public void testUpperLower() {
        Light t = l.provideLight("" + getNumToTest2());
        String name = t.getSystemName();
        assertNull(l.getLight(name.toLowerCase()));
    }

    @Test
    public void testRename() {
        // get light
        Light t1 = l.newLight(getSystemName(getNumToTest1()), "before");
        assertNotNull( t1, "t1 real object ");
        t1.setUserName("after");
        Light t2 = l.getByUserName("after");
        assertEquals( t1, t2, "same object");
        assertNull( l.getByUserName("before"), "no old object");
    }

    @Test
    public void testTwoNames() {
        Light il211 = l.provideLight("IL211");
        Light jl211 = l.provideLight("JL211");

        assertNotNull(il211);
        assertNotNull(jl211);
        assertNotSame(il211, jl211);
    }

    @Test
    public void testDefaultNotInternal() {
        Light lut = l.provideLight("211");

        assertNotNull(lut);
        assertEquals("JL211", lut.getSystemName());
    }

    @Test
    public void testProvideUser() {
        Light l1 = l.provideLight("211");
        l1.setUserName("user 1");
        Light l2 = l.provideLight("user 1");
        Light l3 = l.getLight("user 1");

        assertNotNull(l1);
        assertNotNull(l2);
        assertNotNull(l3);
        assertEquals(l1, l2);
        assertEquals(l3, l2);
        assertEquals(l1, l3);

        Light l4 = l.getLight("JLuser 1");
        assertNull(l4);
    }

    @Test
    public void testInstanceManagerIntegration() {
        JUnitUtil.resetInstanceManager();
        assertNotNull(InstanceManager.getDefault(LightManager.class));

        JUnitUtil.initInternalLightManager();

        assertInstanceOf( ProxyLightManager.class, InstanceManager.getDefault(LightManager.class));

        assertNotNull(InstanceManager.getDefault(LightManager.class));
        assertNotNull(InstanceManager.getDefault(LightManager.class).provideLight("IL1"));

        InternalLightManager m = new InternalLightManager(new InternalSystemConnectionMemo("J", "Juliet"));
        InstanceManager.setLightManager(m);

        assertNotNull(InstanceManager.getDefault(LightManager.class).provideLight("JL1"));
        assertNotNull(InstanceManager.getDefault(LightManager.class).provideLight("IL2"));
    }

    /**
     * Number of light to test. Made a separate method so it can be overridden
     * in subclasses that do or don't support various numbers.
     * 
     * @return the number to test
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
        LightManager ilm = new InternalLightManager(new InternalSystemConnectionMemo("J", "Juliet"));
        InstanceManager.setLightManager(ilm);
        LightManager pl = InstanceManager.getDefault(LightManager.class);
        assertInstanceOf( ProxyLightManager.class, pl,
            "LightManager is not a ProxyLightManager");
        l = (ProxyLightManager) pl;
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
