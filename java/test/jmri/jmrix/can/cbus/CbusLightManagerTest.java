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

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

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
    public void testctor(){
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

    @Override // numeric system name has a + added to it
    @Test
    public void testDefaultSystemName() {
        // create
        String name = "ML+5;-7";
        Light t = l.provideLight(name);
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(name));
    }


    @Override // numeric system name has a + added to it
    @Test
    public void testProvideName() {
        String name = "ML+574;-7123";
        // create
        Light t = l.provide(name);
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(name));
    }


    @Test
    public void testLowerLower() {
        Light t = l.provideLight("mlxabcdef;xfedcba");
        Assert.assertTrue("hex LowerLower ",t == l.getLight(t.getSystemName()));
        Light t2 = l.provideLight("ml+n1e77;-n1e45");
        Assert.assertNotNull("exists",t2);
        Assert.assertTrue("event LowerLower",t2 == l.getLight(t2.getSystemName()));
    }

    @Test
    public void testLowerUpper() {
        Light t = l.provideLight("mlxabcdef;xfedcba");
        Assert.assertTrue("hex Lower Upper",t == l.getLight(t.getSystemName().toUpperCase()));
        Light t2 = l.provideLight("ml+n1e77;-n1e45");
        Assert.assertNotNull("exists",t2);
        Assert.assertTrue("event Lower Upper",t2 == l.getLight(t2.getSystemName().toUpperCase()));
    }

    @Override
    @Test
    public void testUpperLower() {
        Light t = l.provideLight("MLXABCDEF01;XFFEDCCBA");
        Assert.assertTrue("Same hex getLight",t == l.getLight(t.getSystemName().toLowerCase()));
        Assert.assertTrue("Same hex getBySystemName",t == l.getBySystemName(t.getSystemName().toLowerCase()));
        Light t2 = l.provideLight("ML-N66E1;+N125E789");
        Assert.assertTrue("Same long getLight",t2 == l.getLight(t2.getSystemName().toLowerCase()));
        Assert.assertTrue("Same hex getBySystemName",t2 == l.getBySystemName(t2.getSystemName().toLowerCase()));
    }

    @Test
    public void threePartFail() {
        try {
            l.provideLight("ML+7;-5;+11");
            Assert.fail("3 split Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("Invalid system name for newLight:");
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testBadCbusLightAddresses() {
        try {
            Light t1 = l.provideLight("ML+N15E6");
            Assert.assertTrue( t1 != null );
        } catch (Exception e) {
            Assert.fail("Should NOT have thrown an exception");
        }

        try {
            l.provideLight("MLX;+N15E6");
            Assert.fail("X Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("Invalid system name for newLight:");
            Assert.assertTrue(true);
        }

        try {
            l.provideLight("MLXA;+N15E6");
            Assert.fail("A Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("Invalid system name for newLight:");
            Assert.assertTrue(true);
        }

        try {
            l.provideLight("MLXABC;+N15E6");
            Assert.fail("AC Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("Invalid system name for newLight:");
            Assert.assertTrue(true);
        }

        try {
            l.provideLight("MLXABCDE;+N15E6");
            Assert.fail("ABCDE Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("Invalid system name for newLight:");
            Assert.assertTrue(true);
        }
        
        try {
            l.provideLight("MLXABCDEF0;+N15E6");
            Assert.fail("ABCDEF0 Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("Invalid system name for newLight:");
            Assert.assertTrue(true);
        }

        try {
            l.provideLight("MLXABCDEF");
            Assert.fail("Single hex Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("Invalid system name for newLight:");
            Assert.assertTrue(true);
        }

        try {
            l.provideLight("MLXABCDEF;");
            Assert.fail("Single hex ; Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("Invalid system name for newLight:");
            Assert.assertTrue(true);
        }

        try {
            l.provideLight("MLXABCDEF;");
            Assert.fail("Single hex ; Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("Invalid system name for newLight:");
            Assert.assertTrue(true);
        }
        
        try {
            l.provideLight("ML;");
            Assert.fail("; no arg Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("Invalid system name for newLight:");
            Assert.assertTrue(true);
        }        
        
        try {
            l.provideLight("ML;+N15E6");
            Assert.fail("ML Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("Invalid system name for newLight:");
            Assert.assertTrue(true);
        }
        
        try {
            l.provideLight(";+N15E6");
            Assert.fail("; Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("Invalid system name for newLight:");
            Assert.assertTrue(true);
        }

        try {
            l.provideLight("S+N156E77;+N15E6");
            Assert.fail("S Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("Invalid system name for newLight:");
            Assert.assertTrue(true);
        }
        
        try {
            l.provideLight("M+N156E77;+N15E6");
            Assert.fail("M Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("Invalid system name for newLight:");
            Assert.assertTrue(true);
        }

        try {
            l.provideLight("ML++N156E77");
            Assert.fail("++ Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("Invalid system name for newLight:");
            Assert.assertTrue(true);
        }

        try {
            l.provideLight("ML--N156E77");
            Assert.fail("-- Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("Invalid system name for newLight:");
            Assert.assertTrue(true);
        }

        try {
            l.provideLight("MLN156E+77");
            Assert.fail("E+ Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("Invalid system name for newLight:");
            Assert.assertTrue(true);
        }

        try {
            l.provideLight("MLN156+E77");
            Assert.fail("+E Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("Invalid system name for newLight:");
            Assert.assertTrue(true);
        }

        try {
            l.provideLight("MLXLKJK;XLKJK");
            Assert.fail("LKJK Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("Invalid system name for newLight:");
            Assert.assertTrue(true);
        }
    }


    
    @Test
    public void testGoodCbusLightAddresses() {
        
        Light t = l.provideLight("ML+7");
        Assert.assertNotNull("exists",t);

        Light t2 = l.provideLight("ML+1;-1");
        Assert.assertNotNull("exists",t2);
        
        Light t3 = l.provideLight("ML+654e321");
        Assert.assertNotNull("exists",t3);

        Light t4 = l.provideLight("ML-654e321;+123e456");
        Assert.assertNotNull("exists",t4);

        Light t5 = l.provideLight("ML+n654e321");
        Assert.assertNotNull("exists",t5);

        Light t6 = l.provideLight("ML+N299E17;-N123E456");
        Assert.assertNotNull("exists",t6);

        Light t7 = l.provideLight("MLX04;X05");
        Assert.assertNotNull("exists",t7);

        Light t8 = l.provideLight("MLX2301;X30FF");
        Assert.assertNotNull("exists",t8);

        Light t9 = l.provideLight("MLX410001;X56FFFF");
        Assert.assertNotNull("exists",t9);

        Light t10 = l.provideLight("MLX6000010001;X72FFFFFF");
        Assert.assertNotNull("exists",t10);

        Light t11 = l.provideLight("MLX9000010001;X91FFFFFFFF");
        Assert.assertNotNull("exists",t11);

        Light t12 = l.provideLight("MLXB00D60010001;XB1FFFAAFFFFF");
        Assert.assertNotNull("exists",t12);

        Light t13 = l.provideLight("MLXD00D0060010001;XD1FFFAAAFFFFFE");
        Assert.assertNotNull("exists",t13);

        Light t14 = l.provideLight("MLXF00D0A0600100601;XF1FFFFAAFAFFFFFE");
        Assert.assertNotNull("exists",t14);
        
    }
    
    @Test
    public void testgetEntryToolTip() {
        String x = l.getEntryToolTip();
        Assert.assertTrue(x.contains("<html>"));
        
        Assert.assertTrue(l.allowMultipleAdditions("M77"));
        
    }

    @Test
    public void testvalidSystemNameFormat() {
        
        Assert.assertEquals("ML+123",NameValidity.VALID,l.validSystemNameFormat("MS+123"));
        Assert.assertEquals("ML+N123E123",NameValidity.VALID,l.validSystemNameFormat("MS+N123E123"));
        Assert.assertEquals("ML+123;456",NameValidity.VALID,l.validSystemNameFormat("MS+123;456"));
        Assert.assertEquals("ML1",NameValidity.VALID,l.validSystemNameFormat("MS1"));
        Assert.assertEquals("ML1;2",NameValidity.VALID,l.validSystemNameFormat("MS1;2"));
        Assert.assertEquals("ML65535",NameValidity.VALID,l.validSystemNameFormat("MS65535"));
        Assert.assertEquals("ML-65535",NameValidity.VALID,l.validSystemNameFormat("MS-65535"));
        Assert.assertEquals("ML100001",NameValidity.VALID,l.validSystemNameFormat("MS100001"));
        Assert.assertEquals("ML-100001",NameValidity.VALID,l.validSystemNameFormat("MS-100001"));

        Assert.assertEquals("M",NameValidity.INVALID,l.validSystemNameFormat("M"));
        Assert.assertEquals("ML",NameValidity.INVALID,l.validSystemNameFormat("ML"));
        Assert.assertEquals("ML-65536",NameValidity.INVALID,l.validSystemNameFormat("ML-65536"));        
        Assert.assertEquals("ML65536",NameValidity.INVALID,l.validSystemNameFormat("ML65536"));
        Assert.assertEquals("ML+1;+0",NameValidity.INVALID,l.validSystemNameFormat("ML+1;+0"));
        Assert.assertEquals("ML+1;-0",NameValidity.INVALID,l.validSystemNameFormat("ML+1;-0"));        
        Assert.assertEquals("ML+0;+17",NameValidity.INVALID,l.validSystemNameFormat("ML+0;+17"));
        Assert.assertEquals("ML+0;-17",NameValidity.INVALID,l.validSystemNameFormat("ML+0;-17"));
        Assert.assertEquals("ML+0",NameValidity.INVALID,l.validSystemNameFormat("ML+0"));
        Assert.assertEquals("ML-0",NameValidity.INVALID,l.validSystemNameFormat("ML-0"));
        Assert.assertEquals("ML7;0",NameValidity.INVALID,l.validSystemNameFormat("ML7;0"));
        Assert.assertEquals("ML0;7",NameValidity.INVALID,l.validSystemNameFormat("ML0;7"));
        
    }

    @Test
    public void testgetNextValidAddress() {
        CbusLightManager l = new CbusLightManager(memo);
        try {
            Assert.assertEquals("+17","+17",l.getNextValidAddress("+17","M"));
            Light t =  l.provideLight("ML+17");
            Assert.assertNotNull("exists",t);
            Assert.assertEquals("+18","+18",l.getNextValidAddress("+17","M"));
        
            Assert.assertEquals("+N45E22","+N45E22",l.getNextValidAddress("+N45E22","M"));
            Light ta =  l.provideLight("ML+N45E22");
            Assert.assertNotNull("exists",ta);
            Assert.assertEquals("+N45E23","+N45E23",l.getNextValidAddress("+N45E22","M"));        
        
        
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        
        try {
            Assert.assertEquals("null",null,l.getNextValidAddress(null,"M"));
            
        } catch (Exception e) {
            // JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
            // Assert.assertTrue(true);
        }
    }

    @Test
    public void testgetNextValidAddressPt2() {
        CbusLightManager l = new CbusLightManager(memo);
        Light t =  l.provideLight("ML+65535");
        Assert.assertNotNull("exists",t);
            
        try {
            Assert.assertEquals("+65535",null,l.getNextValidAddress("+65535","M"));
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
            Assert.assertTrue(true);
        } catch (Exception e) {
            // JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
            Assert.assertTrue(false);
        }
    }
    
    @Test
    public void testgetNextValidAddressPt3() {
        CbusLightManager l = new CbusLightManager(memo);
        Light t =  l.provideLight("ML+10");
        Assert.assertNotNull("exists",t);
            
        try {
            Assert.assertEquals("+10","+11",l.getNextValidAddress("+10","M"));
            // JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
            Assert.assertTrue(true);
        } catch (Exception e) {
            // JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
            Assert.assertTrue(false);
        }
    }
    
    @Test
    public void testgetNextValidAddressPt4() {
        CbusLightManager l = new CbusLightManager(memo);
        Light t =  l.provideLight("ML+9");
        Light ta =  l.provideLight("ML+10");
        Assert.assertNotNull("exists",t);
        Assert.assertNotNull("exists",ta);

        try {
            Assert.assertEquals(" null +9 +10",null,l.getNextValidAddress("+9","M"));
            // JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
            Assert.assertTrue(true);
        } catch (Exception e) {
            // JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
            Assert.assertTrue(false);
        }
    }

    
    @Test
    public void testProvideswhenNotNull() {
        Light t = l.provideLight("+4");
        Light ta = l.provideLight("+4");
        Assert.assertTrue(t == ta);
        t = null;
        ta = null;
    }
    
    
    @Test
    public void testcreateSystemName() {
        
        CbusLightManager l = new CbusLightManager(memo);
        
        try {
            Assert.assertEquals("ML+10","ML+10",l.createSystemName("+10","M"));
            Assert.assertEquals("ML+N34E610","ML+N34E610",l.createSystemName("+N34E610","M"));
            
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        
        try {
            Assert.assertEquals("M2L+10","ML+10",l.createSystemName("+10","M2"));
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        try {
            Assert.assertEquals("M2L+10","ML+10",l.createSystemName("+10","ZZZZZZZZZ"));
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        
        try {
            Assert.assertEquals("MLL",null,l.createSystemName("L","M"));
            Assert.assertTrue(true);
        } catch (Exception e) {
            // JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testcreateNewLightException(){
        CbusLightManager l = new CbusLightManager(memo);
        try {
            Light t = l.createNewLight("", null);
            Assert.assertNull("not exists",t);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }
    
    @Test
    public void testvalidSystemNameConfig(){
        CbusLightManager l = new CbusLightManager(memo);
        try {
            Assert.assertTrue(l.validSystemNameConfig("ML+123") );
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        try {
            Boolean testbool = l.validSystemNameConfig("");
            Assert.assertNull("exists",testbool);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    // The minimal setup for log4J
    @Before
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
        JUnitUtil.tearDown();
    }
    // private final static Logger log = LoggerFactory.getLogger(CbusLightManagerTest.class);
}
