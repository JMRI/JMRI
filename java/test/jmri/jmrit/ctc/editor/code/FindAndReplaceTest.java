package jmri.jmrit.ctc.editor.code;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/*
* Tests for the FindAndReplace Class
* @author  Dave Sand   Copyright (C) 2019
*/
public class FindAndReplaceTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("FindAndReplace Constructor Return", new FindAndReplace());
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}
