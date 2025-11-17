package jmri.util.swing;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EditableResizableImagePanelTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        EditableResizableImagePanel t = new EditableResizableImagePanel();
        Assertions.assertNotNull(t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EditableResizableImagePanelTest.class);

}
