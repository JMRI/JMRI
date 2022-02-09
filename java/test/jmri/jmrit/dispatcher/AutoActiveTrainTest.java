package jmri.jmrit.dispatcher;

import jmri.InstanceManager;
import jmri.Transit;
import jmri.util.JUnitUtil;

import org.mockito.Mockito;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AutoActiveTrainTest {

    @Test
    public void testCTor() {

        DispatcherFrame df = Mockito.mock(DispatcherFrame.class);
        InstanceManager.setDefault(DispatcherFrame.class,df);

        Transit transit = new Transit("TT1");
        ActiveTrain at = new ActiveTrain(transit,"Train",ActiveTrain.USER);
        AutoActiveTrain t = new AutoActiveTrain(at);
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

    // private final static Logger log = LoggerFactory.getLogger(AutoActiveTrainTest.class);

}
