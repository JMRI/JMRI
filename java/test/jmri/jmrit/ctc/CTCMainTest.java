package jmri.jmrit.ctc;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/*
* Tests for the CTCMain Class
* @author  Dave Sand   Copyright (C) 2019
*/
public class CTCMainTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("CTCMain Constructor Return", new CTCMain());
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        jmri.util.JUnitUtil.tearDown();
    }
}
