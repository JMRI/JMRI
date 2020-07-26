package jmri.jmrix.loconet.pr4;

import jmri.jmrix.loconet.LnCommandStationType;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PR4AdapterTest {

    @Test
    public void testCTor() {
        PR4Adapter t = new PR4Adapter();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testcommandStationOptions() {
        PR4Adapter t = new PR4Adapter();
        String[] cmdStns = t.commandStationOptions();
        boolean foundPR4StandaloneProgrammer = false;
        for (int i=0; i < cmdStns.length; i++) {
            Assert.assertNotEquals("should not find 'Stand-alone LocoNet", 
                    LnCommandStationType.COMMAND_STATION_STANDALONE.getName(), cmdStns[i]);
            if (cmdStns[i].equals(LnCommandStationType.COMMAND_STATION_PR4_ALONE.getName())) {
                foundPR4StandaloneProgrammer = true;
            }
        }
        Assert.assertTrue("Found PR4 in standalone programmer mode", foundPR4StandaloneProgrammer);
            
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PR4AdapterTest.class);

}
