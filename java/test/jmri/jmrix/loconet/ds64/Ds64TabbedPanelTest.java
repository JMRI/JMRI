package jmri.jmrix.loconet.ds64;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class Ds64TabbedPanelTest extends jmri.util.swing.JmriPanelTest {


    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new Ds64TabbedPanel();
        helpTarget="package.jmri.jmrix.loconet.ds64.DS64TabbedPanel";
        title=Bundle.getMessage("MenuItemDS64Programmer");
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Ds64TabbedPanelTest.class);

}
