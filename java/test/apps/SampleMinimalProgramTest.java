package apps;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SampleMinimalProgramTest {

    @Test
    @Ignore("Tries to setup connection with real hardware, that does not exist")
    public void testCTor() {
       String[] args = {"DecoderProConfig3.xml"};
        SampleMinimalProgram t = new SampleMinimalProgram(args);
        Assert.assertNotNull("exists",t);
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

    // private final static Logger log = LoggerFactory.getLogger(SampleMinimalProgramTest.class);

}
