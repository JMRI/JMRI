package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of LayoutTurnoutView
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LayoutTurnoutViewTest extends LayoutTrackViewTest {

    @Test
    public void testCtor() {
        new LayoutTurnoutView(null);
    }

}
