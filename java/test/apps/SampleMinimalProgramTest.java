package apps;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SampleMinimalProgramTest.class);

}
