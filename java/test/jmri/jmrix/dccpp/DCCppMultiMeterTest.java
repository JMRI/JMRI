package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class DCCppMultiMeterTest extends jmri.implementation.AbstractMultiMeterTestBase{

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());

        DCCppSystemConnectionMemo memo = new DCCppSystemConnectionMemo(tc);
        mm = new DCCppMultiMeter(memo);
    }

    @After
    public void tearDown() {
        mm.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DCCppMultiMeterTest.class);

}
