package jmri.jmrit.dualdecoder;

<<<<<<< HEAD
=======
import java.awt.GraphicsEnvironment;
>>>>>>> JMRI/master
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
<<<<<<< HEAD
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
=======
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017
>>>>>>> JMRI/master
 */
public class DualDecoderSelectFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        DualDecoderSelectFrame t = new DualDecoderSelectFrame();
<<<<<<< HEAD
        Assert.assertNotNull("exists",t);
=======
        Assert.assertNotNull("exists", t);
        t.dispose();
>>>>>>> JMRI/master
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(DualDecoderSelectFrameTest.class.getName());

}
