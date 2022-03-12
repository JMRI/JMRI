package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LevelXingView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LevelXingViewTest extends LayoutTrackViewTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Point2D point = new Point2D.Double(150.0, 100.0);

        new LevelXingView(xing, point, layoutEditor);
    }

    LevelXing xing;
    
    @BeforeEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            xing = new LevelXing("X1", layoutEditor);
        }
    }

    @AfterEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() {
        xing = null;
        super.tearDown();
    }

}
