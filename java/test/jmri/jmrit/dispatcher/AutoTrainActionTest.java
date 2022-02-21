package jmri.jmrit.dispatcher;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import org.mockito.Mockito;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AutoTrainActionTest {

    @Test
    public void testCTor() {
        
        DispatcherFrame df = Mockito.mock(DispatcherFrame.class);
        InstanceManager.setDefault(DispatcherFrame.class,df);
        
        jmri.Transit transit = new jmri.Transit("TT1");
        ActiveTrain at = new ActiveTrain(transit,"Train",ActiveTrain.USER);
        AutoActiveTrain aat = new AutoActiveTrain(at);
        AutoTrainAction t = new AutoTrainAction(aat);
        Assertions.assertNotNull(t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AutoTrainActionTest.class);

}
