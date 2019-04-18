package jmri.jmrit.ctc;

import org.junit.*;

/*
* Tests for the SignalDirectionIndicatorsNull Class
* @author  Dave Sand   Copyright (C) 2019
*/
public class SignalDirectionIndicatorsNullTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("SignalDirectionIndicatorsNull Constructor Return", new SignalDirectionIndicatorsNull());
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