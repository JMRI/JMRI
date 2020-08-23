package jmri.jmrit.ctc.editor.code;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/*
* Tests for the ClassCompareContents Class
* @author  Dave Sand   Copyright (C) 2019
*/
public class ClassCompareContentsTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("ClassCompareContents Constructor Return", new ClassCompareContents());
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
