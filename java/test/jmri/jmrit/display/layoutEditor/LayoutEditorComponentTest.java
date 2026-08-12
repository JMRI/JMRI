package jmri.jmrit.display.layoutEditor;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

import jmri.util.JUnitUtil;

import java.awt.GraphicsEnvironment;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LayoutEditorComponentTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor le = new LayoutEditor("Layout Editor Component Test Layout");
        LayoutEditorComponent t = new LayoutEditorComponent(le);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(le);
    }

    /**
     * The clip held by the component is the visible part of the panel, used to
     * cull drawing. It must be intersected with the clip Swing supplied, not
     * substituted for it, so that a partial repaint stays partial.
     */
    @Test
    public void testPaintStaysInsideRequestedClip() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor le = new LayoutEditor("Layout Editor Component Clip Test Layout");
        LayoutEditorComponent t = new LayoutEditorComponent(le);

        // the visible area of the panel, as adjustClip() would set it
        t.setClip(new Rectangle2D.Double(0, 0, 400, 400));

        BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            // the small area Swing asked to be repainted, e.g. one icon
            Rectangle requested = new Rectangle(100, 100, 20, 20);
            g2.setClip(requested);

            t.paint(g2);

            Rectangle after = g2.getClipBounds();
            Assert.assertNotNull("clip still set after painting", after);
            Assert.assertTrue("painting stayed inside the requested clip, was " + after,
                    requested.contains(after));
        } finally {
            g2.dispose();
        }
        JUnitUtil.dispose(le);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
