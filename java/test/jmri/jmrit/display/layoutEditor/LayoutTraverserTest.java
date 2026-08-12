package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of LayoutTraverser
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class LayoutTraverserTest extends LayoutTrackTest {

    LayoutTraverser lt = null;

    @Test
    public void testNew() {
        Assert.assertNotNull("exists", lt);
    }

    @Test
    public void testToString() {
        String ltString = lt.toString();
        Assert.assertNotNull("ltString not null", ltString);
        Assert.assertEquals("LayoutTraverser My Traverser", ltString);
    }

    // from here down is testing infrastructure
    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        JUnitUtil.resetProfileManager();

        Assert.assertNotNull("LayoutEditor not null", layoutEditor);

        lt = new LayoutTraverser("My Traverser", layoutEditor);

    }

    @AfterEach
    @Override
    public void tearDown() {
        lt = null;
        super.tearDown();
    }
}
