package jmri.jmrit.display.palette;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import jmri.jmrit.display.EditorScaffold;
import jmri.jmrit.display.PositionableLabel;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017   
 */
public class FontPanelTest {

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testCTor() {

        EditorScaffold layoutEditor = new EditorScaffold();

        PositionableLabel pos = new PositionableLabel("Some Text", layoutEditor);
        ActionListener a = ((ActionEvent event) -> {
            ca(); // callback
        });
        FontPanel t = new FontPanel(pos.getPopupUtility(), a);
        Assert.assertNotNull("exists",t);
        t.setFontSelections();

        layoutEditor.dispose();

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
