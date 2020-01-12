package jmri.jmrix.loconet.pr4.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class Pr4SelectPaneTest extends jmri.util.swing.JmriPanelTest {


    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new Pr4SelectPane();
        helpTarget="package.jmri.jmrix.loconet.pr4.swing.Pr4Select";
        title=Bundle.getMessage("MenuItemPr4ModeSelect");
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Pr4SelectPaneTest.class);

}
