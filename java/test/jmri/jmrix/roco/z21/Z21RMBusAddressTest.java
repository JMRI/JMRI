package jmri.jmrix.roco.z21;

import jmri.util.JUnitAppender;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

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
