package jmri.util.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ButtonGroupColorChooserPanelTest {

    protected ButtonGroupColorChooserPanel panel = null;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",panel);
    }

    @Test
    public void testGetDisplayName(){
        Assert.assertEquals("display name",Bundle.getMessage("ButtonGroupColorChooserName"),panel.getDisplayName());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new ButtonGroupColorChooserPanel();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ButtonGroupColorChooserPanelTest.class);

}
