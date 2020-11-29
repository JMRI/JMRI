package jmri.jmrix.can.cbus;

import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class CbusDccOpsModeProgrammerTest extends jmri.jmrix.AbstractOpsModeProgrammerTestBase {

    private TrafficControllerScaffold tcis;
    
    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new TrafficControllerScaffold();
        CbusDccOpsModeProgrammer t = new CbusDccOpsModeProgrammer(100,true,tcis);
        programmer = t;
    }
    
    @AfterEach
    @Override
    public void tearDown() {
        programmer = null;
        tcis.terminateThreads();
        tcis = null;
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusDccOpsModeProgrammerTest.class);

}
