package jmri.jmrix.loconet.soundloader;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class EditorPaneTest extends jmri.util.swing.JmriPanelTest {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new EditorPane();
        helpTarget = "package.jmri.jmrix.loconet.soundloader.EditorFrame";
        title = Bundle.getMessage("MenuItemSoundEditor");
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EditorPaneTest.class);

}
