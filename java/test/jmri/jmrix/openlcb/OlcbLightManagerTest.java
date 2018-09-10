package jmri.jmrix.openlcb;

import jmri.Light;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbLightManager class.
 *
 * @author	Jeff Collell
 */
public class OlcbLightManagerTest extends jmri.managers.AbstractLightMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "MLX010203040506070" + i;
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", l);
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        // olcb addresses are hex values requirng 16 digits.
        Light t = l.provideLight("MLx010203040506070" + getNumToTest1());
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct " + t.getSystemName(), t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Override
    @Test
    public void testUpperLower() {
        // olcb addresses are hex values requirng 16 digits.
        Light t = l.provideLight("MLx010203040506070" + getNumToTest2());
        String name = t.getSystemName();
        Assert.assertNull(l.getLight(name.toLowerCase()));
    }

    @Test
    public void testDotted() {
        // olcb addresses are hex values requirng 16 digits.
        Light t = l.provideLight("ML01.02.03.04.05.06.07.0" + getNumToTest2());
        String name = t.getSystemName();
        Assert.assertNull(l.getLight(name.toLowerCase()));
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();

        OlcbSystemConnectionMemo m = OlcbTestInterface.createForLegacyTests();

        l = new OlcbLightManager(m);
    }

    @After
    public void tearDown() {
        l.dispose();
        JUnitUtil.tearDown();
    }

}
