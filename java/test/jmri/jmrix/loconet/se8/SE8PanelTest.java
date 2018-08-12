package jmri.jmrix.loconet.se8;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SE8PanelTest extends jmri.util.swing.JmriPanelTest {


    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new SE8Panel();
        helpTarget = "package.jmri.jmrix.loconet.se8.SE8Frame";
        title = Bundle.getMessage("MenuItemSE8cProgrammer");
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SE8PanelTest.class);

}
