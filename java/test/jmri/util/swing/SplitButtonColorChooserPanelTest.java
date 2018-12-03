package jmri.util.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2018	
 */
public class SplitButtonColorChooserPanelTest {

    protected SplitButtonColorChooserPanel panel = null;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",panel);
    }

    @Test
    public void testGetDisplayName(){
        Assert.assertEquals("display name",Bundle.getMessage("SplitButtonColorChooserName"),panel.getDisplayName());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new SplitButtonColorChooserPanel();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SplitButtonColorChooserPanelTest.class);

}
