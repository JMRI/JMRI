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
public class SignalMastItemPanelTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        PickListModel<jmri.SignalMast> tableModel = PickListModel.signalMastPickModelInstance();
        DisplayFrame df = new DisplayFrame("SignalMast Item Panel Test");
        SignalMastItemPanel t = new SignalMastItemPanel(df,"IM01","",tableModel);
        Assertions.assertNotNull(t,"exists");
        JUnitUtil.dispose(df);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SignalMastItemPanelTest.class);

}
