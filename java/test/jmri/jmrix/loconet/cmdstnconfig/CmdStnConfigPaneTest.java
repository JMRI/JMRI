package jmri.jmrix.loconet.cmdstnconfig;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class CmdStnConfigPaneTest extends jmri.util.swing.JmriPanelTest {

    
    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new CmdStnConfigPane();
        helpTarget = "package.jmri.jmrix.loconet.cmdstnconfig.CmdStnConfigFrame";
        title = Bundle.getMessage("MenuItemCmdStnConfig");
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CmdStnConfigPaneTest.class);

}
