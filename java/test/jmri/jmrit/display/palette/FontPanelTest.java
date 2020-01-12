package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jmri.jmrit.display.PositionableLabel;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017   
 */
public class FontPanelTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PositionableLabel pos = new PositionableLabel("Some Text", null);
        ActionListener a = ((ActionEvent event) -> {
            ca(); // callback
        });
        FontPanel t = new FontPanel(pos.getPopupUtility(), a);
        Assert.assertNotNull("exists",t);
        t.setFontSelections();
    }
    
    void ca() {
        
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

    // private final static Logger log = LoggerFactory.getLogger(RPSItemPanelTest.class);

}
