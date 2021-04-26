package jmri.web.server;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.web.server.WebServerAction class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class WebServerActionTest {

    @Test
    public void testCtor() {
        WebServerAction a = new WebServerAction();
        Assert.assertNotNull(a);
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
