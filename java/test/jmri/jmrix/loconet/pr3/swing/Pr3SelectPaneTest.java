package jmri.jmrix.loconet.pr3.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class Pr3SelectPaneTest extends jmri.util.swing.JmriPanelTest {


    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new Pr3SelectPane();
        helpTarget="package.jmri.jmrix.loconet.pr3.swing.Pr3Select";
        title=Bundle.getMessage("MenuItemPr3ModeSelect");
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Pr3SelectPaneTest.class);

}
