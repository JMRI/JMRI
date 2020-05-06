package jmri.jmrix.loconet.streamport;

import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2018
 */
public class LnStreamConnectionConfigTest extends jmri.jmrix.AbstractConnectionConfigTestBase {

    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        cc = new LnStreamConnectionConfig();
    }

    @After
    @Override
    public void tearDown() {
        cc = null;
        jmri.util.JUnitUtil.tearDown();
    }

}
