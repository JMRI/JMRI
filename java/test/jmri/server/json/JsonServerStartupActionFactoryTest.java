package jmri.server.json;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * @author Paul Bender Copyright (C) 2017
 * @author Randall Wood Copyright 2019
 */
public class JsonServerStartupActionFactoryTest {

    @Test
    public void testCTor() {
        JsonServerStartupActionFactory t = new JsonServerStartupActionFactory();
        Assertions.assertNotNull( t, "exists");
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
