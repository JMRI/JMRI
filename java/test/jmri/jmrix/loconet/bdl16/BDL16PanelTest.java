package jmri.jmrix.loconet.bdl16;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class BDL16PanelTest extends jmri.util.swing.JmriPanelTest {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new BDL16Panel();
        helpTarget = "package.jmri.jmrix.loconet.bdl16.BDL16Frame";
        title = Bundle.getMessage("MenuItemBDL16Programmer");
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(BDL16PanelTest.class);

}
