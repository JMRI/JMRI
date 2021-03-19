package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of BlockValueFile
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class BlockValueFileTest {

    @Test
    public void testCtor() {
        BlockValueFile f = new BlockValueFile();
        Assert.assertNotNull("exists", f);
    }

    // from here down is testing infrastructure
    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BlockValueFileTest.class);
}
