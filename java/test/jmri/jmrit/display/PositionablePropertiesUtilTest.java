package jmri.jmrit.display;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.Color;
import java.awt.Font;

import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.netbeans.jemmy.operators.JFrameOperator;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PositionablePropertiesUtilTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        Editor ef = new EditorScaffold();
        PositionableIcon iti = new PositionableIcon(ef);
        PositionablePropertiesUtil t = new PositionablePropertiesUtil(iti);
        assertNotNull( t, "exists");
        JUnitUtil.dispose(ef);
    }

    @Test
    @DisabledIfHeadless
    public void testNoChangesApplyLabel() throws Positionable.DuplicateIdException {
        Editor ef = new EditorScaffold();
        PositionableLabel label = new PositionableLabel("one", ef);
        label.setBounds(80, 80, 40, 40);
        ef.putItem(label);
        // store properties before
        Color bc = label.getBackground();
        boolean opaque = label.isOpaque();
        Color fc = label.getForeground();
        Font f = label.getFont();
        PositionablePropertiesUtil t = new PositionablePropertiesUtil(label);
        ThreadingUtil.runOnGUI( () -> t.display());
        t.fontApply(); // fontApply is package protected in PositionablePropertiesUtil.
        // we haven't made any changes, so the properties should be the same as before.
        assertEquals( bc.getRed(), label.getBackground().getRed(),
            "No Change Background Color Red");
        assertEquals( bc.getBlue(), label.getBackground().getBlue(),
            "No Change Background Color Blue");
        assertEquals( bc.getGreen(), label.getBackground().getGreen(),
            "No Change Background Color Green");
        assertEquals( opaque, label.isOpaque(), "No Change opaque");
        assertEquals( fc, label.getForeground(),
            "No Change Foreground Color");
        assertEquals( f, label.getFont(), "No Change Font");
        JUnitUtil.waitFor(60000);
        JFrameOperator jfo = new JFrameOperator("Text Label");
        JUnitUtil.dispose(jfo.getWindow());
        JUnitUtil.dispose(ef);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PositionablePropertiesUtilTest.class);

}
