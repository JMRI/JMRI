package jmri.jmrix.tams;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TamsSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    @Override
    public String getSystemName(int i){
       return "TS" + i;
    }
    
    @Disabled("Tams SensorManager does not seem to increment correctly, "
            + "ERROR - systemName is already registered. "
            + "Current system name: TS1:1. New system name: TS1:01")
    @ToDo("Someone with knowledge of Tams could test the expected output.")
    @Override
    public void testGetNextValidAddress(){
    }

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",l);
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        TamsTrafficController tc = new TamsInterfaceScaffold();
        TamsSystemConnectionMemo memo = new TamsSystemConnectionMemo(tc);  
        l = new TamsSensorManager(memo);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TamsSensorManagerTest.class);

}
