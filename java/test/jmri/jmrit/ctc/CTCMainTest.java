package jmri.jmrit.ctc;

import org.junit.*;

/*
* Tests for the CTCMain Class
* @author  Dave Sand   Copyright (C) 2019
*/
public class CTCMainTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("CTCMain Constructor Return", new CTCMain());
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}