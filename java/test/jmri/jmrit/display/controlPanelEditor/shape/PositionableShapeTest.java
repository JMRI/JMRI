package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.GraphicsEnvironment;
import java.awt.Shape;
import jmri.jmrit.display.EditorScaffold;
import jmri.jmrit.display.Positionable;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PositionableShapeTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PositionableShape t = new PositionableShape(new EditorScaffold()) {
            @Override
            protected Shape makeShape() {
                // bogus body, not used in tests
                return null;
            }

            @Override
            public Positionable deepClone() {
                // bogus body, not used in tests
                return null;
            }
        };
        Assert.assertNotNull("exists", t);
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

    // private final static Logger log = LoggerFactory.getLogger(PositionableShapeTest.class);
}
