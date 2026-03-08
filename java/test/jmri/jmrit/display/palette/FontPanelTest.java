package jmri.jmrit.display.palette;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import jmri.jmrit.display.EditorScaffold;
import jmri.jmrit.display.PositionableLabel;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017   
 */
public class FontPanelTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {

        EditorScaffold layoutEditor = new EditorScaffold();

        PositionableLabel pos = new PositionableLabel("Some Text", layoutEditor);
        ActionListener a = ((ActionEvent event) -> {
            ca(); // callback
        });
        FontPanel t = new FontPanel(pos.getPopupUtility(), a);
        Assertions.assertNotNull(t,"exists");
        t.setFontSelections();

        JUnitUtil.dispose(layoutEditor);

    }
    
    void ca() {
        
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

    // private final static Logger log = LoggerFactory.getLogger(RPSItemPanelTest.class);

}
