package jmri.jmrit.display.palette;

import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.picker.PickListModel;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SignalHeadItemPanelTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        PickListModel<jmri.SignalHead> tableModel = PickListModel.signalHeadPickModelInstance();
        DisplayFrame df = new DisplayFrame("SignalHead Item Panel Test");
        SignalHeadItemPanel t = new SignalHeadItemPanel(df,"IH01","",tableModel);
        Assertions.assertNotNull(t,"exists");
        JUnitUtil.dispose(df);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalSignalHeadManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SignalHeadItemPanelTest.class);

}
