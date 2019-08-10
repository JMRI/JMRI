package jmri.jmrix.anyma;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.ToDo;
import org.junit.*;

/**
 * Tests for AnymaDMX_ConnectionConfig class.
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class AnymaDMX_ConnectionConfigTest extends jmri.jmrix.AbstractUsbConnectionConfigTestBase {

    @Test
    @Ignore("superclass method fails because Manufacturer not set in Connection Config class")
    @ToDo("Change AnymaDMX_ConnectionConfig so that  getManufacturer and setManufacturer methods are implemented.")
    @Override
    public void testGetAndSetManufacturer(){
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        cc = new AnymaDMX_ConnectionConfig();
    }

    @After
    @Override
    public void tearDown() {
        cc = null;
        JUnitUtil.tearDown();
    }
}
