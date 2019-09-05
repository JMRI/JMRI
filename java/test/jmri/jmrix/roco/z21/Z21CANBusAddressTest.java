package jmri.jmrix.roco.z21;

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
        softly.assertThat(Z21CanBusAddress.getBitFromSystemName("ZS1234:1","Z")).isEqualTo(1);
        softly.assertThat(Z21CanBusAddress.getBitFromSystemName("ZrABCD:5","Z")).isEqualTo(5);
        softly.assertThat(Z21CanBusAddress.getBitFromSystemName("ZsA1B2:6","Z")).isEqualTo(6);
        softly.assertThat(Z21CanBusAddress.getBitFromSystemName("ZRabcd:6","Z")).isEqualTo(6);
        softly.assertThat(Z21CanBusAddress.getBitFromSystemName("ZRabcd6","Z")).isEqualTo(-1);
        softly.assertAll();
        JUnitAppender.assertWarnMessage("system name ZRabcd6 is in the wrong format.  Should be mm:pp.");
    }

    @Test
    public void testGetEncoderAddressString() {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(Z21CanBusAddress.getEncoderAddressString("ZS1234:1","Z")).isEqualTo("1234");
        softly.assertThat(Z21CanBusAddress.getEncoderAddressString("ZrABCD:5","Z")).isEqualTo("ABCD");
        softly.assertThat(Z21CanBusAddress.getEncoderAddressString("ZsA1B2:6","Z")).isEqualTo("A1B2");
        softly.assertThat(Z21CanBusAddress.getEncoderAddressString("ZRabcd:6","Z")).isEqualTo("abcd");
        softly.assertAll();
        Throwable thrown = catchThrowable(()->{Z21CanBusAddress.getEncoderAddressString("CRabcd:6","Z");});
        assertThat(thrown).isInstanceOf(NamedBean.BadSystemNameException.class)
                .hasMessage(Bundle.getMessage(Locale.ENGLISH,"InvalidSystemNameInvalidPrefix","Z"))
                .hasNoCause();
        JUnitAppender.assertErrorMessage("invalid character in header field of Z21 Can Bus system name: CRabcd:6");
        thrown = catchThrowable(()->{Z21CanBusAddress.getEncoderAddressString("ZRabcd6","Z");});
        assertThat(thrown).isInstanceOf(StringIndexOutOfBoundsException.class);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
