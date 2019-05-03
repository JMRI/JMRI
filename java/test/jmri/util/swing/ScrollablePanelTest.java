package jmri.util.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Bob Jacobsen Copyright (C) 2018
 */
public class ScrollablePanelTest {

    @Test
    public void testCTor() {
        ScrollablePanel sp = new ScrollablePanel(22);
        Assert.assertEquals(22, sp.getScrollableBlockIncrement(null, 0, 0));
        Assert.assertEquals(22, sp.getScrollableUnitIncrement(null, 0, 0));
        
        // test Width and Height logic
        Assert.assertTrue(sp.getScrollableTracksViewportWidth());
        Assert.assertFalse(sp.getScrollableTracksViewportHeight());
        
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SwingSettingsTest.class);

}
