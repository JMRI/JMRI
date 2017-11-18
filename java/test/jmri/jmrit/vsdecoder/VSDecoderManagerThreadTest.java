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
public class VSDecoderManagerThreadTest {

    @Test
    public void testInstance() {
        VSDecoderManagerThread t = VSDecoderManagerThread.instance();
        Assert.assertNotNull("exists",t);
        // the instance method starts a thread, make sure it goes away.
        t.kill();
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

    // private final static Logger log = LoggerFactory.getLogger(VSDecoderManagerThreadTest.class);

}
