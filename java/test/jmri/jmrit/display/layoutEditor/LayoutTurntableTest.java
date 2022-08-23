package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of LayoutTurntable
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class LayoutTurntableTest extends LayoutTrackTest {

    LayoutTurntable lt = null;

    @Test
    public void testNew() {
        Assert.assertNotNull("exists", lt);
    }

    @Test
    public void testToString() {
        String ltString = lt.toString();
        Assert.assertNotNull("ltString not null", ltString);
        Assert.assertEquals("LayoutTurntable My Turntable", ltString);
    }

    // from here down is testing infrastructure
    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        JUnitUtil.resetProfileManager();

        Assert.assertNotNull("LayoutEditor not null", layoutEditor);

        lt = new LayoutTurntable("My Turntable", layoutEditor); // new Point2D.Double(50.0, 100.0),

    }

    @AfterEach
    @Override
    public void tearDown() {
        lt = null;
        super.tearDown();
    }
}
