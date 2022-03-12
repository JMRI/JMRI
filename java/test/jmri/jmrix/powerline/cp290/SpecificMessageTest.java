package jmri.jmrix.powerline.cp290;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for SpecificMessage class.
 *
 * @author Paul Bender Copyright (C) 2016
 *
 */
public class SpecificMessageTest extends jmri.jmrix.AbstractMessageTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        m = new SpecificMessage(5);
    }

    @AfterEach
    public void tearDown() {
        m = null;
        JUnitUtil.tearDown();
    }

}
