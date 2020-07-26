package jmri.jmrix.anyma;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.ToDo;

import org.junit.jupiter.api.*;

/**
 * Tests for AnymaDMX_ConnectionConfig class.
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class AnymaDMX_ConnectionConfigTest extends jmri.jmrix.AbstractUsbConnectionConfigTestBase {

    @Test
    @Disabled("superclass method fails because Manufacturer not set in Connection Config class")
    @ToDo("Change AnymaDMX_ConnectionConfig so that  getManufacturer and setManufacturer methods are implemented.")
    @Override
    public void testGetAndSetManufacturer(){
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        cc = new AnymaDMX_ConnectionConfig();
    }

    @AfterEach
    @Override
    public void tearDown() {
        cc = null;
        JUnitUtil.tearDown();
    }
}
