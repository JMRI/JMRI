package jmri.jmrix.dccpp.swing;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppInterfaceScaffold;
import jmri.jmrix.dccpp.DCCppSensorManager;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.DCCppTurnoutManager;
import jmri.util.JUnitUtil;
import org.junit.*;


/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ConfigBaseStationFrameTest extends jmri.util.JmriJFrameTestBase {

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
        DCCppSystemConnectionMemo memo = new DCCppSystemConnectionMemo(tc);
        if(!GraphicsEnvironment.isHeadless()){
           frame = new ConfigBaseStationFrame(new DCCppSensorManager(tc,memo.getSystemPrefix()),new DCCppTurnoutManager(tc,memo.getSystemPrefix()),tc);
       }

    }

    @After
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ConfigBaseStationFrameTest.class);

}
