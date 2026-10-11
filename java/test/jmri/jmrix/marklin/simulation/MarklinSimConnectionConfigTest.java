package jmri.jmrix.marklin.simulation;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the MarklinSimConnectionConfig class.
 * @author Steve Young Copyright (C) 2024
 */
public class MarklinSimConnectionConfigTest extends jmri.jmrix.AbstractSerialConnectionConfigTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        cc = new MarklinSimConnectionConfig();
    }

    @AfterEach
    @Override
    public void tearDown() {
        cc = null;
        JUnitUtil.tearDown();
    }

}
