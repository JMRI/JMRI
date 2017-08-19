package jmri.jmrit.beantable;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.beantable.AudioTableAction.AudioListenerTableDataModel;
import jmri.jmrit.beantable.AudioTableAction.AudioSourceTableDataModel;
import jmri.jmrit.beantable.AudioTableAction.AudioBufferTableDataModel;

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
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(AudioTablePanelTest.class.getName());

}
