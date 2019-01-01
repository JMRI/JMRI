package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
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
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tc);
        CbusTurnoutManager t = new CbusTurnoutManager(memo);
        Assert.assertNotNull("exists",t);
    }


    public String getSystemName(int i){
        return "MTX0A;+N123E9632" + i;
    }
    
    @Override
    protected int getNumToTest1() {
        return 19;
    }
    
    @Override
    protected int getNumToTest2() {
        return 47269;
    }
    
    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        Turnout t = l.provideTurnout("MTX0A;+N15E741");
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName("MTX0A;+N15E741"));
    }
    
    @Test
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
            Assert.assertTrue( t1 != null );
        } catch (Exception e) {
            Assert.fail("Should NOT have thrown an exception");
        }

        try {
            l.provideTurnout("MTX;+N15E6");
            Assert.fail("X Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Did not find");
            Assert.assertTrue(true);
        }

        try {
            l.provideTurnout("MTXA;+N15E6");
            Assert.fail("A Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Did not find");
            Assert.assertTrue(true);
        }

        try {
            l.provideTurnout("MTXABC;+N15E6");
            Assert.fail("AC Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Did not find");
            Assert.assertTrue(true);
        }

        try {
            l.provideTurnout("MTXABCDE;+N15E6");
            Assert.fail("ABCDE Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Did not find");
            Assert.assertTrue(true);
        }
        
        try {
            l.provideTurnout("MTXABCDEF0;+N15E6");
            Assert.fail("ABCDEF0 Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Did not find");
            Assert.assertTrue(true);
        }

        try {
            l.provideTurnout("MTXABCDEF");
            Assert.fail("Single hex Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: can't make 2nd event");
            Assert.assertTrue(true);
        }

        try {
            l.provideTurnout("MT;XABCDEF");
            Assert.fail("Single hex ; Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Did not find usable ");
            Assert.assertTrue(true);
        }

        try {
            l.provideTurnout("MTXABCDEF;");
            Assert.fail("Single hex ; Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Did not find usable ");
            Assert.assertTrue(true);
        }
        
        try {
            l.provideTurnout("MT;");
            Assert.fail("; no arg Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Did not find");
            Assert.assertTrue(true);
        }        
        
        try {
            l.provideTurnout("MT;+N15E6");
            Assert.fail("MS Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Did not find");
            Assert.assertTrue(true);
        }
        
    }
    
    @Test
    public void testBadCbusTurnoutAddressesPt2() {
        
        try {
            l.provideTurnout(";+N15E62");
            Assert.fail("; Should have thrown an exception");
            
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Did not find");
            Assert.assertTrue(true);
        }

        try {
            l.provideTurnout("T+N156E77;+N123E456");
            Assert.fail("S Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Did not find");
            Assert.assertTrue(true);
        }
    }
    
    @Test
    public void testBadCbusTurnoutAddressesPt3() {
        try {
            l.provideTurnout("M+N156E77;+N15E60");
            Assert.fail("M Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Did not find usabl");
            Assert.assertTrue(true);
        }
    }

    public void testBadCbusTurnoutAddressesPt4() {
        try {
            l.provideTurnout("MT++N156E78");
            Assert.fail("++ Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Did not find usabl");
            Assert.assertTrue(true);
        }

    }
    
    public void testBadCbusTurnoutAddressesPt5() {
        try {
            l.provideTurnout("MT--N156E78");
            Assert.fail("-- Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Did not find usabl");
            Assert.assertTrue(true);
        }

        try {
            l.provideTurnout("MTN156E+80");
            Assert.fail("E+ Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Did not find");
            Assert.assertTrue(true);
        }

        try {
            l.provideTurnout("MTN156+E77");
            Assert.fail("+E Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Did not find");
            Assert.assertTrue(true);
        }

        try {
            l.provideTurnout("MTXLKJK;XLKJK");
            Assert.fail("LKJK Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Did not find");
            Assert.assertTrue(true);
        }

        try {
            l.provideTurnout("MT+7;-5;+11");
            Assert.fail("3 split Should have thrown an exception");
        } catch (Exception e) {
            JUnitAppender.assertErrorMessageStartsWith("java.lang.IllegalArgumentException: Wrong number of events");
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testLowerLower() {
        Turnout t = l.provideTurnout("mt+n1e77;-n1e45");
        Assert.assertNotNull("exists",t);
        Assert.assertTrue("Retrievable lowercase",t == l.getTurnout(t.getSystemName()));
    }

    @Test
    public void testLowerUpper() {
        Turnout t = l.provideTurnout("mt+n1e77;-n1e45");
        Assert.assertNotNull("exists",t);
        Assert.assertTrue("Retrievable lowercase",t == l.getTurnout(t.getSystemName().toUpperCase()));
    }

    @Test
    public void testUpperLower() {
        Turnout t = l.provideTurnout("MTXABCDEF01;XFFEDCCBA");
        Assert.assertTrue("Same hex getTurnout",t == l.getTurnout(t.getSystemName().toLowerCase()));
        Turnout t2 = l.provideTurnout("MT-N66E1;+N15E789");
        Assert.assertTrue("Same long getTurnout",t2 == l.getTurnout(t2.getSystemName().toLowerCase()));
    }

    @Test
    public void testgetEntryToolTip() {
        String x = l.getEntryToolTip();
        Assert.assertTrue(x.contains("<html>"));
    }
    
    // The minimal setup for log4J
    @Before
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
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusTurnoutManagerTest.class);
}
