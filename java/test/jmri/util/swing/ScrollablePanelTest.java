package jmri.util.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Bob Jacobsen Copyright (C) 2018
 */
public class ScrollablePanelTest {

    @Test
    public void testCTor() {
        ScrollablePanel sp = new ScrollablePanel(22);
        assertEquals(22, sp.getScrollableBlockIncrement(null, 0, 0));
        assertEquals(22, sp.getScrollableUnitIncrement(null, 0, 0));
        
        // test Width and Height logic
        assertTrue(sp.getScrollableTracksViewportWidth());
        assertFalse(sp.getScrollableTracksViewportHeight());
        
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ScrollablePanelTest.class);

}
