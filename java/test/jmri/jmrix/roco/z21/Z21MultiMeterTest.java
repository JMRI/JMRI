package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class Z21MultiMeterTest extends jmri.implementation.AbstractMultiMeterTestBase{

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        Z21InterfaceScaffold tc = new Z21InterfaceScaffold();
        Z21SystemConnectionMemo memo = new Z21SystemConnectionMemo();
        memo.setTrafficController(tc);  
        memo.setRocoZ21CommandStation(new RocoZ21CommandStation());
        mm = new Z21MultiMeter(memo);
    }

    @After
    public void tearDown() {
        mm.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DCCppMultiMeterTest.class);

}
