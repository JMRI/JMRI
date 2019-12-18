package jmri.jmrit.ctc.editor.code;

import org.junit.*;

/*
* Tests for the FindAndReplace Class
* @author  Dave Sand   Copyright (C) 2019
*/
public class FindAndReplaceTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("FindAndReplace Constructor Return", new FindAndReplace());
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