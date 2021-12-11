package jmri.jmrix.loconet;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.jmrix.DefaultSystemConnectionMemo;
import jmri.util.JUnitUtil;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.junit.jupiter.api.*;

/**
 * Abstract base class for SystemConnectionMemo objects.
 *
 * @author Paul Bender Copyright (C) 2017
 * @param <M> the supported memo class
 */
abstract public class LnSystemConnectionMemoTestBase<M extends DefaultSystemConnectionMemo>
            extends SystemConnectionMemoTestBase<M> {

    @BeforeEach
    @OverridingMethodsMustInvokeSuper  // invoke first
    public void setUp() {
       JUnitUtil.setUp();
    }

    @AfterEach
    @OverridingMethodsMustInvokeSuper  // invoke last
    public void tearDown() {
        JUnitUtil.removeMatchingThreads("LnPowerManager LnTrackStatusUpdateThread");
        JUnitUtil.removeMatchingThreads("LnSensorUpdateThread");

        JUnitUtil.tearDown();
    }

}
