package jmri.jmrix.loconet.se8;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SE8PanelTest extends jmri.util.swing.JmriPanelTest {


    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new SE8Panel();
        helpTarget = "package.jmri.jmrix.loconet.se8.SE8Frame";
        title = Bundle.getMessage("MenuItemSE8cProgrammer");
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SE8PanelTest.class);

}
