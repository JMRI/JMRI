package jmri.jmrix.dccpp;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2018
 */
public class DCCppStreamConnectionConfigTest extends jmri.jmrix.AbstractConnectionConfigTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        cc = new DCCppStreamConnectionConfig(new DCCppStreamPortController()); // adapter assumed in test
    }

    @AfterEach
    @Override
    public void tearDown() {
        cc = null;
        jmri.util.JUnitUtil.resetWindows(false,false);
        jmri.util.JUnitUtil.tearDown();
    }

}
