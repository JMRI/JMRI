package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.util.JUnitUtil;

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
        assertNotNull( b, "exists");
    }

    @Test
    public void testCtorWithParam() {
        LayoutEditorAction b = new LayoutEditorAction("test");
        assertNotNull( b, "exists");
    }

    // from here down is testing infrastructure
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutEditorActionTest.class);
}
