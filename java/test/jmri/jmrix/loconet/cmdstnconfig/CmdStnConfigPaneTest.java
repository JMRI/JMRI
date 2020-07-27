package jmri.jmrix.loconet.cmdstnconfig;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class CmdStnConfigPaneTest extends jmri.util.swing.JmriPanelTest {

    
    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new CmdStnConfigPane();
        helpTarget = "package.jmri.jmrix.loconet.cmdstnconfig.CmdStnConfigFrame";
        title = Bundle.getMessage("MenuItemCmdStnConfig");
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CmdStnConfigPaneTest.class);

}
