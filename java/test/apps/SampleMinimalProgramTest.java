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
    public void testCTor() {
        // the class under test requires a configuration file name as a 
        // parameter, but the configuration is ignored.
        String[] args = {"DecoderProConfig3.xml"};
        SampleMinimalProgram t = new SampleMinimalProgram(args){
           // actual configuration is performed in the codeConfig method,
           // so we provide a dummy for testing.
           @Override
           protected void codeConfig(String[] args){
           }
        };
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
