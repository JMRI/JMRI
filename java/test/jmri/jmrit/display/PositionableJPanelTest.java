package jmri.jmrit.display;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfHeadless
public class PositionableJPanelTest extends PositionableTestBase {

    @Test
    public void testCtor() {
        assertNotNull( p, "exists");
    }

    @Test
    @Override
    public void testGetAndSetRotationDegrees() {
        p.rotate(50);
        // setting rotation is currently ignored by PositionableJPanel 
        // and it's sub classes.
        assertEquals( 0, p.getDegrees(), "Degrees");
    }

    @Test
    @Override
    public void testGetAndSetViewCoordinates() {
        assertFalse( p.getViewCoordinates(), "Default View Coordinates");
        p.setViewCoordinates(true);
        assertTrue( p.getViewCoordinates(), "View Coordinates after set true");
        p.setViewCoordinates(false);
        assertFalse( p.getViewCoordinates(), "View Coordinates after set false");
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
