package jmri.jmrix.anyma;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for AnymaDMX_ConnectionConfig class.
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class AnymaDMX_ConnectionConfigTest extends jmri.jmrix.AbstractUsbConnectionConfigTestBase {

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        cc = new AnymaDMX_ConnectionConfig();
    }

    @After
    public void tearDown() {
        cc=null;
        JUnitUtil.tearDown();
    }
}
