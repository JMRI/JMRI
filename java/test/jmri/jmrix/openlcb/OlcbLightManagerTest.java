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
        throw new UnsupportedOperationException("olcb lights need 2 addresses");
    }
    
    public String getSystemName(int on, int off) {
        return "MLX010203040506070" + on +";X010203040506070" + off;
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
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct " + t.getSystemName(), t == l.getBySystemName(getSystemName(getNumToTest1(), getNumToTest2())));
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        // olcb addresses are hex values requirng 16 digits.
        Light t = l.provideLight("MLx010203040506070" + getNumToTest1() +";x010203040506070" + getNumToTest2());
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct " + t.getSystemName(), t == l.getBySystemName(getSystemName(getNumToTest1(), getNumToTest2())));
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
        Assert.assertTrue("t1 real object returned ", t1 != null);
        Assert.assertTrue("same by user ", t1 == l.getByUserName("mine"));
        Assert.assertTrue("same by system ", t1 == l.getBySystemName(getSystemName(getNumToTest1(), getNumToTest2())));

        Light t2 = l.newLight(getSystemName(getNumToTest1(), getNumToTest2()), "mine");
        Assert.assertTrue("t2 real object returned ", t2 != null);
        // check
        Assert.assertTrue("same new ", t1 == t2);
    }
    
    @Override
    @Test
    public void testLightPutGet() {
        // create
        Light t = l.newLight(getSystemName(getNumToTest1(), getNumToTest2()), "mine");
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("user name correct ", t == l.getByUserName("mine"));
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1(), getNumToTest2())));
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
        Assert.assertEquals("no old object", null, l.getByUserName("before"));
    }

    @Test
    public void testDotted() {
        // olcb addresses are hex values requirng 16 digits.
        Light t = l.provideLight("ML01.02.03.04.05.06.07.0" + getNumToTest1() + ";01.02.03.04.05.06.07.0" + getNumToTest2());
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
