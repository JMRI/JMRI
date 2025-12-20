package jmri.jmrix.can.cbus;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.Manager.NameValidity;
import jmri.JmriException;
import jmri.Turnout;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2018
 */
public class CbusTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    @Test
    public void testCTor() {
        CbusTurnoutManager t = new CbusTurnoutManager(memo);
        assertNotNull( t, "exists");
    }

    @Override
    public String getSystemName(int i) {
        return "MTX0A;+N123E3" + i;
    }

    @Override
    protected String getASystemNameWithNoPrefix() {
        return "+6";
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
        assertNotNull( t, "real object returned ");
        assertTrue( t == l.getBySystemName("MTX0A;+N15E741"), "system name correct ");
    }

    @Test
    @Override
    public void testProvideName() {

        // create
        Turnout t = l.provide("MT+123");
        // check
        assertNotNull( t, "real object returned ");
        assertTrue( t == l.getBySystemName("MT+123"), "system name correct ");
    }

    @Test
    public void testBadCbusTurnoutAddresses() {

        assertDoesNotThrow(() -> {
            Turnout t1 = l.provideTurnout("MT+N15E6");
            assertNotNull(t1);
        },"Should NOT have thrown an exception");


        IllegalArgumentException e = assertThrows( IllegalArgumentException.class,
            () -> l.provideTurnout("X;+N15E6"),
            "X No hw name Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Wrong number of events in address: X;+N15E6");

        e = assertThrows( IllegalArgumentException.class, () ->
            l.provideTurnout("MTX;+N15E6"), "X hw name Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Wrong number of events in address: X;+N15E6");

        e = assertThrows( IllegalArgumentException.class, () ->
            l.provideTurnout("MTXA;+N15E6"), "A Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Wrong number of events in address: XA;+N15E6");

        e = assertThrows( IllegalArgumentException.class, () ->
            l.provideTurnout("MTXABC;+N15E6"), "AC Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Wrong number of events in address: XABC;+N15E6");

        e = assertThrows( IllegalArgumentException.class, () ->
            l.provideTurnout("MTXABCDE;+N15E6"), "ABCDE Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Wrong number of events in address: XABCDE;+N15E6");

        e = assertThrows( IllegalArgumentException.class, () ->
            l.provideTurnout("MTXABCDEF0;+N15E6"), "ABCDEF0 Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Wrong number of events in address: XABCDEF0;+N15E6");

        e = assertThrows( IllegalArgumentException.class, () ->
            l.provideTurnout("MTXABCDEF"), "Single hex Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Turnout: can't make 2nd event from address XABCDEF");

        e = assertThrows( IllegalArgumentException.class, () ->
            l.provideTurnout("MT;XABCDEF"), "Single hex ; Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Address Too Short? : ");

        e = assertThrows( IllegalArgumentException.class, () ->
            l.provideTurnout("MTXABCDEF;"),"Single hex ; Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Should not end with ; XABCDEF;");

        e = assertThrows( IllegalArgumentException.class, () ->
            l.provideTurnout("MT;"), "; no arg Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Should not end with ; ;");

        e = assertThrows( IllegalArgumentException.class, () ->
            l.provideTurnout("MT;+N15E6"), "MS Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Address Too Short? : ");
    }

    @Test
    public void testBadCbusTurnoutAddressesPt2() {

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () ->
            l.provideTurnout(";+N15E62"), "; Should have thrown an exception");
        assertNotNull(ex);
            JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Address Too Short? : ");
        assertEquals("Address Too Short? : ", ex.getMessage());

        ex = assertThrows( IllegalArgumentException.class, () ->
            l.provideTurnout("T+N156E77;+N123E456"), "Missing M Should have thrown an exception");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("Invalid system name for Turnout: System name \"T+N156E77;+N123E456\" contains invalid character \"T\".");
        assertEquals("System name \"T+N156E77;+N123E456\" contains invalid character \"T\".",
            ex.getMessage());
    }

    @Test
    public void testBadCbusTurnoutAddressesPt3() {
        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () ->
            l.provideTurnout("M+N156E77;+N15E60"), "M Should have thrown an exception");
        assertNotNull(ex);
            JUnitAppender.assertErrorMessage(
                "Invalid system name for Turnout: System name \"M+N156E77;+N15E60\" contains invalid character \"M\".");
        assertEquals("System name \"M+N156E77;+N15E60\" contains invalid character \"M\".",
            ex.getMessage());
    }

    @Test
    public void testBadCbusTurnoutAddressesPt4() {
        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () ->
            l.provideTurnout("MT++N156E78"), "++ Should have thrown an exception");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage(
            "Invalid system name for Turnout: System name \"++N156E78\" contains invalid character \"++\".");
    }

    @Test
    public void testBadCbusTurnoutAddressesPt5() {
        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () ->
            l.provideTurnout("MT--N156E78"), "-- Should have thrown an exception");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage(
            "Invalid system name for Turnout: System name \"--N156E78\" contains invalid character \"--\".");

        ex = assertThrows( IllegalArgumentException.class, () ->
            l.provideTurnout("MTN156E+80"), "E+ Should have thrown an exception");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage(
            "Invalid system name for Turnout: Wrong number of events in address: N156E+80");

        ex = assertThrows( IllegalArgumentException.class, () ->
            l.provideTurnout("MTN156+E77"), "+E Should have thrown an exception");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage(
            "Invalid system name for Turnout: Wrong number of events in address: N156+E77");

        ex = assertThrows( IllegalArgumentException.class, () ->
            l.provideTurnout("MTXLKJK;XLKJK"), "LKJK Should have thrown an exception");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage(
            "Invalid system name for Turnout: System name \"XLKJK;XLKJK\" contains invalid character \"J\".");

        ex = assertThrows( IllegalArgumentException.class, () ->
            l.provideTurnout("MT+7;-5;+11"), "3 split Should have thrown an exception");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage(
            "Invalid system name for Turnout: Unable to convert Address: +7;-5;+11");
    }

    @Test
    public void testLowercaseSystemName() {
        String name = "mt+n1e77;-n1e45";
        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () ->
            l.provideTurnout(name), "Expected exception not thrown");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("Invalid system name for Turnout: Wrong number of events in address: mt+n1e77;-n1e45");

        Turnout t = l.provideTurnout(name.toUpperCase());
        assertNotNull(t);
        assertNotEquals(t, l.getBySystemName(name));
        assertNull(l.getBySystemName(name));
    }

    @Test
    @Override
    public void testGetEntryToolTip() {
        super.testGetEntryToolTip();
        String x = l.getEntryToolTip();
        assertTrue(x.contains("<html>"));

        assertTrue(l.allowMultipleAdditions("M77"));
    }

    @Test
    public void testvalidSystemNameFormat() {

        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MT+123"), "MT+123");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MT+N123E123"), "MT+N123E123");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MT+123;456"), "MT+123;456");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MT1"), "MT1");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MT1;2"), "MT1;2");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MT65535"), "MT65535");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MT-65535"), "MT-65535");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MT100001"), "MT100001");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MT-100001"), "MT-100001");

        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MT+1;+0"), "MT+1;+0");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MT+1;-0"), "MT+1;-0");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MT+0;+17"), "MT+0;+17");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MT+0;-17"), "MT+0;-17");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MT+0"), "MT+0");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MT-0"), "MT-0");

        assertEquals( NameValidity.INVALID, l.validSystemNameFormat("M"), "M");
        assertEquals( NameValidity.INVALID, l.validSystemNameFormat("MT"), "MT");
        assertEquals( NameValidity.INVALID, l.validSystemNameFormat("MT-65536"), "MT-65536");
        assertEquals( NameValidity.INVALID, l.validSystemNameFormat("MT65536"), "MT65536");
        assertEquals( NameValidity.INVALID, l.validSystemNameFormat("MT7;0"), "MT7;0");
        assertEquals( NameValidity.INVALID, l.validSystemNameFormat("MT0;7"), "MT0;7");
    }


    @Test
    public void testSimpleNext() throws JmriException {
        Turnout t =  l.provideTurnout("MT+17");
        String next = l.getNextValidSystemName(t);
        assertEquals("MT+18", next);

        t =  l.provideTurnout("MT+N45E22");
        next = l.getNextValidSystemName(t);
        assertEquals("MT+N45E23", next);

    }

    @Test
    public void testDoubleNext() throws JmriException {
        Turnout t =  l.provideTurnout("MT+18;-21");
        String next = l.getNextValidSystemName(t);
        assertEquals( "MT+19;-22", next);
    }

    @Test
    public void testcreateSystemName() throws JmriException {

        assertEquals( "MT+10", l.createSystemName("10", "M"), "MT+10");
        assertEquals( "MT+N34E610", l.createSystemName("+N34E610", "M"), "MT+N34E610");
        assertEquals( "MT+5;-6", l.createSystemName("5;6", "M"), "MT5;6");

        assertEquals( "M2T+10", l.createSystemName("+10", "M2"), "M2T+10");

        assertEquals( "ZZZZZZZZZT+10", l.createSystemName("+10", "ZZZZZZZZZ"), "ZZZZZZZZZ2T+10");

    }

    @Test
    public void testProvideswhenNotNull() {
        Turnout t = l.provideTurnout("+4");
        Turnout ta = l.provideTurnout("+4");
        assertTrue(t == ta);
    }

    @Test
    @Override
    @Disabled("Requires further development ?")
    public void testAutoSystemNames() {
    }

    @Test
    public void testNotListeningToTcis() {
        assertNotNull(tcis);
        assertEquals(0,tcis.numListeners(), "Turnout mgr not listening to tcis");
    }

    @Test
    @Override
    public void testSetAndGetOutputInterval() {
        assertEquals( 100, l.getOutputInterval(), "default outputInterval");
        l.getMemo().setOutputInterval(21);
        assertEquals( 21, l.getMemo().getOutputInterval(), "new outputInterval in memo"); // set & get in memo
        assertEquals( 21, l.getOutputInterval(), "new outputInterval via manager"); // get via turnoutManager
        l.setOutputInterval(50);
        assertEquals( 50, l.getOutputInterval(), "new outputInterval from manager"); // interval stored in AbstractTurnoutManager
        assertEquals( 50, l.getMemo().getOutputInterval(), "new outputInterval from memo"); // get from memo
    }

    private TrafficControllerScaffold tcis = null;
    private CanSystemConnectionMemo memo = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        l = new CbusTurnoutManager(memo);
    }

    @AfterEach
    public void tearDown() {
        assertNotNull(tcis);
        assertNotNull(memo);
        if ( l!= null ) {
            l.dispose();
            l = null;
        }
        tcis.terminateThreads();
        tcis = null;
        memo.dispose();
        JUnitUtil.tearDown();

    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusTurnoutManagerTest.class);

}
