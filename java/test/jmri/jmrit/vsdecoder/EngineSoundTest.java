package jmri.jmrit.vsdecoder;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class EngineSoundTest {

    @Test
    public void testCTor() {
        EngineSound t = new EngineSound("test");
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        // this created an audio manager, clean that up
        jmri.InstanceManager.getDefault(jmri.AudioManager.class).cleanup();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EngineSoundTest.class);

}
