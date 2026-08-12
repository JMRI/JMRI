package apps.startup.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * StartupPauseModelXmlTest.java
 *
 * Test for the StartupPauseModelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class StartupPauseModelXmlTest {

    @Test
    public void testCtor(){
        Assertions.assertNotNull(new StartupPauseModelXml(), "StartupPauseModelXml constructor");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

