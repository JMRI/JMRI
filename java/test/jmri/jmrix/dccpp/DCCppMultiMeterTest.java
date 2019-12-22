package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class DCCppMultiMeterTest extends jmri.implementation.AbstractMultiMeterTestBase{

    @Test
    public void testMethods() {
        Assert.assertEquals("DCC++", mm.getHardwareMeterName());
    }

    @Test
    public void testCurrentReply(){
        ((DCCppMultiMeter)mm).message(DCCppReply.parseDCCppReply("a10")); // a syntactically valid current reply
	Assert.assertEquals("current level percentage 100.0 - 0.0", (10.0/DCCppConstants.MAX_CURRENT) * 100 ,mm.getCurrent(),0.05);
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());

        DCCppSystemConnectionMemo memo = new DCCppSystemConnectionMemo(tc);
        mm = new DCCppMultiMeter(memo);
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DCCppMultiMeterTest.class);

}
