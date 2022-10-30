package jmri.jmrit.display;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class PositionableJPanelTest extends PositionableTestBase {

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", p);
    }

    @Test
    @Override
    public void testGetAndSetRotationDegrees() {
        p.rotate(50);
        // setting rotation is currently ignored by PositionableJPanel 
        // and it's sub classes.
        Assert.assertEquals("Degrees", 0, p.getDegrees());
    }

    @Test
    @Override
    public void testGetAndSetViewCoordinates() {
        Assert.assertFalse("Default View Coordinates", p.getViewCoordinates());
        p.setViewCoordinates(true);
        Assert.assertTrue("View Coordinates after set true", p.getViewCoordinates());
        p.setViewCoordinates(false);
        Assert.assertFalse("View Coordinates after set false", p.getViewCoordinates());
    }

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();

        editor = new EditorScaffold();
        p = new PositionableJPanel(editor);
        ((PositionableJPanel) p).setName("PositionableJPanel");

    }

    // private final static Logger log = LoggerFactory.getLogger(PositionableJPanelTest.class);

}
