package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for PiSprog3ConnectionConfig class.
 *
 * @author Andrew Crosland (C) 2020
 **/
public class PiSprog3v2ConnectionConfigTest extends jmri.jmrix.AbstractSerialConnectionConfigTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
        cc = new PiSprog3v2ConnectionConfig();
    }

    @AfterEach
    @Override
    public void tearDown(){
        cc = null;
        JUnitUtil.tearDown();
    }

}
