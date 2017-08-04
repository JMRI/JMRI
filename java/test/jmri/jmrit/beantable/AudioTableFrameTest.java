package jmri.jmrit.beantable;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AudioTableFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AudioTableAction a = new AudioTableAction();
        AudioTablePanel p = (AudioTablePanel) a.getPanel();
        AudioTableFrame t = new AudioTableFrame(p,"Audio Table Frame Test");
        Assert.assertNotNull("exists",t);
        t.dispose();
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

    private final static Logger log = LoggerFactory.getLogger(AudioTableFrameTest.class.getName());

}
