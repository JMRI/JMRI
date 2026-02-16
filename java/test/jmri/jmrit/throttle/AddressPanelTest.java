package jmri.jmrit.throttle;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of AddressPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class AddressPanelTest {

    @Test
    public void testCtor() {
        AddressPanel panel = new AddressPanel(null);
        Assertions.assertNotNull( panel, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
