package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.Light;
import jmri.Manager.NameValidity;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2018
 */
public class CbusLightManagerTest extends jmri.managers.AbstractLightMgrTestBase {

    private CanSystemConnectionMemo memo = null;
    // private TrafficControllerScaffold tc;
    // CbusLightManager l;

    @Test
    public void testctor() {
        // create and register the manager object
        l = new CbusLightManager(memo);
        Assert.assertNotNull(l);
    }

    @Override
    protected int getNumToTest1() {
        return 10;
    }

    @Override
    protected int getNumToTest2() {
        return 56517;
    }

    @Override
    public String getSystemName(int i) {
        return "MLX0A;+" + i;
    }

    @Test
    public void testPrefix() {
        // check prefix
        Assert.assertTrue(memo.getSystemPrefix().equals("M"));
    }

    @Override // numeric system name has a + added to it
    @Test
    public void testDefaultSystemName() {
        // create
        String name = "ML+5;-7";
        Light t = l.provideLight(name);
        // check
        Assert.assertNotNull("real object returned ");
        Assert.assertEquals("system name correct ", t, l.getBySystemName(name));
    }

    @Override // numeric system name has a + added to it
    @Test
    public void testProvideName() {
        String name = "ML+574;-7123";
        // create
        Light t = l.provide(name);
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertEquals("system name correct ", t, l.getBySystemName(name));
    }

    @Test
    public void testLowercaseSystemName() {
        String name1 = "mlxabcdef;xfedcba";
        try {
            l.provideLight(name1);
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            JUnitAppender.assertErrorMessage("Invalid system name for Light: Wrong number of events in address: mlxabcdef;xfedcba");
        }
        String name2 = "ml+n1e77;-n1e45";
        try {
            l.provideLight(name2);
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            JUnitAppender.assertErrorMessage("Invalid system name for Light: Wrong number of events in address: ml+n1e77;-n1e45");
        }
    }

    @Test
    public void threePartFail() {
        try {
            l.provideLight("ML+7;-5;+11");
            Assert.fail("3 split Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Light: Wrong number of events in address: +7;-5;+11");
        }
    }

    @Test
    public void testBadCbusLightAddresses() {
        try {
            Light t1 = l.provideLight("ML+N15E6");
            Assert.assertNotNull(t1);
        } catch (IllegalArgumentException e) {
            Assert.fail("Should NOT have thrown an exception");
        }

        try {
            l.provideLight("MLX;+N15E6");
            Assert.fail("X Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Light: Wrong number of events in address: X;+N15E6");
        }

        try {
            l.provideLight("MLXA;+N15E6");
            Assert.fail("A Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Light: Wrong number of events in address: XA;+N15E6");
        }

        try {
            l.provideLight("MLXABC;+N15E6");
            Assert.fail("AC Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Light: Wrong number of events in address: XABC;+N15E6");
        }

        try {
            l.provideLight("MLXABCDE;+N15E6");
            Assert.fail("ABCDE Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Light: Wrong number of events in address: XABCDE;+N15E6");
        }

        try {
            l.provideLight("MLXABCDEF0;+N15E6");
            Assert.fail("ABCDEF0 Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Light: Wrong number of events in address: XABCDEF0;+N15E6");
        }

        try {
            l.provideLight("MLXABCDEF");
            Assert.fail("Single hex Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Light: can't make 2nd event from address XABCDEF");
        }

        try {
            l.provideLight("MLXABCDEF;");
            Assert.fail("Single hex ; Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Light: Should not end with ; XABCDEF;");
        }

        try {
            l.provideLight("MLXABCDEF;");
            Assert.fail("Single hex ; Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Light: Should not end with ; XABCDEF;");
        }

        try {
            l.provideLight("ML;");
            Assert.fail("; no arg Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Light: Should not end with ; ;");
        }

        try {
            l.provideLight("ML;+N15E6");
            Assert.fail("ML Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Light: Address Too Short? : ");
        }

        try {
            l.provideLight(";+N15E6");
            Assert.fail("; Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Light: Address Too Short? : ");
            Assert.assertEquals("Address Too Short? : ", e.getMessage());
        }

        try {
            l.provideLight("S+N156E77;+N15E6");
            Assert.fail("S Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Light: Wrong number of events in address: S+N156E77;+N15E6");
            Assert.assertEquals("Wrong number of events in address: S+N156E77;+N15E6", e.getMessage());
        }

        try {
            l.provideLight("M+N156E77;+N15E6");
            Assert.fail("M Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Light: Wrong number of events in address: M+N156E77;+N15E6");
            Assert.assertEquals("Wrong number of events in address: M+N156E77;+N15E6", e.getMessage());
        }

        try {
            l.provideLight("ML++N156E77");
            Assert.fail("++ Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Light: Wrong number of events in address: ++N156E77");
        }

        try {
            l.provideLight("ML--N156E77");
            Assert.fail("-- Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Light: Wrong number of events in address: --N156E77");
        }

        try {
            l.provideLight("MLN156E+77");
            Assert.fail("E+ Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Light: Wrong number of events in address: N156E+77");
        }

        try {
            l.provideLight("MLN156+E77");
            Assert.fail("+E Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Light: Wrong number of events in address: N156+E77");
        }

        try {
            l.provideLight("MLXLKJK;XLKJK");
            Assert.fail("LKJK Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Light: Wrong number of events in address: XLKJK;XLKJK");
        }
    }

    @Test
    public void testGoodCbusLightAddresses() {

        Light t = l.provideLight("ML+7");
        Assert.assertNotNull("exists", t);

        Light t2 = l.provideLight("ML+1;-1");
        Assert.assertNotNull("exists", t2);

        Light t3 = l.provideLight("ML+654e321");
        Assert.assertNotNull("exists", t3);

        Light t4 = l.provideLight("ML-654e321;+123e456");
        Assert.assertNotNull("exists", t4);

        Light t5 = l.provideLight("ML+n654e321");
        Assert.assertNotNull("exists", t5);

        Light t6 = l.provideLight("ML+N299E17;-N123E456");
        Assert.assertNotNull("exists", t6);

        Light t7 = l.provideLight("MLX04;X05");
        Assert.assertNotNull("exists", t7);

        Light t8 = l.provideLight("MLX2301;X30FF");
        Assert.assertNotNull("exists", t8);

        Light t9 = l.provideLight("MLX410001;X56FFFF");
        Assert.assertNotNull("exists", t9);

        Light t10 = l.provideLight("MLX6000010001;X72FFFFFF");
        Assert.assertNotNull("exists", t10);

        Light t11 = l.provideLight("MLX9000010001;X91FFFFFFFF");
        Assert.assertNotNull("exists", t11);

        Light t12 = l.provideLight("MLXB00D60010001;XB1FFFAAFFFFF");
        Assert.assertNotNull("exists", t12);

        Light t13 = l.provideLight("MLXD00D0060010001;XD1FFFAAAFFFFFE");
        Assert.assertNotNull("exists", t13);

        Light t14 = l.provideLight("MLXF00D0A0600100601;XF1FFFFAAFAFFFFFE");
        Assert.assertNotNull("exists", t14);
    }

    @Test
    public void testgetEntryToolTip() {
        String x = l.getEntryToolTip();
        Assert.assertTrue(x.contains("<html>"));

        Assert.assertTrue(l.allowMultipleAdditions("M77"));
    }

    @Test
    public void testvalidSystemNameFormat() {

        Assert.assertEquals("ML+123", NameValidity.VALID, l.validSystemNameFormat("MS+123"));
        Assert.assertEquals("ML+N123E123", NameValidity.VALID, l.validSystemNameFormat("MS+N123E123"));
        Assert.assertEquals("ML+123;456", NameValidity.VALID, l.validSystemNameFormat("MS+123;456"));
        Assert.assertEquals("ML1", NameValidity.VALID, l.validSystemNameFormat("MS1"));
        Assert.assertEquals("ML1;2", NameValidity.VALID, l.validSystemNameFormat("MS1;2"));
        Assert.assertEquals("ML65535", NameValidity.VALID, l.validSystemNameFormat("MS65535"));
        Assert.assertEquals("ML-65535", NameValidity.VALID, l.validSystemNameFormat("MS-65535"));
        Assert.assertEquals("ML100001", NameValidity.VALID, l.validSystemNameFormat("MS100001"));
        Assert.assertEquals("ML-100001", NameValidity.VALID, l.validSystemNameFormat("MS-100001"));

        Assert.assertEquals("M", NameValidity.INVALID, l.validSystemNameFormat("M"));
        Assert.assertEquals("ML", NameValidity.INVALID, l.validSystemNameFormat("ML"));
        Assert.assertEquals("ML-65536", NameValidity.INVALID, l.validSystemNameFormat("ML-65536"));
        Assert.assertEquals("ML65536", NameValidity.INVALID, l.validSystemNameFormat("ML65536"));
        Assert.assertEquals("ML+1;+0", NameValidity.INVALID, l.validSystemNameFormat("ML+1;+0"));
        Assert.assertEquals("ML+1;-0", NameValidity.INVALID, l.validSystemNameFormat("ML+1;-0"));
        Assert.assertEquals("ML+0;+17", NameValidity.INVALID, l.validSystemNameFormat("ML+0;+17"));
        Assert.assertEquals("ML+0;-17", NameValidity.INVALID, l.validSystemNameFormat("ML+0;-17"));
        Assert.assertEquals("ML+0", NameValidity.INVALID, l.validSystemNameFormat("ML+0"));
        Assert.assertEquals("ML-0", NameValidity.INVALID, l.validSystemNameFormat("ML-0"));
        Assert.assertEquals("ML7;0", NameValidity.INVALID, l.validSystemNameFormat("ML7;0"));
        Assert.assertEquals("ML0;7", NameValidity.INVALID, l.validSystemNameFormat("ML0;7"));
    }

    @Test
    public void testProvideswhenNotNull() {
        Light t = l.provideLight("+4");
        Light ta = l.provideLight("+4");
        Assert.assertTrue(t == ta);
    }

    @Test
    public void testcreateNewLightException() {
        CbusLightManager c = (CbusLightManager) l;
        try {
            c.createNewLight("", null);
            Assert.fail("Expected exception not thrown");
        } catch (StringIndexOutOfBoundsException ex) {
            Assert.assertEquals("String index out of range: -2", ex.getMessage());
        }
    }

    @Test
    public void testvalidSystemNameConfig() {
        Assert.assertTrue(l.validSystemNameConfig("ML+123"));
        try {
            l.validSystemNameConfig("");
            Assert.fail("Expected exception not thrown");
        } catch (StringIndexOutOfBoundsException ex) {
            Assert.assertEquals("String index out of range: -2", ex.getMessage());
        }
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(new TrafficControllerScaffold());
        l = new CbusLightManager(memo);
    }

    @After
    public void tearDown() {
        l.dispose();
        memo.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusLightManagerTest.class);
}
