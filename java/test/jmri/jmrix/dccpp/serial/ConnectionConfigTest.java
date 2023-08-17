package jmri.jmrix.dccpp.serial;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for ConnectionConfig class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class ConnectionConfigTest extends jmri.jmrix.AbstractSerialConnectionConfigTestBase  {

    @Override
    public void testGetInfo() {
        super.testGetInfo();
        jmri.util.JUnitAppender.suppressErrorMessageStartsWith("No usable ports returned");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
        cc = new ConnectionConfig(new DCCppAdapter()); // adapter assumed in test
    }

    @AfterEach
    @Override
    public void tearDown(){
        cc = null;
        JUnitUtil.tearDown();
    }

}
