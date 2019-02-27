package jmri.jmrix.sprog;

import org.junit.*;

/**
 * @author Paul Bender Copyright (C) 2018	
 */
public class SprogCSStreamConnectionConfigTest extends jmri.jmrix.AbstractConnectionConfigTestBase {

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        cc = new SprogCSStreamConnectionConfig();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
