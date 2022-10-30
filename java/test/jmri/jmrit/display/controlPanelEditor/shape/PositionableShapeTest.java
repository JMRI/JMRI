package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Shape;

import jmri.jmrit.display.EditorScaffold;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableJComponentTest;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class PositionableShapeTest extends PositionableJComponentTest {

    @Override
    @Test
    public void testGetAndSetShowToolTip() {

        Assert.assertFalse("Defalt ShowToolTip", p.showToolTip());
        p.setShowToolTip(true);
        Assert.assertTrue("showToolTip after set true", p.showToolTip());
        p.setShowToolTip(false);
        Assert.assertFalse("showToolTip after set false", p.showToolTip());
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        editor = new EditorScaffold();
        p = new PositionableShape(editor) {

            @Override
            protected DrawFrame makeEditFrame(boolean create) {
                // bogus body, not used in tests
                return null;
            }

            @Override
            @javax.annotation.Nonnull
            protected Shape makeShape() {
                // bogus body, not used in tests
                throw new UnsupportedOperationException();
            }

            @Override
            public Positionable deepClone() {
                // bogus body, not used in tests
                return null;
            }
        };

    }

    // private final static Logger log = LoggerFactory.getLogger(PositionableShapeTest.class);
}
