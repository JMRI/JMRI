package jmri.jmrit.beantable;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AudioTablePanelTest {

    @Test
    public void testCTor() {
        // The Table Data Models required to create an AudioTablePanel are 
        // internal classes in the AudioTableAction.
        AudioTableAction a = new AudioTableAction();
        // the getPanel() method of the AudioTableAction calls the 
        // constructor for AudioTablePanel. We need to seperate the
        // Table Data Model objects from the AudioTableAction to be
        // able to create this directly. 
        AudioTablePanel t = (AudioTablePanel) a.getPanel();
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AudioTablePanelTest.class);

}
