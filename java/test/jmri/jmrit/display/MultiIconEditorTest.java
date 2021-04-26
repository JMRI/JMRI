package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 * Test simple functioning of MultiIconEditor
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class MultiIconEditorTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        MultiIconEditor frame = new MultiIconEditor(4);
        Assert.assertNotNull("exists", frame);
        frame.dispose();
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
    // private final static Logger log = LoggerFactory.getLogger(MultiIconEditorTest.class);
}
