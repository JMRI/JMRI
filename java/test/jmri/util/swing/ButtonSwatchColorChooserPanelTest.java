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
public class ButtonSwatchColorChooserPanelTest {

    protected ButtonSwatchColorChooserPanel panel = null;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",panel);
    }

    @Test
    public void testGetDisplayName(){
        Assert.assertEquals("display name",Bundle.getMessage("ButtonSwatchColorChooserName"),panel.getDisplayName());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new ButtonSwatchColorChooserPanel();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ButtonSwatchColorChooserPanelTest.class);

}
