package jmri.jmrix.dccpp;

import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2018	
 */
public class DCCppStreamConnectionConfigTest extends jmri.jmrix.AbstractConnectionConfigTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        cc = new DCCppStreamConnectionConfig();
    }

    @After
    @Override
    public void tearDown() {
        cc = null;
        jmri.util.JUnitUtil.resetWindows(false,false);
        jmri.util.JUnitUtil.tearDown();
    }

}
