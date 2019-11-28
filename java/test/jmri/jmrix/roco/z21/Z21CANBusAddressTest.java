package jmri.jmrix.roco.z21;

import jmri.Manager;
import jmri.NamedBean;
import jmri.util.JUnitAppender;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2019	
 */
public class Z21CANBusAddressTest {

    @Test
    public void testGetBitFromAddress() {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(Z21CanBusAddress.getBitFromSystemName("ZS1234:1", "Z")).isEqualTo(1);
        softly.assertThat(Z21CanBusAddress.getBitFromSystemName("ZrABCD:5", "Z")).isEqualTo(5);
        softly.assertThat(Z21CanBusAddress.getBitFromSystemName("ZsA1B2:6", "Z")).isEqualTo(6);
        softly.assertThat(Z21CanBusAddress.getBitFromSystemName("ZRabcd:6", "Z")).isEqualTo(6);
        softly.assertThat(Z21CanBusAddress.getBitFromSystemName("ZRabcd6", "Z")).isEqualTo(-1);
        softly.assertAll();
        JUnitAppender.assertWarnMessage("system name ZRabcd6 is in the wrong format.  Should be mm:pp.");
    }

    @Test
    public void testGetEncoderAddressString() {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(Z21CanBusAddress.getEncoderAddressString("ZS1234:1", "Z")).isEqualTo("1234");
        softly.assertThat(Z21CanBusAddress.getEncoderAddressString("ZrABCD:5", "Z")).isEqualTo("ABCD");
        softly.assertThat(Z21CanBusAddress.getEncoderAddressString("ZsA1B2:6", "Z")).isEqualTo("A1B2");
        softly.assertThat(Z21CanBusAddress.getEncoderAddressString("ZRabcd:6", "Z")).isEqualTo("abcd");
        softly.assertAll();
        Throwable thrown = catchThrowable(() -> {
            Z21CanBusAddress.getEncoderAddressString("CRabcd:6", "Z");
        });
        assertThat(thrown).isInstanceOf(NamedBean.BadSystemNameException.class).hasMessage(Bundle.getMessage(Locale.ENGLISH, "InvalidSystemNameInvalidPrefix", "Z")).hasNoCause();
        JUnitAppender.assertErrorMessage("invalid character in header field of Z21 Can Bus system name: CRabcd:6");
        thrown = catchThrowable(() -> {
            Z21CanBusAddress.getEncoderAddressString("ZRabcd6", "Z");
        });
        assertThat(thrown).isInstanceOf(StringIndexOutOfBoundsException.class);
    }

    @Test
    public void testValidateSystemNameFormat() {
        Z21TrafficController znis = new Z21InterfaceScaffold();
        Z21SystemConnectionMemo memo = new Z21SystemConnectionMemo();
        memo.setTrafficController(znis);
        memo.setRocoZ21CommandStation(new RocoZ21CommandStation());
        Z21SensorManager sm = new Z21SensorManager(memo);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(Z21CanBusAddress.validateSystemNameFormat("ZS1234:1", sm, Locale.ENGLISH)).isEqualTo("ZS1234:1");
        softly.assertThat(Z21CanBusAddress.validateSystemNameFormat("ZSABCD:5", sm, Locale.ENGLISH)).isEqualTo("ZSABCD:5");
        softly.assertThat(Z21CanBusAddress.validateSystemNameFormat("ZSa1b2:3", sm, Locale.ENGLISH)).isEqualTo("ZSa1b2:3");
        softly.assertThat(Z21CanBusAddress.validateSystemNameFormat("ZSa235:0", sm, Locale.ENGLISH)).isEqualTo("ZSa235:0");
        softly.assertAll();

        Throwable thrown = catchThrowable(() -> {
           Z21CanBusAddress.validateSystemNameFormat("ZSabcd:b", sm, Locale.ENGLISH);
        });
        assertThat(thrown).isInstanceOf(NamedBean.BadSystemNameException.class);
        JUnitAppender.suppressWarnMessage("invalid character in number field of system name: ZSabcd:b");

        thrown = catchThrowable(() -> {
            Z21CanBusAddress.validateSystemNameFormat("ZSabcd1", sm, Locale.ENGLISH);
        });
        assertThat(thrown).isInstanceOf(NamedBean.BadSystemNameException.class);
        JUnitAppender.suppressWarnMessage("system name ZSabcd1 is in the wrong format.  Should be mm:pp.");
        sm.dispose();
        znis.terminateThreads();
    }

    @Test
    public void testValidSystemNameFormat() {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(Z21CanBusAddress.validSystemNameFormat("ZS1234:1",'S',"Z")).isEqualTo(Manager.NameValidity.VALID);
        softly.assertThat(Z21CanBusAddress.validSystemNameFormat("ZSABCD:5",'S',"Z")).isEqualTo(Manager.NameValidity.VALID);
        softly.assertThat(Z21CanBusAddress.validSystemNameFormat("ZSa1b2:3",'S',"Z")).isEqualTo(Manager.NameValidity.VALID);
        softly.assertThat(Z21CanBusAddress.validSystemNameFormat("ZSa235:0",'S',"Z")).isEqualTo(Manager.NameValidity.VALID);

        softly.assertThat(Z21CanBusAddress.validSystemNameFormat("ZSabcd:b",'S',"Z")).isEqualTo(Manager.NameValidity.INVALID);
        JUnitAppender.suppressWarnMessage("invalid character in number field of system name: ZSabcd:b");
        softly.assertThat(Z21CanBusAddress.validSystemNameFormat("ZSabcd1",'S',"Z")).isEqualTo(Manager.NameValidity.INVALID);
        JUnitAppender.suppressWarnMessage("system name ZSabcd1 is in the wrong format.  Should be mm:pp.");

        softly.assertAll();
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
