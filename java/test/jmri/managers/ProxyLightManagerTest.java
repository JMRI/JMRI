package jmri.managers;

import jmri.*;
import jmri.jmrix.internal.InternalLightManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
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
        Assert.assertNotNull("real object returned ", t );
        Assert.assertTrue("user name correct ", t == l.getByUserName("mine"));
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Test
    public void testDefaultSystemName() {
        // create
        Light t = l.provideLight("" + getNumToTest1());
        // check
        Assert.assertNotNull("real object returned ", t );
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Test
    public void testProvideFailure() {
        Assert.assertThrows(IllegalArgumentException.class, () -> l.provideLight(""));
        JUnitAppender.assertErrorMessage("Invalid system name for Light: System name must start with \"" + l.getSystemNamePrefix() + "\".");
    }

    @Test
    public void testSingleObject() {
        // test that you always get the same representation
        Light t1 = l.newLight(getSystemName(getNumToTest1()), "mine");
        Assert.assertNotNull("t1 real object returned ", t1 );
        Assert.assertTrue("same by user ", t1 == l.getByUserName("mine"));
        Assert.assertTrue("same by system ", t1 == l.getBySystemName(getSystemName(getNumToTest1())));

        Light t2 = l.newLight(getSystemName(getNumToTest1()), "mine");
        Assert.assertNotNull("t2 real object returned ", t2 );
        // check
        Assert.assertTrue("same new ", t1 == t2);
    }

    @Test
    public void testMisses() {
        // try to get nonexistant lights
        Assert.assertNull( l.getByUserName("foo"));
        Assert.assertNull( l.getBySystemName("bar"));
    }

    @Test
    public void testUpperLower() {
        Light t = l.provideLight("" + getNumToTest2());
        String name = t.getSystemName();
        Assert.assertNull(l.getLight(name.toLowerCase()));
    }

    @Test
    public void testRename() {
        // get light
        Light t1 = l.newLight(getSystemName(getNumToTest1()), "before");
        Assert.assertNotNull("t1 real object ", t1);
        t1.setUserName("after");
        Light t2 = l.getByUserName("after");
        Assert.assertEquals("same object", t1, t2);
        Assert.assertEquals("no old object", null, l.getByUserName("before"));
    }

    @Test
    public void testTwoNames() {
        Light il211 = l.provideLight("IL211");
        Light jl211 = l.provideLight("JL211");

        Assert.assertNotNull(il211);
        Assert.assertNotNull(jl211);
        Assert.assertTrue(il211 != jl211);
    }

    @Test
    public void testDefaultNotInternal() {
        Light lut = l.provideLight("211");

        Assert.assertNotNull(lut);
        Assert.assertEquals("JL211", lut.getSystemName());
    }

    @Test
    public void testProvideUser() {
        Light l1 = l.provideLight("211");
        l1.setUserName("user 1");
        Light l2 = l.provideLight("user 1");
        Light l3 = l.getLight("user 1");

        Assert.assertNotNull(l1);
        Assert.assertNotNull(l2);
        Assert.assertNotNull(l3);
        Assert.assertEquals(l1, l2);
        Assert.assertEquals(l3, l2);
        Assert.assertEquals(l1, l3);

        Light l4 = l.getLight("JLuser 1");
        Assert.assertNull(l4);
    }

    @Test
    public void testInstanceManagerIntegration() {
        JUnitUtil.resetInstanceManager();
        Assert.assertNotNull(InstanceManager.getDefault(LightManager.class));

        JUnitUtil.initInternalLightManager();

        Assert.assertTrue(InstanceManager.getDefault(LightManager.class) instanceof ProxyLightManager);

        Assert.assertNotNull(InstanceManager.getDefault(LightManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(LightManager.class).provideLight("IL1"));

        InternalLightManager m = new InternalLightManager(new InternalSystemConnectionMemo("J", "Juliet"));
        InstanceManager.setLightManager(m);

        Assert.assertNotNull(InstanceManager.getDefault(LightManager.class).provideLight("JL1"));
        Assert.assertNotNull(InstanceManager.getDefault(LightManager.class).provideLight("IL2"));
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
        if ( pl instanceof ProxyLightManager ) {
            l = (ProxyLightManager) pl;
        } else {
            Assertions.fail("LightManager is not a ProxyLightManager");
        }
        
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
