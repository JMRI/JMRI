package jmri.jmrix.roco.z21;

import jmri.Manager;
import jmri.NamedBean;
import jmri.util.JUnitAppender;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.assertj.core.api.Assertions.assertThat;
/**
 *
 * @author Paul Bender Copyright (C) 2019	
 */
public class Z21RMBusAddressTest {

    @Test
    public void testGetBitFromAddress() {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(Z21RMBusAddress.getBitFromSystemName("ZS150","Z")).isEqualTo(150);
        softly.assertThat(Z21RMBusAddress.getBitFromSystemName("ZS999","Z")).isEqualTo(-1);
        softly.assertAll();
        JUnitAppender.assertWarnMessage("Z21 RM Bus hardware address out of range in system name ZS999");
    }
    @Test
    public void testValidateSystemNameFormat() {
        Z21TrafficController znis = new Z21InterfaceScaffold();
        Z21SystemConnectionMemo memo = new Z21SystemConnectionMemo();
        memo.setTrafficController(znis);
        memo.setRocoZ21CommandStation(new RocoZ21CommandStation());
        Z21SensorManager sm = new Z21SensorManager(memo);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(Z21RMBusAddress.validateSystemNameFormat("ZS1", sm, Locale.ENGLISH)).isEqualTo("ZS1");
        softly.assertThat(Z21RMBusAddress.validateSystemNameFormat("ZS75", sm, Locale.ENGLISH)).isEqualTo("ZS75");
        softly.assertThat(Z21RMBusAddress.validateSystemNameFormat("ZS128", sm, Locale.ENGLISH)).isEqualTo("ZS128");
        softly.assertAll();

        Throwable thrown = catchThrowable(() -> {
            Z21RMBusAddress.validateSystemNameFormat("ZS0a:b", sm, Locale.ENGLISH);
        });
        assertThat(thrown).isInstanceOf(NamedBean.BadSystemNameException.class);
        JUnitAppender.suppressWarnMessage("invalid character in number field of system name: ZS0b:a");

        thrown = catchThrowable(() -> {
            Z21RMBusAddress.validateSystemNameFormat("ZS999", sm, Locale.ENGLISH);
        });
        assertThat(thrown).isInstanceOf(NamedBean.BadSystemNameException.class);
        JUnitAppender.suppressWarnMessage("Z21 RM Bus hardware address out of range in system name ZS999");
        sm.dispose();
        znis.terminateThreads();
    }

    @Test
    public void testValidSystemNameFormat() {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(Z21RMBusAddress.validSystemNameFormat("ZS1",'S',"Z")).isEqualTo(Manager.NameValidity.VALID);
        softly.assertThat(Z21RMBusAddress.validSystemNameFormat("ZS75",'S',"Z")).isEqualTo(Manager.NameValidity.VALID);
        softly.assertThat(Z21RMBusAddress.validSystemNameFormat("ZS128",'S',"Z")).isEqualTo(Manager.NameValidity.VALID);
        softly.assertThat(Z21RMBusAddress.validSystemNameFormat("ZS0b:a",'S',"Z")).isEqualTo(Manager.NameValidity.INVALID);
        softly.assertThat(Z21RMBusAddress.validSystemNameFormat("ZS999",'S',"Z")).isEqualTo(Manager.NameValidity.INVALID);
        softly.assertAll();
        JUnitAppender.assertWarnMessage("Z21 RM Bus hardware address out of range in system name ZS999");
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        jmri.util.JUnitUtil.tearDown();

    }

}
