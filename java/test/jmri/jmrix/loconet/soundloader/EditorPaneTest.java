package jmri.jmrix.loconet.soundloader;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EditorPaneTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new EditorPane();
        helpTarget = "package.jmri.jmrix.loconet.soundloader.EditorFrame";
        title = Bundle.getMessage("MenuItemSoundEditor");
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EditorPaneTest.class);

}
