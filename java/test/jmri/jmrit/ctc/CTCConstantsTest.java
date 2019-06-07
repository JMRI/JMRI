package jmri.jmrit.ctc;

import org.junit.*;

/*
* Tests for the CTCConstants Class
* @author  Dave Sand   Copyright (C) 2019
*/
public class CTCConstantsTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("CTCConstants Constructor Return", new CTCConstants());
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