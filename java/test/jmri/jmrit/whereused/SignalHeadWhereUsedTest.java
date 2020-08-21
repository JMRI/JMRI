package jmri.jmrit.whereused;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the SignalHeadWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class SignalHeadWhereUsedTest {

    @Test
    public void testSignalHeadWhereUsed() {
        SignalHeadWhereUsed ctor = new SignalHeadWhereUsed();
        Assert.assertNotNull("exists", ctor);
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
