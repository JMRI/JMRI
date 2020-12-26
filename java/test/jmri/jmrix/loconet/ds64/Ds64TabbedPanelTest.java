package jmri.jmrix.loconet.ds64;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Ds64TabbedPanelTest extends jmri.util.swing.JmriPanelTest {


    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new Ds64TabbedPanel();
        helpTarget="package.jmri.jmrix.loconet.ds64.DS64TabbedPanel";
        title=Bundle.getMessage("MenuItemDS64Programmer");
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Ds64TabbedPanelTest.class);

}
