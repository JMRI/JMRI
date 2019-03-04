package jmri.jmrix.lenz;

import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class XNetStreamConnectionConfigTest extends jmri.jmrix.AbstractConnectionConfigTestBase {

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        cc = new XNetStreamConnectionConfig(new XNetStreamPortController());
    }

    @After
    public void tearDown() {
	cc = null;
        jmri.util.JUnitUtil.tearDown();
    }

}
