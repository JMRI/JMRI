package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutEditorAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LayoutEditorActionTest {

    @Test
    public void testCtor() {
        LayoutEditorAction b = new LayoutEditorAction();
        Assert.assertNotNull("exists", b);
    }

    @Test
    public void testCtorWithParam() {
        LayoutEditorAction b = new LayoutEditorAction("test");
        Assert.assertNotNull("exists", b);
    }

    // from here down is testing infrastructure
    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutEditorActionTest.class);
}
