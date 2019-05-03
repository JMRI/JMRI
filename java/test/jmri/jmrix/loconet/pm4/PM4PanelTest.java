package jmri.jmrix.loconet.pm4;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PM4PanelTest extends jmri.util.swing.JmriPanelTest {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new PM4Panel();
        helpTarget="package.jmri.jmrix.loconet.pm4.PM4Frame";
        title=Bundle.getMessage("MenuItemPM4Programmer");
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PM4PanelTest.class);

}
