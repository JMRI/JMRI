package jmri.jmrix.can.cbus;

import jmri.Manager.NameValidity;
import jmri.Sensor;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.*;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.can.cbus.CbusSensorManager class.
 *
 * @author	Bob Jacobsen Copyright 2008
 * @author	Paul Bender Copyright (C) 2016
 */
public class CbusSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tcis;

    @Override
    public String getSystemName(int i) {
        return "MSX0A;+N15E" + i;
    }

    @Test
    @Override
    public void testCreate() {
        Assert.assertNotNull("createsSensor", l.provideSensor(memo.getSystemPrefix() + "SX0A;+N15E6"));
    }

    @Test
    @Override
    public void testDefaultSystemName() {
        // create
        Sensor t = l.provideSensor("MSX0A;+N15E" + getNumToTest1());
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Test
    @Override
    public void testProvideName() {
        // create
        Sensor t = l.provide("" + getSystemName(getNumToTest1()));
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }


    @Test
    public void testLowerLower() {
        Sensor t = l.provideSensor("ms+n1e77;-n1e45");
        Assert.assertNotNull("exists",t);
        Assert.assertTrue("event lowercase",t == l.getSensor(t.getSystemName()));
        Sensor t2 = l.provideSensor("msxabcdef;xfedcba");
        Assert.assertNotNull("exists",t2);
        Assert.assertTrue("hex lowercase",t2 == l.getSensor(t2.getSystemName()));
    }

    @Test
    public void testLowerUpper() {
        Sensor t = l.provideSensor("ms+n1e77;-n1e45");
        Assert.assertNotNull("exists",t);
        Assert.assertTrue("event lower upper",t == l.getSensor(t.getSystemName().toUpperCase()));
        Sensor t2 = l.provideSensor("msxabcdef;xfedcba");
        Assert.assertNotNull("exists",t2);
        Assert.assertTrue("hex lower upper",t2 == l.getSensor(t2.getSystemName().toUpperCase()));
    }

    @Override
    @Test
    public void testUpperLower() {
        Sensor t = l.provideSensor("MSXABCDEF01;XFFEDCCBA");
        Assert.assertTrue(" hex upper lower",t == l.getSensor(t.getSystemName().toLowerCase()));
        Sensor t2 = l.provideSensor("MS-N66E1;+N15E789");
        Assert.assertTrue("event upper lower",t2 == l.getSensor(t2.getSystemName().toLowerCase()));
    }

    @Override
    @Test
    public void testMoveUserName() {
        Sensor t1 = l.provideSensor("MSX0A;+N15E" + getNumToTest1());
        Sensor t2 = l.provideSensor("MSX0A;+N15E" + getNumToTest2());
        t1.setUserName("UserName");
        Assert.assertTrue(t1 == l.getByUserName("UserName"));

        t2.setUserName("UserName");
        Assert.assertTrue(t2 == l.getByUserName("UserName"));

        Assert.assertTrue(null == t1.getUserName());
    }
    
    @Test
    public void testBadCbusSensorAddresses() {

        try {
            Sensor t1 = l.provideSensor("MS+N15E6");
            Assert.assertTrue( t1 != null );
        } catch (Exception e) {
            Assert.fail("Should NOT have thrown an exception");
        }

        try {
            l.provideSensor("MSX;+N15E6");
            Assert.fail("X Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
            Assert.assertTrue(true);
        }

        try {
            l.provideSensor("MSXA;+N15E6");
            Assert.fail("A Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
            Assert.assertTrue(true);
        }

        try {
            l.provideSensor("MSXABC;+N15E6");
            Assert.fail("AC Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
            Assert.assertTrue(true);
        }

        try {
            l.provideSensor("MSXABCDE;+N15E6");
            Assert.fail("ABCDE Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
            Assert.assertTrue(true);
        }
        
        try {
            l.provideSensor("MSXABCDEF0;+N15E6");
            Assert.fail("ABCDEF0 Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
            Assert.assertTrue(true);
        }

        try {
            l.provideSensor("MSXABCDEF");
            Assert.fail("Single hex Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: can't make 2nd event");
            Assert.assertTrue(true);
        }

        try {
            l.provideSensor("MS;XABCDEF");
            Assert.fail("Single hex ; Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException:");
            Assert.assertTrue(true);
        }

        try {
            l.provideSensor("MSXABCDEF;");
            Assert.fail("Single hex ; Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
            Assert.assertTrue(true);
        }
        
        try {
            l.provideSensor("MS;");
            Assert.fail("; no arg Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
            Assert.assertTrue(true);
        }        
        
        try {
            l.provideSensor("MS;+N15E6");
            Assert.fail("MS Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
            Assert.assertTrue(true);
        }
        
        try {
            l.provideSensor(";+N15E6");
            Assert.fail("; Should have thrown an exception");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        try {
            l.provideSensor("S+N156E77;+N15E6");
            Assert.fail("S Should have thrown an exception");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
        
        try {
            l.provideSensor("M+N156E77;+N15E6");
            Assert.fail("M Should have thrown an exception");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        try {
            l.provideSensor("MS++N156E77");
            Assert.fail("++ Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
            Assert.assertTrue(true);
        }

        try {
            l.provideSensor("MS--N156E77");
            Assert.fail("-- Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
            Assert.assertTrue(true);
        }

        try {
            l.provideSensor("MSN156E+77");
            Assert.fail("E+ Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
            Assert.assertTrue(true);
        }

        try {
            l.provideSensor("MSN156+E77");
            Assert.fail("+E Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
            Assert.assertTrue(true);
        }

        try {
            l.provideSensor("MSXLKJK;XLKJK");
            Assert.fail("LKJK Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
            Assert.assertTrue(true);
        }

        try {
            l.provideSensor("MS+7;-5;+11");
            Assert.fail("3 split Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Wrong number of events");
            Assert.assertTrue(true);
        }
    }
    
    @Test
    public void testGoodCbusSensorAddresses() {
        
        Sensor t = l.provideSensor("MS+7");
        Assert.assertNotNull("exists",t);

        Sensor t2 = l.provideSensor("MS+1;-1");
        Assert.assertNotNull("exists",t2);
        
        Sensor t3 = l.provideSensor("MS+654e321");
        Assert.assertNotNull("exists",t3);

        Sensor t4 = l.provideSensor("MS-654e321;+123e456");
        Assert.assertNotNull("exists",t4);

        Sensor t5 = l.provideSensor("MS+n654e321");
        Assert.assertNotNull("exists",t5);

        Sensor t6 = l.provideSensor("MS+N299E17;-N123E456");
        Assert.assertNotNull("exists",t6);

        Sensor t7 = l.provideSensor("MSX04;X05");
        Assert.assertNotNull("exists",t7);

        Sensor t8 = l.provideSensor("MSX2301;X30FF");
        Assert.assertNotNull("exists",t8);

        Sensor t9 = l.provideSensor("MSX410001;X56FFFF");
        Assert.assertNotNull("exists",t9);

        Sensor t10 = l.provideSensor("MSX6000010001;X72FFFFFF");
        Assert.assertNotNull("exists",t10);

        Sensor t11 = l.provideSensor("MSX9000010001;X91FFFFFFFF");
        Assert.assertNotNull("exists",t11);

        Sensor t12 = l.provideSensor("MSXB00D60010001;XB1FFFAAFFFFF");
        Assert.assertNotNull("exists",t12);

        Sensor t13 = l.provideSensor("MSXD00D0060010001;XD1FFFAAAFFFFFE");
        Assert.assertNotNull("exists",t13);

        Sensor t14 = l.provideSensor("MSXF00D0A0600100601;XF1FFFFAAFAFFFFFE");
        Assert.assertNotNull("exists",t14);
        
    }




    @Test
    public void testQueryAll() {
        tcis.outbound.clear();
        Sensor t1 = l.provideSensor("MS+N123E456");
        Sensor t2 = l.provideSensor("MS-N9875E45670");

        Assert.assertTrue(0 == tcis.outbound.size());
        
        l.updateAll();
        
        // log.warn("size {}",tcis.outbound);
        Assert.assertTrue(2 == tcis.outbound.size());
        
        Sensor t3 = l.provideSensor("MSX0A;X5E6DEEF4");
        tcis.outbound.clear();
        l.updateAll();
        Assert.assertTrue(3 == tcis.outbound.size());
        Assert.assertNotNull("exists",t1);
        Assert.assertNotNull("exists",t2);
        Assert.assertNotNull("exists",t3);

    }
    
    @Test
    public void testgetEntryToolTip() {
        String x = l.getEntryToolTip();
        Assert.assertTrue(x.contains("<html>"));
        
        Assert.assertTrue(l.allowMultipleAdditions("M77"));
        
    }
    
    @Test
    public void testvalidSystemNameFormat() {
        
        Assert.assertEquals("MS+123",NameValidity.VALID,l.validSystemNameFormat("MS+123"));
        Assert.assertEquals("MS+N123E123",NameValidity.VALID,l.validSystemNameFormat("MS+N123E123"));
        Assert.assertEquals("MS+123;456",NameValidity.VALID,l.validSystemNameFormat("MS+123;456"));
        Assert.assertEquals("MS1",NameValidity.VALID,l.validSystemNameFormat("MS1"));
        Assert.assertEquals("MS1;2",NameValidity.VALID,l.validSystemNameFormat("MS1;2"));
        Assert.assertEquals("MS65535",NameValidity.VALID,l.validSystemNameFormat("MS65535"));
        Assert.assertEquals("MS-65535",NameValidity.VALID,l.validSystemNameFormat("MS-65535"));
        Assert.assertEquals("MS100001",NameValidity.VALID,l.validSystemNameFormat("MS100001"));
        Assert.assertEquals("MS-100001",NameValidity.VALID,l.validSystemNameFormat("MS-100001"));
        Assert.assertEquals("MS+N65535e65535",NameValidity.VALID,l.validSystemNameFormat("MS+N65535e65535"));
        Assert.assertEquals("MS+N65535e65535;-N65535e65535",NameValidity.VALID,l.validSystemNameFormat("MS+N65535e65535;-N65535e65535"));
        Assert.assertEquals("MS+N1E2;-N3E4",NameValidity.VALID,l.validSystemNameFormat("MS+N1E2;-N3E4"));
        Assert.assertEquals("MS-N1E2;+N3E4",NameValidity.VALID,l.validSystemNameFormat("MS-N1E2;+N3E4"));

        Assert.assertEquals("M",NameValidity.INVALID,l.validSystemNameFormat("M"));
        Assert.assertEquals("MS",NameValidity.INVALID,l.validSystemNameFormat("MS"));
        Assert.assertEquals("MS-65536",NameValidity.INVALID,l.validSystemNameFormat("MS-65536"));        
        Assert.assertEquals("MS65536",NameValidity.INVALID,l.validSystemNameFormat("MS65536"));
        Assert.assertEquals("MS+1;+0",NameValidity.INVALID,l.validSystemNameFormat("MS+1;+0"));
        Assert.assertEquals("MS+1;-0",NameValidity.INVALID,l.validSystemNameFormat("MS+1;-0"));        
        Assert.assertEquals("MS+0;+17",NameValidity.INVALID,l.validSystemNameFormat("MS+0;+17"));
        Assert.assertEquals("MS+0;-17",NameValidity.INVALID,l.validSystemNameFormat("MS+0;-17"));
        Assert.assertEquals("MS+0",NameValidity.INVALID,l.validSystemNameFormat("MS+0"));
        Assert.assertEquals("MS-0",NameValidity.INVALID,l.validSystemNameFormat("MS-0"));
        Assert.assertEquals("MS7;0",NameValidity.INVALID,l.validSystemNameFormat("MS7;0"));
        Assert.assertEquals("MS0;7",NameValidity.INVALID,l.validSystemNameFormat("MS0;7"));
        Assert.assertEquals("MS+N17E0",NameValidity.INVALID,l.validSystemNameFormat("MS+N17E0"));
        Assert.assertEquals("MS+N17E00",NameValidity.INVALID,l.validSystemNameFormat("MS+N17E00"));
        Assert.assertEquals("MS+N0E17",NameValidity.VALID,l.validSystemNameFormat("MS+N0E17"));
        Assert.assertEquals("MS+N00E17",NameValidity.VALID,l.validSystemNameFormat("MS+N00E17"));
        Assert.assertEquals("MS+0E17",NameValidity.VALID,l.validSystemNameFormat("MS+0E17"));
        Assert.assertEquals("MS0E17",NameValidity.INVALID,l.validSystemNameFormat("MS0E17"));
        Assert.assertEquals("MS+N65535e65536",NameValidity.INVALID,l.validSystemNameFormat("MS+N65535e65536"));
        Assert.assertEquals("MS+N65536e65535",NameValidity.INVALID,l.validSystemNameFormat("MS+N65536e65535"));
        
    }
    
    @Test
    public void testgetNextValidAddress() {
        
        try {
            Assert.assertEquals("+17","+17",l.getNextValidAddress("+17","M"));
            Sensor t =  l.provideSensor("MS+17");
            Assert.assertNotNull("exists",t);
            Assert.assertEquals("+18","+18",l.getNextValidAddress("+17","M"));
        
            Assert.assertEquals("+N45E22","+N45E22",l.getNextValidAddress("+N45E22","M"));
            Sensor ta =  l.provideSensor("MS+N45E22");
            Assert.assertNotNull("exists",ta);
            Assert.assertEquals("+N45E23","+N45E23",l.getNextValidAddress("+N45E22","M"));        
        
        
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        
        try {
            Assert.assertEquals("null",null,l.getNextValidAddress(null,"M"));
            
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testgetNextValidAddressPt2() {
        Sensor t =  l.provideSensor("MS+65535");
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
        
        Sensor t =  l.provideSensor("MS+10");
        Assert.assertNotNull("exists",t);
            
        try {
            Assert.assertEquals("+10","+11",l.getNextValidAddress("+10","M"));
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }
    
    @Test
    public void testgetNextValidAddressPt4() {

        Sensor t =  l.provideSensor("MS+9");
        Sensor ta =  l.provideSensor("MS+10");
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
    public void testcreateSystemName() {
        
        try {
            Assert.assertEquals("MS+10","MS+10",l.createSystemName("+10","M"));
            Assert.assertEquals("MS+N34E610","MS+N34E610",l.createSystemName("+N34E610","M"));
            Assert.assertEquals("MS-N34E610","MS-N34E610",l.createSystemName("-N34E610","M"));
            Assert.assertEquals("MS+N34E610;-N987E654","MS+N34E610;-N987E654",l.createSystemName("+N34E610;-N987E654","M"));
            
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        
        try {
            Assert.assertEquals("M2S+10","MS+10",l.createSystemName("+10","M2"));
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        try {
            Assert.assertEquals("M2S+10","MS+10",l.createSystemName("+10","ZZZZZZZZZ"));
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        
        try {
            Assert.assertEquals("MSS",null,l.createSystemName("S","M"));
            Assert.assertTrue(false);
        } catch (Exception e) {
            // JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
            Assert.assertTrue(true);
        }
    }
    
    
    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        l = new CbusSensorManager(memo);
    }

    @After
    public void tearDown() {
        l.dispose();
        memo.dispose();
        JUnitUtil.tearDown();
    }
    // private final static Logger log = LoggerFactory.getLogger(CbusSensorManagerTest.class);
}
