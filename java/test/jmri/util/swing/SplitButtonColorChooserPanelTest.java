package jmri.util.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author Paul Bender Copyright (C) 2018
 */
public class SplitButtonColorChooserPanelTest {

    protected SplitButtonColorChooserPanel panel = null;

    @Test
    public void testCTor() {
        assertNotNull( panel, "exists");
    }

    @Test
    public void testGetDisplayName(){
        assertEquals(Bundle.getMessage("SplitButtonColorChooserName"),panel.getDisplayName(), "display name");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new SplitButtonColorChooserPanel();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SplitButtonColorChooserPanelTest.class);

}
