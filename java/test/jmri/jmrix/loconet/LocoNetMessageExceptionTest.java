package jmri.jmrix.loconet;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for LocoNetMessageException class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LocoNetMessageExceptionTest {

    @Test
    public void lnMsgExcpConstructorTest(){
        assertNotNull( new LocoNetMessageException(),
            "LocoNetMessageException constructor");
    }

    @Test
    public void lnMsgExcpStringConstructorTest(){
        assertNotNull( new LocoNetMessageException("test exception"),
            "LocoNetMessageException string constructor");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown(){
        JUnitUtil.tearDown();
    }

}
