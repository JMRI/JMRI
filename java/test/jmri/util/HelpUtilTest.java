package jmri.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class HelpUtilTest {

    @Test
    public void testCTor() {
        JMenuBar menuBar = new JMenuBar();
        HelpUtil.helpMenu(menuBar,"test",true);
        Assert.assertNotNull("help menu created",menuBar.getHelpMenu());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(HelpUtilTest.class);

}
