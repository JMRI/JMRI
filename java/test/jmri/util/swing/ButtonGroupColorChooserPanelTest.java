package jmri.util.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ButtonGroupColorChooserPanelTest {

    protected ButtonGroupColorChooserPanel panel = null;

    @Test
    public void testCTor() {
        assertNotNull( panel, "exists");
    }

    @Test
    public void testGetDisplayName(){
        assertEquals( Bundle.getMessage("ButtonGroupColorChooserName"),panel.getDisplayName(), "display name");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new ButtonGroupColorChooserPanel();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ButtonGroupColorChooserPanelTest.class);

}
