package jmri.jmrix.roco.z21;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;

import jmri.Manager;
import jmri.NamedBean;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2019
 */
public class Z21RMBusAddressTest {

    @Test
    public void testGetBitFromAddress() {
        assertEquals( 150, Z21RMBusAddress.getBitFromSystemName("ZS150","Z"));
        assertEquals( -1, Z21RMBusAddress.getBitFromSystemName("ZS999","Z"));
        JUnitAppender.assertWarnMessage("Z21 RM Bus hardware address out of range in system name ZS999");
    }

    @Test
    public void testValidateSystemNameFormat() {
        Z21TrafficController znis = new Z21InterfaceScaffold();
        Z21SystemConnectionMemo memo = new Z21SystemConnectionMemo();
        memo.setTrafficController(znis);
        memo.setRocoZ21CommandStation(new RocoZ21CommandStation());
        Z21SensorManager sm = new Z21SensorManager(memo);

        assertEquals( "ZS1", Z21RMBusAddress.validateSystemNameFormat("ZS1", sm, Locale.ENGLISH));
        assertEquals( "ZS75", Z21RMBusAddress.validateSystemNameFormat("ZS75", sm, Locale.ENGLISH));
        assertEquals( "ZS128", Z21RMBusAddress.validateSystemNameFormat("ZS128", sm, Locale.ENGLISH));


        Throwable thrown = assertThrows(NamedBean.BadSystemNameException.class, () -> {
            Z21RMBusAddress.validateSystemNameFormat("ZS0a:b", sm, Locale.ENGLISH);
        });
        assertNotNull(thrown);
        JUnitAppender.suppressWarnMessage("invalid character in number field of system name: ZS0b:a");

        thrown = assertThrows( NamedBean.BadSystemNameException.class, () -> {
            Z21RMBusAddress.validateSystemNameFormat("ZS999", sm, Locale.ENGLISH);
        });
        assertNotNull(thrown);
        JUnitAppender.suppressWarnMessage("Z21 RM Bus hardware address out of range in system name ZS999");
        sm.dispose();
        znis.terminateThreads();
    }

    @Test
    public void testValidSystemNameFormat() {

        assertEquals(Manager.NameValidity.VALID, Z21RMBusAddress.validSystemNameFormat("ZS1",'S',"Z"));
        assertEquals(Manager.NameValidity.VALID, Z21RMBusAddress.validSystemNameFormat("ZS75",'S',"Z"));
        assertEquals(Manager.NameValidity.VALID, Z21RMBusAddress.validSystemNameFormat("ZS128",'S',"Z"));

        assertEquals(Manager.NameValidity.INVALID, Z21RMBusAddress.validSystemNameFormat("ZS0b:a",'S',"Z"));
        JUnitAppender.assertWarnMessage("invalid character in number field of system name: ZS0b:a");
        assertEquals(Manager.NameValidity.INVALID, Z21RMBusAddress.validSystemNameFormat("ZS999",'S',"Z"));
        JUnitAppender.assertWarnMessage("Z21 RM Bus hardware address out of range in system name ZS999");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
