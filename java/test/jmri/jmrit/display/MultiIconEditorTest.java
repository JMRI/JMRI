package jmri.jmrit.display;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of MultiIconEditor
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class MultiIconEditorTest {

    @Test
    @DisabledIfHeadless
    public void testCtor() {
        MultiIconEditor frame = new MultiIconEditor(4);
        Assertions.assertNotNull(frame, "exists");
        frame.dispose();
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
    // private final static Logger log = LoggerFactory.getLogger(MultiIconEditorTest.class);
}
