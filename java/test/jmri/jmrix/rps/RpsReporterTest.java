package jmri.jmrix.rps;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RpsReporterTest extends jmri.implementation.AbstractReporterTestBase {

    @Override
    protected Object generateObjectToReport(){
        return "3";
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        r = new RpsReporter("RR(0,0,0);(1,0,0);(1,1,0);(0,1,0)", "R");
    }

    @After
    @Override
    public void tearDown() {
	    r = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RpsReporterTest.class);

}
