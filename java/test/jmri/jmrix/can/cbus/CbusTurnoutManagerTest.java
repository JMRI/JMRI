package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.Manager.NameValidity;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.Turnout;
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
public class CbusTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    CanSystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        TrafficControllerScaffold tc = new TrafficControllerScaffold();
        memo.setTrafficController(tc);
        CbusTurnoutManager t = new CbusTurnoutManager(memo);
        Assert.assertNotNull("exists", t);
    }

    @Override
    public String getSystemName(int i) {
        return "MTX0A;+N123E3" + i;
    }

    @Override
    protected int getNumToTest1() {
        return 19;
    }

    @Override
    protected int getNumToTest2() {
        return 7269;
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        Turnout t = l.provideTurnout("MTX0A;+N15E741");
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertTrue("system name correct ", t == l.getBySystemName("MTX0A;+N15E741"));
    }

    @Test
    @Override
    public void testProvideName() {

        // create
        Turnout t = l.provide("MT+123");
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName("MT+123"));
    }

    @Test
    public void testBadCbusTurnoutAddresses() {

        try {
            Turnout t1 = l.provideTurnout("MT+N15E6");
            Assert.assertNotNull(t1);
        } catch (IllegalArgumentException e) {
            Assert.fail("Should NOT have thrown an exception");
        }

        try {
            l.provideTurnout("MTX;+N15E6");
            Assert.fail("X Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Wrong number of events in address: X;+N15E6");
        }

        try {
            l.provideTurnout("MTXA;+N15E6");
            Assert.fail("A Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Wrong number of events in address: XA;+N15E6");
        }

        try {
            l.provideTurnout("MTXABC;+N15E6");
            Assert.fail("AC Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Wrong number of events in address: XABC;+N15E6");
        }

        try {
            l.provideTurnout("MTXABCDE;+N15E6");
            Assert.fail("ABCDE Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Wrong number of events in address: XABCDE;+N15E6");
        }

        try {
            l.provideTurnout("MTXABCDEF0;+N15E6");
            Assert.fail("ABCDEF0 Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Wrong number of events in address: XABCDEF0;+N15E6");
        }

        try {
            l.provideTurnout("MTXABCDEF");
            Assert.fail("Single hex Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Turnout: can't make 2nd event from address XABCDEF");
        }

        try {
            l.provideTurnout("MT;XABCDEF");
            Assert.fail("Single hex ; Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Address Too Short? : ");
        }

        try {
            l.provideTurnout("MTXABCDEF;");
            Assert.fail("Single hex ; Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Should not end with ; XABCDEF;");
        }

        try {
            l.provideTurnout("MT;");
            Assert.fail("; no arg Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Should not end with ; ;");
        }

        try {
            l.provideTurnout("MT;+N15E6");
            Assert.fail("MS Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Address Too Short? : ");
        }
    }

    @Test
    public void testBadCbusTurnoutAddressesPt2() {

        try {
            l.provideTurnout(";+N15E62");
            Assert.fail("; Should have thrown an exception");
        } catch (IllegalArgumentException ex) {
            JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Address Too Short? : ");
            Assert.assertEquals("Address Too Short? : ", ex.getMessage());
        }

        try {
            l.provideTurnout("T+N156E77;+N123E456");
            Assert.fail("Missing M Should have thrown an exception");
        } catch (IllegalArgumentException ex) {
            JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Wrong number of events in address: T+N156E77;+N123E456");
            Assert.assertEquals("Wrong number of events in address: T+N156E77;+N123E456",
                    ex.getMessage());
        }
    }

    @Test
    public void testBadCbusTurnoutAddressesPt3() {
        try {
            l.provideTurnout("M+N156E77;+N15E60");
            Assert.fail("M Should have thrown an exception");
        } catch (IllegalArgumentException ex) {
            JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Wrong number of events in address: M+N156E77;+N15E60");
            Assert.assertEquals("Wrong number of events in address: M+N156E77;+N15E60",
                    ex.getMessage());
        }
    }

    public void testBadCbusTurnoutAddressesPt4() {
        try {
            l.provideTurnout("MT++N156E78");
            Assert.fail("++ Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Did not find usabl");
        }
    }

    public void testBadCbusTurnoutAddressesPt5() {
        try {
            l.provideTurnout("MT--N156E78");
            Assert.fail("-- Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Did not find usabl");
        }

        try {
            l.provideTurnout("MTN156E+80");
            Assert.fail("E+ Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Did not find");
        }

        try {
            l.provideTurnout("MTN156+E77");
            Assert.fail("+E Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Did not find");
        }

        try {
            l.provideTurnout("MTXLKJK;XLKJK");
            Assert.fail("LKJK Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Did not find");
        }

        try {
            l.provideTurnout("MT+7;-5;+11");
            Assert.fail("3 split Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Wrong number of events");
        }
    }

    @Test
    public void testLowercaseSystemName() {
        String name = "mt+n1e77;-n1e45";
        try {
            l.provideTurnout(name);
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Wrong number of events in address: mt+n1e77;-n1e45");
        }
        Turnout t = l.provideTurnout(name.toUpperCase());
        Assert.assertNotNull(t);
        Assert.assertNotEquals(t, l.getBeanBySystemName(name));
        Assert.assertNull(l.getBeanBySystemName(name));
    }

    @Test
    public void testgetEntryToolTip() {
        String x = l.getEntryToolTip();
        Assert.assertTrue(x.contains("<html>"));

        Assert.assertTrue(l.allowMultipleAdditions("M77"));
    }

    @Test
    public void testvalidSystemNameFormat() {

        Assert.assertEquals("MT+123", NameValidity.VALID, l.validSystemNameFormat("MT+123"));
        Assert.assertEquals("MT+N123E123", NameValidity.VALID, l.validSystemNameFormat("MT+N123E123"));
        Assert.assertEquals("MT+123;456", NameValidity.VALID, l.validSystemNameFormat("MT+123;456"));
        Assert.assertEquals("MT1", NameValidity.VALID, l.validSystemNameFormat("MT1"));
        Assert.assertEquals("MT1;2", NameValidity.VALID, l.validSystemNameFormat("MT1;2"));
        Assert.assertEquals("MT65535", NameValidity.VALID, l.validSystemNameFormat("MT65535"));
        Assert.assertEquals("MT-65535", NameValidity.VALID, l.validSystemNameFormat("MT-65535"));
        Assert.assertEquals("MT100001", NameValidity.VALID, l.validSystemNameFormat("MT100001"));
        Assert.assertEquals("MT-100001", NameValidity.VALID, l.validSystemNameFormat("MT-100001"));

        Assert.assertEquals("M", NameValidity.INVALID, l.validSystemNameFormat("M"));
        Assert.assertEquals("MT", NameValidity.INVALID, l.validSystemNameFormat("MT"));
        Assert.assertEquals("MT-65536", NameValidity.INVALID, l.validSystemNameFormat("MT-65536"));
        Assert.assertEquals("MT65536", NameValidity.INVALID, l.validSystemNameFormat("MT65536"));
        Assert.assertEquals("MT+1;+0", NameValidity.INVALID, l.validSystemNameFormat("MT+1;+0"));
        Assert.assertEquals("MT+1;-0", NameValidity.INVALID, l.validSystemNameFormat("MT+1;-0"));
        Assert.assertEquals("MT+0;+17", NameValidity.INVALID, l.validSystemNameFormat("MT+0;+17"));
        Assert.assertEquals("MT+0;-17", NameValidity.INVALID, l.validSystemNameFormat("MT+0;-17"));
        Assert.assertEquals("MT+0", NameValidity.INVALID, l.validSystemNameFormat("MT+0"));
        Assert.assertEquals("MT-0", NameValidity.INVALID, l.validSystemNameFormat("MT-0"));
        Assert.assertEquals("MT7;0", NameValidity.INVALID, l.validSystemNameFormat("MT7;0"));
        Assert.assertEquals("MT0;7", NameValidity.INVALID, l.validSystemNameFormat("MT0;7"));
    }

    @Test
    public void testgetNextValidAddress() throws JmriException {
        
        Assert.assertEquals("+17", "+17", l.getNextValidAddress("+17", "M"));
        Turnout t =  l.provideTurnout("MT+17");
        Assert.assertNotNull("exists", t);
        Assert.assertEquals("+18", "+18", l.getNextValidAddress("+17", "M"));
    
        Assert.assertEquals("+N45E22", "+N45E22", l.getNextValidAddress("+N45E22", "M"));
        Turnout ta =  l.provideTurnout("MT+N45E22");
        Assert.assertNotNull("exists", ta);
        Assert.assertEquals("+N45E23", "+N45E23", l.getNextValidAddress("+N45E22", "M"));        
        
        try {
            Assert.assertNull("null", l.getNextValidAddress(null, "M"));
        } catch (JmriException ex) {
            Assert.assertEquals("java.lang.IllegalArgumentException: No address Passed ", ex.getMessage());
        }
    }

    @Test
    public void testgetNextValidAddressPt2() throws JmriException {
        Turnout t =  l.provideTurnout("MT+65535");
        Assert.assertNotNull("exists", t);
            
        Assert.assertEquals("+65535", null, l.getNextValidAddress("+65535", "M"));
        JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: ");
    }
    
    @Test
    public void testgetNextValidAddressPt3() throws JmriException {
        
        Turnout t =  l.provideTurnout("MT+10");
        Assert.assertNotNull("exists", t);
            
        Assert.assertEquals("+10", "+11", l.getNextValidAddress("+10", "M"));
    }
    
    @Test
    public void testgetNextValidAddressPt4() throws JmriException {

        Turnout t = l.provideTurnout("MT+9");
        Turnout ta = l.provideTurnout("MT+10");
        Assert.assertNotNull("exists", t);
        Assert.assertNotNull("exists", ta);

        Assert.assertEquals(" null +9 +10", "+11", l.getNextValidAddress("+9", "M"));
    }
    
    @Test
    public void testcreateSystemName() throws JmriException {
        
        Assert.assertEquals("MT+10", "MT+10", l.createSystemName("10", "M"));
        Assert.assertEquals("MT+N34E610", "MT+N34E610", l.createSystemName("+N34E610", "M"));
        Assert.assertEquals("MT5;6", "MT+5;-6", l.createSystemName("5;6", "M"));
        
        Assert.assertEquals("M2T+10", "M2T+10", l.createSystemName("+10", "M2"));

        Assert.assertEquals("ZZZZZZZZZ2T+10", "ZZZZZZZZZT+10", l.createSystemName("+10", "ZZZZZZZZZ"));
        
        try {
            Assert.assertEquals("MTT", null, l.createSystemName("S", "M"));
        } catch (JmriException ex) {
            Assert.assertEquals("java.lang.IllegalArgumentException: Wrong number of events in address: S", ex.getMessage());
        }
    }

    @Test
    public void testProvideswhenNotNull() {
        Turnout t = l.provideTurnout("+4");
        Turnout ta = l.provideTurnout("+4");
        Assert.assertTrue(t == ta);
    }
    
    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(new TrafficControllerScaffold());
        l = new CbusTurnoutManager(memo);
    }

    @After
    public void tearDown() {
        l.dispose();
        memo.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusTurnoutManagerTest.class);

}
