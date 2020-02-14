package jmri.jmrix.can.cbus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.Manager.NameValidity;
import jmri.JmriException;
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
 * @author Andrew Crosland Copyright (C) 2020
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

        Throwable thrown = catchThrowable(() -> {
            Turnout t1 = l.provideTurnout("MT+N15E6");
            Assert.assertNotNull(t1);
        });
        assertThat(thrown).isNull();
        
        thrown = catchThrowable(() -> {
            l.provideTurnout("MTX;+N15E6");
        });
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        
        thrown = catchThrowable(() -> {
            l.provideTurnout("MTXA;+N15E6");
        });
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        
        thrown = catchThrowable(() -> {
            l.provideTurnout("MTXABC;+N15E6");
        });
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        
        thrown = catchThrowable(() -> {
            l.provideTurnout("MTXABCDE;+N15E6");
        });
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        
        thrown = catchThrowable(() -> {
            l.provideTurnout("MTXABCDEF0;+N15E6");
        });
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        
        thrown = catchThrowable(() -> {
            l.provideTurnout("MTXABCDEF");
        });
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        
        thrown = catchThrowable(() -> {
            l.provideTurnout("MT;XABCDEF");
        });
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        
        thrown = catchThrowable(() -> {
            l.provideTurnout("MTXABCDEF;");
        });
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        
        thrown = catchThrowable(() -> {
            l.provideTurnout("MT;");
        });
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        
        thrown = catchThrowable(() -> {
            l.provideTurnout("MT;+N15E6");
        });
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testBadCbusTurnoutAddressesPt2() {

        Throwable thrown = catchThrowable(() -> {
            l.provideTurnout(";+N15E62");
        });
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        assertThat(thrown).hasMessage("Address Too Short? : ");
        
        thrown = catchThrowable(() -> {
            l.provideTurnout("T+N156E77;+N123E456");
        });
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        assertThat(thrown).hasMessage("Wrong number of events in address: T+N156E77;+N123E456");
    }

    @Test
    public void testBadCbusTurnoutAddressesPt3() {
        Throwable thrown = catchThrowable(() -> {
            l.provideTurnout("M+N156E77;+N15E60");
        });
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        assertThat(thrown).hasMessage("Wrong number of events in address: M+N156E77;+N15E60");
    }

    @Test
    public void testBadCbusTurnoutAddressesPt4() {
        Throwable thrown = catchThrowable(() -> {
            l.provideTurnout("MT++N156E78");
        });
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        assertThat(thrown).hasMessage("Wrong number of events in address: ++N156E78");
    }

    @Test
    public void testBadCbusTurnoutAddressesPt5() {
        Throwable thrown = catchThrowable(() -> {
            l.provideTurnout("MT--N156E78");
        });
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        assertThat(thrown).hasMessage("Wrong number of events in address: --N156E78");
        
        thrown = catchThrowable(() -> {
            l.provideTurnout("MTN156E+80");
        });
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        assertThat(thrown).hasMessage("Wrong number of events in address: N156E+80");
        
        thrown = catchThrowable(() -> {
            l.provideTurnout("MTN156+E77");
        });
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        assertThat(thrown).hasMessage("Wrong number of events in address: N156+E77");
        
        thrown = catchThrowable(() -> {
            l.provideTurnout("MTXLKJK;XLKJK");
        });
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        assertThat(thrown).hasMessageContaining("Wrong number of events in address: XLKJK;XLKJK");
        
        thrown = catchThrowable(() -> {
            l.provideTurnout("MT+7;-5;+11");
        });
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        assertThat(thrown).hasMessage("Wrong number of events in address: +7;-5;+11");
    }

    @Test
    public void testLowercaseSystemName() {
        String name = "mt+n1e77;-n1e45";
        Throwable thrown = catchThrowable(() -> {
            l.provideTurnout(name);
        });
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        assertThat(thrown).hasMessage("Wrong number of events in address: mt+n1e77;-n1e45");
        
        Turnout t = l.provideTurnout(name.toUpperCase());
        Assert.assertNotNull(t);
        Assert.assertNotEquals(t, l.getBySystemName(name));
        Assert.assertNull(l.getBySystemName(name));
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
    
    @Test
    @Override
    public void testAutoSystemNames() {
        Assert.assertEquals("No auto system names",0,tcis.numListeners());
    }
    
    private TrafficControllerScaffold tcis;
    
    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        l = new CbusTurnoutManager(memo);
    }

    @After
    public void tearDown() {
        tcis = null;
        l.dispose();
        memo.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusTurnoutManagerTest.class);

}
