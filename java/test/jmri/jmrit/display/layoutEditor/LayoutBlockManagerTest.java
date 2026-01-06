package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutBlockManager
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LayoutBlockManagerTest {

    @Test
    public void testCtor() {
        LayoutBlockManager b = new LayoutBlockManager();
        Assertions.assertNotNull( b, "exists");
        b.dispose();
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
