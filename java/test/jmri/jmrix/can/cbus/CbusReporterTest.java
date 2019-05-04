package jmri.jmrix.can.cbus;

import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class CbusReporterTest extends jmri.implementation.AbstractReporterTestBase {

    @Override
    protected Object generateObjectToReport(){
        return new jmri.implementation.DefaultIdTag("ID0413276BC1", "Test Tag");
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        r = new CbusReporter(1, new TrafficControllerScaffold(), "Test");
    }

    @After
    public void tearDown() {
	r = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusReporterTest.class);

}
