package jmri.jmrix.loconet.pr4.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Pr4SelectPaneTest extends jmri.util.swing.JmriPanelTest {


    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new Pr4SelectPane();
        helpTarget="package.jmri.jmrix.loconet.pr4.swing.Pr4Select";
        title=Bundle.getMessage("MenuItemPr4ModeSelect");
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Pr4SelectPaneTest.class);

}
