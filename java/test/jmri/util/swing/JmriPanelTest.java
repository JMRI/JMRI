package jmri.util.swing;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JmriPanelTest {

    protected JmriPanel panel = null;
    protected String helpTarget = null;
    protected String title = null;

    @Test
    public void testCTor() {
        assertNotNull( panel, "exists");
    }

    @Test
    public void testInitComponents() throws Exception{
        // for now, just make sure there isn't an exception.
        panel.initComponents();
    }

    @DisabledIfHeadless
    @Test
    public void testAccessibility() throws Exception{
        panel.initComponents();
        jmri.util.AccessibilityChecks.check(panel);
    }

    @Test
    public void testGetHelpTarget(){
        assertEquals( helpTarget, panel.getHelpTarget(), "help target");
    }

    @Test
    public void testGetTitle(){
        assertEquals( title, panel.getTitle(), "title");
    }

    @Test
    @DisabledIfHeadless
    public void testGetMenus() {
        assertNotNull(panel.getMenus());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new JmriPanel();
        helpTarget = "package.jmri.util.swing.JmriPanel";
    }

    @AfterEach
    public void tearDown() {
        if (panel!=null) {
           panel.dispose();
        }
        panel = null;
        helpTarget = null;
        title = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JmriPanelTest.class);

}
