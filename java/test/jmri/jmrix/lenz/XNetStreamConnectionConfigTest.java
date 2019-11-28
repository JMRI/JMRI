package jmri.jmrix.lenz;

import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class XNetStreamConnectionConfigTest extends jmri.jmrix.AbstractConnectionConfigTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        cc = new XNetStreamConnectionConfig(new XNetStreamPortController());
    }

    @After
    @Override
    public void tearDown() {
	cc = null;
        jmri.util.JUnitUtil.tearDown();
    }

}
