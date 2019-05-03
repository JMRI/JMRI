package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class Z21MultiMeterTest extends jmri.implementation.AbstractMultiMeterTestBase{

    private Z21InterfaceScaffold tc;
    private Z21SystemConnectionMemo memo;

    @Test
    public void testMethods() {
        Assert.assertEquals("Z21", mm.getHardwareMeterName());
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        tc = new Z21InterfaceScaffold();
        memo = new Z21SystemConnectionMemo();
        memo.setTrafficController(tc);  
        memo.setRocoZ21CommandStation(new RocoZ21CommandStation());
        mm = new Z21MultiMeter(memo);
    }

    @Override
    @After
    public void tearDown(){
        tc.terminateThreads();
        memo = null;
        tc = null;
        super.tearDown(); 
    }

    // private final static Logger log = LoggerFactory.getLogger(DCCppMultiMeterTest.class);

}
