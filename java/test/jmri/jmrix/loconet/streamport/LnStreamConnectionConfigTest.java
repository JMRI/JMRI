package jmri.jmrix.loconet.streamport;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2018
 */
public class LnStreamConnectionConfigTest extends jmri.jmrix.AbstractConnectionConfigTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        cc = new LnStreamConnectionConfig();
    }

    @AfterEach
    @Override
    public void tearDown() {
        cc = null;
        jmri.util.JUnitUtil.tearDown();
    }

}
