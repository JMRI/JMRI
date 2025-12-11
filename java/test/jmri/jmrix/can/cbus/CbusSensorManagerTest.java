package jmri.jmrix.can.cbus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.Manager.NameValidity;
import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.can.cbus.CbusSensorManager class.
 *
 * @author Bob Jacobsen Copyright 2008
 * @author Paul Bender Copyright (C) 2016
 */
public class CbusSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tcis;

    @Override
    public String getSystemName(int i) {
        return "MSX0A;+N15E" + i;
    }

    @Override
    protected String getASystemNameWithNoPrefix() {
        return "+6";
    }

    @Test
    @Override
    public void testCreate() {
        assertNotNull( l.provideSensor(memo.getSystemPrefix() + "SX0A;+N15E6"), "createsSensor");
    }

    @Test
    @Override
    public void testDefaultSystemName() {
        // create
        Sensor t = l.provideSensor("MSX0A;+N15E" + getNumToTest1());
        // check
        assertNotNull( t, "real object returned ");
        assertEquals( t, l.getBySystemName(getSystemName(getNumToTest1())), "system name correct ");
    }

    @Test
    @Override
    public void testProvideName() {
        // create
        Sensor t = l.provide("" + getSystemName(getNumToTest1()));
        // check
        assertNotNull( t, "real object returned ");
        assertEquals( t, l.getBySystemName(getSystemName(getNumToTest1())), "system name correct");
    }

    @Test
    public void testLowercaseSystemName() {
        String name1 = "ms+n1e77;-n1e45";
        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class,
            () -> l.provideSensor(name1), "Expected exception not thrown");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: Wrong number of events in address: ms+n1e77;-n1e45");

        String name2 = "msxabcdef;xfedcba";
        ex = assertThrows( IllegalArgumentException.class,
            () -> l.provideSensor(name2),"Expected exception not thrown");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: Wrong number of events in address: msxabcdef;xfedcba");
    }

    @Override
    @Test
    public void testMoveUserName() {
        Sensor t1 = l.provideSensor("MSX0A;+N15E" + getNumToTest1());
        Sensor t2 = l.provideSensor("MSX0A;+N15E" + getNumToTest2());
        t1.setUserName("UserName");
        assertEquals(t1, l.getByUserName("UserName"));

        t2.setUserName("UserName");
        assertEquals(t2, l.getByUserName("UserName"));

        assertNull(t1.getUserName());
    }

    @Test
    public void testBadCbusSensorAddresses() {

        Sensor t1 = l.provideSensor("MS+N15E6");
        assertNotNull(t1);

        IllegalArgumentException e = assertThrows( IllegalArgumentException.class,
            () -> l.provideSensor("MSX;+N15E6"),
            "X Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: Wrong number of events in address: X;+N15E6");

        e = assertThrows( IllegalArgumentException.class,
            () -> l.provideSensor("MSXA;+N15E6"),
            "A Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: Wrong number of events in address: XA;+N15E6");

        e = assertThrows( IllegalArgumentException.class,
            () -> l.provideSensor("MSXABC;+N15E6"),
            "AC Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: Wrong number of events in address: XABC;+N15E6");

        e = assertThrows( IllegalArgumentException.class,
            () -> l.provideSensor("MSXABCDE;+N15E6"),
            "ABCDE Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: Wrong number of events in address: XABCDE;+N15E6");

        e = assertThrows( IllegalArgumentException.class,
            () -> l.provideSensor("MSXABCDEF0;+N15E6"),
            "ABCDEF0 Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: Wrong number of events in address: XABCDEF0;+N15E6");

        e = assertThrows( IllegalArgumentException.class,
            () -> l.provideSensor("MSXABCDEF"),
            "Single hex Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: can't make 2nd event from address XABCDEF");

        e = assertThrows( IllegalArgumentException.class,
            () -> l.provideSensor("MS;XABCDEF"),
            "Single hex ; Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: Address Too Short? : ");

        e = assertThrows( IllegalArgumentException.class,
            () -> l.provideSensor("MSXABCDEF;"),
            "Single hex ; Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: Should not end with ; XABCDEF;");

        e = assertThrows( IllegalArgumentException.class,
            () -> l.provideSensor("MS;"),
            "; no arg Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: Should not end with ; ;");

        e = assertThrows( IllegalArgumentException.class,
            () -> l.provideSensor("MS;+N15E6"),
            "MS Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: Address Too Short? : ");

        e = assertThrows( IllegalArgumentException.class,
            () -> l.provideSensor(";+N15E6"),
            "; Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: Address Too Short? : ");

        e = assertThrows( IllegalArgumentException.class,
            () -> l.provideSensor("S+N156E77;+N15E6"),
            "S Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: System name \"S+N156E77;+N15E6\" contains invalid character \"S\".");

        e = assertThrows( IllegalArgumentException.class,
            () -> l.provideSensor("M+N156E77;+N15E6"),
            "M Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: System name \"M+N156E77;+N15E6\" contains invalid character \"M\".");

        e = assertThrows( IllegalArgumentException.class,
            () -> l.provideSensor("MS++N156E77"),
            "++ Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: System name \"++N156E77\" contains invalid character \"++\".");

        e = assertThrows( IllegalArgumentException.class,
            () -> l.provideSensor("MS--N156E77"),
            "-- Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: System name \"--N156E77\" contains invalid character \"--\".");

        e = assertThrows( IllegalArgumentException.class,
            () -> l.provideSensor("MSN156E+77"),
            "E+ Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: Wrong number of events in address: N156E+77");

        e = assertThrows( IllegalArgumentException.class,
            () -> l.provideSensor("MSN156+E77"),
            "E+ Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: Wrong number of events in address: N156+E77");

        e = assertThrows( IllegalArgumentException.class,
            () -> l.provideSensor("MSXLKJK;XLKJK"),
            "LKJK Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: System name \"XLKJK;XLKJK\" contains invalid character \"J\".");

        e = assertThrows( IllegalArgumentException.class,
            () -> l.provideSensor("MS+7;-5;+11"),
            "3 split Should have thrown an exception");
        assertNotNull(e);
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: Unable to convert Address: +7;-5;+11");

    }

    @Test
    public void testGoodCbusSensorAddresses() {

        Sensor t = l.provideSensor("MS+7");
        assertNotNull( t, "exists");

        t = l.provideSensor("MS+1;-1");
        assertNotNull( t, "exists");

        t = l.provideSensor("MS+654e321");
        assertNotNull( t, "exists");

        t = l.provideSensor("MS-654e321;+123e456");
        assertNotNull( t, "exists");

        t = l.provideSensor("MS+n654e321");
        assertNotNull( t, "exists");

        t = l.provideSensor("MS+N299E17;-N123E456");
        assertNotNull( t, "exists");

        t = l.provideSensor("MSX04;X05");
        assertNotNull( t, "exists");

        t = l.provideSensor("MSX2301;X30FF");
        assertNotNull( t, "exists");

        t = l.provideSensor("MSX410001;X56FFFF");
        assertNotNull( t, "exists");

        t = l.provideSensor("MSX6000010001;X72FFFFFF");
        assertNotNull( t, "exists");

        t = l.provideSensor("MSX9000010001;X91FFFFFFFF");
        assertNotNull( t, "exists");

        t = l.provideSensor("MSXB00D60010001;XB1FFFAAFFFFF");
        assertNotNull( t, "exists");

        t = l.provideSensor("MSXD00D0060010001;XD1FFFAAAFFFFFE");
        assertNotNull( t, "exists");

        t = l.provideSensor("MSXF00D0A0600100601;XF1FFFFAAFAFFFFFE");
        assertNotNull( t, "exists");
    }

    @Test
    public void testQueryAll() {
        tcis.outbound.clear();
        memo.setOutputInterval(2); // reduce output interval for tests

        Sensor t1 = l.provideSensor("MS+N123E456");
        Sensor t2 = l.provideSensor("MS-N9875E45670");

        assertTrue(tcis.outbound.isEmpty());

        l.updateAll();
        JUnitUtil.waitFor(() -> ( 2 == tcis.outbound.size()),"2 messages sent");
        assertEquals(2, tcis.outbound.size());

        Sensor t3 = l.provideSensor("MSX0A;X5E6DEEF4");
        tcis.outbound.clear();
        l.updateAll();
        JUnitUtil.waitFor(() -> ( 3 == tcis.outbound.size()),"3 messages sent");
        assertEquals(3, tcis.outbound.size());
        assertNotNull( t1, "exists");
        assertNotNull( t2, "exists");
        assertNotNull( t3, "exists");
    }

    @Override
    @Test
    public void testGetEntryToolTip() {
        super.testGetEntryToolTip();
        String x = l.getEntryToolTip();
        assertTrue(x.contains("<html>"));

        assertTrue(l.allowMultipleAdditions("M77"));
    }

    @Test
    public void testvalidSystemNameFormat() {

        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS+123"), "MS+123");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS+N123E123"), "MS+N123E123");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS+123;456"), "MS+123;456");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS1"), "MS1");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS1;2"), "MS1;2");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS65535"), "MS65535");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS-65535"), "MS-65535");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS100001"), "MS100001");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS-100001"), "MS-100001");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS+N65535e65535"), "MS+N65535e65535");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS+N65535e65535;-N65535e65535"), "MS+N65535e65535;-N65535e65535");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS+N1E2;-N3E4"), "MS+N1E2;-N3E4");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS-N1E2;+N3E4"), "MS-N1E2;+N3E4");

        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS+1;+0"), "MS+1;+0");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS+1;-0"), "MS+1;-0");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS+0;+17"), "MS+0;+17");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS+0;-17"), "MS+0;-17");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS+0"), "MS+0");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS-0"), "MS-0");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS+N17E0"), "MS+N17E0");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS+N17E00"), "MS+N17E00");


        assertEquals( NameValidity.INVALID, l.validSystemNameFormat("M"), "M");
        assertEquals( NameValidity.INVALID, l.validSystemNameFormat("MS"), "MS");
        assertEquals( NameValidity.INVALID, l.validSystemNameFormat("MS-65536"), "MS-65536");
        assertEquals( NameValidity.INVALID, l.validSystemNameFormat("MS65536"), "MS65536");

        assertEquals( NameValidity.INVALID, l.validSystemNameFormat("MS7;0"), "MS7;0");
        assertEquals( NameValidity.INVALID, l.validSystemNameFormat("MS0;7"), "MS0;7");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS+N0E17"), "MS+N0E17");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS+N00E17"), "MS+N00E17");
        assertEquals( NameValidity.VALID, l.validSystemNameFormat("MS+0E17"), "MS+0E17");
        assertEquals( NameValidity.INVALID, l.validSystemNameFormat("MS0E17"), "MS0E17");
        assertEquals( NameValidity.INVALID, l.validSystemNameFormat("MS+N65535e65536"), "MS+N65535e65536");
        assertEquals( NameValidity.INVALID, l.validSystemNameFormat("MS+N65536e65535"), "MS+N65536e65535");
    }

    @Test
    public void testSimpleNext() throws JmriException {
        Sensor t =  l.provideSensor("MS+17");
        String next = l.getNextValidSystemName(t);
        assertEquals("MS+18", next);

        t =  l.provideSensor("MS+N45E22");
        next = l.getNextValidSystemName(t);
        assertEquals("MS+N45E23", next);

    }

    @Test
    public void testDoubleNext() throws JmriException {
        Sensor t =  l.provideSensor("MS+18;-21");
        String next = l.getNextValidSystemName(t);
        assertEquals("MS+19;-22", next);
    }

    @Test
    public void testcreateSystemName() throws JmriException {

        assertEquals( "MS+10", l.createSystemName("+10", "M"), "MS+10");
        assertEquals( "MS+N34E610", l.createSystemName("+N34E610", "M"), "MS+N34E610");
        assertEquals( "MS-N34E610", l.createSystemName("-N34E610", "M"), "MS-N34E610");
        assertEquals( "MS+N34E610;-N987E654", l.createSystemName("+N34E610;-N987E654", "M"), "MS+N34E610;-N987E654");

        JmriException ex = assertThrows( JmriException.class,
            () -> l.createSystemName("S", "M"));
        assertEquals("System name \"S\" contains invalid character \"S\".", ex.getMessage());

        ex = assertThrows(JmriException.class,
            ()-> l.createSystemName("+10", "M2"));
        assertNotNull(ex);
        String msg = ex.getMessage();
        assertNotNull(msg);
        assertTrue( msg.contains("System name must start with \"MS\""), "Exception message relevant");

        ex = assertThrows(JmriException.class,
            ()-> l.createSystemName("+10", "ZZZZZZZZZ"));
        assertNotNull(ex);
        msg = ex.getMessage();
        assertNotNull(msg);
        assertTrue( msg.contains("System name must start with \"MS\""), "Exception message relevant");

    }

    @Test
    @Override
    public void testAutoSystemNames() {
        assertEquals( 0, tcis.numListeners(), "No auto system names");
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        l = new CbusSensorManager(memo);
    }

    @AfterEach
    public void tearDown() {
        l.dispose();
        memo.dispose();
        tcis.terminateThreads();
        JUnitUtil.tearDown();

    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusSensorManagerTest.class);
}
